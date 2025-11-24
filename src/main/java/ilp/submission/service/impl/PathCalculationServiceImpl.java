package ilp.submission.service.impl;

import ilp.submission.model.*;
import ilp.submission.service.DroneAvailabilityService;
import ilp.submission.service.IlpRestClient;
import ilp.submission.service.PathCalculationService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementation of PathCalculationService for Group 5.
 * Uses A* pathfinding with 16 compass directions.
 */
@Service
public class PathCalculationServiceImpl implements PathCalculationService {

    private static final double MOVE_DISTANCE = 0.00015;
    private static final double CLOSE_THRESHOLD = 0.00015;
    private static final double[] DIRECTIONS = {
            0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
            180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5
    };

    private final IlpRestClient ilpRestClient;
    private final DroneAvailabilityService availabilityService;

    public PathCalculationServiceImpl(IlpRestClient ilpRestClient,
                                      DroneAvailabilityService availabilityService) {
        this.ilpRestClient = ilpRestClient;
        this.availabilityService = availabilityService;
    }

    @Override
    public DeliveryPathResult calculateDeliveryPaths(List<MedDispatchRec> dispatches) {
        if (dispatches == null || dispatches.isEmpty()) {
            return new DeliveryPathResult(0, 0, List.of());
        }

        List<RestrictedArea> noFlyZones;
        List<DroneServicePoint> servicePoints;
        List<Drone> drones;
        List<DroneForServicePoint> droneAvailability;

        try {
            noFlyZones = ilpRestClient.fetchRestrictedAreas();
            servicePoints = ilpRestClient.fetchServicePoints();
            drones = ilpRestClient.fetchDrones();
            droneAvailability = ((IlpRestClientImpl) ilpRestClient).fetchDroneAvailability();

            System.out.println("Fetched " + servicePoints.size() + " service points");
            System.out.println("Fetched " + drones.size() + " drones");
            System.out.println("Fetched drone availability for " + droneAvailability.size() + " service points");

            for (DroneServicePoint sp : servicePoints) {
                System.out.println("  Service Point: " + sp);
            }
        } catch (Exception e) {
            System.err.println("Error fetching API data: " + e.getMessage());
            e.printStackTrace();
            noFlyZones = List.of();
            servicePoints = List.of();
            drones = List.of();
            droneAvailability = List.of();
        }

        // Default values
        LngLat defaultLocation = new LngLat(-3.186874, 55.944494);
        double costPerMove = 0.001;
        double costInitial = 0.1;
        double costFinal = 0.1;

        // Build service point lookup map
        Map<Integer, DroneServicePoint> servicePointMap = new HashMap<>();
        if (servicePoints != null) {
            for (DroneServicePoint sp : servicePoints) {
                if (sp.getId() != null) {
                    servicePointMap.put(sp.getId(), sp);
                }
            }
        }

        // Get drone costs from first available drone
        if (drones != null && !drones.isEmpty()) {
            Drone drone = drones.get(0);
            if (drone.getCapability() != null) {
                costPerMove = drone.getCapability().getCostPerMove();
                costInitial = drone.getCapability().getCostInitial();
                costFinal = drone.getCapability().getCostFinal();
            }
        }

        // Group dispatches by service point (assign each dispatch to nearest service point)
        Map<Integer, List<MedDispatchRec>> dispatchesByServicePoint = groupDispatchesByServicePoint(
                dispatches, servicePoints, servicePointMap, defaultLocation);

        // Calculate paths for each service point
        List<DeliveryPathResult.DronePathInfo> dronePaths = new ArrayList<>();
        double totalCost = 0;
        int totalMoves = 0;

        for (Map.Entry<Integer, List<MedDispatchRec>> entry : dispatchesByServicePoint.entrySet()) {
            Integer servicePointId = entry.getKey();
            List<MedDispatchRec> servicePointDispatches = entry.getValue();

            // Get service point location
            LngLat servicePointLocation = defaultLocation;
            if (servicePointMap.containsKey(servicePointId)) {
                LngLat loc = servicePointMap.get(servicePointId).getLocation();
                if (loc != null) {
                    servicePointLocation = loc;
                }
            }

            System.out.println("Processing " + servicePointDispatches.size() +
                    " dispatches for service point " + servicePointId +
                    " at " + servicePointLocation);

            // Get available drones at this service point
            List<Drone> servicePointDrones = getDronesAtServicePoint(
                    servicePointId, droneAvailability, drones);

            System.out.println("  Available drones: " + servicePointDrones.size());

            // Assign dispatches to drones at this service point
            List<List<MedDispatchRec>> droneAssignments = assignDispatchesToDrones(
                    servicePointDispatches, servicePointDrones, servicePointLocation);

            // Calculate paths for each drone at this service point
            for (int droneIndex = 0; droneIndex < droneAssignments.size(); droneIndex++) {
                List<MedDispatchRec> droneDispatches = droneAssignments.get(droneIndex);
                if (droneDispatches.isEmpty()) {
                    continue;
                }

                // Get drone ID
                String droneId;
                if (servicePointDrones != null && droneIndex < servicePointDrones.size() &&
                        servicePointDrones.get(droneIndex).getId() != null) {
                    droneId = servicePointDrones.get(droneIndex).getId();
                } else {
                    droneId = "SP" + servicePointId + "-D" + (droneIndex + 1);
                }

                // Calculate path for this drone
                List<DeliveryPathResult.DeliveryInfo> deliveries = new ArrayList<>();
                List<LngLat> combinedPath = new ArrayList<>();
                int droneMoves = 0;
                LngLat currentLocation = servicePointLocation;

                // Add start location to combined path
                combinedPath.add(servicePointLocation);

                // Use greedy nearest-neighbor for delivery order
                List<MedDispatchRec> orderedDispatches = orderByNearestNeighbor(droneDispatches, servicePointLocation);

                for (int deliveryIndex = 0; deliveryIndex < orderedDispatches.size(); deliveryIndex++) {
                    MedDispatchRec dispatch = orderedDispatches.get(deliveryIndex);
                    boolean isLastDelivery = (deliveryIndex == orderedDispatches.size() - 1);

                    LngLat pickupLocation = dispatch.getPickupLocation();
                    LngLat deliveryLocation = dispatch.getDeliveryLocation();

                    // Debug logging
                    System.out.println("  Processing dispatch " + dispatch.getId() +
                            ": pickup=" + pickupLocation + ", delivery=" + deliveryLocation);

                    // Use default locations if missing
                    if (pickupLocation == null) {
                        System.out.println("  WARNING: Dispatch " + dispatch.getId() + " has no pickup location, using service point");
                        pickupLocation = servicePointLocation;
                    }
                    if (deliveryLocation == null) {
                        System.out.println("  WARNING: Dispatch " + dispatch.getId() + " has no delivery location, using service point");
                        deliveryLocation = servicePointLocation;
                    }

                    // Path to pickup
                    List<LngLat> toPickup = findPath(currentLocation, pickupLocation, noFlyZones);
                    // Path from pickup to delivery
                    List<LngLat> toDelivery = findPath(pickupLocation, deliveryLocation, noFlyZones);

                    // Combine paths avoiding duplicate at pickup location
                    List<LngLat> flightPath = new ArrayList<>();
                    flightPath.addAll(toPickup);

                    // Skip first point of toDelivery to avoid duplicate at pickup
                    for (int i = 1; i < toDelivery.size(); i++) {
                        flightPath.add(toDelivery.get(i));
                    }

                    // Ensure delivery location is in the path
                    if (flightPath.isEmpty() || !flightPath.get(flightPath.size() - 1).equals(deliveryLocation)) {
                        flightPath.add(deliveryLocation);
                    }

                    // Add hover (duplicate delivery location to indicate delivery)
                    flightPath.add(deliveryLocation);

                    // If this is the last delivery, add return path to service point
                    if (isLastDelivery && !deliveryLocation.equals(servicePointLocation)) {
                        List<LngLat> returnPath = findPath(deliveryLocation, servicePointLocation, noFlyZones);
                        // Skip first point of return path to avoid duplicate at delivery location
                        for (int i = 1; i < returnPath.size(); i++) {
                            flightPath.add(returnPath.get(i));
                        }
                    }

                    // Add to combined path (skip first point if it duplicates the last point)
                    for (int i = 0; i < flightPath.size(); i++) {
                        if (i == 0 && !combinedPath.isEmpty() &&
                                combinedPath.get(combinedPath.size() - 1).equals(flightPath.get(i))) {
                            continue;
                        }
                        combinedPath.add(flightPath.get(i));
                    }

                    deliveries.add(new DeliveryPathResult.DeliveryInfo(dispatch.getId(), flightPath));

                    int moves = Math.max(0, flightPath.size() - 1);
                    droneMoves += moves;

                    currentLocation = deliveryLocation;
                }

                // Calculate drone cost
                double droneCost = costInitial + (droneMoves * costPerMove) + costFinal;
                totalCost += droneCost;
                totalMoves += droneMoves;

                dronePaths.add(new DeliveryPathResult.DronePathInfo(droneId, servicePointLocation, deliveries, combinedPath, droneMoves));
            }
        }

        return new DeliveryPathResult(totalCost, totalMoves, dronePaths);
    }

