package ilp.submission.service;

import ilp.submission.model.*;

import java.util.List;

/**
 * REST client interface for ILP service communication.
 */
public interface IlpRestClient {
    /**
     * Fetches all drones from the ILP service.
     *
     * @return List of drones
     */
    List<Drone> fetchDrones();

    /**
     * Fetches all regions from the ILP service.
     *
     * @return List of regions
     */
    List<Region> fetchRegions();

    /**
     * Fetches all restricted areas from the ILP service.
     *
     * @return List of restricted areas
     */
    List<RestrictedArea> fetchRestrictedAreas();

    /**
     * Fetches all drone service points from the ILP service.
     *
     * @return List of drone service points
     */
    List<DroneServicePoint> fetchServicePoints();

    /**
     * Fetches medical dispatch records for a given date.
     *
     * @param date the date in ISO format (yyyy-MM-dd)
     * @return List of medical dispatch records
     */
    List<MedDispatchRec> fetchMedDispatchRecords(String date);

    /**
     * Fetches drone availability for service points.
     *
     * @return list of drones for service points
     */
    List<DroneForServicePoint> fetchDroneAvailability();

    /**
     * Checks if the ILP service is alive.
     *
     * @return true if service is responding, false otherwise
     */
    boolean isAlive();
}
