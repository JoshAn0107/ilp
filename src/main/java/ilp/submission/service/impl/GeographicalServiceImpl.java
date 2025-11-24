package ilp.submission.service.impl;

import ilp.submission.model.LngLat;
import ilp.submission.model.Region;
import ilp.submission.service.GeographicalService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 *This is the implementation of geographical service operations.
 */
@Service
public class GeographicalServiceImpl implements GeographicalService {

    @Override
    public double calculateDistance(LngLat from, LngLat to) {
        Objects.requireNonNull(from, "Starting coordinate cannot be null");
        Objects.requireNonNull(to, "Destination coordinate cannot be null");

        // Delegate to domain model
        return from.distanceTo(to);
    }

    @Override
    public boolean isCloseTo(LngLat from, LngLat to) {
        Objects.requireNonNull(from, "Starting coordinate cannot be null");
        Objects.requireNonNull(to, "Destination coordinate cannot be null");

        // Delegate to domain model
        return from.isCloseTo(to);
    }

    @Override
    public LngLat calculateNextPosition(LngLat start, double angleDegrees) {
        Objects.requireNonNull(start, "Starting coordinate cannot be null");

        // Delegate to domain model
        return start.nextPosition(angleDegrees);
    }

    @Override
    public boolean isInRegion(LngLat point, Region region) {
        Objects.requireNonNull(point, "Point cannot be null");
        Objects.requireNonNull(region, "Region cannot be null");

        if (!region.isValid()) {
            throw new IllegalArgumentException(
                    "Region must be a valid closed polygon with at least 4 vertices");
        }

        // Delegate to domain model
        return region.contains(point);
    }
}
