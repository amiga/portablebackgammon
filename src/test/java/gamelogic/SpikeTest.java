package gamelogic;

import data.PlayerColor;
import data.SpikeType;
import graphics.Geometry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpikeTest {

    @Test
    @DisplayName("Spike can be constructed")
    void test1() {
        int canvasWidth = 810;
        int canvasHeight = 500;
        Geometry geometry = new Geometry(canvasWidth, canvasHeight);
        Spike spike = new Spike(geometry,2);
        assertEquals(2, spike.getPosition());
        assertEquals(1, spike.getSpikeNumber());
        assertEquals(0, spike.getAmountOfPieces(PlayerColor.WHITE));
        assertEquals(0, spike.getAmountOfPieces(PlayerColor.BLACK));
        assertEquals("1", spike.getName());
        assertEquals(SpikeType.STALECTITE, spike.getType());

        Graphics graphics = Mockito.mock(Graphics.class);
        spike.paint(graphics, null);
        int middleX = geometry.boardWidth() - 2 * geometry.spikeWidth() -
            geometry.borderWidth() + geometry.spikeWidth() / 2;
        int middleY = geometry.borderWidth() + geometry.spikeHeight() / 2;
        assertEquals(middleX, spike.getMiddlePoint().x);
        assertEquals(middleY, spike.getMiddlePoint().y);
        assertTrue(spike.userClickedOnThis(middleX, middleY));
    }
}
