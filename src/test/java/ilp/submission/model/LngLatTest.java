package ilp.submission.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is my very comprehensive Unit tests
 */
@DisplayName("LngLat Domain Model Tests")
class LngLatTest {

    private static final double EPSILON = 0.000001;

    // ==================== Constructor and Accessors ====================

    @Test
    @DisplayName("Should create LngLat with correct values")
    void testConstruction() {
        LngLat coord = new LngLat(-3.192473, 55.946233);

        assertEquals(-3.192473, coord.lng(), EPSILON);
        assertEquals(55.946233, coord.lat(), EPSILON);
    }

    @Test
    @DisplayName("Should handle extreme coordinates")
    void testExtremeCoordinates() {
        LngLat coord = new LngLat(-180.0, -90.0);
        assertEquals(-180.0, coord.lng());
        assertEquals(-90.0, coord.lat());

        LngLat coord2 = new LngLat(180.0, 90.0);
        assertEquals(180.0, coord2.lng());
        assertEquals(90.0, coord2.lat());
    }

    // ==================== Distance Calculations ====================

    @Test
    @DisplayName("Should calculate distance between different coordinates")
    void testDistanceTo_DifferentCoordinates() {
        LngLat from = new LngLat(0.0, 0.0);
        LngLat to = new LngLat(3.0, 4.0);

        double distance = from.distanceTo(to);

        assertEquals(5.0, distance, EPSILON, "Distance should follow Pythagorean theorem");
    }

    @Test
    @DisplayName("Should return zero distance for same coordinates")
    void testDistanceTo_SameCoordinates() {
        LngLat coord = new LngLat(1.5, 2.5);

        double distance = coord.distanceTo(coord);

        assertEquals(0.0, distance, EPSILON);
    }

    @ParameterizedTest
    @CsvSource({
            "-3.192473, 55.946233, -3.192473, 55.942617, 0.003616",
            "0.0, 0.0, 0.001, 0.001, 0.001414",
            "1.0, 1.0, 1.0, 1.0, 0.0"
    })
    @DisplayName("Should calculate correct distances for various coordinates")
    void testDistanceTo_ParameterizedTest(double lng1, double lat1, double lng2, double lat2, double expected) {
        LngLat from = new LngLat(lng1, lat1);
        LngLat to = new LngLat(lng2, lat2);

        double distance = from.distanceTo(to);

        assertEquals(expected, distance, 0.001);
    }

    @Test
    @DisplayName("Should throw NPE when calculating distance to null")
    void testDistanceTo_NullCoordinate() {
        LngLat coord = new LngLat(0.0, 0.0);

        assertThrows(NullPointerException.class, () -> coord.distanceTo(null));
    }

    // ==================== Proximity Checks ====================

    @Test
    @DisplayName("Should return true for coordinates within threshold")
    void testIsCloseTo_WithinThreshold() {
        LngLat from = new LngLat(0.0, 0.0);
        LngLat to = new LngLat(0.0001, 0.0001);

        assertTrue(from.isCloseTo(to), "Coordinates should be close");
    }

    @Test
    @DisplayName("Should return false for coordinates beyond threshold")
    void testIsCloseTo_BeyondThreshold() {
        LngLat from = new LngLat(0.0, 0.0);
        LngLat to = new LngLat(1.0, 1.0);

        assertFalse(from.isCloseTo(to), "Coordinates should not be close");
    }

    @Test
    @DisplayName("Should return true for identical coordinates")
    void testIsCloseTo_SameCoordinates() {
        LngLat coord = new LngLat(1.5, 2.5);

        assertTrue(coord.isCloseTo(coord));
    }

    @Test
    @DisplayName("Should return false for coordinates exactly at threshold")
    void testIsCloseTo_ExactlyAtThreshold() {
        LngLat from = new LngLat(0.0, 0.0);
        LngLat to = new LngLat(0.00015, 0.0);

        assertFalse(from.isCloseTo(to), "Distance exactly at threshold should not be close");
    }

