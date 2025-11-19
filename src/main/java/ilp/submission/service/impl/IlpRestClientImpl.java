package ilp.submission.service.impl;

import ilp.submission.model.*;
import ilp.submission.service.IlpRestClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Implementation of IlpRestClient that fetches data from ILP REST API.
 * Data is fetched fresh on every call (no caching).
 */
@Service
public class IlpRestClientImpl implements IlpRestClient {

    private final String ilpEndpoint;
    private final RestTemplate restTemplate;

    public IlpRestClientImpl(
            @Qualifier("ilpEndpoint") String ilpEndpoint,
            RestTemplate restTemplate) {
        this.ilpEndpoint = ilpEndpoint;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Drone> fetchDrones() {
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
    public List<Region> fetchRegions() {
        String url = ilpEndpoint + "regions";
        ResponseEntity<List<Region>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Region>>() {}
        );
        return response.getBody() != null ? response.getBody() : List.of();
    }

    @Override
    public List<RestrictedArea> fetchRestrictedAreas() {
        String url = ilpEndpoint + "restricted-areas";
        ResponseEntity<List<RestrictedArea>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RestrictedArea>>() {}
        );
        return response.getBody() != null ? response.getBody() : List.of();
    }

    @Override
    public List<DroneServicePoint> fetchServicePoints() {
        String url = ilpEndpoint + "service-points";
        ResponseEntity<List<DroneServicePoint>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<DroneServicePoint>>() {}
        );
        return response.getBody() != null ? response.getBody() : List.of();
    }

    @Override
    public List<MedDispatchRec> fetchMedDispatchRecords(String date) {
        String url = ilpEndpoint + "medDispatchRecs/" + date;
        ResponseEntity<List<MedDispatchRec>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MedDispatchRec>>() {}
        );
        return response.getBody() != null ? response.getBody() : List.of();
    }

    @Override
    public boolean isAlive() {
        try {
            String url = ilpEndpoint + "actuator/health/livenessState";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            // Check for 2xx status and "UP" in response
            return response.getStatusCode().is2xxSuccessful() &&
                   response.getBody() != null &&
                   response.getBody().contains("UP");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Fetches the central area region.
     *
     * @return the central area region
     */
    public Region fetchCentralArea() {
        String url = ilpEndpoint + "centralArea";
        return restTemplate.getForObject(url, Region.class);
    }

    /**
     * Fetches drone availability for service points.
     *
     * @return list of drones for service points
     */
    public List<DroneForServicePoint> fetchDroneAvailability() {
        String url = ilpEndpoint + "drones-for-service-points";
        ResponseEntity<List<DroneForServicePoint>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<DroneForServicePoint>>() {}
        );
        return response.getBody() != null ? response.getBody() : List.of();
    }
}
