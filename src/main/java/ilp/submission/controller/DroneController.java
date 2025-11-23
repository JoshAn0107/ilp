package ilp.submission.controller;

import ilp.submission.model.*;
import ilp.submission.service.DroneAvailabilityService;
import ilp.submission.service.DroneQueryService;
import ilp.submission.service.PathCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//REST controller for drone-related endpoints (CW2).
@RestController
@RequestMapping("/api/v1")
public class DroneController {

    private final DroneQueryService queryService;
    private final DroneAvailabilityService availabilityService;
    private final PathCalculationService pathService;

    public DroneController(DroneQueryService queryService,
                           DroneAvailabilityService availabilityService,
                           PathCalculationService pathService) {
        this.queryService = queryService;
        this.availabilityService = availabilityService;
        this.pathService = pathService;
    }


    @GetMapping("/dronesWithCooling/{state}")
    public ResponseEntity<List<String>> getDronesWithCooling(@PathVariable boolean state) {
        List<String> droneIds = queryService.findDronesWithCooling(state);
        return ResponseEntity.ok(droneIds);
    }


    @GetMapping("/droneDetails/{id}")
    public ResponseEntity<Drone> getDroneDetails(@PathVariable String id) {
        return queryService.findDroneById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/queryAsPath/{attribute}/{value}")
    public ResponseEntity<List<String>> queryAsPath(
            @PathVariable String attribute,
            @PathVariable String value) {
        List<String> droneIds = queryService.queryByAttribute(attribute, value);
        return ResponseEntity.ok(droneIds);
    }


    @PostMapping("/query")
    public ResponseEntity<List<String>> query(@RequestBody List<QueryAttribute> queries) {
        List<String> droneIds = queryService.queryByMultipleAttributes(queries);
        return ResponseEntity.ok(droneIds);
    }


    @PostMapping("/queryAvailableDrones")
    public ResponseEntity<List<String>> queryAvailableDrones(
            @RequestBody List<MedDispatchRec> dispatches) {
        if (dispatches == null || dispatches.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<String> droneIds = availabilityService.findAvailableDrones(dispatches);
        return ResponseEntity.ok(droneIds);
    }


    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<DeliveryPathResult> calcDeliveryPath(
            @RequestBody List<MedDispatchRec> dispatches) {
        if (dispatches == null || dispatches.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            DeliveryPathResult result = pathService.calculateDeliveryPaths(dispatches);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Return empty result on error
            return ResponseEntity.ok(new DeliveryPathResult(0, 0, List.of()));
        }
    }

  
    @PostMapping(value = "/calcDeliveryPathAsGeoJson", produces = "application/json")
    public ResponseEntity<String> calcDeliveryPathAsGeoJson(
            @RequestBody List<MedDispatchRec> dispatches) {
        if (dispatches == null || dispatches.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            String geoJson = pathService.generateGeoJson(dispatches);
            return ResponseEntity.ok(geoJson);
        } catch (Exception e) {
            // Return empty GeoJSON on error
            return ResponseEntity.ok("{\"type\":\"FeatureCollection\",\"features\":[]}");
        }
    }
}