    // ==================== Next Position ====================

    @Test
    @DisplayName("Should move east at 0 degrees")
    void testNextPosition_ZeroDegrees() {
        LngLat start = new LngLat(0.0, 0.0);

        LngLat next = start.nextPosition(0);

        assertEquals(0.00015, next.lng(), EPSILON);
        assertEquals(0.0, next.lat(), EPSILON);
    }

    @Test
    @DisplayName("Should move north at 90 degrees")
    void testNextPosition_NinetyDegrees() {
        LngLat start = new LngLat(0.0, 0.0);

        LngLat next = start.nextPosition(90);

        assertEquals(0.0, next.lng(), EPSILON);
        assertEquals(0.00015, next.lat(), EPSILON);
    }

    @Test
    @DisplayName("Should move west at 180 degrees")
    void testNextPosition_OneEightyDegrees() {
        LngLat start = new LngLat(0.0, 0.0);

        LngLat next = start.nextPosition(180);

        assertEquals(-0.00015, next.lng(), EPSILON);
        assertEquals(0.0, next.lat(), EPSILON);
    }

    @Test
    @DisplayName("Should move northeast at 45 degrees")
    void testNextPosition_FortyFiveDegrees() {
        LngLat start = new LngLat(0.0, 0.0);

        LngLat next = start.nextPosition(45);

        double expected = 0.00015 / Math.sqrt(2);
        assertEquals(expected, next.lng(), EPSILON);
        assertEquals(expected, next.lat(), EPSILON);
    }

    @Test
    @DisplayName("Should work from non-origin coordinates")
    void testNextPosition_NonOrigin() {
        LngLat start = new LngLat(-3.192473, 55.946233);

        LngLat next = start.nextPosition(45);

        assertTrue(next.lng() > start.lng());
        assertTrue(next.lat() > start.lat());
    }

    // ==================== Equals and HashCode ====================

    @Test
    @DisplayName("Should be equal to itself")
    void testEquals_SameInstance() {
        LngLat coord = new LngLat(1.0, 2.0);

        assertEquals(coord, coord);
    }

    @Test
    @DisplayName("Should be equal to coordinate with same values")
    void testEquals_SameValues() {
        LngLat coord1 = new LngLat(1.0, 2.0);
        LngLat coord2 = new LngLat(1.0, 2.0);

        assertEquals(coord1, coord2);
        assertEquals(coord1.hashCode(), coord2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal to coordinate with different values")
    void testEquals_DifferentValues() {
        LngLat coord1 = new LngLat(1.0, 2.0);
        LngLat coord2 = new LngLat(1.0, 2.1);

        assertNotEquals(coord1, coord2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void testEquals_Null() {
        LngLat coord = new LngLat(1.0, 2.0);

        assertNotEquals(null, coord);
    }

    // ==================== Approximate Equals ====================

    @Test
    @DisplayName("Should approximately equal within epsilon")
    void testApproximatelyEquals_WithinEpsilon() {
        LngLat coord1 = new LngLat(1.0, 2.0);
        LngLat coord2 = new LngLat(1.0000001, 2.0000001);

        assertTrue(coord1.approximatelyEquals(coord2, 0.00001));
    }

    @Test
    @DisplayName("Should not approximately equal beyond epsilon")
    void testApproximatelyEquals_BeyondEpsilon() {
        LngLat coord1 = new LngLat(1.0, 2.0);
        LngLat coord2 = new LngLat(1.01, 2.01);

        assertFalse(coord1.approximatelyEquals(coord2, 0.001));
    }

    // ==================== toString ====================

    @Test
    @DisplayName("Should format toString correctly")
    void testToString() {
        LngLat coord = new LngLat(-3.192473, 55.946233);

        String result = coord.toString();

        assertTrue(result.contains("-3.192473"));
        assertTrue(result.contains("55.946233"));
    }
}
