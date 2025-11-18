package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record  LngLat(
    @JsonProperty("lng") Double lng,
    @JsonProperty("lat") Double lat
) {
    private static final double CLOSE_THRESHOLD = 0.00015;
    private static final double MOVE_DISTANCE = 0.00015;

    /**
     * Compact constructor for validation.
     * Ensures that both lng and lat are not null and within valid geographic ranges.
     */
    public LngLat {
        if (lng == null) {
            throw new IllegalArgumentException("lng cannot be null");
        }
        if (lat == null) {
            throw new IllegalArgumentException("lat cannot be null");
        }
        if (lng < -180.0 || lng > 180.0) {
            throw new IllegalArgumentException("lng must be between -180 and 180, got: " + lng);
        }
        if (lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException("lat must be between -90 and 90, got: " + lat);
        }
    }

    public double distanceTo(LngLat other) {
        Objects.requireNonNull(other, "Target coordinate cannot be null");
        double lngDiff = this.lng - other.lng;
        double latDiff = this.lat - other.lat;
        return Math.sqrt(lngDiff * lngDiff + latDiff * latDiff);
    }

    public boolean isCloseTo(LngLat other) {
        return distanceTo(other) < CLOSE_THRESHOLD;
    }

    public LngLat nextPosition(double angleDegrees) {
        double angleRadians = Math.toRadians(angleDegrees);
        double newLng = this.lng + MOVE_DISTANCE * Math.cos(angleRadians);
        double newLat = this.lat + MOVE_DISTANCE * Math.sin(angleRadians);
        return new LngLat(newLng, newLat);
    }

    public boolean approximatelyEquals(LngLat other, double epsilon) {
        if (other == null) return false;
        return Math.abs(this.lng - other.lng) < epsilon &&
               Math.abs(this.lat - other.lat) < epsilon;
    }

    @Override
    public String toString() {
        return String.format("(%.6f, %.6f)", lng, lat);
    }
}
