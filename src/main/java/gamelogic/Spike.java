package gamelogic;
import java.awt.*;

import data.DieType;
import data.PlayerColor;
import data.SpikeType;
import graphics.Geometry;
import lowlevel.*;

import java.util.Enumeration;
import java.util.Vector;

import static data.DieType.*;
import static data.SpikeType.STALECMITE;
import static data.SpikeType.STALECTITE;

/**
 * Board triangle that can store up to 15 checkers.
 */
public class Spike {

    public static int BLACK_SPIKE_COLOUR = 0x993802;
    public static int WHITE_SPIKE_COLOUR = 0xffcc7e;
    private Color black_spike_colour, white_spike_colour;
    private static final Color flashColor = new Color(255,225,0);

    Vector pieces = new Vector();
    private int position; // 1 to 24
    private final SpikeType type;
    private final String spikeName;
    private Geometry geometry;
    private Utils utils = new Utils();

    // these variables (along with TRIANGLE_WIDTH & TRIANGLE_HEIGHT) are used to work out if the player has clicked on the piece
    private int collision_x;
    private int collision_y;

    private static final int NOT_A_REAL_SPIKE_MINUS_99 = -99;
    private Die storedDie;
    private DieType whichDiei; // the die combination that brings piece at mouse to given spike
    private boolean flash = false;

    private int x1 = 0, y1 = 0; // left most point
    private int x2 = 0, y2 = 0; // middle point, can go up or down depending on type
    private int x3 = 0, y3 = 0; // right most point

    Spike(Geometry geometry, int position) {
        this.geometry = geometry;
        this.position = position;
        assert position != 0;
        if (position < 0) {
            log("Container spike is made");
            this.position = NOT_A_REAL_SPIKE_MINUS_99;
            this.spikeName = "Container";
            this.type = SpikeType.CONTAINER;
        } else if (position > 24) {
            log("Bar spike is make");
            this.position = NOT_A_REAL_SPIKE_MINUS_99;
            this.spikeName = "Bar";
            this.type = SpikeType.BAR;
        } else {
            spikeName = Integer.toString(position - 1);
            this.type = position <= 12 ? STALECTITE : STALECMITE;
        }
        log("Spike made "+ position);
        makeColourObjects();
    }

    String getName() {
        return spikeName;
    }

    @Override
    public String toString() {
        return spikeName;
    }

    @Override
    public boolean equals(Object that) {
        if (! (that instanceof Spike)) {
            return false;
        }
        Spike thatSpike = (Spike)that;
        return position == thatSpike.getPosition();
    }

    boolean isEmpty() {
        return pieces.isEmpty();
    }

    boolean userClickedOnThis(int mouseX, int mouseY) {
        return (mouseX >= collision_x && mouseX <= collision_x + width()) &&
            (mouseY >= collision_y && mouseY <= collision_y + height());
    }

    // add a piece to this spike
    boolean addPiece(Piece p) {
        if (spikeName != null) {
            log("Spike "+ getSpikeNumber()+ " just has a piece added.");
        }
        pieces.add(p);
        return true;
    }

    // remove this piece from the spike, pass spike in to remove, or pass null and the
    // first one will be removed.
    boolean removePiece(Piece p) {
        if (spikeName != null) {
            log("Spike "+getSpikeNumber()+" just has a piece removed.");
        } else {
            Utils._E("removePiece:spikeName is null");
        }
        pieces.remove(p);
        return true;
    }

    int getAmountOfPieces(PlayerColor playerColor) {
        if (pieces.size() == 0 || ((Piece)pieces.get(0)).getColour() != playerColor)
            return 0;
        return pieces.size();
    }

    void paint(Graphics g, Board board) {
        workOutPositionsOfSpike(geometry.boardHeight() - 2 * geometry.borderWidth(),
            geometry.spikeWidth());
        drawSpike(g);
        drawPieces(g, spikeName);
        if (flash) {
            drawPotentialDieMoves(g, board);
        }
        flash = false;
    }

    int getSpikeNumber() {
        return position - 1;
    }

    int getPosition() {
        return position;
    }

    SpikeType getType() {
        return type;
    }

    // makes a colour that flashes, this gets called from board
    // when the spike needs to indicate its a potential move to the player.
    // whichDice tells us which die would be causing this move, so we can show player.
    void flash(DieType whichDice) {
        flash = true;
        whichDiei = whichDice;
    }

    void makeColourObjects() {
        black_spike_colour = new Color(BLACK_SPIKE_COLOUR);
        white_spike_colour = new Color(WHITE_SPIKE_COLOUR);
    }

    /*
     * when a piece is on the bar,and we are checking which spikes it can go to, and this spike is a valid option
     * for ease we store the die which would get it there in this spike, and grab it later, this is the only instance in which this
     * method is used.
     */
    void store_this_die(Die die) {
        storedDie = die;
    }

    Die get_stored_die() {
        return storedDie;
    }

    boolean isContainer() {
        return type == SpikeType.CONTAINER;
    }

    boolean isBar() {
        return type == SpikeType.BAR;
    }

    Point getMiddlePoint() {
        int dy = type == STALECTITE ? -height() / 2 : height() / 2;
        return new Point(x2, y2 + dy);
    }

