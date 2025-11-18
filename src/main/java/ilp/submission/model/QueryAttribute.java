package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class QueryAttribute {
    @JsonProperty("name")
    private final String name;

    @JsonProperty("value")
    private final String value;

    public QueryAttribute(
            @JsonProperty("name") String name,
            @JsonProperty("value") String value
    ) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.value = Objects.requireNonNull(value, "Value cannot be null");
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryAttribute that = (QueryAttribute) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return String.format("QueryAttribute{name='%s', value='%s'}", name, value);
    }
}
