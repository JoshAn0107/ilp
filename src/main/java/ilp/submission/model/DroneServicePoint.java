package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DroneServicePoint {
    @JsonProperty("id")
    private final Integer id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("location")
    private final Location location;

    public DroneServicePoint(
            @JsonProperty("id") Integer id,
            @JsonProperty("name") String name,
            @JsonProperty("location") Location location
    ) {
        this.id = id;
        this.name = name != null ? name : "";
        this.location = location;
    }



    /**
     * Location with lng, lat, and optional alt.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        @JsonProperty("lng")
        private double lng;

        @JsonProperty("lat")
        private double lat;

        @JsonProperty("alt")
        private Integer alt;

        public Location() {}

        public Location(double lng, double lat, Integer alt) {
            this.lng = lng;
            this.lat = lat;
            this.alt = alt;
        }

        public double getLng() { return lng; }
        public double getLat() { return lat; }
        public Integer getAlt() { return alt; }
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocationObject() {
        return location;
    }

    public LngLat getLocation() {
        if (location == null) {
            return null;
        }
        return new LngLat(location.getLng(), location.getLat());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DroneServicePoint that = (DroneServicePoint) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, location);
    }

    @Override
    public String toString() {
        if (location != null) {
            return String.format("DroneServicePoint{id=%d, name='%s', location=(%.6f,%.6f)}",
                    id, name, location.getLng(), location.getLat());
        }
        return String.format("DroneServicePoint{id=%d, name='%s', location=null}", id, name);
    }
}
