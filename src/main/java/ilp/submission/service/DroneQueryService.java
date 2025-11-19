package ilp.submission.service;

import ilp.submission.model.Drone;
import ilp.submission.model.QueryAttribute;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for querying drone information.
 */
public interface DroneQueryService {
    /**
     * Finds drones with specific cooling capability.
     *
     * @param hasCooling true to get drones with cooling, false without
     * @return List of drone IDs as strings
     */
    List<String> findDronesWithCooling(boolean hasCooling);

    /**
     * Finds a drone by its ID.
     *
     * @param id the drone ID
     * @return Optional containing the drone if found
     */
    Optional<Drone> findDroneById(String id);

    /**
     * Queries drones by a single attribute.
     *
     * @param attribute the attribute name
     * @param value the attribute value
     * @return List of drone IDs matching the criteria
     */
    List<String> queryByAttribute(String attribute, String value);

    /**
     * Queries drones by multiple attributes (AND logic).
     *
     * @param queries the query attributes with operators
     * @return List of drone IDs matching all criteria
     */
    List<String> queryByMultipleAttributes(List<QueryAttribute> queries);
}
