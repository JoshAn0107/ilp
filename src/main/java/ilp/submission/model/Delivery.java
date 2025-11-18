package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Delivery {
    @JsonProperty("orderNo")
    private final String orderNo;

    @JsonProperty("outcome")
    private final String outcome;

    @JsonProperty("costInPence")
    private final int costInPence;

    public Delivery(
            @JsonProperty("orderNo") String orderNo,
            @JsonProperty("outcome") String outcome,
            @JsonProperty("costInPence") int costInPence
    ) {
        this.orderNo = Objects.requireNonNull(orderNo, "Order number cannot be null");
        this.outcome = Objects.requireNonNull(outcome, "Outcome cannot be null");
        this.costInPence = costInPence;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getOutcome() {
        return outcome;
    }

    public int getCostInPence() {
        return costInPence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Delivery delivery = (Delivery) o;
        return costInPence == delivery.costInPence &&
                Objects.equals(orderNo, delivery.orderNo) &&
                Objects.equals(outcome, delivery.outcome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNo, outcome, costInPence);
    }

    @Override
    public String toString() {
        return String.format("Delivery{orderNo='%s', outcome='%s', costInPence=%d}",
                orderNo, outcome, costInPence);
    }
}
