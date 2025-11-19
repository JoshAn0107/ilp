package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Represents which drones are available at a service point.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DroneForServicePoint {
    @JsonProperty("servicePointId")
    private Integer servicePointId;

    @JsonProperty("drones")
    private List<DroneAvailability> drones;

    public DroneForServicePoint() {
    }

    public DroneForServicePoint(Integer servicePointId, List<DroneAvailability> drones) {
        this.servicePointId = servicePointId;
        this.drones = drones;
    }

    public Integer getServicePointId() {
        return servicePointId;
    }

    public void setServicePointId(Integer servicePointId) {
        this.servicePointId = servicePointId;
    }

    public List<DroneAvailability> getDrones() {
        return drones;
    }

    public void setDrones(List<DroneAvailability> drones) {
        this.drones = drones;
    }

    /**
     * Drone availability information.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DroneAvailability {
        @JsonProperty("id")
        private String id;  // Drone ID is String

        @JsonProperty("availability")
        private List<AvailabilityWindow> availability;

        public DroneAvailability() {
        }

        public DroneAvailability(String id, List<AvailabilityWindow> availability) {
            this.id = id;
            this.availability = availability;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<AvailabilityWindow> getAvailability() {
            return availability;
        }

        public void setAvailability(List<AvailabilityWindow> availability) {
            this.availability = availability;
        }
    }

    /**
     * Availability time window for a specific day.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AvailabilityWindow {
        @JsonProperty("dayOfWeek")
        private String dayOfWeek;

        @JsonProperty("from")
        private String from;

        @JsonProperty("until")
        private String until;

        public AvailabilityWindow() {
        }

        public AvailabilityWindow(String dayOfWeek, String from, String until) {
            this.dayOfWeek = dayOfWeek;
            this.from = from;
            this.until = until;
        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(String dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getUntil() {
            return until;
        }

        public void setUntil(String until) {
            this.until = until;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DroneForServicePoint that = (DroneForServicePoint) o;
        return Objects.equals(servicePointId, that.servicePointId) &&
                Objects.equals(drones, that.drones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servicePointId, drones);
    }

    @Override
    public String toString() {
        return String.format("DroneForServicePoint{servicePointId=%d, drones=%d}",
                servicePointId, drones != null ? drones.size() : 0);
    }
}
