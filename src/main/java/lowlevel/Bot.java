package lowlevel;
import data.PlayerColor;
import gamelogic.Board;
import gamelogic.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;


/** A robotic player who can move mouse etc, for demo and test automation and cpu player
 *
 */
public class Bot extends Thread {

    private Robot robot;
    private CustomCanvas canvas;
    private JFrame mainWindow;

    static  long DELAY_BETWEEN_CLICKS_MILLIS = 1000;
    static  long ROBOT_DELAY_AFTER_CLICKS = 100;
    static boolean dead = true;
    public static int x = 0;
    public static int y = 0;
    public static int destX;
    public static int destY;

    private static boolean JUMP_DIRECT_TO_DEST = false;
    private boolean READY2CLICK = true;
    private static boolean STOPCLICKING = false;
    private long clickedTime;
    private int lastX, lastY;
    private int sameDestCounter;

    private static boolean FULL_AUTO_PLAY = false; // Plays everything via the bot so you can watch it all
    private static boolean TAKES_OVER_MOUSE = false;
    private boolean isRunning = true;

    public Bot(CustomCanvas canvas, JFrame mainWindow) {
        log("Bot born.");
        this.canvas = canvas;
        this.mainWindow = mainWindow;
        try {
            robot = new Robot();
        }
        catch(Exception e) {
            Utils._E("ERROR making robot " + e.getMessage());
        }
    }

    public static boolean getFullAutoPlay() {
        return FULL_AUTO_PLAY;
    }

    static void setFullAutoPlay(boolean value) {
        FULL_AUTO_PLAY = value;
    }

    private void tick() throws Exception {
        if (dead || canvas.gameComplete()) {
            try {
                Thread.sleep(50);
                return;
            } catch (InterruptedException ie) {
                log("Interrupted while sleeping");
                Thread.currentThread().interrupt();
            }
        }

        if (x == destX && y == destY && x != 0 && y != 0) {
            long difference = System.currentTimeMillis() - clickedTime;
            if (difference > DELAY_BETWEEN_CLICKS_MILLIS && !STOPCLICKING) {
                READY2CLICK = true;
                log("DEST REACHED. destX:" + destX + " destY:" + destY);
            }

            if (READY2CLICK) {
                if (lastX == destX && lastY == destY) {
                    sameDestCounter++;
                    if(sameDestCounter > 3) {
                        log("SAME DEST FIXER~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                        canvas.mouseClickedX(destX, destY, CustomCanvas.RIGHT_MOUSE_BUTTON);
                        clickedTime = System.currentTimeMillis();
                        READY2CLICK = false;
                    }
                } else {
                    sameDestCounter = 0;
                }
                lastX = destX;
                lastY = destY;
                if (TAKES_OVER_MOUSE) {
                    robot.delay(100);
                    clickedTime = System.currentTimeMillis();
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                    robot.delay(100);
                } else {
                    try {
                        Thread.sleep(25);
                    } catch(Exception e) {
                        Utils._E("insomnia!");
                    }
                    canvas.mouseClickedX(destX, destY, CustomCanvas.LEFT_MOUSE_BUTTON);
                    clickedTime = System.currentTimeMillis();
                    READY2CLICK = false;
                }
            }
        }
        Point windowPosition = mainWindow.getLocationOnScreen();
        windowPosition.setLocation(windowPosition.x, windowPosition.y + 20);
        if (JUMP_DIRECT_TO_DEST) {
            x = destX;
            y = destY;
            if (TAKES_OVER_MOUSE) {
                robot.mouseMove(x + windowPosition.x, y + windowPosition.y);
            }
        } else {
            try {
                Thread.sleep(1);
            }
            catch(Exception e) {
                Utils._E("insomnia!");
            }
            if (x < destX) {
                x++;
                if (TAKES_OVER_MOUSE) {
                    robot.mouseMove(x + windowPosition.x, y + windowPosition.y);
                }
            }
            if (x > destX) {
                x--;
                if (TAKES_OVER_MOUSE) {
                    robot.mouseMove(x + windowPosition.x, y + windowPosition.y);
                }
            } if (y < destY) {
                y++;
                if (TAKES_OVER_MOUSE) {
                    robot.mouseMove(x + windowPosition.x, y + windowPosition.y);
                }
            }
            if (y > destY) {
                y--;
                if (TAKES_OVER_MOUSE) {
                    robot.mouseMove(x + windowPosition.x, y + windowPosition.y);
                }
            }
        }
    }

    @Override
    public void run() {
        while(isRunning) {
            try {
                tick();
            } catch (Exception exception) {
                Utils._E("Bot Exception: " + exception);
            }
        }
    }

    private void log(String s) {
        Utils.log("Bot{}:" + s);
    }
}
