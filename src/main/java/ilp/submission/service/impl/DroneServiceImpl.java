package ilp.submission.service.impl;

import ilp.submission.model.Drone;
import ilp.submission.service.DroneService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of DroneService that fetches drone data from ILP REST endpoint.
 */
@Service
public class DroneServiceImpl implements DroneService {

    private final String ilpEndpoint;
    private final RestTemplate restTemplate;

    public DroneServiceImpl(
            @Qualifier("ilpEndpoint") String ilpEndpoint,
            RestTemplate restTemplate) {
        // Ensure endpoint ends with /
        this.ilpEndpoint = (ilpEndpoint != null && !ilpEndpoint.isEmpty())
                ? (ilpEndpoint.endsWith("/") ? ilpEndpoint : ilpEndpoint + "/")
                : "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/";
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Drone> getAllDrones() {
        String url = ilpEndpoint + "drones";
        ResponseEntity<List<Drone>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Drone>>() {}
        );
        return response.getBody() != null ? response.getBody() : List.of();
    }

    @Override
    public List<String> getDronesWithCooling(boolean hasCooling) {
        List<Drone> allDrones = getAllDrones();
        return allDrones.stream()
                .filter(drone -> drone.getCapability() != null &&
                        drone.getCapability().isCooling() == hasCooling)
                .map(Drone::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Drone> getDroneById(String id) {
        List<Drone> allDrones = getAllDrones();
        return allDrones.stream()
                .filter(drone -> drone.getId() != null && drone.getId().equals(id))
                .findFirst();
    }
}
