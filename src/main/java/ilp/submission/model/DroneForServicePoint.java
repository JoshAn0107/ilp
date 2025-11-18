package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class DroneForServicePoint {
    @JsonProperty("name")
    private final String name;

    @JsonProperty("location")
    private final LngLat location;

    public DroneForServicePoint(
            @JsonProperty("name") String name,
            @JsonProperty("location") LngLat location
    ) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.location = Objects.requireNonNull(location, "Location cannot be null");
    }

    public String getName() {
        return name;
    }

    public LngLat getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DroneForServicePoint that = (DroneForServicePoint) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, location);
    }

    @Override
    public String toString() {
        return String.format("DroneForServicePoint{name='%s', location=%s}", name, location);
    }
}
