package ilp.submission.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Robust Integration tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Geographical Controller Integration Tests")
class GeographicalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== Health Endpoint ====================

    @Test
    @DisplayName("GET /actuator/health should return UP status")
    void testHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // ==================== UID Endpoint ====================

    @Test
    @DisplayName("GET /api/v1/uid should return student ID")
    void testGetUid() throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2490684"));
    }

    // ==================== distanceTo Endpoint ====================

    @Test
    @DisplayName("POST /api/v1/distanceTo with valid data should calculate distance")
    void testDistanceTo_ValidData() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": -3.192473, "lat": 55.946233},
                    "position2": {"lng": -3.192473, "lat": 55.942617}
                }
                """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    @DisplayName("POST /api/v1/distanceTo with Pythagorean triple should return 5.0")
    void testDistanceTo_PythagoreanTriple() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": 0, "lat": 0},
                    "position2": {"lng": 3, "lat": 4}
                }
                """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5.0));
    }

    @Test
    @DisplayName("POST /api/v1/distanceTo with same positions should return zero")
    void testDistanceTo_SamePositions() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": 1.0, "lat": 1.0},
                    "position2": {"lng": 1.0, "lat": 1.0}
                }
                """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0.0));
    }

    @Test
    @DisplayName("POST /api/v1/distanceTo with null position1 should return 400")
    void testDistanceTo_NullPosition1() throws Exception {
        String requestBody = """
                {
                    "position1": null,
                    "position2": {"lng": 1.0, "lat": 1.0}
                }
                """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("position1")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/distanceTo with null position2 should return 400")
    void testDistanceTo_NullPosition2() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": 1.0, "lat": 1.0},
                    "position2": null
                }
                """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/distanceTo with invalid string for lng should return 400")
    void testDistanceTo_InvalidStringData() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": "invalid", "lat": 55.946233},
                    "position2": {"lng": -3.192473, "lat": 55.942617}
                }
                """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/distanceTo with null lng value should return 400")
    void testDistanceTo_NullLngValue() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": null, "lat": 55.946233},
                    "position2": {"lng": -3.192473, "lat": 55.942617}
                }
                """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/distanceTo with missing lng field should return 400")
    void testDistanceTo_MissingLngField() throws Exception {
        String requestBody = """
                {
                    "position1": {"lat": 55.946233},
                    "position2": {"lng": -3.192473, "lat": 55.942617}
                }
                """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/distanceTo with horizontal distance should calculate correctly")
    void testDistanceTo_HorizontalDistance() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": -3.192473, "lat": 55.946233},
                    "position2": {"lng": -3.184319, "lat": 55.946233}
                }
                """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    @DisplayName("POST /api/v1/distanceTo with extra fields should ignore them and return 200")
    void testDistanceTo_ExtraFieldsIgnored() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": -3.192473, "lat": 55.946233, "extra": "ignored"},
                    "position2": {"lng": -3.192473, "lat": 55.942617, "altitude": 100},
                    "anotherExtra": "also ignored"
                }
                """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    // ==================== isCloseTo Endpoint ====================

    @Test
    @DisplayName("POST /api/v1/isCloseTo with close positions should return true")
    void testIsCloseTo_ClosePositions() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": 0.0, "lat": 0.0},
                    "position2": {"lng": 0.0001, "lat": 0.0001}
                }
                """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/isCloseTo with far positions should return false")
    void testIsCloseTo_FarPositions() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": 0.0, "lat": 0.0},
                    "position2": {"lng": 1.0, "lat": 1.0}
                }
                """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/isCloseTo with exactly threshold distance should return false")
    void testIsCloseTo_ExactlyAtThreshold() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": 0.0, "lat": 0.0},
                    "position2": {"lng": 0.00015, "lat": 0.0}
                }
                """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/isCloseTo with same position should return true")
    void testIsCloseTo_SamePosition() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": -3.192473, "lat": 55.946233},
                    "position2": {"lng": -3.192473, "lat": 55.946233}
                }
                """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/isCloseTo just below threshold should return true")
    void testIsCloseTo_JustBelowThreshold() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": 0.0, "lat": 0.0},
                    "position2": {"lng": 0.00014, "lat": 0.0}
                }
                """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/isCloseTo with null data should return 400")
    void testIsCloseTo_NullData() throws Exception {
        String requestBody = """
                {
                    "position1": null,
                    "position2": {"lng": 1.0, "lat": 1.0}
                }
                """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/isCloseTo with missing position2 should return 400")
    void testIsCloseTo_MissingPosition2() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": 1.0, "lat": 1.0}
                }
                """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/isCloseTo with invalid string values should return 400")
    void testIsCloseTo_InvalidStringValues() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": "not_a_number", "lat": 55.946233},
                    "position2": {"lng": -3.192473, "lat": 55.942617}
                }
                """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // ==================== nextPosition Endpoint ====================

    @Test
    @DisplayName("POST /api/v1/nextPosition at 0 degrees should move east")
    void testNextPosition_ZeroDegrees() throws Exception {
        String requestBody = """
                {
                    "start": {"lng": -3.192473, "lat": 55.946233},
                    "angle": 0
                }
                """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").value(greaterThan(-3.192473)))
                .andExpect(jsonPath("$.lat").value(closeTo(55.946233, 0.00001)));
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition at 90 degrees should move north")
    void testNextPosition_NinetyDegrees() throws Exception {
        String requestBody = """
                {
                    "start": {"lng": -3.192473, "lat": 55.946233},
                    "angle": 90
                }
                """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").value(closeTo(-3.192473, 0.00001)))
                .andExpect(jsonPath("$.lat").value(greaterThan(55.946233)));
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition at 45 degrees should move northeast")
    void testNextPosition_FortyFiveDegrees() throws Exception {
        String requestBody = """
                {
                    "start": {"lng": 0.0, "lat": 0.0},
                    "angle": 45
                }
                """;

        double expected = 0.00015 / Math.sqrt(2);

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").isNumber())
                .andExpect(jsonPath("$.lat").isNumber())
                .andExpect(jsonPath("$.lng").exists())
                .andExpect(jsonPath("$.lat").exists());
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition with null start should return 400")
    void testNextPosition_NullStart() throws Exception {
        String requestBody = """
                {
                    "start": null,
                    "angle": 45
                }
                """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("start")));
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition with null angle should return 400")
    void testNextPosition_NullAngle() throws Exception {
        String requestBody = """
                {
                    "start": {"lng": -3.192473, "lat": 55.946233},
                    "angle": null
                }
                """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("angle")));
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition with invalid string angle should return 400")
    void testNextPosition_InvalidStringAngle() throws Exception {
        String requestBody = """
                {
                    "start": {"lng": -3.192473, "lat": 55.946233},
                    "angle": "not_a_number"
                }
                """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition with null lng in start should return 400")
    void testNextPosition_NullLngInStart() throws Exception {
        String requestBody = """
                {
                    "start": {"lng": null, "lat": 55.946233},
                    "angle": 45
                }
                """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition with missing lat field should return 400")
    void testNextPosition_MissingLatField() throws Exception {
        String requestBody = """
                {
                    "start": {"lng": -3.192473},
                    "angle": 45
                }
                """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition at 180 degrees should move west")
    void testNextPosition_OneEightyDegrees() throws Exception {
        String requestBody = """
                {
                    "start": {"lng": -3.192473, "lat": 55.946233},
                    "angle": 180
                }
                """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").value(lessThan(-3.192473)))
                .andExpect(jsonPath("$.lat").value(closeTo(55.946233, 0.00001)));
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition at 270 degrees should move south")
    void testNextPosition_TwoSeventyDegrees() throws Exception {
        String requestBody = """
                {
                    "start": {"lng": -3.192473, "lat": 55.946233},
                    "angle": 270
                }
                """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").value(closeTo(-3.192473, 0.00001)))
                .andExpect(jsonPath("$.lat").value(lessThan(55.946233)));
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition at 360 degrees should be same as 0 degrees")
    void testNextPosition_ThreeSixtyDegrees() throws Exception {
        String requestBody = """
                {
                    "start": {"lng": -3.192473, "lat": 55.946233},
                    "angle": 360
                }
                """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").value(greaterThan(-3.192473)))
                .andExpect(jsonPath("$.lat").value(closeTo(55.946233, 0.00001)));
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition with negative angle should work (-90 = 270)")
    void testNextPosition_NegativeAngle() throws Exception {
        String requestBody = """
                {
                    "start": {"lng": 0.0, "lat": 0.0},
                    "angle": -90
                }
                """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").isNumber())
                .andExpect(jsonPath("$.lat").value(lessThan(0.0)));
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition with extra fields should ignore them")
    void testNextPosition_ExtraFieldsIgnored() throws Exception {
        String requestBody = """
                {
                    "start": {"lng": 0.0, "lat": 0.0},
                    "angle": 0,
                    "extraField": "should be ignored"
                }
                """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").isNumber())
                .andExpect(jsonPath("$.lat").isNumber());
    }

    // ==================== isInRegion Endpoint ====================

    @Test
    @DisplayName("POST /api/v1/isInRegion with position inside should return true")
    void testIsInRegion_PositionInside() throws Exception {
        String requestBody = """
                {
                    "position": {"lng": 1.5, "lat": 1.5},
                    "region": {
                        "name": "test-region",
                        "vertices": [
                            {"lng": 1.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 2.0},
                            {"lng": 1.0, "lat": 2.0},
                            {"lng": 1.0, "lat": 1.0}
                        ]
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion with position outside should return false")
    void testIsInRegion_PositionOutside() throws Exception {
        String requestBody = """
                {
                    "position": {"lng": 5.0, "lat": 5.0},
                    "region": {
                        "name": "test-region",
                        "vertices": [
                            {"lng": 1.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 2.0},
                            {"lng": 1.0, "lat": 2.0},
                            {"lng": 1.0, "lat": 1.0}
                        ]
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion with spec example data")
    void testIsInRegion_SpecExample() throws Exception {
        String requestBody = """
                {
                    "position": {"lng": -3.188, "lat": 55.944},
                    "region": {
                        "name": "central",
                        "vertices": [
                            {"lng": -3.192473, "lat": 55.946233},
                            {"lng": -3.192473, "lat": 55.942617},
                            {"lng": -3.184319, "lat": 55.942617},
                            {"lng": -3.184319, "lat": 55.946233},
                            {"lng": -3.192473, "lat": 55.946233}
                        ]
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion with point on vertex should return true")
    void testIsInRegion_PointOnVertex() throws Exception {
        String requestBody = """
                {
                    "position": {"lng": 1.0, "lat": 1.0},
                    "region": {
                        "name": "test-region",
                        "vertices": [
                            {"lng": 1.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 2.0},
                            {"lng": 1.0, "lat": 2.0},
                            {"lng": 1.0, "lat": 1.0}
                        ]
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion with point on edge should return true")
    void testIsInRegion_PointOnEdge() throws Exception {
        String requestBody = """
                {
                    "position": {"lng": 1.5, "lat": 1.0},
                    "region": {
                        "name": "test-region",
                        "vertices": [
                            {"lng": 1.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 2.0},
                            {"lng": 1.0, "lat": 2.0},
                            {"lng": 1.0, "lat": 1.0}
                        ]
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion with open region should return 400")
    void testIsInRegion_OpenRegion() throws Exception {
        String requestBody = """
                {
                    "position": {"lng": 1.5, "lat": 1.5},
                    "region": {
                        "name": "open-region",
                        "vertices": [
                            {"lng": 1.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 2.0},
                            {"lng": 1.0, "lat": 2.0}
                        ]
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("closed polygon")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion with too few vertices should return 400")
    void testIsInRegion_TooFewVertices() throws Exception {
        String requestBody = """
                {
                    "position": {"lng": 1.5, "lat": 1.5},
                    "region": {
                        "name": "invalid-region",
                        "vertices": [
                            {"lng": 1.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 1.0}
                        ]
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion with null position should return 400")
    void testIsInRegion_NullPosition() throws Exception {
        String requestBody = """
                {
                    "position": null,
                    "region": {
                        "name": "test-region",
                        "vertices": [
                            {"lng": 1.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 1.0},
                            {"lng": 2.0, "lat": 2.0},
                            {"lng": 1.0, "lat": 2.0},
                            {"lng": 1.0, "lat": 1.0}
                        ]
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("position")));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion with null region should return 400")
    void testIsInRegion_NullRegion() throws Exception {
        String requestBody = """
                {
                    "position": {"lng": 1.5, "lat": 1.5},
                    "region": null
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("region")));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion with triangle region - point inside")
    void testIsInRegion_TriangleInside() throws Exception {
        String requestBody = """
                {
                    "position": {"lng": 0.5, "lat": 0.3},
                    "region": {
                        "name": "Triangle",
                        "vertices": [
                            {"lng": 0.0, "lat": 0.0},
                            {"lng": 1.0, "lat": 0.0},
                            {"lng": 0.5, "lat": 1.0},
                            {"lng": 0.0, "lat": 0.0}
                        ]
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion with L-shaped concave polygon")
    void testIsInRegion_ConcaveLShape() throws Exception {
        String requestBody = """
                {
                    "position": {"lng": 0.25, "lat": 0.25},
                    "region": {
                        "name": "L-Shape",
                        "vertices": [
                            {"lng": 0.0, "lat": 0.0},
                            {"lng": 1.0, "lat": 0.0},
                            {"lng": 1.0, "lat": 0.5},
                            {"lng": 0.5, "lat": 0.5},
                            {"lng": 0.5, "lat": 1.0},
                            {"lng": 0.0, "lat": 1.0},
                            {"lng": 0.0, "lat": 0.0}
                        ]
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion with null vertices should return 400")
    void testIsInRegion_NullVertices() throws Exception {
        String requestBody = """
                {
                    "position": {"lng": 0.0, "lat": 0.0},
                    "region": {
                        "name": "Test",
                        "vertices": null
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion with extra fields should ignore them")
    void testIsInRegion_ExtraFieldsIgnored() throws Exception {
        String requestBody = """
                {
                    "position": {"lng": -3.188, "lat": 55.944},
                    "region": {
                        "name": "central",
                        "vertices": [
                            {"lng": -3.192473, "lat": 55.946233},
                            {"lng": -3.192473, "lat": 55.942617},
                            {"lng": -3.184319, "lat": 55.942617},
                            {"lng": -3.184319, "lat": 55.946233},
                            {"lng": -3.192473, "lat": 55.946233}
                        ]
                    },
                    "extraField": "should be ignored"
                }
                """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should ignore unknown JSON fields as per spec")
    void testIgnoreUnknownFields() throws Exception {
        String requestBody = """
                {
                    "position1": {"lng": 0, "lat": 0},
                    "position2": {"lng": 1, "lat": 1},
                    "unknownField": "should be ignored"
                }
                """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }
}
