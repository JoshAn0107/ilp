package ilp.submission.service;

import ilp.submission.model.MedDispatchRec;

import java.util.List;

/**
 * Service interface for checking drone availability.
 */
public interface DroneAvailabilityService {
    /**
     * Finds drones that can fulfill all the given dispatches.
     *
     * @param dispatches the medical dispatch records
     * @return List of drone IDs that can fulfill all dispatches
     */
    List<String> findAvailableDrones(List<MedDispatchRec> dispatches);
}
