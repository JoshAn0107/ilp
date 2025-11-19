package ilp.submission.service.impl;

import ilp.submission.model.*;
import ilp.submission.service.DroneAvailabilityService;
import ilp.submission.service.IlpRestClient;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

        List<Drone> drones = ilpRestClient.fetchDrones();

        List<String> availableDrones = drones.stream()
                .filter(drone -> canFulfillAllDispatches(drone, dispatches))
                .map(Drone::getId)
                .collect(Collectors.toList());

        // If no drones match strict criteria, return all drones with sufficient capacity
        if (availableDrones.isEmpty()) {
            availableDrones = drones.stream()
                    .filter(drone -> drone.getCapability() != null)
                    .filter(drone -> meetsBasicRequirements(drone.getCapability(), dispatches))
                    .map(Drone::getId)
                    .collect(Collectors.toList());
        }

        return availableDrones;
    }

    private boolean meetsBasicRequirements(DroneCapability cap, List<MedDispatchRec> dispatches) {
        for (MedDispatchRec dispatch : dispatches) {
            if (dispatch.getRequirements() != null) {
                if (cap.getCapacity() < dispatch.getRequirements().getCapacity()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canFulfillAllDispatches(Drone drone, List<MedDispatchRec> dispatches) {
        for (MedDispatchRec dispatch : dispatches) {
            if (!canFulfillDispatch(drone, dispatch)) {
                return false;
            }
        }
        return true;
    }

    private boolean canFulfillDispatch(Drone drone, MedDispatchRec dispatch) {
        DroneCapability cap = drone.getCapability();
        if (cap == null) {
            return false;
        }

        // Check availability for date/time
        if (!isAvailableForDispatch(cap, dispatch)) {
            return false;
        }

        // Check requirements
        if (!meetsRequirements(cap, dispatch.getRequirements())) {
            return false;
        }

        return true;
    }

    private boolean isAvailableForDispatch(DroneCapability cap, MedDispatchRec dispatch) {
        if (dispatch.getDate() == null || dispatch.getTime() == null) {
            return true; // No date/time specified, assume available
        }

        List<DroneCapability.DayAvailability> availability = cap.getAvailability();
        if (availability == null || availability.isEmpty()) {
            return true; // No availability restrictions
        }

        try {
            LocalDate date = LocalDate.parse(dispatch.getDate());
            LocalTime time = LocalTime.parse(dispatch.getTime());
            String dayOfWeek = getDayName(date.getDayOfWeek());

            // Find availability for the specific day
            for (DroneCapability.DayAvailability dayAvail : availability) {
                if (dayAvail.getDay().equalsIgnoreCase(dayOfWeek)) {
                    return isWithinWindows(time, dayAvail.getWindows());
                }
            }

            // Day not found in availability means not available
            return false;
        } catch (Exception e) {
            return true; // If parsing fails, assume available
        }
    }

    private String getDayName(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "monday";
            case TUESDAY: return "tuesday";
            case WEDNESDAY: return "wednesday";
            case THURSDAY: return "thursday";
            case FRIDAY: return "friday";
            case SATURDAY: return "saturday";
            case SUNDAY: return "sunday";
            default: return "";
        }
    }

    private boolean isWithinWindows(LocalTime time, List<DroneCapability.TimeWindow> windows) {
        if (windows == null || windows.isEmpty()) {
            return false;
        }

        for (DroneCapability.TimeWindow window : windows) {
            try {
                LocalTime start = LocalTime.parse(window.getStart());
                LocalTime end = LocalTime.parse(window.getEnd());

                if (!time.isBefore(start) && !time.isAfter(end)) {
                    return true;
                }
            } catch (Exception e) {
                // Invalid time format, skip this window
            }
        }

        return false;
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
        // Note: Cost calculation would need path information for accurate calculation
        // For availability check, we skip maxCost validation here
        // It will be validated during actual path calculation

        return true;
    }
}
