package ilp.submission.service;

import ilp.submission.model.LngLat;
import ilp.submission.model.Region;


public interface GeographicalService {

    double calculateDistance(LngLat from, LngLat to);


    boolean isCloseTo(LngLat from, LngLat to);


    LngLat calculateNextPosition(LngLat start, double angleDegrees);


    boolean isInRegion(LngLat point, Region region);
}
