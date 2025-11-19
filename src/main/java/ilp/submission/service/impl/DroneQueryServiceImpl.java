package ilp.submission.service.impl;

import ilp.submission.model.Drone;
import ilp.submission.model.DroneCapability;
import ilp.submission.model.QueryAttribute;
import ilp.submission.service.DroneQueryService;
import ilp.submission.service.IlpRestClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of DroneQueryService for Groups 2 & 3.
 */
@Service
public class DroneQueryServiceImpl implements DroneQueryService {

    private final IlpRestClient ilpRestClient;

    public DroneQueryServiceImpl(IlpRestClient ilpRestClient) {
        this.ilpRestClient = ilpRestClient;
    }

    @Override
    public List<String> findDronesWithCooling(boolean hasCooling) {
        List<Drone> drones = ilpRestClient.fetchDrones();
        return drones.stream()
                .filter(drone -> drone.getCapability() != null &&
                        drone.getCapability().isCooling() == hasCooling)
                .map(Drone::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Drone> findDroneById(String id) {
        List<Drone> drones = ilpRestClient.fetchDrones();
        return drones.stream()
                .filter(drone -> drone.getId() != null && drone.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<String> queryByAttribute(String attribute, String value) {
        List<Drone> drones = ilpRestClient.fetchDrones();
        return drones.stream()
                .filter(drone -> matchesAttribute(drone, attribute, "=", value))
                .map(Drone::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> queryByMultipleAttributes(List<QueryAttribute> queries) {
        List<Drone> drones = ilpRestClient.fetchDrones();
        return drones.stream()
                .filter(drone -> matchesAllQueries(drone, queries))
                .map(Drone::getId)
                .collect(Collectors.toList());
    }

    private boolean matchesAllQueries(Drone drone, List<QueryAttribute> queries) {
        if (queries == null || queries.isEmpty()) {
            return true;
        }
        return queries.stream()
                .allMatch(query -> matchesAttribute(drone, query.getAttribute(),
                        query.getOperator(), query.getValue()));
    }

    private boolean matchesAttribute(Drone drone, String attribute, String operator, String value) {
        if (attribute == null || value == null) {
            return false;
        }

        String attrLower = attribute.toLowerCase();
        DroneCapability cap = drone.getCapability();

        switch (attrLower) {
            case "id":
                return compareString(drone.getId(), operator, value);
            case "name":
                return compareString(drone.getName(), operator, value);
            case "cooling":
                if (cap == null) return false;
                return compareBoolean(cap.isCooling(), operator, value);
            case "heating":
                if (cap == null) return false;
                return compareBoolean(cap.isHeating(), operator, value);
            case "capacity":
                if (cap == null) return false;
                return compareDouble(cap.getCapacity(), operator, value);
            case "maxmoves":
                if (cap == null) return false;
                return compareInt(cap.getMaxMoves(), operator, value);
            case "costpermove":
                if (cap == null) return false;
                return compareDouble(cap.getCostPerMove(), operator, value);
            case "costinitial":
                if (cap == null) return false;
                return compareDouble(cap.getCostInitial(), operator, value);
            case "costfinal":
                if (cap == null) return false;
                return compareDouble(cap.getCostFinal(), operator, value);
            default:
                return false;
        }
    }

    private boolean compareString(String actual, String operator, String expected) {
        if (actual == null) return false;
        switch (operator) {
            case "=":
                return actual.equals(expected);
            case "!=":
                return !actual.equals(expected);
            default:
                return actual.equals(expected);
        }
    }

    private boolean compareBoolean(boolean actual, String operator, String expected) {
        boolean expectedBool = Boolean.parseBoolean(expected);
        switch (operator) {
            case "=":
                return actual == expectedBool;
            case "!=":
                return actual != expectedBool;
            default:
                return actual == expectedBool;
        }
    }

    private boolean compareDouble(double actual, String operator, String expected) {
        try {
            double expectedVal = Double.parseDouble(expected);
            switch (operator) {
                case "=":
                    return Double.compare(actual, expectedVal) == 0;
                case "!=":
                    return Double.compare(actual, expectedVal) != 0;
                case "<":
                    return actual < expectedVal;
                case ">":
                    return actual > expectedVal;
                case "<=":
                    return actual <= expectedVal;
                case ">=":
                    return actual >= expectedVal;
                default:
                    return Double.compare(actual, expectedVal) == 0;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean compareInt(int actual, String operator, String expected) {
        try {
            int expectedVal = Integer.parseInt(expected);
            switch (operator) {
                case "=":
                    return actual == expectedVal;
                case "!=":
                    return actual != expectedVal;
                case "<":
                    return actual < expectedVal;
                case ">":
                    return actual > expectedVal;
                case "<=":
                    return actual <= expectedVal;
                case ">=":
                    return actual >= expectedVal;
                default:
                    return actual == expectedVal;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