    /**
     * Assigns dispatches to drones, considering:
     * - Time conflicts (same time = different drones)
     * - Drone capabilities (cooling, heating, capacity)
     * - Max moves constraint
     * - Minimizing drone usage
     *
     * CORRECTED: Capacity is NOT accumulated in chained deliveries since items
     * are delivered before picking up the next one.
     */
    private List<List<MedDispatchRec>> assignDispatchesToDrones(
            List<MedDispatchRec> dispatches,
            List<Drone> drones,
            LngLat servicePointLocation) {

        // Track state for each drone
        Map<Integer, Set<String>> droneTimeSlots = new HashMap<>();
        Map<Integer, Integer> droneEstimatedMoves = new HashMap<>();
        List<List<MedDispatchRec>> assignments = new ArrayList<>();

        // Determine available drones
        int maxDrones = drones != null && !drones.isEmpty() ? drones.size() : dispatches.size();

        for (MedDispatchRec dispatch : dispatches) {
            String timeSlot = getTimeSlot(dispatch);
            MedDispatchRec.Requirements req = dispatch.getRequirements();

            // Get dispatch requirements
            double requiredCapacity = req != null ? req.getCapacity() : 0;
            boolean requiresCooling = req != null && req.requiresCooling();
            boolean requiresHeating = req != null && req.requiresHeating();

            // Estimate moves for this dispatch (round trip from service point to delivery)
            LngLat deliveryLoc = dispatch.getDeliveryLocation();
            int estimatedMoves = 0;
            if (deliveryLoc != null) {
                double dist = distance(servicePointLocation, deliveryLoc);
                estimatedMoves = (int) Math.ceil(dist / MOVE_DISTANCE) * 2; // Round trip
            }

            int assignedDrone = -1;

            // Find a suitable drone
            for (int i = 0; i < maxDrones; i++) {
                // Check time conflict
                Set<String> occupiedSlots = droneTimeSlots.computeIfAbsent(i, k -> new HashSet<>());
                if (occupiedSlots.contains(timeSlot)) {
                    continue; // Time conflict
                }

                // Check drone capabilities
                if (drones != null && i < drones.size()) {
                    Drone drone = drones.get(i);
                    DroneCapability cap = drone.getCapability();

                    if (cap != null) {
                        // Check cooling requirement
                        if (requiresCooling && !cap.isCooling()) {
                            continue; // Drone doesn't have cooling
                        }

                        // Check heating requirement
                        if (requiresHeating && !cap.isHeating()) {
                            continue; // Drone doesn't have heating
                        }

                        // Check capacity (NOT accumulated - each delivery is independent)
                        if (requiredCapacity > cap.getCapacity()) {
                            continue; // Not enough capacity for this single item
                        }

                        // Check max moves constraint (accumulated for chained deliveries)
                        int currentMoves = droneEstimatedMoves.getOrDefault(i, 0);
                        if (cap.getMaxMoves() > 0 && currentMoves + estimatedMoves > cap.getMaxMoves()) {
                            continue; // Would exceed max moves
                        }
                    }
                }

                // This drone is suitable
                assignedDrone = i;
                occupiedSlots.add(timeSlot);

                // Update moves used (but NOT capacity - items delivered individually)
                int currentMoves = droneEstimatedMoves.getOrDefault(i, 0);
                droneEstimatedMoves.put(i, currentMoves + estimatedMoves);
                break;
            }

            // If no suitable drone found, try to create/use a new one
            if (assignedDrone == -1) {
                // Find or create a drone that can handle this dispatch
                for (int i = 0; i < dispatches.size(); i++) {
                    Set<String> occupiedSlots = droneTimeSlots.computeIfAbsent(i, k -> new HashSet<>());
                    if (!occupiedSlots.contains(timeSlot)) {
                        // Check if this virtual drone can be matched to any available drone
                        boolean canHandle = true;
                        if (drones != null && !drones.isEmpty()) {
                            canHandle = false;
                            for (Drone drone : drones) {
                                DroneCapability cap = drone.getCapability();
                                if (cap != null) {
                                    boolean hasCooling = !requiresCooling || cap.isCooling();
                                    boolean hasHeating = !requiresHeating || cap.isHeating();
                                    boolean hasCapacity = requiredCapacity <= cap.getCapacity();
                                    if (hasCooling && hasHeating && hasCapacity) {
                                        canHandle = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (canHandle) {
                            assignedDrone = i;
                            occupiedSlots.add(timeSlot);
                            break;
                        }
                    }
                }
            }

            // Fallback: assign to a new drone index
            if (assignedDrone == -1) {
                assignedDrone = droneTimeSlots.size();
                Set<String> newSlots = new HashSet<>();
                newSlots.add(timeSlot);
                droneTimeSlots.put(assignedDrone, newSlots);
            }

            // Ensure we have enough lists
            while (assignments.size() <= assignedDrone) {
                assignments.add(new ArrayList<>());
            }

            assignments.get(assignedDrone).add(dispatch);
        }

        return assignments;
    }

    /**
     * Gets the time slot key for a dispatch (date + time).
     */
    private String getTimeSlot(MedDispatchRec dispatch) {
        String date = dispatch.getDate() != null ? dispatch.getDate() : "";
        String time = dispatch.getTime() != null ? dispatch.getTime() : "";
        return date + "T" + time;
    }

    @Override
    public String generateGeoJson(List<MedDispatchRec> dispatches) {
        DeliveryPathResult result = calculateDeliveryPaths(dispatches);

        StringBuilder json = new StringBuilder();
        json.append("{\"type\":\"FeatureCollection\",\"features\":[");

        boolean firstFeature = true;
        for (DeliveryPathResult.DronePathInfo dronePath : result.getDronePaths()) {
            List<LngLat> path = dronePath.getPath();
            if (path == null || path.isEmpty()) {
                continue;
            }

            if (!firstFeature) {
                json.append(",");
            }
            firstFeature = false;

            json.append("{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[");

            for (int i = 0; i < path.size(); i++) {
                LngLat point = path.get(i);
                json.append(String.format("[%.6f,%.6f]", point.lng(), point.lat()));
                if (i < path.size() - 1) {
                    json.append(",");
                }
            }

            json.append("]},\"properties\":{\"droneId\":\"");
            json.append(dronePath.getDroneId() != null ? dronePath.getDroneId() : "1");
            json.append("\"}}");
        }

        json.append("]}");

        return json.toString();
    }

    /**
     * Orders dispatches using greedy nearest-neighbor heuristic.
     * From current location, always pick the dispatch with nearest pickup.
     */
    private List<MedDispatchRec> orderByNearestNeighbor(List<MedDispatchRec> dispatches, LngLat start) {
        if (dispatches == null || dispatches.isEmpty()) {
            return new ArrayList<>();
        }
        if (dispatches.size() == 1) {
            return new ArrayList<>(dispatches);
        }

        List<MedDispatchRec> remaining = new ArrayList<>(dispatches);
        List<MedDispatchRec> ordered = new ArrayList<>();
        LngLat current = start;

        while (!remaining.isEmpty()) {
            MedDispatchRec nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (MedDispatchRec dispatch : remaining) {
                LngLat pickup = dispatch.getPickupLocation();
                if (pickup == null) continue;

                double dist = distance(current, pickup);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = dispatch;
                }
            }

            if (nearest != null) {
                ordered.add(nearest);
                remaining.remove(nearest);
                current = nearest.getDeliveryLocation() != null ? nearest.getDeliveryLocation() : current;
            } else {
                // No valid pickup found, add remaining as-is
                ordered.addAll(remaining);
                break;
            }
        }

        return ordered;
    }

    /**
     * A* pathfinding algorithm with 16 compass directions.
     */
    private List<LngLat> findPath(LngLat start, LngLat end, List<RestrictedArea> noFlyZones) {
        if (start == null || end == null) {
            return List.of();
        }

        // If already close to target, return single point (no movement needed)
        if (isCloseTo(start, end)) {
            return List.of(start);
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<String, Node> allNodes = new HashMap<>();
        Set<String> closedSet = new HashSet<>();

        Node startNode = new Node(start, null, 0, heuristic(start, end));
        openSet.add(startNode);
        allNodes.put(nodeKey(start), startNode);

        // With fixed priority queue logic, A* should find paths efficiently
        int maxIterations = 20000;
        int iterations = 0;

        while (!openSet.isEmpty() && iterations < maxIterations) {
            iterations++;
            Node current = openSet.poll();

            if (isCloseTo(current.position, end)) {
                // A* successfully found path
                if (iterations > 5000) {
                    System.out.println("  A* found path in " + iterations + " iterations (complex path)");
                }
                return reconstructPath(current, end);
            }

            closedSet.add(nodeKey(current.position));

            // Try all 16 directions
            for (double angle : DIRECTIONS) {
                LngLat nextPos = nextPosition(current.position, angle);
                String key = nodeKey(nextPos);

                if (closedSet.contains(key)) {
                    continue;
                }

                // Check if move crosses no-fly zone
                if (crossesNoFlyZone(current.position, nextPos, noFlyZones)) {
                    continue;
                }

                double tentativeG = current.gScore + MOVE_DISTANCE;
                Node neighbor = allNodes.get(key);

                if (neighbor == null) {
                    neighbor = new Node(nextPos, current, tentativeG, tentativeG + heuristic(nextPos, end));
                    allNodes.put(key, neighbor);
                    openSet.add(neighbor);
                } else if (tentativeG < neighbor.gScore) {
                    // CRITICAL FIX: Remove from queue before updating, then re-add
                    // This ensures the priority queue reorders based on new fScore
                    openSet.remove(neighbor);
                    neighbor.parent = current;
                    neighbor.gScore = tentativeG;
                    neighbor.fScore = tentativeG + heuristic(nextPos, end);
                    openSet.add(neighbor);
                }
            }
        }

        // No path found - A* exceeded max iterations or no valid path exists
        System.err.println("WARNING: A* pathfinding failed after " + iterations + " iterations from " +
                         start + " to " + end + ". Attempting fallback.");
        return generateDirectPath(start, end, noFlyZones);
    }

    /**
     * Generates a direct path from start to end using compass directions.
     * This checks for no-fly zones at each step and tries alternative directions if blocked.
     * Uses a visited set to avoid loops and implements wall-following when stuck.
     * Used as fallback when A* cannot find a path.
     */
    private List<LngLat> generateDirectPath(LngLat start, LngLat end, List<RestrictedArea> noFlyZones) {
        List<LngLat> path = new ArrayList<>();
        path.add(start);

        LngLat current = start;
        int maxMoves = 2000; // Increased safety limit
        int moves = 0;
        int consecutiveBlocked = 0;
        Set<String> visited = new HashSet<>();
        visited.add(nodeKey(start));

        while (!isCloseTo(current, end) && moves < maxMoves) {
            // Calculate angle to target
            double dx = end.lng() - current.lng();
            double dy = end.lat() - current.lat();
            double angleToTarget = Math.toDegrees(Math.atan2(dy, dx));
            if (angleToTarget < 0) {
                angleToTarget += 360;
            }

            // Sort directions by how close they are to target angle
            List<Double> sortedDirections = new ArrayList<>();
            for (double angle : DIRECTIONS) {
                sortedDirections.add(angle);
            }
            double finalAngleToTarget = angleToTarget;
            sortedDirections.sort((a1, a2) -> {
                double diff1 = Math.abs(a1 - finalAngleToTarget);
                if (diff1 > 180) diff1 = 360 - diff1;
                double diff2 = Math.abs(a2 - finalAngleToTarget);
                if (diff2 > 180) diff2 = 360 - diff2;
                return Double.compare(diff1, diff2);
            });

            // Try directions in order of preference, checking for no-fly zones
            boolean foundValidMove = false;
            LngLat bestUnvisitedMove = null;

            for (double angle : sortedDirections) {
                LngLat nextPos = nextPosition(current, angle);
                String nextKey = nodeKey(nextPos);

                // Check if this move crosses a no-fly zone
                if (!crossesNoFlyZone(current, nextPos, noFlyZones)) {
                    // Prefer unvisited positions
                    if (!visited.contains(nextKey)) {
                        current = nextPos;
                        path.add(current);
                        visited.add(nextKey);
                        foundValidMove = true;
                        consecutiveBlocked = 0;
                        break;
                    } else if (bestUnvisitedMove == null) {
                        // Keep track of first valid move even if visited (for last resort)
                        bestUnvisitedMove = nextPos;
                    }
                }
            }

            // If no unvisited valid move found, use visited position as last resort
            if (!foundValidMove && bestUnvisitedMove != null) {
                current = bestUnvisitedMove;
                path.add(current);
                foundValidMove = true;
                consecutiveBlocked++;

                // If we're revisiting positions too much, we're likely stuck in a loop
                if (consecutiveBlocked > 20) {
                    System.err.println("ERROR: Stuck in loop trying to reach " + end + " from " + current);
                    return path;
                }
            }

            if (!foundValidMove) {
                // All directions blocked - we're trapped
                consecutiveBlocked++;
                if (consecutiveBlocked > 10) {
                    System.err.println("ERROR: Cannot find valid path from " + start + " to " + end +
                                     " - all directions blocked by no-fly zones at " + current);
                    // Return path to current position (partial path)
                    return path;
                }
                // Hover in place
                path.add(current);
            }

            moves++;
        }

        if (!isCloseTo(current, end)) {
            System.err.println("WARNING: Fallback path reached max moves (" + maxMoves + ") without reaching target");
        }

        return path;
    }

    private List<LngLat> reconstructPath(Node endNode, LngLat target) {
        List<LngLat> path = new ArrayList<>();
        Node current = endNode;

        while (current != null) {
            path.add(0, current.position);
            current = current.parent;
        }

        // Add the actual target position
        if (!path.isEmpty() && !isCloseTo(path.get(path.size() - 1), target)) {
            path.add(target);
        }

        return path;
    }

    private LngLat nextPosition(LngLat current, double angleDegrees) {
        // 0° = East, 90° = North, 180° = West, 270° = South
        double angleRadians = Math.toRadians(angleDegrees);
        double newLng = current.lng() + MOVE_DISTANCE * Math.cos(angleRadians);
        double newLat = current.lat() + MOVE_DISTANCE * Math.sin(angleRadians);
        return new LngLat(newLng, newLat);
    }

    private boolean isCloseTo(LngLat p1, LngLat p2) {
        return distance(p1, p2) < CLOSE_THRESHOLD;
    }

    private double distance(LngLat p1, LngLat p2) {
        double dx = p1.lng() - p2.lng();
        double dy = p1.lat() - p2.lat();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double heuristic(LngLat from, LngLat to) {
        return distance(from, to);
    }

    private String nodeKey(LngLat pos) {
        // Round to avoid floating point precision issues
        long lng = Math.round(pos.lng() * 1000000);
        long lat = Math.round(pos.lat() * 1000000);
        return lng + "," + lat;
    }

    private boolean crossesNoFlyZone(LngLat from, LngLat to, List<RestrictedArea> noFlyZones) {
        for (RestrictedArea zone : noFlyZones) {
            // Check if line segment intersects polygon boundary
            if (lineIntersectsPolygon(from, to, zone.getVertices())) {
                return true;
            }
            // Check if starting point is inside the zone
            if (isPointInPolygon(from, zone.getVertices())) {
                return true;
            }
            // Check if endpoint is inside the zone
            if (isPointInPolygon(to, zone.getVertices())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPointInPolygon(LngLat point, List<LngLat> vertices) {
        if (vertices == null || vertices.size() < 3) {
            return false;
        }

        int n = vertices.size();

        // Check if polygon is closed (first vertex equals last vertex)
        boolean isClosed = vertices.get(0).equals(vertices.get(n - 1));

        // If closed, exclude the duplicate last vertex from edge checks
        int edgeCount = isClosed ? n - 1 : n;

        boolean inside = false;

        for (int i = 0, j = edgeCount - 1; i < edgeCount; j = i++) {
            LngLat vi = vertices.get(i);
            LngLat vj = vertices.get(j);

            if ((vi.lat() > point.lat()) != (vj.lat() > point.lat()) &&
                    point.lng() < (vj.lng() - vi.lng()) * (point.lat() - vi.lat()) /
                            (vj.lat() - vi.lat()) + vi.lng()) {
                inside = !inside;
            }
        }

        return inside;
    }

    private boolean lineIntersectsPolygon(LngLat p1, LngLat p2, List<LngLat> vertices) {
        if (vertices == null || vertices.size() < 3) {
            return false;
        }

        int n = vertices.size();

        // Check if polygon is closed (first vertex equals last vertex)
        boolean isClosed = vertices.get(0).equals(vertices.get(n - 1));

        // If closed, we only need to check edges up to n-1 (excluding the duplicate last vertex)
        // If open, we need to check all consecutive edges plus the closing edge
        if (isClosed) {
            // For closed polygon, check edges: 0-1, 1-2, ..., (n-2)-(n-1)
            // The last edge (n-1)-0 is already represented as it's the same as (n-1)-(duplicate of 0)
            for (int i = 0; i < n - 1; i++) {
                if (linesIntersect(p1, p2, vertices.get(i), vertices.get(i + 1))) {
                    return true;
                }
            }
        } else {
            // For open polygon, check consecutive edges
            for (int i = 0; i < n - 1; i++) {
                if (linesIntersect(p1, p2, vertices.get(i), vertices.get(i + 1))) {
                    return true;
                }
            }
            // Check closing edge from last to first vertex
            if (linesIntersect(p1, p2, vertices.get(n - 1), vertices.get(0))) {
                return true;
            }
        }

        return false;
    }

    private boolean linesIntersect(LngLat p1, LngLat p2, LngLat p3, LngLat p4) {
        double d1 = direction(p3, p4, p1);
        double d2 = direction(p3, p4, p2);
        double d3 = direction(p1, p2, p3);
        double d4 = direction(p1, p2, p4);

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
                ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }

        if (d1 == 0 && onSegment(p3, p4, p1)) return true;
        if (d2 == 0 && onSegment(p3, p4, p2)) return true;
        if (d3 == 0 && onSegment(p1, p2, p3)) return true;
        if (d4 == 0 && onSegment(p1, p2, p4)) return true;

        return false;
    }

    private double direction(LngLat p1, LngLat p2, LngLat p3) {
        return (p3.lng() - p1.lng()) * (p2.lat() - p1.lat()) -
                (p2.lng() - p1.lng()) * (p3.lat() - p1.lat());
    }

    private boolean onSegment(LngLat p1, LngLat p2, LngLat p) {
        return Math.min(p1.lng(), p2.lng()) <= p.lng() &&
                p.lng() <= Math.max(p1.lng(), p2.lng()) &&
                Math.min(p1.lat(), p2.lat()) <= p.lat() &&
                p.lat() <= Math.max(p1.lat(), p2.lat());
    }

    /**
     * Groups dispatches by their nearest service point.
     * Each dispatch is assigned to the service point that is closest to its delivery location.
     */
    private Map<Integer, List<MedDispatchRec>> groupDispatchesByServicePoint(
            List<MedDispatchRec> dispatches,
            List<DroneServicePoint> servicePoints,
            Map<Integer, DroneServicePoint> servicePointMap,
            LngLat defaultLocation) {

        Map<Integer, List<MedDispatchRec>> result = new HashMap<>();

        // If no service points, use a default service point ID of 0
        if (servicePoints == null || servicePoints.isEmpty()) {
            result.put(0, new ArrayList<>(dispatches));
            return result;
        }

        // Assign each dispatch to nearest service point
        for (MedDispatchRec dispatch : dispatches) {
            LngLat deliveryLoc = dispatch.getDeliveryLocation();
            if (deliveryLoc == null) {
                deliveryLoc = defaultLocation;
            }

            // Find nearest service point
            DroneServicePoint nearestSP = null;
            double minDistance = Double.MAX_VALUE;

            for (DroneServicePoint sp : servicePoints) {
                LngLat spLoc = sp.getLocation();
                if (spLoc != null) {
                    double dist = distance(spLoc, deliveryLoc);
                    if (dist < minDistance) {
                        minDistance = dist;
                        nearestSP = sp;
                    }
                }
            }

            // Add dispatch to the nearest service point's list
            Integer spId = nearestSP != null && nearestSP.getId() != null ? nearestSP.getId() : 0;
            result.computeIfAbsent(spId, k -> new ArrayList<>()).add(dispatch);
        }

        return result;
    }

    /**
     * Gets the list of drones available at a specific service point.
     * Merges drone IDs from availability data with drone details from the drones list.
     */
    private List<Drone> getDronesAtServicePoint(
            Integer servicePointId,
            List<DroneForServicePoint> droneAvailability,
            List<Drone> allDrones) {

        // Find drone IDs at this service point
        Set<String> droneIdsAtServicePoint = new HashSet<>();

        if (droneAvailability != null) {
            for (DroneForServicePoint dfsp : droneAvailability) {
                if (dfsp.getServicePointId() != null &&
                        dfsp.getServicePointId().equals(servicePointId)) {
                    List<DroneForServicePoint.DroneAvailability> drones = dfsp.getDrones();
                    if (drones != null) {
                        for (DroneForServicePoint.DroneAvailability da : drones) {
                            if (da.getId() != null) {
                                droneIdsAtServicePoint.add(da.getId());
                            }
                        }
                    }
                }
            }
        }

        // If no specific drones found, return all drones (fallback)
        if (droneIdsAtServicePoint.isEmpty()) {
            System.out.println("  WARNING: No specific drones found for service point " +
                    servicePointId + ", using all available drones");
            return allDrones != null ? allDrones : List.of();
        }

        // Filter drones list to only include drones at this service point
        List<Drone> result = new ArrayList<>();
        if (allDrones != null) {
            for (Drone drone : allDrones) {
                if (drone.getId() != null && droneIdsAtServicePoint.contains(drone.getId())) {
                    result.add(drone);
                }
            }
        }

        return result;
    }

    private static class Node {
        LngLat position;
        Node parent;
        double gScore;
        double fScore;

        Node(LngLat position, Node parent, double gScore, double fScore) {
            this.position = position;
            this.parent = parent;
            this.gScore = gScore;
            this.fScore = fScore;
        }
    }
}
