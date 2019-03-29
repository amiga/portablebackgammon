package gamelogic;
import java.awt.Color;
import lowlevel.*;
import java.awt.Graphics;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Board triangle that can store up to 15 checkers.
 */
public class Spike {

    public static int BLACK_SPIKE_COLOUR = 0x993802;
    public static int WHITE_SPIKE_COLOUR = 0xffcc7e;
    private static Color black_spike_colour, white_spike_colour;

    // -- pieces within the spike
    public  Vector pieces = new Vector();
    private int position; // 1 to 24
    private Utils utils = new Utils();

    // -- pre-calc'd and tmp variables
    static int TRIANGLE_WIDTH=0;
    static int TRIANGLE_HEIGHT=0;
    private static int TRIANGLE_HEIGHT_MINUS_VAL=0; // this is the value taken off the height of a triangle to stop them touching
    String spikeName;

    static final int STALECTITE = 1; //spike going down
    static final int STALECMITE = 2; //spike going up

    // these variables (along with TRIANGLE_WIDTH & TRIANGLE_HEIGHT) are used to work out if the player has clicked on the piece
    int collision_x;
    int collision_y;

    public static final int NOT_A_REAL_SPIKE_MINUS_99=-99;
    public static final String NOT_A_REAL_SPIKE_MINUS_99_STR="-99";
    private Die storedDie;
    int whichDiei = -1;
    private boolean flash=false;

    // these are used for drawing the spike (3 points, each with x,y)
    int x1=0, y1=0;
    int x2=0, y2=0;
    int x3=0, y3=0;

    Spike(int position) {
        this.position = position;
        log("Spike made "+ position);
        if (position == NOT_A_REAL_SPIKE_MINUS_99) {
            //SPECIAL CASE. when player gets to the point where they can place pieces in the piece container then the model breaks down
            // ie: current model: it picks from a pair of spikes (spike one is the source spike and spike 2 is the destination spike)
            //but when spike 2 (the destination spike) is no longer a spike (ie the destination is infact the piece container and
            //not a regular spike), we need a special case, this is not a Spike but a Spike Piece Container HYBRID if you will
            log("Special spike made, this isnt a spike at all, its a piece container");
            spikeName = NOT_A_REAL_SPIKE_MINUS_99_STR;
        } else {
            spikeName = Integer.toString(position - 1);
        }
        makeColourObjects(false); //does no work if theyre done
    }

    // add a piece to this spike
    public boolean addPiece(Piece p) {
        if (spikeName != null) {
            log("Spike "+ getSpikeNumber()+ " just has a piece added.");
        }
        pieces.add(p);
        return true;
    }

    // remove this piece from the spike, pass spike in to remove, or pass null and the
    // first one will be removed.
    public boolean removePiece(Piece p) {
        if (spikeName != null) {
            log("Spike "+getSpikeNumber()+" just has a piece removed.");
        } else {
            Utils._E("removePiece:spikeName is null");
        }
        pieces.remove(p);
        return true;
    }

    public int getAmountOfPieces(int playerColor) {
        if (pieces.size() == 0 || ((Piece)pieces.get(0)).colour != playerColor)
            return 0;
        return pieces.size();
    }

    public void paint(Graphics g, int WIDTH, int HEIGHT) {
        //compute the vals we need
        TRIANGLE_WIDTH            = (WIDTH-((Board.BORDER*2)+Board.BAR+Board.PIECE_CONTAINER*2) ) /12;
        TRIANGLE_HEIGHT           = ((HEIGHT-(Board.BORDER*2))/2);
        TRIANGLE_HEIGHT_MINUS_VAL = TRIANGLE_HEIGHT/10;

        drawSpike(g, spikeName);
        drawPieces(g,spikeName);
        drawPotentialDieMoves(g);

        if (CustomCanvas.pieceStuckToMouse != null) {
            //draw piece on mouse again ontop
            CustomCanvas.pieceStuckToMouse.drawPieceOnMouse(g, x1, y1);
        }
    }

    public int getSpikeNumber() {
        return position - 1;
    }

    public int getPosition() {
        return position;
    }

