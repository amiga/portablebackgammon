package gamelogic;
import java.awt.Color;
import java.awt.Graphics;

import graphics.Geometry;
import lowlevel.*;

public class Die {
    public static int DIE_COLOUR = 0xFFFFFF;
    public static int DOT_COLOUR = 0x000000;
    private static Color die_colour, dot_colour;

    private Geometry geometry;
    private Utils utils = new Utils();
    private int value;
    private int usesCounter;

    Die(Geometry geometry) {
        log("Die made");
        this.geometry = geometry;
        makeColourObjects();
    }

    public static void makeColourObjects() {
        die_colour = new Color(DIE_COLOUR);
        dot_colour = new Color(DOT_COLOUR);
    }

    int roll() {
        value = Utils.getRand(1,6);
        usesCounter = 1;
        return value;
    }

    void doubleRoll() {
        this.usesCounter = 2;
    }

    void setValue(int newValue) {
        assert newValue >= 0 && newValue <= 6;
        value = newValue;
    }

    public int getValue() {
        return value;
    }

    // disables a die so that the value is zero and this no logic will work out potential moves with this die now etc
    void disable() {
        if (usesCounter <= 0) {
            throw new RuntimeException("Disabling disabled die, value: " + value);
        }
        --usesCounter;
        if (usesCounter == 0)
            value = 0;
    }

    void noOptions() {
        value = 0;
    }

    boolean enabled() {
        return value > 0;
    }

    boolean disabled() {
        return value == 0;
    }

    void paint(Graphics g, int x, int y) {
        if (enabled()) {
            int dotDiameter = geometry.dieDotDiameter();
            int halfDotDiameter = dotDiameter / 2;
            drawOutline(g, x, y, geometry.dieSize(), geometry.dieSize());
            drawDots(g, x, y, geometry.dieSize(), geometry.dieSize(), dotDiameter, halfDotDiameter);
        }
    }

    void drawMiniDie(Graphics g, int x, int y) {
        int miniDotDiameter = geometry.miniDieSize() / 5;
        int miniHalfDotDiameter = geometry.dieDotDiameter() / 2;
        drawOutline(g,x, y, geometry.miniDieSize(), geometry.miniDieSize());
        drawDots(g, x, y, geometry.miniDieSize(), geometry.miniDieSize(),
            miniDotDiameter, miniHalfDotDiameter);
    }

    private void drawOutline(Graphics g, int x, int y, int DIE_WIDTH, int DIE_HEIGHT) {
        utils.setColor(g, die_colour);
        utils.fillRoundRect(g, x, y, DIE_WIDTH, DIE_HEIGHT);
        utils.setColor(g, Color.black);
        utils.drawRoundRect(g, x, y, DIE_WIDTH, DIE_HEIGHT);
        
        if (CustomCanvas.showBoundaryBoxes) {
            utils.setColor(g,Color.RED);
            utils.drawRect(g,x, y,DIE_WIDTH, DIE_HEIGHT);
        }
    }

    private void drawDots(Graphics g, int x, int y, int DIE_WIDTH, int DIE_HEIGHT,
                          int DOT_DIAMETER, int HALF_DOT_DIAMETER) {
        utils.setColor(g, dot_colour);
        int dots = value;
        int tinyGap = geometry.tinyGap();
        switch(dots) {
            case 1:
                //draw single dot in middle
                utils.fillCircle(g, (x+DIE_WIDTH/2)-HALF_DOT_DIAMETER, (y+DIE_HEIGHT/2)-HALF_DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                break;
            case 2:
                //draw 2 dots in opposing corners
                utils.fillCircle(g, (x+DIE_WIDTH)-(DOT_DIAMETER+tinyGap), y+DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+tinyGap), y+(DIE_HEIGHT-DOT_DIAMETER-tinyGap), DOT_DIAMETER, DOT_DIAMETER);
                break;
            case 3:
                //draw 3 dots diagnally
                utils.fillCircle(g, (x+DIE_WIDTH)-(DOT_DIAMETER+tinyGap), y+DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+tinyGap), y+(DIE_HEIGHT-DOT_DIAMETER-tinyGap), DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+DIE_WIDTH/2)-HALF_DOT_DIAMETER, (y+DIE_HEIGHT/2)-HALF_DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                break;
            case 4:
                //draw 4 dots 2 at top and 2 at bottom
                utils.fillCircle(g, (x+DIE_WIDTH)-(DOT_DIAMETER+tinyGap), y+DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+tinyGap), y+DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+DIE_WIDTH)-(DOT_DIAMETER+tinyGap), y+(DIE_HEIGHT-DOT_DIAMETER-tinyGap), DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+tinyGap), y+(DIE_HEIGHT-DOT_DIAMETER-tinyGap), DOT_DIAMETER, DOT_DIAMETER);
                break;
            case 5:
                //draw 4 dots 2 at top and 2 at bottom
                utils.fillCircle(g, (x+DIE_WIDTH)-(DOT_DIAMETER+tinyGap), y+DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+tinyGap), y+DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+DIE_WIDTH)-(DOT_DIAMETER+tinyGap), y+(DIE_HEIGHT-DOT_DIAMETER-tinyGap), DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+tinyGap), y+(DIE_HEIGHT-DOT_DIAMETER-tinyGap), DOT_DIAMETER, DOT_DIAMETER);
                //and one in the middle
                utils.fillCircle(g, (x+DIE_WIDTH/2)-HALF_DOT_DIAMETER, (y+DIE_HEIGHT/2)-HALF_DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                break;
            case 6:
                //draw 4 dots 2 at top and 2 at bottom
                utils.fillCircle(g, (x+DIE_WIDTH)-(DOT_DIAMETER+tinyGap), y+DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+tinyGap), y+DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+DIE_WIDTH)-(DOT_DIAMETER+tinyGap), y+(DIE_HEIGHT-DOT_DIAMETER-tinyGap), DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+tinyGap), y+(DIE_HEIGHT-DOT_DIAMETER-tinyGap), DOT_DIAMETER, DOT_DIAMETER);
                //and 2 in middle top and bottom
                utils.fillCircle(g, (x+DIE_WIDTH/2)-HALF_DOT_DIAMETER, y+DOT_DIAMETER, DOT_DIAMETER, DOT_DIAMETER);
                utils.fillCircle(g, (x+DIE_WIDTH/2)-HALF_DOT_DIAMETER, y+(DIE_HEIGHT-DOT_DIAMETER-tinyGap), DOT_DIAMETER, DOT_DIAMETER);
                break;
            default:
                break;
        }
    }

    private void log(String s) {
        Utils.log("Die{}:" + s);
    }
}
