package ilp.submission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ilp.submission.model.LngLat;

/**
* I am not using a record. This is implement in the old class way. I hope this is good choice please feedback on this if you see it :)
 */
public class CloseToRequest {
    @JsonProperty("position1")
    private LngLat position1;

    @JsonProperty("position2")
    private LngLat position2;

    public CloseToRequest() {
    }

    public CloseToRequest(LngLat position1, LngLat position2) {
        this.position1 = position1;
        this.position2 = position2;
    }

    public LngLat getPosition1() {
        return position1;
    }

    public void setPosition1(LngLat position1) {
        this.position1 = position1;
    }

    public LngLat getPosition2() {
        return position2;
    }

    public void setPosition2(LngLat position2) {
        this.position2 = position2;
    }

    public void validate() {
        if (position1 == null) {
            throw new IllegalArgumentException("position1 cannot be null");
        }
        if (position2 == null) {
            throw new IllegalArgumentException("position2 cannot be null");
        }
    }
}