    private void drawPieces(Graphics g, String spikeName) {
        Enumeration e = pieces.elements();
        int yPosForPieces = y1 - geometry.pieceDiameter();
        if(getType() == STALECMITE) {
           yPosForPieces = y1;
        }

        int overlapOnPieces = 0;
        if (pieces.size() > 5) {
            overlapOnPieces = geometry.pieceDiameter() / 3;
        }
        if (pieces.size() > 7) {
            overlapOnPieces = geometry.pieceDiameter() / 2;
        }
        if (pieces.size() > 9) {
            overlapOnPieces = geometry.pieceDiameter() / 2 + pieces.size() / 3;
        }
        int piecex = x2 - geometry.pieceDiameter() / 2;
        while (e.hasMoreElements()) {
            Piece p = (Piece) e.nextElement();
            if (!p.stickToMouse()) {
                int piecey = -1;
                if (getType() == STALECTITE) {
                    piecey = yPosForPieces += (geometry.pieceDiameter() - overlapOnPieces);
                } else if (getType() == STALECMITE) {
                    piecey = yPosForPieces -= (geometry.pieceDiameter() - overlapOnPieces);
                }
                if (getType() == STALECTITE) {
                    p.paint(g, piecex, piecey + overlapOnPieces);
                } else {
                    p.paint(g, piecex, piecey - overlapOnPieces);
                }
            }
        }
    }

    private void drawSpike(Graphics g) {
        if (isContainer()) {
            assert false;
            return;
        }
        if (paintBlackColour(g)) {
            utils.setColor(g, black_spike_colour);
        } else {
            utils.setColor(g, white_spike_colour);
        }
        if (flash) {
            utils.setColor(g, flashColor);
        }
        utils.fillTriangle(g, x1, y1, x2, y2, x3, y3);

        //draw outline after otherwise it gets distored by the filled shape
        utils.setColor(g, Color.BLACK);
        utils.drawTriangle(g, x1, y1, x2, y2, x3, y3);

        if (CustomCanvas.showBoundaryBoxes) {
            utils.setColor(g, Color.RED);
            utils.drawRect(g, collision_x, collision_y, width(), height());
        }
    }

    private void drawPotentialDieMoves(Graphics g, Board board) {
        final int miniDieX = x2 - geometry.miniDieSize() / 2;
        final int miniDieHeight = geometry.miniDieSize();
        if (whichDiei == DIE1) {
            if (getType() == STALECMITE) {
                board.die1.drawMiniDie(g, miniDieX, y1 - miniDieHeight);
            } else {
                board.die1.drawMiniDie(g, miniDieX, y1);
            }
        } else if (whichDiei == DIE2) {
            if (getType() == STALECMITE) {
                board.die2.drawMiniDie(g, miniDieX, y1 - miniDieHeight);
            } else {
                board.die2.drawMiniDie(g, miniDieX, y1);
            }
        } else if (whichDiei == DIE1AND2) {
            if (getType() == STALECMITE) {
                board.die1.drawMiniDie(g, miniDieX, y1 - miniDieHeight * 2);
                board.die2.drawMiniDie(g, miniDieX, y1 - miniDieHeight);
            } else {
                board.die1.drawMiniDie(g, miniDieX, y1);
                board.die2.drawMiniDie(g, miniDieX, y1 + miniDieHeight);
            }
        }
    }

    // sets the colour based on odd and even to alternative spike colours
    private boolean paintBlackColour(Graphics g) {
        if (getSpikeNumber() % 2 == 0) {
            g.setColor(Color.BLACK);
            return true;
        } else {
            g.setColor(Color.WHITE);
            return false;
        }
    }

    // this calculates the 3 points for this spike, each with x,y value
    // and boundaries for mouse click event
    private void workOutPositionsOfSpike(int boardHeight, int TRIANGLE_WIDTH) {
        int widthMinusBorderAndPieceComponent = geometry.boardWidth() - geometry.borderWidth();
        int total = geometry.spikeWidth() * 12 + geometry.centralBarWidth() + 2 * geometry.borderWidth();
        assert total == geometry.boardWidth();
        y1 = geometry.borderWidth();
        if (position <= 6) {
            //TOP RIGHT SEGMENT OF BOARD (6 spikes_
            x1 = widthMinusBorderAndPieceComponent - TRIANGLE_WIDTH * position;
        } else if (position <= 12) {
            // TOP LEFT
            x1 = widthMinusBorderAndPieceComponent - TRIANGLE_WIDTH * position - geometry.centralBarWidth();
        } else {
            // BOTTOM
            x1 = widthMinusBorderAndPieceComponent - (TRIANGLE_WIDTH * (25 - position));
            y1 = geometry.borderWidth() + boardHeight;
            if (position <= 18) {
                // BOTTOM LEFT
                 x1 -= geometry.centralBarWidth();
            }
        }

        x3 = x1 + TRIANGLE_WIDTH;
        y3 = y1;
        collision_x = x1;
        if (type == STALECTITE) {
            x2 = x1 + TRIANGLE_WIDTH / 2;
            y2 = y1 + height();
            collision_y = y1;
        } else {
            x2 = x1 + TRIANGLE_WIDTH / 2;
            y2 = y1 - height();
            collision_y = y1 - height();
        }
    }

    private void log(String s) {
        Utils.log("Spike{}:" + s);
    }

    private int height() {
        return geometry.spikeHeight();
    }

    private int width() {
        return geometry.spikeWidth();
    }
}
