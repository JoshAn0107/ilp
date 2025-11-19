package ilp.submission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Result of delivery path calculation containing cost, moves, and drone paths.
 */
public class DeliveryPathResult {
    @JsonProperty("totalCost")
    private double totalCost;

    @JsonProperty("totalMoves")
    private int totalMoves;

    @JsonProperty("dronePaths")
    private List<DronePathInfo> dronePaths;

    public DeliveryPathResult() {
    }

    public DeliveryPathResult(double totalCost, int totalMoves, List<DronePathInfo> dronePaths) {
        this.totalCost = totalCost;
        this.totalMoves = totalMoves;
        this.dronePaths = dronePaths != null ? List.copyOf(dronePaths) : List.of();
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public int getTotalMoves() {
        return totalMoves;
    }

    public void setTotalMoves(int totalMoves) {
        this.totalMoves = totalMoves;
    }

    public List<DronePathInfo> getDronePaths() {
        return dronePaths;
    }

    public void setDronePaths(List<DronePathInfo> dronePaths) {
        this.dronePaths = dronePaths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryPathResult that = (DeliveryPathResult) o;
        return Double.compare(that.totalCost, totalCost) == 0 &&
                totalMoves == that.totalMoves &&
                Objects.equals(dronePaths, that.dronePaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalCost, totalMoves, dronePaths);
    }

    @Override
    public String toString() {
        return String.format("DeliveryPathResult{totalCost=%.2f, totalMoves=%d, drones=%d}",
                totalCost, totalMoves, dronePaths != null ? dronePaths.size() : 0);
    }

    /**
     * Information about a single drone's delivery path.
     */
    public static class DronePathInfo {
        @JsonProperty("droneId")
        private String droneId;

        @JsonProperty("servicePoint")
        private LngLat servicePoint;

        @JsonProperty("deliveries")
        private List<DeliveryInfo> deliveries;

        @JsonProperty("path")
        private List<LngLat> path;

        @JsonProperty("totalMoves")
        private int totalMoves;

        public DronePathInfo() {
        }

        public DronePathInfo(String droneId, List<DeliveryInfo> deliveries) {
            this.droneId = droneId;
            this.deliveries = deliveries != null ? List.copyOf(deliveries) : List.of();
            this.path = List.of();
            this.totalMoves = 0;
        }

        public DronePathInfo(String droneId, LngLat servicePoint, List<DeliveryInfo> deliveries, List<LngLat> path, int totalMoves) {
            this.droneId = droneId;
            this.servicePoint = servicePoint;
            this.deliveries = deliveries != null ? List.copyOf(deliveries) : List.of();
            this.path = path != null ? List.copyOf(path) : List.of();
            this.totalMoves = totalMoves;
        }

        public String getDroneId() {
            return droneId;
        }

        public void setDroneId(String droneId) {
            this.droneId = droneId;
        }

        public LngLat getServicePoint() {
            return servicePoint;
        }

        public void setServicePoint(LngLat servicePoint) {
            this.servicePoint = servicePoint;
        }

        public List<DeliveryInfo> getDeliveries() {
            return deliveries;
        }

        public void setDeliveries(List<DeliveryInfo> deliveries) {
            this.deliveries = deliveries;
        }

        public List<LngLat> getPath() {
            return path;
        }

        public void setPath(List<LngLat> path) {
            this.path = path;
        }

        public int getTotalMoves() {
            return totalMoves;
        }

        public void setTotalMoves(int totalMoves) {
            this.totalMoves = totalMoves;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DronePathInfo that = (DronePathInfo) o;
            return totalMoves == that.totalMoves &&
                    Objects.equals(droneId, that.droneId) &&
                    Objects.equals(servicePoint, that.servicePoint) &&
                    Objects.equals(deliveries, that.deliveries) &&
                    Objects.equals(path, that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(droneId, servicePoint, deliveries, path, totalMoves);
        }
    }

    /**
     * Information about a single delivery within a drone path.
     */
    public static class DeliveryInfo {
        @JsonProperty("deliveryId")
        private int deliveryId;

        @JsonProperty("flightPath")
        private List<LngLat> flightPath;

        public DeliveryInfo() {
        }

        public DeliveryInfo(int deliveryId, List<LngLat> flightPath) {
            this.deliveryId = deliveryId;
            this.flightPath = flightPath != null ? List.copyOf(flightPath) : List.of();
        }

        public int getDeliveryId() {
            return deliveryId;
        }

        public void setDeliveryId(int deliveryId) {
            this.deliveryId = deliveryId;
        }

        public List<LngLat> getFlightPath() {
            return flightPath;
        }

        public void setFlightPath(List<LngLat> flightPath) {
            this.flightPath = flightPath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeliveryInfo that = (DeliveryInfo) o;
            return deliveryId == that.deliveryId &&
                    Objects.equals(flightPath, that.flightPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(deliveryId, flightPath);
        }
    }
}
