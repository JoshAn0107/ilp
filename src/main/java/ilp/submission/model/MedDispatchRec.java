package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class MedDispatchRec {
    @JsonProperty("orderNo")
    private final String orderNo;

    @JsonProperty("pickupName")
    private final String pickupName;

    @JsonProperty("pickupLocation")
    private final LngLat pickupLocation;

    @JsonProperty("deliveryLocation")
    private final LngLat deliveryLocation;

    public MedDispatchRec(
            @JsonProperty("orderNo") String orderNo,
            @JsonProperty("pickupName") String pickupName,
            @JsonProperty("pickupLocation") LngLat pickupLocation,
            @JsonProperty("deliveryLocation") LngLat deliveryLocation
    ) {
        this.orderNo = Objects.requireNonNull(orderNo, "Order number cannot be null");
        this.pickupName = Objects.requireNonNull(pickupName, "Pickup name cannot be null");
        this.pickupLocation = Objects.requireNonNull(pickupLocation, "Pickup location cannot be null");
        this.deliveryLocation = Objects.requireNonNull(deliveryLocation, "Delivery location cannot be null");
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getPickupName() {
        return pickupName;
    }

    public LngLat getPickupLocation() {
        return pickupLocation;
    }

    public LngLat getDeliveryLocation() {
        return deliveryLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedDispatchRec that = (MedDispatchRec) o;
        return Objects.equals(orderNo, that.orderNo) &&
                Objects.equals(pickupName, that.pickupName) &&
                Objects.equals(pickupLocation, that.pickupLocation) &&
                Objects.equals(deliveryLocation, that.deliveryLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNo, pickupName, pickupLocation, deliveryLocation);
    }

    @Override
    public String toString() {
        return String.format("MedDispatchRec{orderNo='%s', pickupName='%s', pickup=%s, delivery=%s}",
                orderNo, pickupName, pickupLocation, deliveryLocation);
    }
}
