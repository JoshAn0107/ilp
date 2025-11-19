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

        try {
            noFlyZones = ilpRestClient.fetchRestrictedAreas();
            servicePoints = ilpRestClient.fetchServicePoints();
            drones = ilpRestClient.fetchDrones();
        } catch (Exception e) {
            noFlyZones = List.of();
            servicePoints = List.of();
            drones = List.of();
        }

        // Default values
        LngLat startLocation = new LngLat(-3.186874, 55.944494);
        String droneId = "1";
        double costPerMove = 0.001;
        double costInitial = 0.1;
        double costFinal = 0.1;

        // Use actual service point if available
        if (servicePoints != null && !servicePoints.isEmpty()) {
            LngLat loc = servicePoints.get(0).getLocation();
            if (loc != null) {
                startLocation = loc;
            }
        }

        // Use actual drone if available
        if (drones != null && !drones.isEmpty()) {
            Drone drone = drones.get(0);
            if (drone.getId() != null) {
                droneId = drone.getId();
            }
            if (drone.getCapability() != null) {
                costPerMove = drone.getCapability().getCostPerMove();
                costInitial = drone.getCapability().getCostInitial();
                costFinal = drone.getCapability().getCostFinal();
            }
        }

        // Calculate paths for all deliveries
        List<DeliveryPathResult.DeliveryInfo> deliveries = new ArrayList<>();
        double totalCost = costInitial;
        int totalMoves = 0;
        LngLat currentLocation = startLocation;

        // Use greedy nearest-neighbor for delivery order
        List<MedDispatchRec> orderedDispatches = orderByNearestNeighbor(dispatches, startLocation);

        for (MedDispatchRec dispatch : orderedDispatches) {
            LngLat pickupLocation = dispatch.getPickupLocation();
            LngLat deliveryLocation = dispatch.getDeliveryLocation();

            // Use default locations if missing
            if (pickupLocation == null) {
                pickupLocation = startLocation;
            }
            if (deliveryLocation == null) {
                deliveryLocation = startLocation;
            }

            // Path to pickup
            List<LngLat> toPickup = findPath(currentLocation, pickupLocation, noFlyZones);
            // Path from pickup to delivery
            List<LngLat> toDelivery = findPath(pickupLocation, deliveryLocation, noFlyZones);

            // Combine paths with hover at delivery
            List<LngLat> flightPath = new ArrayList<>();
            flightPath.addAll(toPickup);
            flightPath.addAll(toDelivery);

            // Add hover (duplicate location to indicate delivery)
            if (!flightPath.isEmpty()) {
                flightPath.add(flightPath.get(flightPath.size() - 1));
            }

            deliveries.add(new DeliveryPathResult.DeliveryInfo(dispatch.getId(), flightPath));

            int moves = Math.max(0, flightPath.size() - 1);
            totalMoves += moves;
            totalCost += moves * costPerMove;

            currentLocation = deliveryLocation;
        }

        // Return to service point
        if (currentLocation != null && !currentLocation.equals(startLocation)) {
            List<LngLat> returnPath = findPath(currentLocation, startLocation, noFlyZones);
            int returnMoves = Math.max(0, returnPath.size() - 1);
            totalMoves += returnMoves;
            totalCost += returnMoves * costPerMove;
        }
        totalCost += costFinal;

        List<DeliveryPathResult.DronePathInfo> dronePaths = List.of(
                new DeliveryPathResult.DronePathInfo(droneId, deliveries)
        );

        return new DeliveryPathResult(totalCost, totalMoves, dronePaths);
    }

    @Override
    public String generateGeoJson(List<MedDispatchRec> dispatches) {
        DeliveryPathResult result = calculateDeliveryPaths(dispatches);

        List<double[]> coordinates = new ArrayList<>();

        for (DeliveryPathResult.DronePathInfo dronePath : result.getDronePaths()) {
            for (DeliveryPathResult.DeliveryInfo delivery : dronePath.getDeliveries()) {
                for (LngLat point : delivery.getFlightPath()) {
                    coordinates.add(new double[]{point.lng(), point.lat()});
                }
            }
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"type\":\"FeatureCollection\",\"features\":[");

        if (!coordinates.isEmpty()) {
            json.append("{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[");

            for (int i = 0; i < coordinates.size(); i++) {
                double[] coord = coordinates.get(i);
                json.append(String.format("[%.6f,%.6f]", coord[0], coord[1]));
                if (i < coordinates.size() - 1) {
                    json.append(",");
                }
            }

            json.append("]},\"properties\":{}}");
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

        // If already close to target, return direct path
        if (isCloseTo(start, end)) {
            return List.of(start, end);
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<String, Node> allNodes = new HashMap<>();
        Set<String> closedSet = new HashSet<>();

        Node startNode = new Node(start, null, 0, heuristic(start, end));
        openSet.add(startNode);
        allNodes.put(nodeKey(start), startNode);

        int maxIterations = 10000;
        int iterations = 0;

        while (!openSet.isEmpty() && iterations < maxIterations) {
            iterations++;
            Node current = openSet.poll();

            if (isCloseTo(current.position, end)) {
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
                    neighbor.parent = current;
                    neighbor.gScore = tentativeG;
                    neighbor.fScore = tentativeG + heuristic(nextPos, end);
                }
            }
        }

        // No path found, return direct path (may cross no-fly zones)
        return List.of(start, end);
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
            if (lineIntersectsPolygon(from, to, zone.getVertices())) {
                return true;
            }
            // Also check if endpoint is inside the zone
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
        boolean inside = false;

        for (int i = 0, j = n - 1; i < n; j = i++) {
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
        for (int i = 0; i < n - 1; i++) {
            if (linesIntersect(p1, p2, vertices.get(i), vertices.get(i + 1))) {
                return true;
            }
        }
        // Check last edge
        if (linesIntersect(p1, p2, vertices.get(n - 1), vertices.get(0))) {
            return true;
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
