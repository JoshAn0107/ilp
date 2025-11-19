package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Represents the capabilities and availability of a drone.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DroneCapability {
    @JsonProperty("cooling")
    private boolean cooling;

    @JsonProperty("heating")
    private boolean heating;

    @JsonProperty("capacity")
    private double capacity;

    @JsonProperty("maxMoves")
    private int maxMoves;

    @JsonProperty("costPerMove")
    private double costPerMove;

    @JsonProperty("costInitial")
    private double costInitial;

    @JsonProperty("costFinal")
    private double costFinal;

    @JsonProperty("availability")
    private List<DayAvailability> availability;

    public DroneCapability() {
    }

    public DroneCapability(boolean cooling, boolean heating, double capacity, int maxMoves,
                           double costPerMove, double costInitial, double costFinal,
                           List<DayAvailability> availability) {
        this.cooling = cooling;
        this.heating = heating;
        this.capacity = capacity;
        this.maxMoves = maxMoves;
        this.costPerMove = costPerMove;
        this.costInitial = costInitial;
        this.costFinal = costFinal;
        this.availability = availability;
    }

    public boolean isCooling() {
        return cooling;
    }

    public void setCooling(boolean cooling) {
        this.cooling = cooling;
    }

    public boolean isHeating() {
        return heating;
    }

    public void setHeating(boolean heating) {
        this.heating = heating;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public int getMaxMoves() {
        return maxMoves;
    }

    public void setMaxMoves(int maxMoves) {
        this.maxMoves = maxMoves;
    }

    public double getCostPerMove() {
        return costPerMove;
    }

    public void setCostPerMove(double costPerMove) {
        this.costPerMove = costPerMove;
    }

    public double getCostInitial() {
        return costInitial;
    }

    public void setCostInitial(double costInitial) {
        this.costInitial = costInitial;
    }

    public double getCostFinal() {
        return costFinal;
    }

    public void setCostFinal(double costFinal) {
        this.costFinal = costFinal;
    }

    public List<DayAvailability> getAvailability() {
        return availability;
    }

    public void setAvailability(List<DayAvailability> availability) {
        this.availability = availability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DroneCapability that = (DroneCapability) o;
        return cooling == that.cooling &&
                heating == that.heating &&
                Double.compare(that.capacity, capacity) == 0 &&
                maxMoves == that.maxMoves &&
                Double.compare(that.costPerMove, costPerMove) == 0 &&
                Double.compare(that.costInitial, costInitial) == 0 &&
                Double.compare(that.costFinal, costFinal) == 0 &&
                Objects.equals(availability, that.availability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cooling, heating, capacity, maxMoves, costPerMove, costInitial, costFinal, availability);
    }

    /**
     * Represents availability for a specific day.
     */
    public static class DayAvailability {
        @JsonProperty("day")
        private String day;

        @JsonProperty("windows")
        private List<TimeWindow> windows;

        public DayAvailability() {
        }

        public DayAvailability(String day, List<TimeWindow> windows) {
            this.day = day;
            this.windows = windows;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public List<TimeWindow> getWindows() {
            return windows;
        }

        public void setWindows(List<TimeWindow> windows) {
            this.windows = windows;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DayAvailability that = (DayAvailability) o;
            return Objects.equals(day, that.day) && Objects.equals(windows, that.windows);
        }

        @Override
        public int hashCode() {
            return Objects.hash(day, windows);
        }
    }

    /**
     * Represents a time window for availability.
     */
    public static class TimeWindow {
        @JsonProperty("start")
        private String start;

        @JsonProperty("end")
        private String end;

        public TimeWindow() {
        }

        public TimeWindow(String start, String end) {
            this.start = start;
            this.end = end;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TimeWindow that = (TimeWindow) o;
            return Objects.equals(start, that.start) && Objects.equals(end, that.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }
    }
}
