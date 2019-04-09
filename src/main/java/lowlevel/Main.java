/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lowlevel;
import gamelogic.Board;
import gamelogic.GameConfig;
import gamelogic.TestableBoard;
import graphics.GameColour;
import graphics.Geometry;
import org.aeonbits.owner.ConfigFactory;

import javax.swing.*;

import java.awt.Toolkit;
import java.awt.Dimension;

public class Main {

    private static final int INIT_WINDOW_WIDTH = 816;
    private static final int INIT_WINDOW_HEIGHT = 500;
    private static final int WINDOWY_MINUS = 50;
    private static final String MAIN_WINDOW_TITLE = "Backgammon";

    private static JFrame frame;
    private static CustomCanvas canvas;
    private static Board board;
    private static Bot bot;

    private static final int FRAME_DELAY_MILLIS = 50; //50;//20; // 20ms. implies 50fps (1000/20) = 50

    public static void main(String[] args) {
        log("Main called, Backgammon starting.");
        ConfigFactory.setProperty("configFileName", "backgammon.config");
        GameConfig config = ConfigFactory.create(GameConfig.class);
        frame = new JFrame();
        GameColour colours = new GameColour();
        Geometry geometry = new Geometry(0, 0);
        // board = new TestableBoard(colours, geometry, config, 1, 2);
        board = new Board(colours, geometry, config);
        canvas = new CustomCanvas(frame, colours, geometry, board, config);
        initMainWindow(frame, true);
        canvas.init();
        bot = new Bot(canvas, frame);
        bot.start();

        Thread gameThread = new Thread(() -> {
            long cycleTime = System.currentTimeMillis();
            while (true) {
                canvas.repaint();
                cycleTime = cycleTime + FRAME_DELAY_MILLIS;
                long difference = cycleTime - System.currentTimeMillis();
                try {
                    Thread.sleep(Math.max(0, difference));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        gameThread.setPriority(Thread.MIN_PRIORITY);
        gameThread.start();
        log("Backgammon visible.");
    }

    private static void initMainWindow(JFrame frame, boolean visible) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(INIT_WINDOW_WIDTH, INIT_WINDOW_HEIGHT);
        frame.setResizable(false);
        frame.setVisible(visible);
        frame.setIconImage(Utils.loadImage("/icon.gif"));
        centralise(frame);
        frame.setTitle(MAIN_WINDOW_TITLE);
    }

    static String getTitle() {
        return frame.getTitle();
     }

    static void setTitle(String title) {
        frame.setTitle(title);
     }

    private static void centralise(JFrame frame) {
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screenDim.width, (screenDim.height / 8) - WINDOWY_MINUS);
        frame.setLocation(screenDim.width / 2 - frame.getWidth() / 2,
            (screenDim.height / 2) - (frame.getHeight() / 2) - WINDOWY_MINUS);
    }

     private static void log(String s) {
         Utils.log("Main{}:" + s);
     }
}
