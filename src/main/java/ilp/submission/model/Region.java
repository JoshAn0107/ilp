package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Region {
    @JsonProperty("name")
    private final String name;

    @JsonProperty("vertices")
    private final List<LngLat> vertices;

    public Region(
            @JsonProperty("name") String name,
            @JsonProperty("vertices") List<LngLat> vertices
    ) {
        this.name = Objects.requireNonNull(name, "Region name cannot be null");
        this.vertices = vertices != null ? List.copyOf(vertices) : List.of();
    }

    public Region() {
        this.name = "";
        this.vertices = List.of();
    }

    public String getName() {
        return name;
    }

    public List<LngLat> getVertices() {
        return vertices;
    }

    public boolean isValid() {
        if (vertices == null || vertices.size() < 4) {
            return false;
        }
        return vertices.get(0).equals(vertices.get(vertices.size() - 1));
    }

    public boolean isClosed() {
        if (vertices == null || vertices.size() < 2) {
            return false;
        }
        return vertices.get(0).equals(vertices.get(vertices.size() - 1));
    }

    public boolean contains(LngLat point) {
        Objects.requireNonNull(point, "Point cannot be null");
        if (!isValid()) {
            throw new IllegalStateException("Cannot check containment on invalid region");
        }

        int n = vertices.size();

        for (int i = 0; i < n - 1; i++) {
            LngLat vi = vertices.get(i);
            LngLat vj = vertices.get(i + 1);
            if (point.equals(vi)) {
                return true;
            }
            if (isPointOnSegment(point, vi, vj)) {
                return true;
            }
        }

        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            LngLat vi = vertices.get(i);
            LngLat vj = vertices.get(j);
            boolean intersect = ((vi.lat() > point.lat()) != (vj.lat() > point.lat())) &&
                    (point.lng() < (vj.lng() - vi.lng()) * (point.lat() - vi.lat()) /
                            (vj.lat() - vi.lat()) + vi.lng());
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }

    private boolean isPointOnSegment(LngLat point, LngLat segmentStart, LngLat segmentEnd) {
        final double EPSILON = 1e-10;
        double minLng = Math.min(segmentStart.lng(), segmentEnd.lng());
        double maxLng = Math.max(segmentStart.lng(), segmentEnd.lng());
        double minLat = Math.min(segmentStart.lat(), segmentEnd.lat());
        double maxLat = Math.max(segmentStart.lat(), segmentEnd.lat());

        if (point.lng() < minLng - EPSILON || point.lng() > maxLng + EPSILON ||
            point.lat() < minLat - EPSILON || point.lat() > maxLat + EPSILON) {
            return false;
        }

        double crossProduct = (segmentEnd.lng() - segmentStart.lng()) * (point.lat() - segmentStart.lat()) -
                              (segmentEnd.lat() - segmentStart.lat()) * (point.lng() - segmentStart.lng());
        return Math.abs(crossProduct) < EPSILON;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region region = (Region) o;
        return Objects.equals(name, region.name) &&
                Objects.equals(vertices, region.vertices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, vertices);
    }

    @Override
    public String toString() {
        return String.format("Region{name='%s', vertices=%d}", name, vertices.size());
    }
}
