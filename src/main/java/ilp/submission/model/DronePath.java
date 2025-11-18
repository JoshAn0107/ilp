package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class DronePath {
    @JsonProperty("orderNo")
    private final String orderNo;

    @JsonProperty("fromLongitude")
    private final double fromLongitude;

    @JsonProperty("fromLatitude")
    private final double fromLatitude;

    @JsonProperty("angle")
    private final Double angle;

    @JsonProperty("toLongitude")
    private final double toLongitude;

    @JsonProperty("toLatitude")
    private final double toLatitude;

    public DronePath(
            @JsonProperty("orderNo") String orderNo,
            @JsonProperty("fromLongitude") double fromLongitude,
            @JsonProperty("fromLatitude") double fromLatitude,
            @JsonProperty("angle") Double angle,
            @JsonProperty("toLongitude") double toLongitude,
            @JsonProperty("toLatitude") double toLatitude
    ) {
        this.orderNo = Objects.requireNonNull(orderNo, "Order number cannot be null");
        this.fromLongitude = fromLongitude;
        this.fromLatitude = fromLatitude;
        this.angle = angle;
        this.toLongitude = toLongitude;
        this.toLatitude = toLatitude;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public double getFromLongitude() {
        return fromLongitude;
    }

    public double getFromLatitude() {
        return fromLatitude;
    }

    public Double getAngle() {
        return angle;
    }

    public double getToLongitude() {
        return toLongitude;
    }

    public double getToLatitude() {
        return toLatitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DronePath dronePath = (DronePath) o;
        return Double.compare(dronePath.fromLongitude, fromLongitude) == 0 &&
                Double.compare(dronePath.fromLatitude, fromLatitude) == 0 &&
                Double.compare(dronePath.toLongitude, toLongitude) == 0 &&
                Double.compare(dronePath.toLatitude, toLatitude) == 0 &&
                Objects.equals(orderNo, dronePath.orderNo) &&
                Objects.equals(angle, dronePath.angle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNo, fromLongitude, fromLatitude, angle, toLongitude, toLatitude);
    }

    @Override
    public String toString() {
        return String.format("DronePath{orderNo='%s', from=(%.6f,%.6f), angle=%s, to=(%.6f,%.6f)}",
                orderNo, fromLongitude, fromLatitude, angle, toLongitude, toLatitude);
    }
}
