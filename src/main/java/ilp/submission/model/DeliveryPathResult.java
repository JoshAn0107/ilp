package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class DeliveryPathResult {
    @JsonProperty("deliveries")
    private final List<Delivery> deliveries;

    @JsonProperty("flightPath")
    private final List<DronePath> flightPath;

    public DeliveryPathResult(
            @JsonProperty("deliveries") List<Delivery> deliveries,
            @JsonProperty("flightPath") List<DronePath> flightPath
    ) {
        this.deliveries = deliveries != null ? List.copyOf(deliveries) : List.of();
        this.flightPath = flightPath != null ? List.copyOf(flightPath) : List.of();
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public List<DronePath> getFlightPath() {
        return flightPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryPathResult that = (DeliveryPathResult) o;
        return Objects.equals(deliveries, that.deliveries) &&
                Objects.equals(flightPath, that.flightPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveries, flightPath);
    }

    @Override
    public String toString() {
        return String.format("DeliveryPathResult{deliveries=%d, flightPath=%d}",
                deliveries.size(), flightPath.size());
    }
}
