package ilp.submission.controller;

import ilp.submission.dto.CloseToRequest;
import ilp.submission.dto.DistanceRequest;
import ilp.submission.dto.NextPositionRequest;
import ilp.submission.dto.RegionRequest;
import ilp.submission.model.LngLat;
import ilp.submission.service.GeographicalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class GeographicalController {
    private final GeographicalService geographicalService;

    public GeographicalController(GeographicalService geographicalService) {
        this.geographicalService = geographicalService;
    }

    @GetMapping("/uid")
    public ResponseEntity<String> getUid() {
        return ResponseEntity.ok("s2490684");
    }

    @PostMapping("/distanceTo")
    public ResponseEntity<Double> distanceTo(@RequestBody DistanceRequest request) {
        request.validate();
        double distance = geographicalService.calculateDistance(
                request.getPosition1(),
                request.getPosition2()
        );
        return ResponseEntity.ok(distance);
    }

    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody CloseToRequest request) {
        request.validate();
        boolean close = geographicalService.isCloseTo(
                request.getPosition1(),
                request.getPosition2()
        );
        return ResponseEntity.ok(close);
    }

    @PostMapping("/nextPosition")
    public ResponseEntity<LngLat> nextPosition(@RequestBody NextPositionRequest request) {
        request.validate();
        LngLat nextPosition = geographicalService.calculateNextPosition(
                request.getStart(),
                request.getAngle()
        );
        return ResponseEntity.ok(nextPosition);
    }

    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody RegionRequest request) {
        request.validate();
        boolean inRegion = geographicalService.isInRegion(
                request.getPosition(),
                request.getRegion()
        );
        return ResponseEntity.ok(inRegion);
    }
}
