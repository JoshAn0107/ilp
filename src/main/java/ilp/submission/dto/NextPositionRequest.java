package ilp.submission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ilp.submission.model.LngLat;

public class NextPositionRequest {
    @JsonProperty("start")
    private LngLat start;

    @JsonProperty("angle")
    private Double angle;

    public NextPositionRequest() {
    }

    public NextPositionRequest(LngLat start, Double angle) {
        this.start = start;
        this.angle = angle;
    }

    public LngLat getStart() {
        return start;
    }

    public void setStart(LngLat start) {
        this.start = start;
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }

    public void validate() {
        if (start == null) {
            throw new IllegalArgumentException("start position cannot be null");
        }
        if (angle == null) {
            throw new IllegalArgumentException("angle cannot be null");
        }
        if (angle < 0.0 || angle > 360.0) {
            throw new IllegalArgumentException("angle must be between 0 and 360, got: " + angle);
        }
    }
}
