package ilp.submission.service;

import ilp.submission.model.*;

import java.util.List;

/**
 * Service interface for calculating drone flight paths.
 */
public interface PathCalculationService {
    /**
     * Calculates delivery paths for the given dispatches.
     *
     * @param dispatches the medical dispatch records
     * @return DeliveryPathResult containing all paths and costs
     */
    DeliveryPathResult calculateDeliveryPaths(List<MedDispatchRec> dispatches);

    /**
     * Generates GeoJSON representation of delivery paths.
     *
     * @param dispatches the medical dispatch records
     * @return GeoJSON string
     */
    String generateGeoJson(List<MedDispatchRec> dispatches);
}
