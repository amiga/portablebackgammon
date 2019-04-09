package gamelogic;

import data.GuiState;
import data.PlayerColor;
import graphics.GameColour;
import graphics.Geometry;
import lowlevel.CustomCanvas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class TestableCanvas extends CustomCanvas {

    TestableCanvas(JFrame frame, Geometry geometry, Board board, GameConfig config) {
        super(frame, new GameColour(), geometry, board, config);
    }

    @Override
    public int getWidth() {
        return 810;
    }

    @Override
    public int getHeight() {
        return 500;
    }
}

class CustomCanvasTest {

    static Field makeCanvasFieldPublic(String fieldName) throws Exception {
        Field field = CustomCanvas.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    @Test
    @DisplayName("Transit to start game state")
    void test1() throws Exception {
        GameConfig config = Mockito.mock(GameConfig.class);
        Mockito.when(config.maxSplashCounter()).thenReturn(50);

        JFrame frame = new JFrame();
        Geometry geometry = new Geometry(810, 510);
        Board board = new Board(new GameColour(), geometry, config);
        CustomCanvas canvas = new TestableCanvas(frame, geometry, board, config);

        assertEquals(GuiState.SPLASH_SCREEN, canvas.getState());
        Graphics graphics = Mockito.mock(Graphics.class);
        for (int i = 0; i <= config.maxSplashCounter(); ++i) {
            canvas.paint(graphics);
        }
        assertEquals(GuiState.OPTIONS_SCREEN_LOCAL_OR_NETWORK, canvas.getState());
        canvas.paint(graphics);

        Field buttonxA = makeCanvasFieldPublic("buttonxA");
        Field buttonyA = makeCanvasFieldPublic("buttonyA");
        Field buttonwA = makeCanvasFieldPublic("buttonwA");
        Field buttonhA = makeCanvasFieldPublic("buttonhA");

        int buttonX = (int)buttonxA.get(canvas) + (int)buttonwA.get(canvas) / 2;
        int buttonY = (int)buttonyA.get(canvas) + (int)buttonhA.get(canvas) / 2;
        canvas.mouseClicked(new MouseEvent(canvas, 0, System.nanoTime(), 0, buttonX, buttonY, 1, false));
        assertEquals(GuiState.OPTIONS_SCREEN_LOCAL_COMPUTER_OR_HUMAN, canvas.getState());
        canvas.paint(graphics);

        canvas.mouseClicked(new MouseEvent(canvas, 0, System.nanoTime(), 0, buttonX, buttonY, 1, false));
        assertEquals(GuiState.GAME_IN_PROGRESS, canvas.getState());
        canvas.paint(graphics);
    }

    @Test
    @DisplayName("All at home, double, no valid options")
    void test2() throws Exception {
        GameConfig config = Mockito.mock(GameConfig.class);
        Mockito.when(config.maxSplashCounter()).thenReturn(50);

        JFrame frame = new JFrame();
        Geometry geometry = new Geometry(0, 0);
        Board board = new TestableBoard(new GameColour(), geometry, config, 1);
        CustomCanvas canvas = new TestableCanvas(frame, geometry, board, config);

        assertEquals(GuiState.SPLASH_SCREEN, canvas.getState());
        Graphics graphics = Mockito.mock(Graphics.class);
        for (int i = 0; i <= config.maxSplashCounter(); ++i) {
            canvas.paint(graphics);
        }
        assertEquals(GuiState.OPTIONS_SCREEN_LOCAL_OR_NETWORK, canvas.getState());
        canvas.paint(graphics);
        Field buttonxA = makeCanvasFieldPublic("buttonxA");
        Field buttonyA = makeCanvasFieldPublic("buttonyA");
        Field buttonwA = makeCanvasFieldPublic("buttonwA");
        Field buttonhA = makeCanvasFieldPublic("buttonhA");

        int buttonX = (int)buttonxA.get(canvas) + (int)buttonwA.get(canvas) / 2;
        int buttonY = (int)buttonyA.get(canvas) + (int)buttonhA.get(canvas) / 2;
        MouseEvent clickComputerPlay = new MouseEvent(canvas, 0, System.nanoTime(), 0, buttonX, buttonY, 1, false);
        canvas.mouseClicked(clickComputerPlay);
        assertEquals(GuiState.OPTIONS_SCREEN_LOCAL_COMPUTER_OR_HUMAN, canvas.getState());
        canvas.paint(graphics);

        canvas.mouseClicked(new MouseEvent(canvas, 0, System.nanoTime(), 0, buttonX, buttonY, 1, false));
        assertEquals(GuiState.GAME_IN_PROGRESS, canvas.getState());
        canvas.paint(graphics);

        int[] whiteHome = {
            0,15,0,0,0,0,
            0,0,0,0,0,0,
            0,0,0,0,0,0,
            0,0,0,0,0,0
        };

        int[] blackHome = {
            2,0,0,0,0,0,
            0,0,0,0,0,0,
            0,0,0,0,0,0,
            0,0,13,0,0,0
        };
        board.initialiseBoardForNewGame(whiteHome, blackHome);
        board.setCurrentPlayer(board.getWhitePlayer());
        assertTrue(board.showRollButton());

        Field rollButtonX = makeCanvasFieldPublic("rollButtonX");
        Field rollButtonY = makeCanvasFieldPublic("rollButtonY");
        Field rollButtonW = makeCanvasFieldPublic("rollButtonW");
        Field rollButtonH = makeCanvasFieldPublic("rollButtonH");

        buttonX = (int)rollButtonX.get(canvas) + (int)rollButtonW.get(canvas) / 2;
        buttonY = (int)rollButtonY.get(canvas) + (int)rollButtonH.get(canvas) / 2;
        MouseEvent clickRollButton = new MouseEvent(canvas, 0, System.nanoTime(), 0, buttonX, buttonY, 1, false);
        assertTrue(board.showRollButton());
        canvas.mouseClicked(clickRollButton);
        assertTrue(board.getCurrentPlayer().isWhite());

        assertTrue(board.rolledDouble());
        canvas.paint(graphics);
        canvas.paint(graphics);
        assertFalse(board.getCurrentPlayer().isWhite());
        assertTrue(board.showRollButton());
    }
}
