package lowlevel;
import java.awt.*;
import java.util.*;
import java.awt.image.*;

public class Utils {

    private static final String ERROR_STRING = "****ERROR**** ";
    public static boolean CANVAS_LOGGING = false;
    private static final Random randomizer = new Random();

    private static int LINES_THAT_FIT_VERTICALLY = 25;
    static Vector systemOuts = new Vector(0);

    private Color colour;
    private Color transparent;

    public void setColor(Graphics g, Color colour_) {
        colour = colour_;
        g.setColor(colour_);
    }

    public static void setColor(Graphics g, int c) {
        Color cl = new Color(c);
        g.setColor(cl);
    }

    public void setColor(Graphics g, int red, int green, int blue, int alpha) {
        transparent = new Color(red,green,blue,alpha);
        g.setColor(transparent);
    }

    public Color getColor() {
        return colour;

    }

    public void drawRect(Graphics g, int x, int y, int WIDTH, int HEIGHT) {
        g.drawRect(x,y, WIDTH, HEIGHT);
    }

    public void fillRect(Graphics g, int x, int y, int WIDTH, int HEIGHT) {
         g.fillRect(x, y, WIDTH, HEIGHT);
    }

    public void drawRoundRect(Graphics g, int x, int y, int WIDTH, int HEIGHT) {
        g.drawRoundRect(x,y, WIDTH, HEIGHT,10,10);
    }

    public void fillRoundRect(Graphics g, int x, int y, int WIDTH, int HEIGHT) {
        g.fillRoundRect(x,y, WIDTH, HEIGHT,10,10);
    }

    public void drawTriangle(Graphics g, int x1, int y1, int x2, int y2, int x3, int y3) {
        Polygon poly = new Polygon();
        poly.addPoint(x1, y1);
        poly.addPoint(x2, y2);
        poly.addPoint(x3, y3);
        g.drawPolygon(poly);
    }

    public void fillTriangle(Graphics g, int x1, int y1, int x2, int y2, int x3, int y3) {
        Polygon poly = new Polygon();
        poly.addPoint(x1, y1);
        poly.addPoint(x2, y2);
        poly.addPoint(x3, y3);
        g.fillPolygon(poly);
    }

    public void drawCircle(Graphics g, int x, int y,int width, int height) {
         g.drawArc(x, y,width, height, 1, 360);
    }

    public void fillCircle(Graphics g, int x, int y,int width, int height) {
          g.fillArc(x, y, width, height, 1, 360);
    }

    static Image loadImage(String path) {
        Image image = null;
        try {
            log("LOADIMAGE: Attempting to load: " + path);
            image = new javax.swing.ImageIcon(Utils.class.getResource(path)).getImage();
        } catch (Exception e) {
            _E("error loading image ("+path+") "+e.getMessage());
        }
        return image;
    }

    void backGround(Graphics g, Color c, int WIDTH, int HEIGHT) {
        setColor(g, c);
        fillRect(g,0,0, WIDTH, HEIGHT);
    }

    void drawImage(Graphics g, Image i, int x, int y, ImageObserver observer) {
        g.drawImage(i,x-(i.getWidth(observer)/2),y-(i.getHeight(observer)/2), observer);
    }

    public static void log(String s) {
        if (CANVAS_LOGGING) {
            systemOuts.add(s);
            if (systemOuts.capacity() > LINES_THAT_FIT_VERTICALLY) {
                systemOuts.remove(0);
            }
        }
        System.out.println(s);
    }

    public static void _E(String s) {
        CustomCanvas.playErrorSound();
        log(ERROR_STRING+s);
    }

    // get random val between min and max, both inclusive
    public static final int getRand(int min, int max) {
        int r = Math.abs(randomizer.nextInt());
        return (r % ((max - min + 1))) + min;
    }
}
