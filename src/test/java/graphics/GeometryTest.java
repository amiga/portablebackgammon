package graphics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeometryTest {

    @Test
    @DisplayName("Geometry default values")
    void test1() {
        Geometry geometry = new Geometry(810, 478);
        assertEquals(640, geometry.boardWidth());
        assertEquals(478, geometry.boardHeight());
        assertEquals(5, geometry.tinyGap());
        assertEquals(5, geometry.panelSizeFraction());
        assertEquals(160, geometry.panelWidth());
        assertEquals(geometry.getCanvasWidth(),
            geometry.boardWidth() + geometry.panelWidth() + geometry.borderWidth());
        assertEquals(10, geometry.borderWidth());
        assertEquals(20, geometry.centralBarWidth());
        assertEquals(50, geometry.spikeWidth());
        assertEquals(207, geometry.spikeHeight());
        assertEquals(32, geometry.pieceDiameter());
        assertEquals(32, geometry.dieSize());
        assertEquals(6, geometry.dieDotDiameter());
        assertEquals(26, geometry.miniDieSize());
        assertEquals(0,
            (geometry.boardWidth() - 2 * geometry.borderWidth() - geometry.centralBarWidth()) % 12);

        assertEquals(geometry.boardWidth(),
            2*geometry.borderWidth() + geometry.centralBarWidth() + 12 * geometry.spikeWidth());
        assertEquals(654, geometry.containerX());
    }
}
