package ilp.submission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ilp.submission.model.LngLat;
import ilp.submission.model.Region;

public class RegionRequest {
    @JsonProperty("position")
    private LngLat position;

    @JsonProperty("region")
    private Region region;

    public RegionRequest() {
    }

    public RegionRequest(LngLat position, Region region) {
        this.position = position;
        this.region = region;
    }

    public LngLat getPosition() {
        return position;
    }

    public void setPosition(LngLat position) {
        this.position = position;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void validate() {
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        if (region == null) {
            throw new IllegalArgumentException("region cannot be null");
        }
        if (!region.isValid()) {
            throw new IllegalArgumentException(
                    "region must be a valid closed polygon with at least 4 vertices");
        }
    }
}
