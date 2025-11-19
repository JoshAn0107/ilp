package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Query attribute with optional operator for drone queries.
 */
public class QueryAttribute {
    @JsonProperty("attribute")
    private String attribute;

    @JsonProperty("operator")
    private String operator;

    @JsonProperty("value")
    private String value;

    public QueryAttribute() {
    }

    public QueryAttribute(String attribute, String value) {
        this.attribute = attribute;
        this.operator = "=";
        this.value = value;
    }

    public QueryAttribute(String attribute, String operator, String value) {
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getOperator() {
        return operator != null ? operator : "=";
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryAttribute that = (QueryAttribute) o;
        return Objects.equals(attribute, that.attribute) &&
                Objects.equals(operator, that.operator) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attribute, operator, value);
    }

    @Override
    public String toString() {
        return String.format("QueryAttribute{%s %s %s}", attribute, getOperator(), value);
    }
}
