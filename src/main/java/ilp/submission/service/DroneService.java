package ilp.submission.service;

import ilp.submission.model.Drone;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for drone-related operations.
 */
public interface DroneService {
    /**
     * Fetches all drones from the ILP REST service.
     *
     * @return List of all drones
     */
    List<Drone> getAllDrones();

    /**
     * Gets IDs of drones with specified cooling capability.
     *
     * @param hasCooling true to get drones with cooling, false without
     * @return List of drone IDs
     */
    List<String> getDronesWithCooling(boolean hasCooling);

    /**
     * Gets detailed information about a specific drone.
     *
     * @param id the drone ID
     * @return Optional containing the drone if found, empty otherwise
     */
    Optional<Drone> getDroneById(String id);
}