    private void drawPieces(Graphics g, String spikeName) {
        //draw its pieces
        Enumeration e = pieces.elements();
        int yPosForPieces=y1-Piece.PIECE_DIAMETER;

        //adjust the starting point of the pieces pending on type of spike:
        /////////////////////////////////////////////
        if(getType()==STALECTITE) {
           yPosForPieces=y1-Piece.PIECE_DIAMETER;
        } else if(getType()==STALECMITE) {
           yPosForPieces=y1; // -Piece.PIECE_DIAMETER;
        } else {
            Utils._E(spikeName+">>>Cannot work out the Y value for a piece since the spike claims to have no type!");
        }
        ///////////////////////////////////////////////

        int overlapOnPieces = 0;
        while (e.hasMoreElements()) {
            Piece p = (Piece) e.nextElement();
            int piecex=x2-(Piece.PIECE_DIAMETER/2);
            int piecey=-1;

            //caters for overlappin pieces when manny are added.
            if (pieces.size() <= 5){
                overlapOnPieces=0;//Piece.PIECE_DIAMETER;
            } 
            if (pieces.size() > 5) {
                overlapOnPieces=Piece.PIECE_DIAMETER / 3;
            } 
            if (pieces.size() > 7) {
                overlapOnPieces=Piece.PIECE_DIAMETER / 2;
            } 
            if (pieces.size() > 9) {
                overlapOnPieces=(Piece.PIECE_DIAMETER / 2)+pieces.size()/3;
            }
            // we need a different y value for top and bottom spikes
            // so that on top spikes the pieces move down
            // and on bottom spikes the pieces move up
            if(getType()==STALECTITE) {
               piecey=yPosForPieces+=(Piece.PIECE_DIAMETER-overlapOnPieces);
            } else if(getType()==STALECMITE) {
                piecey=yPosForPieces-=(Piece.PIECE_DIAMETER-overlapOnPieces);
            } else {
                Utils._E(spikeName+"---Cannot work out the Y value for a piece since the spike claims to have no type!");
            }
            if(getType()==STALECTITE) {   //overlap here just squares them up to the bottom/top of spike if there overlapping
                 p.paint(g,piecex,piecey+overlapOnPieces);
            } else {
                 p.paint(g,piecex,piecey-overlapOnPieces);
            }
        }
    }

    //makes a colour that flashes, this gets called from board
    //when the spike needs to indicate its a potential move to the player.
    //whichDice tells us which die would be causing this move, so we can show player.
    void flash(int whichDice) {
        flash = true;
        whichDiei = whichDice;
    }

    // draw this spike
    private void drawSpike(Graphics g, String spikeName) {
        if (position == -NOT_A_REAL_SPIKE_MINUS_99) {
            return;
        }
        Color flashColor = new Color(255,225,0);
        workOutPositionsOfSpike(); // this will work out each point of the triangle.

        // draw a black outline of triangle then paint it
        // the correct colour
        if (paintBlackColour(g)) {
            utils.setColor(g, black_spike_colour);
        } else {
            utils.setColor(g, white_spike_colour);
        }
        if (flash) {
            //so it indicates when its a potential move for player
            utils.setColor(g, flashColor);
            flash = false;
        }
        utils.fillTriangle(g, x1, y1, x2, y2, x3, y3);
        //draw outline after otherwise it gets distored by the filled shape
        utils.setColor(g, Color.BLACK);
        utils.drawTriangle(g, x1, y1, x2, y2, x3, y3);

        if (CustomCanvas.showBoundaryBoxes) {
            utils.setColor(g,Color.RED);
            utils.drawRect(g,collision_x, collision_y,TRIANGLE_WIDTH, TRIANGLE_HEIGHT);
        }
    }

