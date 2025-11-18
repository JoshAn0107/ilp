package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a drone with its identification and capabilities.
 */
public class Drone {
    @JsonProperty("name")
    private String name;

    @JsonProperty("id")
    private int id;

    @JsonProperty("capability")
    private DroneCapability capability;

    public Drone() {
    }

    public Drone(String name, int id, DroneCapability capability) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DroneCapability getCapability() {
        return capability;
    }

    public void setCapability(DroneCapability capability) {
        this.capability = capability;
    }
}
