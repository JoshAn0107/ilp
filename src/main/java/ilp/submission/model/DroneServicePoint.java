package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class DroneServicePoint {
    @JsonProperty("id")
    private final String id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("longitude")
    private final double longitude;

    @JsonProperty("latitude")
    private final double latitude;

    public DroneServicePoint(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("longitude") double longitude,
            @JsonProperty("latitude") double latitude
    ) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Record-style accessors
    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public LngLat location() {
        return new LngLat(longitude, latitude);
    }

    // Traditional getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public LngLat getLocation() {
        return new LngLat(longitude, latitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DroneServicePoint that = (DroneServicePoint) o;
        return Double.compare(that.longitude, longitude) == 0 &&
                Double.compare(that.latitude, latitude) == 0 &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, longitude, latitude);
    }

    @Override
    public String toString() {
        return String.format("DroneServicePoint{id='%s', name='%s', location=(%.6f,%.6f)}",
                id, name, longitude, latitude);
    }
}
