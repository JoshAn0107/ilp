package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a drone with its identification and capabilities.
 */
public class Drone {
    @JsonProperty("name")
    private String name;

    @JsonProperty("id")
    private String id;

    @JsonProperty("capability")
    private DroneCapability capability;

    public Drone() {
    }

    public Drone(String name, String id, DroneCapability capability) {
        this.name = name;
        this.id = id;
        this.capability = capability;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DroneCapability getCapability() {
        return capability;
    }

    public void setCapability(DroneCapability capability) {
        this.capability = capability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Drone drone = (Drone) o;
        return Objects.equals(id, drone.id) &&
                Objects.equals(name, drone.name) &&
                Objects.equals(capability, drone.capability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, capability);
    }

    @Override
    public String toString() {
        return String.format("Drone{id='%s', name='%s'}", id, name);
    }
}
