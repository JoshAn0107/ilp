package ilp.submission.service.impl;

import ilp.submission.model.*;
import ilp.submission.service.DroneAvailabilityService;
import ilp.submission.service.IlpRestClient;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of DroneAvailabilityService for Group 4.
 */
@Service
public class DroneAvailabilityServiceImpl implements DroneAvailabilityService {

    private final IlpRestClient ilpRestClient;

    public DroneAvailabilityServiceImpl(IlpRestClient ilpRestClient) {
        this.ilpRestClient = ilpRestClient;
    }

    @Override
    public List<String> findAvailableDrones(List<MedDispatchRec> dispatches) {
        if (dispatches == null || dispatches.isEmpty()) {
            return List.of();
        }

        // Fetch all necessary data
        List<Drone> allDrones = ilpRestClient.fetchDrones();
        List<DroneForServicePoint> droneAvailability = ilpRestClient.fetchDroneAvailability();
        List<DroneServicePoint> servicePoints = ilpRestClient.fetchServicePoints();

        // Create lookup maps
        Map<String, Drone> droneMap = allDrones.stream()
                .collect(Collectors.toMap(Drone::getId, d -> d));

        Map<String, DroneServicePoint> servicePointMap = servicePoints.stream()
                .collect(Collectors.toMap(DroneServicePoint::getName, sp -> sp));

        // Start with all drone IDs
        List<String> result = allDrones.stream()
                .map(Drone::getId)
                .collect(Collectors.toList());

        // Iteratively filter by each dispatch (AND logic)
        for (MedDispatchRec dispatch : dispatches) {
            result = result.stream()
                    .filter(droneId -> canFulfillDispatch(
                            droneId,
                            dispatch,
                            droneMap,
                            droneAvailability,
                            servicePointMap))
                    .collect(Collectors.toList());

            // Early exit if no drones remain
            if (result.isEmpty()) {
                return List.of();
            }
        }

        return result;
    }

    private boolean canFulfillDispatch(
            String droneId,
            MedDispatchRec dispatch,
            Map<String, Drone> droneMap,
            List<DroneForServicePoint> droneAvailability,
            Map<String, DroneServicePoint> servicePointMap) {

        // Step 1: Check capacity, cooling, heating from /drones endpoint
        Drone drone = droneMap.get(droneId);
        if (drone == null || drone.getCapability() == null) {
            return false;
        }

        if (!meetsRequirements(drone.getCapability(), dispatch.getRequirements())) {
            return false;
        }

        // Step 2: Check date/time availability and maxMoves from /drones-for-service-points endpoint
        if (!isAvailableAtServicePoint(droneId, drone, dispatch, droneAvailability, servicePointMap)) {
            return false;
        }

        return true;
    }

    private boolean isAvailableAtServicePoint(
            String droneId,
            Drone drone,
            MedDispatchRec dispatch,
            List<DroneForServicePoint> droneAvailability,
            Map<String, DroneServicePoint> servicePointMap) {

        // If no date/time specified, assume available
        if (dispatch.getDate() == null || dispatch.getTime() == null) {
            return true;
        }

        try {
            LocalDate date = LocalDate.parse(dispatch.getDate());
            LocalTime time = LocalTime.parse(dispatch.getTime());
            String dayOfWeek = getDayOfWeek(date.getDayOfWeek());

            // Check ALL service points to see if drone is available at ANY of them
            for (DroneForServicePoint availability : droneAvailability) {
                if (availability.getDrones() == null) {
                    continue;
                }

                // Find this specific drone's availability at this service point
                DroneForServicePoint.DroneAvailability droneAvail = availability.getDrones().stream()
                        .filter(da -> da.getId().equals(droneId))
                        .findFirst()
                        .orElse(null);

                if (droneAvail == null || droneAvail.getAvailability() == null) {
                    continue; // This drone is not available at this service point
                }

                // Check if the dispatch date/time falls within availability windows
                for (DroneForServicePoint.AvailabilityWindow window : droneAvail.getAvailability()) {
                    // First check: Does the day match?
                    if (window.getDayOfWeek().equalsIgnoreCase(dayOfWeek)) {
                        LocalTime from = LocalTime.parse(window.getFrom());
                        LocalTime until = LocalTime.parse(window.getUntil());

                        // Second check: Does the time fall within the window?
                        if (!time.isBefore(from) && !time.isAfter(until)) {
                            // Third check: Does the drone have enough maxMoves for this delivery?
                            if (hasEnoughMovesForDelivery(drone, availability.getServicePointId(),
                                    dispatch.getDeliveryLocation(), servicePointMap)) {
                                return true; // Drone is available and can complete this delivery!
                            }
                        }
                    }
                }
            }

            return false; // Drone is not available at any service point for this date/time
        } catch (Exception e) {
            return false; // Parsing error
        }
    }

    private String getDayOfWeek(DayOfWeek dayOfWeek) {
        return dayOfWeek.name(); // Returns "MONDAY", "TUESDAY", etc.
    }

    private boolean hasEnoughMovesForDelivery(
            Drone drone,
            Integer servicePointId,
            LngLat deliveryLocation,
            Map<String, DroneServicePoint> servicePointMap) {

        if (deliveryLocation == null || drone.getCapability() == null) {
            return false;
        }

        // Find the service point by ID
        DroneServicePoint servicePoint = servicePointMap.values().stream()
                .filter(sp -> sp.getId().equals(servicePointId))
                .findFirst()
                .orElse(null);

        if (servicePoint == null || servicePoint.getLocation() == null) {
            return false;
        }

        // Calculate distance from service point to delivery location
        LngLat servicePointLocation = servicePoint.getLocation();
        double distance = servicePointLocation.distanceTo(deliveryLocation);

        // Each move covers 0.00015 units (from LngLat.MOVE_DISTANCE)
        double moveDistance = 0.00015;

        // Calculate moves needed for one-way trip (round up)
        int movesOneWay = (int) Math.ceil(distance / moveDistance);

        // Round trip: service point -> delivery -> service point
        int totalMovesNeeded = movesOneWay * 2;

        // Check if drone has enough maxMoves
        return drone.getCapability().getMaxMoves() >= totalMovesNeeded;
    }

    private boolean meetsRequirements(DroneCapability cap, MedDispatchRec.Requirements requirements) {
        if (requirements == null) {
            return true;
        }

        // Check capacity
        if (cap.getCapacity() < requirements.getCapacity()) {
            return false;
        }

        // Check cooling (mutually exclusive with heating)
        if (requirements.requiresCooling() && !cap.isCooling()) {
            return false;
        }

        // Check heating (mutually exclusive with cooling)
        if (requirements.requiresHeating() && !cap.isHeating()) {
            return false;
        }

        // Cooling and heating are mutually exclusive
        if (requirements.requiresCooling() && requirements.requiresHeating()) {
            return false;
        }

        // Check maxCost if specified
        // At minimum, a drone costs costInitial + costFinal (even with zero moves)
        // If maxCost is less than this minimum, the drone cannot fulfill the requirement
        if (requirements.getMaxCost() != null) {
            double minCost = cap.getCostInitial() + cap.getCostFinal();
            if (requirements.getMaxCost() < minCost) {
                return false;
            }
        }

        return true;
    }
}
