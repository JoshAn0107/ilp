package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Medical dispatch record for delivery requests.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedDispatchRec {
    @JsonProperty("id")
    private int id;

    @JsonProperty("date")
    private String date;

    @JsonProperty("time")
    private String time;

    @JsonProperty("pickupName")
    private String pickupName;

    @JsonProperty("pickupLocation")
    private LngLat pickupLocation;

    @JsonProperty("deliveryLocation")
    @JsonAlias("delivery")
    private LngLat deliveryLocation;

    @JsonProperty("requirements")
    private Requirements requirements;

    public MedDispatchRec() {
    }

    public MedDispatchRec(int id, String date, String time, String pickupName,
                          LngLat pickupLocation, LngLat deliveryLocation, Requirements requirements) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.pickupName = pickupName;
        this.pickupLocation = pickupLocation;
        this.deliveryLocation = deliveryLocation;
        this.requirements = requirements;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPickupName() {
        return pickupName;
    }

    public void setPickupName(String pickupName) {
        this.pickupName = pickupName;
    }

    public LngLat getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(LngLat pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public LngLat getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(LngLat deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public Requirements getRequirements() {
        return requirements;
    }

    public void setRequirements(Requirements requirements) {
        this.requirements = requirements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedDispatchRec that = (MedDispatchRec) o;
        return id == that.id &&
                Objects.equals(date, that.date) &&
                Objects.equals(time, that.time) &&
                Objects.equals(pickupName, that.pickupName) &&
                Objects.equals(pickupLocation, that.pickupLocation) &&
                Objects.equals(deliveryLocation, that.deliveryLocation) &&
                Objects.equals(requirements, that.requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, time, pickupName, pickupLocation, deliveryLocation, requirements);
    }

    @Override
    public String toString() {
        return String.format("MedDispatchRec{id=%d, date='%s', time='%s'}", id, date, time);
    }

    /**
     * Requirements for a medical dispatch.
     */
    public static class Requirements {
        @JsonProperty("capacity")
        private double capacity;

        @JsonProperty("cooling")
        private Boolean cooling;

        @JsonProperty("heating")
        private Boolean heating;

        @JsonProperty("maxCost")
        private Double maxCost;

        public Requirements() {
        }

        public Requirements(double capacity, Boolean cooling, Boolean heating, Double maxCost) {
            this.capacity = capacity;
            this.cooling = cooling;
            this.heating = heating;
            this.maxCost = maxCost;
        }

        public double getCapacity() {
            return capacity;
        }

        public void setCapacity(double capacity) {
            this.capacity = capacity;
        }

        public Boolean getCooling() {
            return cooling;
        }

        public void setCooling(Boolean cooling) {
            this.cooling = cooling;
        }

        public Boolean getHeating() {
            return heating;
        }

        public void setHeating(Boolean heating) {
            this.heating = heating;
        }

        public Double getMaxCost() {
            return maxCost;
        }

        public void setMaxCost(Double maxCost) {
            this.maxCost = maxCost;
        }

        public boolean requiresCooling() {
            return cooling != null && cooling;
        }

        public boolean requiresHeating() {
            return heating != null && heating;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Requirements that = (Requirements) o;
            return Double.compare(that.capacity, capacity) == 0 &&
                    Objects.equals(cooling, that.cooling) &&
                    Objects.equals(heating, that.heating) &&
                    Objects.equals(maxCost, that.maxCost);
        }

        @Override
        public int hashCode() {
            return Objects.hash(capacity, cooling, heating, maxCost);
        }
    }
}