    private void drawPotentialDieMoves(Graphics g) {
        if (whichDiei == Board.DIE1) {
            if (getType() == STALECMITE) {
                Board.die1.drawMiniDie(g, x2 - Board.die1.MINI_DIE_WIDTH / 2, y1 - Board.die1.MINI_DIE_HEIGHT);
            } else {
                Board.die1.drawMiniDie(g, x2 - Board.die1.MINI_DIE_WIDTH / 2, y1);
            }

        } else if (whichDiei == Board.DIE2) {
            if (getType() == STALECMITE) {
                Board.die2.drawMiniDie(g, x2 - Board.die2.MINI_DIE_WIDTH / 2, y1 - Board.die2.MINI_DIE_HEIGHT);
            } else {
                Board.die2.drawMiniDie(g, x2 - Board.die2.MINI_DIE_WIDTH / 2, y1);
            }
        } else if (whichDiei == Board.DIE1AND2) {
            if (getType() == STALECMITE) {
                Board.die1.drawMiniDie(g, x2 - Board.die1.MINI_DIE_WIDTH / 2, y1 - Board.die1.MINI_DIE_HEIGHT * 2);
                Board.die2.drawMiniDie(g, x2 - Board.die2.MINI_DIE_WIDTH / 2, y1 - Board.die2.MINI_DIE_HEIGHT);
            } else {
                Board.die1.drawMiniDie(g, x2 - Board.die1.MINI_DIE_WIDTH / 2, y1);
                Board.die2.drawMiniDie(g, x2 - Board.die2.MINI_DIE_WIDTH / 2, y1 + Board.die1.MINI_DIE_HEIGHT);
            }
        } else if (whichDiei == -1) {
            //not an error now just means dont draw s it must be -1
        } else {
            Utils._E("whichDiei type is unknown");
        }
    }

    //sets the colour based on odd and even to alternative spike colours
    private boolean paintBlackColour(Graphics g) {
        //yeh comparing strings might be a bit slow :)
        if (spikeName.equals("0")  ||  spikeName.equals("2")  ||
            spikeName.equals("4")  ||  spikeName.equals("6")  ||
            spikeName.equals("8")  ||  spikeName.equals("10") ||
            spikeName.equals("12") ||  spikeName.equals("14") ||
            spikeName.equals("16") ||  spikeName.equals("18") ||
            spikeName.equals("20") ||  spikeName.equals("22") ||
            spikeName.equals("24")
           )
        {
            g.setColor(Color.BLACK);
            return true;
        } else {
            g.setColor(Color.WHITE);
            return false;
        }
    }
    //returns either STALECTITE or stalecmite
    private int type = -1;

    int getType() {
        if (position == NOT_A_REAL_SPIKE_MINUS_99) {
            return NOT_A_REAL_SPIKE_MINUS_99;//special case
        }
        if (type == -1) {
            Utils._E(spikeName+": returned no type tite nor mite!");
        }
        return type;
    }

