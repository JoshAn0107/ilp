package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class RestrictedArea {
    @JsonProperty("name")
    private final String name;

    @JsonProperty("id")
    private final Integer id;

    @JsonProperty("limits")
    private final Limits limits;

    @JsonProperty("vertices")
    private final List<LngLat> vertices;

    public RestrictedArea(
            @JsonProperty("name") String name,
            @JsonProperty("id") Integer id,
            @JsonProperty("limits") Limits limits,
            @JsonProperty("vertices") List<LngLat> vertices
    ) {
        this.name = name != null ? name : "";
        this.id = id;
        this.limits = limits;
        this.vertices = vertices != null ? List.copyOf(vertices) : List.of();
    }

    public RestrictedArea() {
        this.name = "";
        this.id = null;
        this.limits = null;
        this.vertices = List.of();
    }

    /**
     * Limits for restricted area altitude.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Limits {
        @JsonProperty("lower")
        private int lower;

        @JsonProperty("upper")
        private int upper;

        public Limits() {}

        public Limits(int lower, int upper) {
            this.lower = lower;
            this.upper = upper;
        }

        public int getLower() { return lower; }
        public int getUpper() { return upper; }
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    public Limits getLimits() {
        return limits;
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

    public boolean contains(LngLat point) {
        Objects.requireNonNull(point, "Point cannot be null");
        if (!isValid()) {
            throw new IllegalStateException("Cannot check containment on invalid restricted area");
        }

        int n = vertices.size();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestrictedArea that = (RestrictedArea) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(vertices, that.vertices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, vertices);
    }

    @Override
    public String toString() {
        return String.format("RestrictedArea{name='%s', vertices=%d}", name, vertices.size());
    }
}