    // this calculates the 3 points for this spike, each with x,y value
    private void workOutPositionsOfSpike() {
        boolean stalectite = false; //indicates spikes that look like staletites (for drawing)
        boolean stalecmite = false; // as above but for stalecmites.

        int widthMinusBorderAndPieceComponent=(CustomCanvas.WIDTH-Board.BORDER-Board.PIECE_CONTAINER);

        //work out the initial x,y positions based on
        //which spike this is, baring in mind it starts at
        //1 which is in top right of board, and works anticlockwise
        // todo, optimise this into smaller code.
        switch(position) {
            //TOP RIGHT SEGMENT OF BOARD (6 spikes_
            case 1: x1=widthMinusBorderAndPieceComponent-TRIANGLE_WIDTH;
                    y1=(Board.BORDER);
                    stalectite=true;
                    break;
            case 2: x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*2);
                    y1=(Board.BORDER);
                    stalectite=true;
                    break;
            case 3: x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*3);
                    y1=(Board.BORDER);
                    stalectite=true;
                    break;
            case 4: x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*4);
                    y1=(Board.BORDER);
                    stalectite=true;
                    break;
            case 5: x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*5);
                    y1=(Board.BORDER);
                    stalectite=true;
                    break;
            case 6: x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*6);
                    y1=(Board.BORDER);
                    stalectite=true;
                    break;
            ////// TOP LEFT SEGMENT OF THE BOARD (6 spikes)
            case 7:
                    x1= widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*7)-Board.BAR;
                    y1=(Board.BORDER);
                    stalectite=true;
                    break;
            case 8:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*8)-Board.BAR;
                    y1=(Board.BORDER);
                    stalectite=true;
                    break;
            case 9:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*9)-Board.BAR;
                    y1=(Board.BORDER);
                    stalectite=true;
                    break;
            case 10:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*10)-Board.BAR;
                    y1=(Board.BORDER);
                    stalectite=true;
                    break;
            case 11:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*11)-Board.BAR;
                    y1=(Board.BORDER);
                    stalectite=true;
                    break;
            case 12:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*12)-Board.BAR;
                    y1=(Board.BORDER);
                    stalectite=true;
                    break;
            ////BOTTOM LEFT SEGMENT///////////////////////////////
            case 13:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*12)-Board.BAR;
                    y1=(Board.BORDER+TRIANGLE_HEIGHT*2);
                    stalecmite=true;
                    break;
            case 14:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*11)-Board.BAR;
                    y1=(Board.BORDER+TRIANGLE_HEIGHT*2);
                    stalecmite=true;
                    break;
             case 15:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*10)-Board.BAR;
                    y1=(Board.BORDER+TRIANGLE_HEIGHT*2);
                    stalecmite=true;
                    break;
             case 16:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*9)-Board.BAR;
                    y1=(Board.BORDER+TRIANGLE_HEIGHT*2);
                    stalecmite=true;
                    break;
             case 17:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*8)-Board.BAR;
                    y1=(Board.BORDER+TRIANGLE_HEIGHT*2);
                    stalecmite=true;
                    break;
             case 18:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*7)-Board.BAR;
                    y1=(Board.BORDER+TRIANGLE_HEIGHT*2);
                    stalecmite=true;
                    break;
             ///////////////LAST SECTION BOTTOM RIGHT
             case 19:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*6);
                    y1=(Board.BORDER+TRIANGLE_HEIGHT*2);
                    stalecmite=true;
                    break;
             case 20:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*5);
                    y1=(Board.BORDER+TRIANGLE_HEIGHT*2);
                    stalecmite=true;
                    break;
            case 21:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*4);
                    y1=(Board.BORDER+TRIANGLE_HEIGHT*2);
                    stalecmite=true;
                    break;
            case 22:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*3);
                    y1=(Board.BORDER+TRIANGLE_HEIGHT*2);
                    stalecmite=true;
                    break;

            case 23:
                    x1=widthMinusBorderAndPieceComponent-(TRIANGLE_WIDTH*2);
                    y1=(Board.BORDER+TRIANGLE_HEIGHT*2);
                    stalecmite=true;
                    break;
            case 24:
                    x1=widthMinusBorderAndPieceComponent-TRIANGLE_WIDTH;
                    y1=(Board.BORDER+TRIANGLE_HEIGHT*2);
                    stalecmite=true;
                    break;
        }

        //draw the spikes up and down
        if (stalectite) {
           x2=x1+(TRIANGLE_WIDTH/2);
           y2=y1+(TRIANGLE_HEIGHT-TRIANGLE_HEIGHT_MINUS_VAL);
           x3=x1+TRIANGLE_WIDTH;
           y3=y1;

           type=STALECTITE;
            // grab collision x,y vals
            collision_x=x1;
            collision_y=y1;
        } else if (stalecmite) {
           x2=x1+(TRIANGLE_WIDTH/2);
           y2=y1-(TRIANGLE_HEIGHT-TRIANGLE_HEIGHT_MINUS_VAL);
           x3=x1+TRIANGLE_WIDTH;
           y3=y1;

           type=STALECMITE;
            // grab collision x,y vals
            collision_x=x1;
            collision_y=y1-TRIANGLE_HEIGHT;
        }
    }

    //wrapper around system outs
    private void log(String s) {
        Utils.log("Spike{}:" + s);
    }

    public static void makeColourObjects(boolean forceRecreation) {
        if (black_spike_colour==null || forceRecreation) {
            black_spike_colour = new Color(BLACK_SPIKE_COLOUR);
        }
        if (white_spike_colour==null || forceRecreation) {
            white_spike_colour=new Color(WHITE_SPIKE_COLOUR);
        }
    }

    public boolean userClickedOnThis(int mouseX, int mouseY) {
        return (mouseX >= collision_x && mouseX <= collision_x + TRIANGLE_WIDTH) &&
            (mouseY >= collision_y && mouseY <= collision_y + TRIANGLE_HEIGHT);
    }

    /*
     * when a piece is on the bar,and we are checking which spikes it can go to, and this spike is a valid option
     * for ease we store the die which would get it there in this spike, and grab it later, this is the only instance in which this
     * method is used.
     */
    public void store_this_die(Die die) {
        storedDie=die;
    }

    public Die get_stored_die() {
        return storedDie;
    }
}
