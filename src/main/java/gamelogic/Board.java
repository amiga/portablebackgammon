package gamelogic;
import java.awt.*;
import java.util.*;

import data.PlayerColor;
import graphics.GameColour;
import graphics.Geometry;
import lowlevel.*;

public class Board {

    private Color board_colour, bar_colour;
    private GameColour gameColour;
    private Geometry geometry;

    // game state variables
    private ArrayList<Spike> spikes;
    private Player whitePlayer, blackPlayer;
    private Player currentPlayer;
    public static Die die1, die2;

    private Utils utils = new Utils();

    public static final int DIE1 = 1; // these variables are simply for passing over to the spike when it flashes
    public static final int DIE2 = 2; // so it knows which die is carrying out its potential move to tell the player
    public static final int DIE1AND2 = 3;

    private Sound sfxNoMove;

    // Garbage
    public static boolean pickingPieceUpFromBar;
    public static Vector spikesAllowedToMoveToFromBar = new Vector(6);

    public static boolean listBotsOptions;
    public static String botOptions = "<<NONE YET>>";
    public boolean thereAreOptions = false;
    public SpikePair SPtheMoveToMake; // stores the move they will make

    //these flags indicate if the player has took their go yet for that dice
    //if the dice are combined then they are both set to false in one go.
    public static boolean die1HasBeenUsed, die2HasBeenUsed;

    public static boolean NOT_A_BOT_BUT_A_NETWORKED_PLAYER=false;
    private static final int LAST_SPIKE = 23;
    private static final int FIRST_SPIKE = 0;

    private static final int BOARD_NEW_GAME = 0;
    private static final int DEBUG_PIECES_IN_THEIR_HOME = 1;
    private static final int INIT_CONFIGURATION = BOARD_NEW_GAME;
    private static final int[] whiteInitPositions = {
        0, 0, 0, 0, 0, 5,
        0, 3, 0, 0, 0, 0,
        5, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 2
    };

    private static final int[] blackInitPositioin = new int[24];
    static {
        int blackIdx = 23;
        for (int i = 0; i < whiteInitPositions.length; ++i, --blackIdx) {
            blackInitPositioin[blackIdx] = whiteInitPositions[i];
        }
    }

    public Board(GameColour gameColour, Geometry geometry, GameConfig config) {
        this.gameColour = gameColour;
        this.geometry = geometry;
        sfxNoMove = new Sound("/nomove.wav");
        loadSounds(config.soundOn());
        whitePlayer = new Player(PlayerColor.WHITE,"Player1");
        blackPlayer = new Player(PlayerColor.BLACK,"Player2");
        currentPlayer = whitePlayer;
        spikes = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            spikes.add(new Spike(geometry, i));
        }
        die1 = new Die();
        die2 = new Die();
        makeColourObjects();
        initialiseBoard(INIT_CONFIGURATION);
        log("Board made");
    }

    public void loadSounds(boolean soundOn) {
        sfxNoMove.loadSound(soundOn);
    }

    public void makeColourObjects() {
        board_colour = new Color(gameColour.getBoardColor());
        bar_colour = new Color(gameColour.getBarColor());
        for (Spike spike: spikes) {
            spike.makeColourObjects();
        }
    }
    
    public void paint(Graphics g, int boardWidth, int boardHeight, boolean gameInProgress) {
        utils.setColor(g, Color.BLACK);
        int borderWidth = geometry.borderWidth();
        int barWidth = geometry.centralBarWidth();
        int widthMinusBorder = boardWidth - barWidth;

        //draw the board:
        // outline:
        utils.setColor(g, board_colour);
        utils.fillRect(g,borderWidth,borderWidth,widthMinusBorder,boardHeight-borderWidth*2);
        utils.setColor(g, Color.BLACK);
        utils.drawRect(g,borderWidth,borderWidth,widthMinusBorder,boardHeight-borderWidth*2);
        // bar between 2 halves
        utils.setColor(g, bar_colour);
        utils.fillRect(g,boardWidth / 2 - barWidth / 2, borderWidth, barWidth,boardHeight - borderWidth * 2);
        utils.setColor(g, Color.BLACK);
        utils.drawRect(g,boardWidth / 2 - barWidth / 2, borderWidth, barWidth,boardHeight - borderWidth * 2);

        for (Spike spike: spikes) {
           spike.paint(g, boardWidth, boardHeight);
        }

        paintDice(g,boardWidth,boardHeight);

        // draw the potential moves for whoevers go it is
        if (gameInProgress) {
            // SPECIAL CASE: PIECES ON THE BAR NEED TO BE MOVED FIRST/////
            if (whoseTurnIsIt() == PlayerColor.WHITE && CustomCanvas.theBarWHITE.size() > 0) {
                drawPotentialMovesFromBar(g);
            } else if (whoseTurnIsIt() == PlayerColor.BLACK && CustomCanvas.theBarBLACK.size() > 0) {
                drawPotentialMovesFromBar(g);
            } else {
                // if no piece is stuck to mouse then just show up the potential
                // moves as the mouse is hovered over each spike
                if (!CustomCanvas.pieceOnMouse) {
                    drawPotentialMoves(g);
                } else {
                    //if there is a piece stuck on the mouse then pulsate the copied
                    //versions of the potential moves from before it was stuck on,
                    //this simply allows the player to move the piece around and still
                    //see the potential moves for the piece they are "holding" currently
                    keepPotentialSpikesPulsing();
                }
            }
            if (!die1HasBeenUsed || !die2HasBeenUsed) {
                calculatePotentialMoves();
            }
        }
    }

    private void paintDice(Graphics g, int WIDTH, int HEIGHT) {
        if (!CustomCanvas.showRollButton) {
            int diex = (geometry.borderWidth() + ((WIDTH/4)*3)) + Die.getWidth();
            int diey = (geometry.borderWidth() + (HEIGHT/2)) - Die.getHeight();
            if (!die1HasBeenUsed) {
                die1.paint(g, diex, diey);
            } else {
                die1.disable();
            }

            diex += Die.getWidth() + geometry.tinyGap();
            if (!die2HasBeenUsed) {
                die2.paint(g, diex, diey);
            } else {
                die2.disable();
            }
        }
    }

    // when piece is on the bar this should display the options for it
    private void drawPotentialMovesFromBar(Graphics g) {
        // this was copied from drawPotentialMoves/////////
        allowPieceToStickToMouse = false; // make this false right away since its decided in this method, but could still be true from last time.
        getRidOfLittleDice(); // kind of intensive?
        detectIfPiecesAreHome(); // sets the right bools if pieces are home

        boolean cantGetOfBarWithDie1 = false;
        boolean cantGetOfBarWithDie2 = false;

        if (CustomCanvas.showRollButton) {
            return;
        }

        if (!die1HasBeenUsed) {
            if (!canWeGetOffTheBarWithThisDie(die1,1)) { // this tells us if there are options (and stores in spikesAllowedToMoveToFromBar)and draws them graphically to player if so
                ///dont set this yet as they might have options when they get off the bar die1HasBeenUsed=true;
                //leaving it unset results in correct behaviour
                //however if they cant get off the bar at all then we set both dies to used since they #d be stuck on the bar otherwise
                //so we keep a flag of it til bottom
                cantGetOfBarWithDie1 = true;
                log("NO OPTIONS FOR GETTING OFF BAR WITH DIE1");
            } else {
                //there are optiosn for getting off generation in here for the cpu player
                log("DIE1:");
                getOffTheBarOptions();
            }
        } else {
            cantGetOfBarWithDie1 = true;
        }
        if (!die2HasBeenUsed) {
            if (!canWeGetOffTheBarWithThisDie(die2,2)){//this tells us if there are optiosn (and stores in spikesAllowedToMoveToFromBar)and draws them graphically to player if so
                //dont set this yet as they might have optiosn when they get off the bar  die2HasBeenUsed=true;
                //leaving it unset results in correct behaviour
                //however if they cant get off the bar at all then we set both dies to used since they #d be stuck on the bar otherwise
                //so we keep a flag of it til bottom
                cantGetOfBarWithDie2 = true;
                log("NO OPTIONS FOR GETTING OFF BAR WITH DIE2");
            } else {
                log("DIE2:");
                //there are optiosn for getting off generation in here for the cpu player
                getOffTheBarOptions();
            }
        } else {
            cantGetOfBarWithDie2=true;
        }

        if (cantGetOfBarWithDie1 && cantGetOfBarWithDie2) {
            log("NO OPTIONS FROM BAR NEXT TURN!!!!!!!!!!!!!!");
            die1HasBeenUsed = true;
            die2HasBeenUsed = true;
        }
    }

    private void getOffTheBarOptions() {
         // there are options for getting off the bar
        Enumeration e = spikesAllowedToMoveToFromBar.elements();
        log("OFF THE BAR OPTIONS:");
        while(e.hasMoreElements()) {
            Spike spike = (Spike) e.nextElement();
            log("SPIKE:" + spike.getSpikeNumber() + " using ROLL OF " + spike.get_stored_die().getValue());
        }
        // grab the first spike for later:
        Spike destinationSpike = (Spike) spikesAllowedToMoveToFromBar.firstElement();

        // travel to piece on bar first
        Vector theBarPieces = whoseTurnIsIt() == PlayerColor.WHITE ? CustomCanvas.theBarWHITE : CustomCanvas.theBarBLACK;
        Enumeration weepingDonkey = theBarPieces.elements();
        while(weepingDonkey.hasMoreElements()) {
            log("White pieces on bar potential pick ups:");
            Piece piece = (Piece)weepingDonkey.nextElement();
            log("piece:" + piece.getColour());
        }
        if (CustomCanvas.pieceOnMouse) {
            log("Destination PLACE ON AVAIL SPIKE:" + destinationSpike.getSpikeNumber());
            Point spikeMiddle = destinationSpike.getMiddlePoint();
            setBotDestination(spikeMiddle.x, spikeMiddle.y, "PLACE FROM BAR ONTO AVAIL SPIKES");
        } else {
            log("DESTINATION FOR BOT, PIECE ON BAR......");
            Piece p = (Piece) theBarPieces.firstElement();
            setBotDestination(p.collision_x+Piece.PIECE_DIAMETER/2,
                p.collision_y+Piece.PIECE_DIAMETER/2,"DESTINATION FOR BOT, PIECE ON BAR......");
        }
    }

    // given the die this method will add spikes to spikesAllowedToMoveToFromBar
    // for current player so that the spikes available will flash and be ready to have a piece added to them
    private boolean canWeGetOffTheBarWithThisDie(Die die, int whichDie) {
        int destinationSpikeId = whoseTurnIsIt() == PlayerColor.BLACK ? die.getValue() - 1 : 24 - die.getValue();
        Spike destinationSpike = (Spike) spikes.get(destinationSpikeId);
        if (destinationSpike.pieces.size() <= 1 ||
            doesThisSpikeBelongToPlayer(destinationSpike, whoseTurnIsIt())) {
            destinationSpike.flash(whichDie);
            if (!spikesAllowedToMoveToFromBar.contains(destinationSpike)) {
                destinationSpike.store_this_die(die); // bit of a hack here : just so we have a record of which die it will use
                spikesAllowedToMoveToFromBar.add(destinationSpike);
            }
            pickingPieceUpFromBar = true;
            return true;
        }
        return false;
    }

    //called after game over when we return to the main meu to make sure all
    //vars are cleaned up properly
    public void RESET_ENTIRE_GAME_VARS(boolean soundOn) {
        loadSounds(soundOn);
        currentPlayer = whitePlayer;
        spikesAllowedToMoveToFromBar = new Vector(6);
        pickingPieceUpFromBar = false;
        allowPieceToStickToMouse = false;
        allWhitePiecesAreHome = false;
        allBlackPiecesAreHome = false;
        potentialNumberOfMoves = 0;
        noMovesAvailable = false; // this gets set to true when no moves at all are available.
        pulsateWhiteContainer = false;
        pulsateBlackContainer = false;
        movePhase = 0;
        die1HasBeenUsed = false;
        die2HasBeenUsed = false;

        whichDieGetsUsToPieceContainer = -1;

        listBotsOptions = false;
        botOptions = "<<NONE YET>>";
        thereAreOptions = false;
        SPtheMoveToMake = null;

        initialiseBoard(INIT_CONFIGURATION);
    }

    // simply pulses the spikes while they are not null
    // "Pulses" means color them differently, so it is clear that a piece can be moved there.
    private void keepPotentialSpikesPulsing() {
        boolean debugmessages = false;
        if (debugmessages) {
            log("keepPotentialSpikesPulsing");
        }
        String debugstr = "";
        if (copy_of_reachableFromDie1 != null) {
            pulsatePotentialSpike(copy_of_reachableFromDie1, DIE1);
            debugstr = "die1";
        }

        if (copy_of_reachableFromDie2 != null) {
            pulsatePotentialSpike(copy_of_reachableFromDie2, DIE2);
            debugstr += ", die2";
        }
        if (copy_of_reachableFromBothDice!= null) {
            pulsatePotentialSpike(copy_of_reachableFromBothDice, DIE1AND2);
            debugstr += ", bothdice";
        }
        if (debugmessages) {
            log("debugstr:" + debugstr);
        }
    }

    // this is always the current x and y vals of the mouse pointer
    public static int mouseHoverX,mouseHoverY;

    // controls whether the piece should follow the mouse when clicked on by player
    // (based on if there are potential moves to be made)
    public static boolean allowPieceToStickToMouse = false;

    // we take copies of the "pulsating" spikes (ie those that are valid spikes that
    // the current player could move a piece to) - we take these copies so that
    // when the player picks up a piece (ie it becomes stuck to the mouse pointer)
    // and moves it around, we can still show the pulsating valid spikes that he
    // can drop this piece onto. Without these copies as soon as the pointer
    // stopped hovering over the current spike (like it would if player was placing
    // a piece) the pulsating indication of valid options would vanish.
    public static Spike copy_of_reachableFromDie1;
    public static Spike copy_of_reachableFromDie2;
    public static Spike copy_of_reachableFromBothDice;

    // simply nullifies the string that tells the spike to show its little dice
    // this is shown to user when they have potential moves.
    private void getRidOfLittleDice() {
        //would have carried out the potential move (shouldnt they be linked to these 3 spikes? not sure yet)
        for (Spike s: spikes) {
            s.whichDiei = -1;
        }
    }

    // this handles:
    // detecting if the pieces for each team are in their home area
    // will throw an error if all pieces in play (or on piece container) arent equals to 15 for both players
    private void detectIfPiecesAreHome() {
         //RESET THESE HERE?
        pulsateWhiteContainer = false;
        pulsateBlackContainer = false;

        //detect if all the pieces are in the "home" side of the board,
        //and if so make the piece container a different colour. (piece containers are painted in
        //green when this is true) - this also takes into accoutn when pieces are already safely in the piece container
        // TODO Optimise this so its only called once each time not constantly *******
        allWhitePiecesAreHome = calculateAmountOfPiecesInHomeArea(whitePlayer) +
            CustomCanvas.whitePiecesSafelyInContainer.size() == 15;

        allBlackPiecesAreHome = calculateAmountOfPiecesInHomeArea(blackPlayer) +
            CustomCanvas.blackPiecesSafelyInContainer.size() == 15;

        final int whitePieces = calculateAmountOfPiecesOnBoard(PlayerColor.WHITE) +
            CustomCanvas.whitePiecesSafelyInContainer.size() +
            CustomCanvas.theBarWHITE.size();
        if (whitePieces != 15) {
            throw new RuntimeException("PIECES NOT EQUAL TO 15 FOR WHITE its " + whitePieces);
        }
        final int blackPieces = calculateAmountOfPiecesOnBoard(PlayerColor.BLACK) +
            CustomCanvas.blackPiecesSafelyInContainer.size() +
            CustomCanvas.theBarBLACK.size();
        if (blackPieces != 15) {
            throw new RuntimeException("PIECES NOT EQUAL TO 15 FOR BLACK its " + blackPieces);
        }
    }

    //indicates when the pieces are all in their home section, and thus we indicate
    // to the player they can put them into their container now.
    public static boolean allWhitePiecesAreHome;
    public static boolean allBlackPiecesAreHome;
    static int potentialNumberOfMoves = 0;
    static public boolean noMovesAvailable = false; // this gets set to true when no moves at all are available.

    // gives us meaningful commands for the robot mostly
    static int movePhase;

    public static boolean pulsateWhiteContainer, pulsateBlackContainer;

    /*
     * This method draws an indicator to show the player potential moves
     * for the piece they currently have the mouse hovered over
     * there can be up to 3 potential moves: die1, die2, die1+die2
     */
    private void drawPotentialMoves(Graphics g) {
        boolean debugmessages = false; // this one can be very handy for debugging, too verbose once you know it works
        allowPieceToStickToMouse = false; // make this false right away since its decided in this method, but could still be true from last time.
        getRidOfLittleDice(); // kind of intensive?
        detectIfPiecesAreHome(); // sets the right bools if pieces are home

        if (CustomCanvas.showRollButton) {
            return;
        }

        Spike currentSpikeHoveringOver = doesThisSpikeBelongToPlayer();
        if (currentSpikeHoveringOver == null) {
            return;
        }
        boolean die1AnOption = checkValidPotentialMove(currentSpikeHoveringOver, die1.getValue(), DIE1);

        //this boolean is used to keep track of whether we let a piece stick to mouse, so we need to keep a track on if die1
        //is an option (since if it is we want that piece to be able to be picked up) but we do further checks below
        //so this "stillAnOption" variable gets updated below and used at the bottom to update canWeStickPieceToMouse
        boolean die1StillAnOption = false;//set to false first so we know if its true its been updated below

        // if die1 would yield a potential valid spike to land on AND if the
        // player has not used die1 this turn yet.
        if (die1AnOption && !die1HasBeenUsed) {
            copy_of_reachableFromDie1 = null; // Set to null here so we know if its valid by the end it true is
            int potentialSpikeIndex = currentPlayer.getDestinationSpike(currentSpikeHoveringOver, die1.getValue());
            // only for the case when a piece can go into the piece container
            boolean highlightPieceContainerAsOption = potentialSpikeIndex == currentPlayer.containerId()
                && !anybodyNotInHome(currentPlayer);
            if (highlightPieceContainerAsOption) { //<-- this can get set by checkValidPotentialMove when the situation is right
                if (potentialSpikeIndex == FIRST_SPIKE - 1 && whoseTurnIsIt() == PlayerColor.WHITE) {
                    pulsateWhiteContainer = true;
                    die1StillAnOption = true;
                    copy_of_reachableFromDie1 = null; // STOPS OLD SPIKES FLASHING IN PICE CONTAINER CIRCUMSTANCES
                } else if (potentialSpikeIndex==LAST_SPIKE+1 && whoseTurnIsIt() ==PlayerColor.BLACK) {
                    log("yes " + potentialSpikeIndex + " is a valid option DIE1 TO GET ONTO PIECE BLACK CONTAINER");
                    pulsateBlackContainer = true;
                    die1StillAnOption = true;
                    copy_of_reachableFromDie1 = null; // STOPS OLD SPIKES FLASHING IN PICE CONTAINER CIRCUMSTANCES
                }
            }
           ///NO NEED FOR ELSE HERE EXPERIMENTAL 21JAN 1018AM else // normal situation. ie a spike is the option
            if (potentialSpikeIndex >= FIRST_SPIKE && potentialSpikeIndex <= LAST_SPIKE) {
                 Spike reachableFromDie1 = spikes.get(potentialSpikeIndex);
                 if (debugmessages) {
                    log("yes " + potentialSpikeIndex + " is a valid option DIE1");
                 }
                 //graphically indicate the spike that is a valid move using die1s value:
                 pulsatePotentialSpike(reachableFromDie1, DIE1);

                 //copy this so we can keep it pulsating during placing of piece
                 //and not just when the point is hovered over this spike
                 copy_of_reachableFromDie1 = reachableFromDie1;
                 if (debugmessages) {
                    log("copy_of_reachableFromDie1 set up.");
                 }
                 die1StillAnOption=true;
            }
        } else {
            //EXPERIMENTAL JAN 14TH 2010 [seems to work remove this line]
            //make this null if we know for certain its not a potential move or ot gets remembered
            copy_of_reachableFromDie1 = null;
        }

        //do DIE2 in same way
        boolean die2AnOption = checkValidPotentialMove(currentSpikeHoveringOver, die2.getValue(), DIE2);

        //this boolean is used to keep track of whether we let a piece stick to mouse, so we need to keep a track on if die1
        //is an option (since if it is we want that piece to be able to be picked up) but we do further checks below
        //so this "stillAnOption" variable gets updated below and used at the bottom to update canWeStickPieceToMouse
        boolean die2StillAnOption = false;//set to false first so we know if its true its been updated below

         // if die2 would yield a potential valid spike to land on AND if the
        //player has not used die2 this turn yet.
        if (die2AnOption && !die2HasBeenUsed) {
            copy_of_reachableFromDie2 = null; //Set to null here so we know if its valid by the end it true is
            int potentialSpikeIndex = currentPlayer.getDestinationSpike(currentSpikeHoveringOver, die2.getValue());
            boolean highlightPieceContainerAsOption = potentialSpikeIndex == currentPlayer.containerId()
                && !anybodyNotInHome(currentPlayer);
              //only for the case when a piece can go into the piece container
            if (highlightPieceContainerAsOption) {//<-- this can get set by checkValidPotentialMove when the situation is right
                if (potentialSpikeIndex==FIRST_SPIKE-1 && whoseTurnIsIt() == PlayerColor.WHITE)
                {
                     log("yes " + potentialSpikeIndex + " is a valid option DIE2 TO GET ONTO PIECE WHITE CONTAINER");
                    pulsateWhiteContainer=true;
                    die2StillAnOption=true;
                    copy_of_reachableFromDie2=null;//EXPERIMENT- YES IT WORKS
                } else if (potentialSpikeIndex==LAST_SPIKE+1 && whoseTurnIsIt() == PlayerColor.BLACK) {
                     log("yes " + potentialSpikeIndex + " is a valid option DIE2 TO GET ONTO PIECE BLACK CONTAINER");
                    pulsateBlackContainer=true;
                    die2StillAnOption=true;
                    copy_of_reachableFromDie2=null;//EXPERIMENT-YES IT WORKS
                }
            }
            ///NO NEED FOR ELSE HERE EXPERIMENTAL 21JAN 1018AM else // normal situation. ie a spike is the option
            if (potentialSpikeIndex>=FIRST_SPIKE && potentialSpikeIndex<=LAST_SPIKE) {
                 Spike reachableFromDie2 = spikes.get(potentialSpikeIndex);
                 if (debugmessages) {
                    log("yes " + potentialSpikeIndex + " is a valid option DIE2");
                 }
                 //graphically indicate the spike that is a valid move using die1s value:
                 pulsatePotentialSpike(reachableFromDie2,DIE2);

                 //copy this so we can keep it pulsating during placing of piece
                 //and not just when the point is hovered over this spike
                 copy_of_reachableFromDie2=reachableFromDie2;
                 if (debugmessages) {
                    log("copy_of_reachableFromDie2 set up.");
                 }
                 die2StillAnOption=true;
            }
        } else {
            //EXPERIMENTAL JAN 14TH 2010 [seems to work remove this line]
            //make this null if we know for certain its not a potential move or ot gets remembered
            copy_of_reachableFromDie2=null;
        }

        //and DIE1 + DIE2
        boolean bothDiceAnOption = checkValidPotentialMove(currentSpikeHoveringOver, die1.getValue()+die2.getValue(), DIE1AND2);

        //this boolean is used to keep track of whether we let a piece stick to mouse, so we need to keep a track on if die1
        //is an option (since if it is we want that piece to be able to be picked up) but we do further checks below
        //so this "stillAnOption" variable gets updated below and used at the bottom to update canWeStickPieceToMouse
        boolean bothDiceStillAnOption=false;//set to false first so we know if its true its been updated below


        //if die1+die2 would yield a potential valid spike to land on AND if the
        //player has not used die1 OR die2 this turn yet.
        //AND ***IMPORTANTLY IF die1 OR die2 are valid options, since the player needs to be able to take each
        //turn and not simply add them together (therefore making an invalid move by combining their rolls)
        //this is not how backgammon works.
        if (bothDiceAnOption && !die1HasBeenUsed && !die2HasBeenUsed && (die1AnOption || die2AnOption)) {
            copy_of_reachableFromBothDice = null; // Set to null here so we know if its valid by the end it true is
            int potentialSpikeIndex = currentPlayer.getDestinationSpike(currentSpikeHoveringOver, die1.getValue() + die2.getValue());
            boolean highlightPieceContainerAsOption = potentialSpikeIndex == currentPlayer.containerId() &&
                !anybodyNotInHome(currentPlayer);
            // only for the case when a piece can go into the piece container
            if (highlightPieceContainerAsOption) {//<-- this can get set by checkValidPotentialMove when the situation is right
                if (potentialSpikeIndex == FIRST_SPIKE - 1 && whoseTurnIsIt() == PlayerColor.WHITE) {
                    log("yes " + potentialSpikeIndex + " is a valid option DIE1+DIE2 TO GET ONTO PIECE WHITE CONTAINER");
                    pulsateWhiteContainer = true;
                    bothDiceStillAnOption = true;
                } else if (potentialSpikeIndex == LAST_SPIKE + 1 && whoseTurnIsIt() == PlayerColor.BLACK) {
                    log("yes " + potentialSpikeIndex + " is a valid option DIE1+DIE2 TO GET ONTO PIECE BLACK CONTAINER");
                    pulsateBlackContainer = true;
                    bothDiceStillAnOption = true;
                }
            }
            ///NO NEED FOR ELSE HERE EXPERIMENTAL 21JAN 1018AM else // normal situation. ie a spike is the option
            if (potentialSpikeIndex >= FIRST_SPIKE && potentialSpikeIndex <= LAST_SPIKE) {
                 Spike reachableFromBothDice = spikes.get(potentialSpikeIndex);
                 if (debugmessages) {
                    log("yes " + potentialSpikeIndex + " is a valid option BOTH DICE");
                 }
                 //graphically indicate the spike that is a valid move using die1s value:
                 reachableFromBothDice.flash(DIE1AND2);

                 //copy this so we can keep it pulsating during placing of piece
                 //and not just when the point is hovered over this spike
                 copy_of_reachableFromBothDice = reachableFromBothDice;
                 if (debugmessages) {
                    log("copy_of_reachableFromBothDice set up.");
                 }
                 bothDiceStillAnOption=true;
            }
        } else {
            //EXPERIMENTAL JAN 14TH 2010 [seems to work remove this line]
            //make this null if we know for certain its not a potential move or ot gets remembered
            copy_of_reachableFromBothDice=null;
        }

        //by this point we know whether we have our options (decided above for pulsating the correct
        //spikes, so we also know whether we should allow a piece to stick to the mouse, ie only if that
        //piece is relevant and can be moved etc

        //the spike they are currently hovering over has options, thus if
        //the player clicks on a piece on this spike it will stick to the mouse
        //point and move with it, until its either placed back or placed
        //somewhere new
        allowPieceToStickToMouse = die1StillAnOption || die2StillAnOption || bothDiceStillAnOption;
    }

    private int calculateAmountOfPiecesOnBoard(PlayerColor player) {
        int piecesOnBoard = 0;
        for (Spike spike: spikes) {
            piecesOnBoard += spike.getAmountOfPieces(player);
        }
        return piecesOnBoard;
    }

    private int calculateAmountOfPiecesInHomeArea(Player player) {
        int piecesinHomeArea = 0;
        for (Spike spike: spikes) {
            if (!spike.pieces.isEmpty() &&
                spike.getSpikeNumber() >= player.homeSpikeStart() && spike.getSpikeNumber() <= player.homeSpikeEnd()) {
                Piece piece = (Piece) spike.pieces.firstElement();
                if (piece.getColour() == player.getColour()) {
                    piecesinHomeArea += spike.pieces.size();
                }
            }
        }
        return piecesinHomeArea;
    }

    private boolean anybodyNotInHome(Player player) {
        for (Spike spike: spikes) {
            if (spike.getAmountOfPieces(player.getColour()) > 0 &&
                (spike.getSpikeNumber() < player.homeSpikeStart() || spike.getSpikeNumber() > player.homeSpikeEnd()))
                return true;
        }
        return false;
    }

    //takes in a spike and a die value,
    //this is to indicate to player its a potential move they can make
    //whichDice tells the spike which die would allow this potential move, so it an be displayed to player
    private void pulsatePotentialSpike(Spike spike, int whichDice) {
        //this makes the spike colour pulse nicely to indicate its an option
        spike.flash(whichDice);
    }

    // takes in spike the player is currently hovering over with mouse
    // and also a die roll, and returns true if the potential spike (ie currentSpike + dieRoll=potentialSpike)
    // is able to be moved to. TODO: this variable is only needed if active player wants to put piece into container.
    public static int whichDieGetsUsToPieceContainer = -1;

    // whichDieIsThis is passed in which indicates which die roll will be used in this potential move
    // 1 is die 1
    // 2 is die 2
    // 3 is die1+die2
    // these are used simply to update whichDieGetsUsToPieceContainer as it doesnt know otherwise
    private boolean checkValidPotentialMove(Spike currentSpike, int dieRoll, int whichDieIsThis) {
        int potentialSpike = currentPlayer.getDestinationSpike(currentSpike, dieRoll);
        if (potentialSpike == currentPlayer.containerId() && !anybodyNotInHome(currentPlayer)) {
            whichDieGetsUsToPieceContainer = whichDieIsThis;
            return true;
        }
        return (potentialSpike >= FIRST_SPIKE) && (potentialSpike <= LAST_SPIKE) &&
            (isThisSpikeEmpty(spikes.get(potentialSpike)) ||
                doesThisSpikeBelongToPlayer(spikes.get(potentialSpike), whoseTurnIsIt()));
    }

    //returns the Spike the player has their mouse over currently IFF it
    //actually belongs to them (that is, already contains one of their pieces)
    //otherwise returns null.
    private Spike doesThisSpikeBelongToPlayer() {
        Spike hoverSpike = grabSpikeHoveringOver();
        boolean containsOneOfTheirPieces = false;
        if (hoverSpike != null) {
           containsOneOfTheirPieces = doesThisSpikeBelongToPlayer(hoverSpike, whoseTurnIsIt());
        }
        if (containsOneOfTheirPieces) {
            return hoverSpike;
        } else {
            return null;
        }
    }

    // this returns a Spike object representing the spike that the players
    // mouse pointer is currently hovering over. If its not over any then
    // it returns null
    private Spike grabSpikeHoveringOver() {
       for (Spike currentSpike: spikes) {
           if (currentSpike.userClickedOnThis(mouseHoverX, mouseHoverY)) {
               return currentSpike;
           }
        }
        return null;
    }

    //returns true if the spike is empty
    //false if not.
    // ALSO RETURNS TRUE IF THERE IS ONLY ONE ENEMY PIECE ON THE SPIKE
    private boolean isThisSpikeEmpty(Spike checkme) {
        return  checkme.pieces.size() <= 1;
    }

    //returns true if the spike passed in contains a piece belonging to
    //the player colour passed in. returns false if not.
    private boolean doesThisSpikeBelongToPlayer(Spike checkme, PlayerColor playerColour) {
         Enumeration piecesE = checkme.pieces.elements();
         while(piecesE.hasMoreElements()) {
             Piece aPiece = (Piece) piecesE.nextElement();
             if (aPiece.getColour() == playerColour) {
                return true;
             }
        }
        return false;
    }

    void initialiseBoard(int mode) {
        log("mode: BOARD_NEW_GAME");
        initialiseBoardForNewGame(whiteInitPositions, blackInitPositioin);
        if (mode == DEBUG_PIECES_IN_THEIR_HOME) {
            log("mode: DEBUG_BOARD_WHITE_PIECES_IN_THEIR_HOME");
            int[] whiteHome = {
                5, 5, 5, 0, 0, 0,
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0
            };
            int[] blackHome = {
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0,
                5, 5, 5, 0, 0, 0
            };
            initialiseBoardForNewGame(whiteHome, blackHome);
        }
    }


    // puts the pieces where they need to be to initialise a new game of backgammon
    void initialiseBoardForNewGame(int[] whiteInitPositions, int[] blackInitPositions) {
        log("initialiseBoardForNewGame");
        for (Spike spike: spikes) {
            spike.pieces.clear();
        }
        for (int i = 0; i < whiteInitPositions.length; ++i) {
            for (int j = 0; j < whiteInitPositions[i]; ++j) {
                Piece newPiece = new Piece(whitePlayer);
                spikes.get(i).addPiece(newPiece);
            }
        }
        for (int i = 0; i < whiteInitPositions.length; ++i) {
            for (int j = 0; j < blackInitPositions[i]; ++j) {
                Piece newPiece = new Piece(blackPlayer);
                spikes.get(i).addPiece(newPiece);
            }
        }
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    //takes a piece and its current spike and a new spike and returns
    //true if this piece can be moved from old spike to new spike
    //will catry out the move on ly if doit is true
    private boolean movePiece(Piece p, Spike oldSpile, Spike newSpike, boolean doit) {
        return true;
    }

    private static void log(String s) {
        Utils.log(String.format("thread-%s Board{}:%s", Thread.currentThread().getName(), s));
    }

    void calculatePotentialMoves() {
        if (!CustomCanvas.showRollButton && !CustomCanvas.pieceOnMouse && !thereAreOptions) {
            log("_______________________RECALCULATE MOVES die1:" + die1HasBeenUsed + " die2:" + die2HasBeenUsed);
            Vector spikePairs = getValidOptions();
            // if this is true by the time we get to "no options!" we know we need to let them take a special move and allow the
            // die roll to wotkeven tho its to big
            log("spikePairs size:" + spikePairs.size());
            thereAreOptions = spikePairs.size() > 0;
            if (thereAreOptions) {
                listBotsOptions = true;
                if (listBotsOptions) {
                    botOptions = "";
                    Enumeration ee = spikePairs.elements();
                    while (ee.hasMoreElements()) {
                        SpikePair sp = (SpikePair) ee.nextElement();
                        botOptions += "->" + sp.pickMyPiece.getName() + "->" + sp.dropPiecesOnMe.getName() + " ";
                    }
                    log("valid options: " + botOptions);
                }
                //PICK ONE AT RANDOM
                SPtheMoveToMake = (SpikePair) spikePairs.elementAt(Utils.getRand(0, spikePairs.size() - 1));

                if (SPtheMoveToMake.dropPiecesOnMe.isContainer()) {
                    //SPECIAL CONDITION, GO TO PIECE CONTAINER NOT SPIKE
                    log("SPECIAL CASE randomly chose to go to spike:" +
                        SPtheMoveToMake.pickMyPiece.getName() + " and drop off at CONTAINER");
                    CustomCanvas.tellRobot(true, "->" + SPtheMoveToMake.pickMyPiece.getName() + "->Container");
                    Spike takeMyPiece = SPtheMoveToMake.pickMyPiece;
                    Piece firstPiece = ((Piece) takeMyPiece.pieces.firstElement());
                    setBotDestination(firstPiece.collision_x + firstPiece.PIECE_DIAMETER / 2,
                        firstPiece.collision_y + firstPiece.PIECE_DIAMETER / 2,
                        "TAKE A PIECE TO CONTAINER");
                } else {
                    //NORMAL CONDITION
                    log("-randomly chose to go to spike:" +
                        SPtheMoveToMake.pickMyPiece + " and drop off at spike:" +
                        SPtheMoveToMake.dropPiecesOnMe);
                    CustomCanvas.tellRobot(true, "->" + SPtheMoveToMake.pickMyPiece +
                        "->" + SPtheMoveToMake.dropPiecesOnMe);
                    Spike takeMyPiece = SPtheMoveToMake.pickMyPiece;
                    Piece firstPiece = ((Piece) takeMyPiece.pieces.firstElement());
                    int goToX = firstPiece.collision_x + firstPiece.PIECE_DIAMETER / 2;
                    int goToY = firstPiece.collision_y + firstPiece.PIECE_DIAMETER / 2;
                    setBotDestination(goToX, goToY, "RANDOMLY CHOOSE A PIECE");
                    log("***************PIECE IM LOOKING FOR IS AT: " + goToX + "," + goToY);
                }
            } else {
                log("NO OPTIONS!");
                // OK NO POTENTIAL MOVES TO BE MADE HERE, NOW WHAT?
                if (!die1HasBeenUsed){//if this is die 1 were dealing with
                    //SPECIAL CASE LARGE DIE ROLLS NEED TO BECOME VALID NOW. AS THEY NEED TO PUT PIECES AWAY
                    //so what we do is sneaky, reduce die value number (hiding it from players of course)
                    //which makes optiosn become available in this case.
                    if (whoseTurnIsIt() == PlayerColor.WHITE && allWhitePiecesAreHome) {
                        log("WHITE LOWERING THEVALUE OF DIE 1");
                        die1.setValue(die1.getValue() - 1);
                    } else if (whoseTurnIsIt() == PlayerColor.BLACK && allBlackPiecesAreHome) {
                        log("BLACK LOWERING THEVALUE OF DIE 1");
                        die1.setValue(die1.getValue() - 1);
                    } else {
                        //ORDINARY CASE
                        //use this die up so it can move onto next one
                        die1HasBeenUsed = true;
                        log("DISABLED DIE 1x");
                        // canvas.tellPlayers("No option with Die 1 (" + die1.getValue() + ")");
                        sfxNoMove.playSound();
                    }
                } else if (!die2HasBeenUsed) {
                    //SPECIAL CASE LARGE DIE ROLLS NEED TO BECOME VALID NOW. AS THEY NEED TO PUT PIECES AWAY
                    //so what we do is sneaky, reduce die value number (hiding it from players of course)
                    //which makes optiosn become available in this case.
                    if (whoseTurnIsIt() == PlayerColor.WHITE && allWhitePiecesAreHome) {
                        log("WHITE LOWERING THEVALUE OF DIE 2");
                        die2.setValue(die2.getValue() - 1);
                    } else if (whoseTurnIsIt() == PlayerColor.BLACK && allBlackPiecesAreHome) {
                        log("BLACK LOWERING THEVALUE OF DIE 2");
                        die2.setValue(die2.getValue() - 1);
                    } else {
                        //use this die up so it can move onto next go
                        die2HasBeenUsed = true;
                        log("DISABLED DIE 2x");
                        // canvas.tellPlayers("No options available with Die 2 (" + die2.getValue() + ")");
                        sfxNoMove.playSound();
                        //it should move onto next players go NOW...
                        if (CustomCanvas.someoneRolledADouble) {
                            //even cancel a double go if theres no options for die2
                            CustomCanvas.someoneRolledADouble = false;
                            CustomCanvas.doubleRollCounter = 3;
                            log("NO OPTIONS SO CANCELLED DOUBLE TURN!");
                        }
                    }
                }
            }
        } else if (CustomCanvas.pieceOnMouse) {
            theyWantToPlaceAPiece();
            thereAreOptions = false;
        }
    }

    /**
     * Checks the currently available die and populates spikePairs vector with
     * valid (sourceSpike, destinationSpike) pairs.
     */
    private Vector getValidOptions() {
        log("Getting valid spike pairs");
        Vector spikePairs = new Vector(5);
        int diceRoll = -1;
        if (!die1HasBeenUsed) {
            diceRoll = die1.getValue();
            log("using DIE1 value " + diceRoll);
        } else if (!die2HasBeenUsed) {
            diceRoll = die2.getValue();
            log("using DIE2 value " + diceRoll);
        }
        // blacks pieces move anticlockwise, white clockwise
        boolean clockwise = whoseTurnIsIt() == PlayerColor.WHITE;
        //this gets set according to whose go it is
        boolean checkAbleToGetIntoPieceContainer = clockwise ? allWhitePiecesAreHome : allBlackPiecesAreHome;

        if (diceRoll > 0) {
            for (Spike spike: spikes) {
                // check if we own it already (ie we have parts on it)
                if (doesThisSpikeBelongToPlayer(spike, whoseTurnIsIt()) && spike.pieces.size() > 0) {
                    // so we have a spike that belongs to us for sure and has our pieces  on it
                    // now check if we were to take one of those pieces, would we be able to place it?
                    int potentialSpike = clockwise ? spike.getSpikeNumber() - diceRoll :
                        spike.getSpikeNumber() + diceRoll;

                    ////SPECIAL CASE FOR PUTTING ON PIECE CONTAINER
                    // only for the case when a piece can go into the piece container
                    if (checkAbleToGetIntoPieceContainer) { //<-- this can get set by checkValidPotentialMove when the situation is right
                        /* theres one more condition here, they DONT NEED AN EXACT ROLL IF that roll is too high and would
                         * end up being considered invalid, in this EXACT instance they can use that roll to get onto piece container
                         * still
                         */
                        if (potentialSpike >= FIRST_SPIKE - 1 && potentialSpike <= LAST_SPIKE + 1) { //keep it on the board.BUT REMEMBER PIECE CONTAINERS
                            log("checkAbleToGetIntoPieceContainer is true - potentialSpike:" + potentialSpike);
                            // so we now know that the piece container is an option and we have a valid roll
                            // to get us there.
                            // but in this case we cannot grab a spike to navigate to, and the piecesSafelyHome is just a vector
                            // but what we do is sneaky, we make a fake spikePair and tell the spikePair its fake
                            //so that it (the piece container, that is not a spike) is considered an option just like any normal spike.

                            if (potentialSpike == LAST_SPIKE + 1 || potentialSpike == FIRST_SPIKE - 1) {//so we know for sure its destined for piece container
                                log("PIECECONTAINER: MAKING A FAKE SPIKE, potentialspike is " + potentialSpike);
                                // pass in -99 to make spike a very special one which is basically a piece container (see Spike constructor)
                                Spike destinationSpike = new Spike(geometry,-1);
                                log("yes " + destinationSpike + " IS A PIECE CONTAINER we can move to");
                                spikePairs.add(new SpikePair(spike, destinationSpike));

                            }
                        }
                    }
                    ///////////////WE STILL NEED THIS DONE TOO I THINK EXPERIMENT, NOT AN ELSE AS FAR AS I KNOW else
                    //so we grab the potential spike (based on the dieroll given) where we would place
                    //a piece from the ones that belong to us
                    if (potentialSpike >= FIRST_SPIKE && potentialSpike <= LAST_SPIKE){//keep it on the board.
                        /*FORMULATE SETS THE PICKER UPER AND THE ENDER, KEEP THESE SETS THATS THE
                         KEY... */
                        //this sets up the spike we use for moving piece to
                        Spike destinationSpike = spikes.get(potentialSpike);
                        //if its empty or we own it, we can move to it (ADD IN HERE KILLING OTHER PLAYER)
                        if (destinationSpike.pieces.size() <= 1 ||
                            doesThisSpikeBelongToPlayer(destinationSpike, whoseTurnIsIt())) {
                            spikePairs.add(new SpikePair(spike, destinationSpike));
                        }
                    }
                }
            }
        } else {
            log(" warnign dice roll was under 0 - this indicates no options for this player");
        }
        log("finished getValidOptions");
        return spikePairs;
    }

    public static void setBotDestination(int x, int y, String desc) {
        if (Bot.destX != x || Bot.destY != y) {
            log("NEW BOT DEST: " + x + "," + y + ":" + desc);
            Bot.destX = x;
            Bot.destY = y;
        }
    }

    private void theyWantToPlaceAPiece() {
        if (CustomCanvas.barPieceStuckOnMouse) {
            //stops tem going on with normal case stuff will bar piece is put down
            //works briliantly!
            log("dont do anythign til we palce this");
            return;
        }
        if (SPtheMoveToMake == null) {
            log("DOUBLE RECALC.>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            thereAreOptions = false;
            return;
        }
        if (!Bot.getFullAutoPlay() && whoseTurnIsIt() == PlayerColor.WHITE) {
            return;
        }
        Spike dropOnMe = SPtheMoveToMake.dropPiecesOnMe;
        if (dropOnMe != null) {
            if (dropOnMe.isContainer()) {
                int pieceContainerX = 0;
                int pieceContainerY = 0;
                int pieceContainerWidth = 0;
                int pieceContainerHeight = 0;
                if (whoseTurnIsIt() == PlayerColor.WHITE) {
                    pieceContainerX = CustomCanvas.whiteContainerX;
                    pieceContainerY = CustomCanvas.whiteContainerY;
                    pieceContainerWidth = CustomCanvas.whiteContainerWidth;
                    pieceContainerHeight = CustomCanvas.whiteContainerHeight;
                } else if (whoseTurnIsIt() == PlayerColor.BLACK) {
                    pieceContainerX = CustomCanvas.blackContainerX;
                    pieceContainerY = CustomCanvas.blackContainerY;
                    pieceContainerWidth = CustomCanvas.blackContainerWidth;
                    pieceContainerHeight = CustomCanvas.blackContainerHeight;
                } else {
                    Utils._E("errori n theywanttoplaceapiece, turn is invalid");
                }
                setBotDestination(pieceContainerX + pieceContainerWidth / 2,
                    pieceContainerY + pieceContainerHeight / 2,"PIECE CONTAINER DESTINATION");
            } else {
                Point middlePoint = dropOnMe.getMiddlePoint();
                setBotDestination(middlePoint.x, middlePoint.y, "NORMAL CASE DROP ON SPIKE A");
            }
        } else {
            Utils._E("DROP ON ME IS NULL.");
        }
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(PlayerColor player) {
        if (player == PlayerColor.WHITE) {
            currentPlayer = whitePlayer;
        } else {
            currentPlayer = blackPlayer;
        }
    }

    public void nextTurn() {
        if (currentPlayer.getColour() == PlayerColor.WHITE) {
            currentPlayer = blackPlayer;
            log("BLACKS TURN");
        } else {
            currentPlayer = whitePlayer;
            log("WHITES TURN");
        }
    }

    // returns the current pip count of the given player.
    public int calculatePips(PlayerColor player) {
        int pips = 0;
        /*pips is the amount of dots on the die it would take to get off the board, so to count them you go through the spikes
         * counting the number of pieces of that colour on the spike, then multiply that by the amount of spikes it is away from the
         * end of the board (INCLUDING the one to get onto the pice container), add these all up . as an example the starting pip count is 167 because:
         * 2 pieces on spike 0 (23 steps from end) * 2 = 48
         * 5 pieces on spike 11 (13 steps from end)*5=65
         * 3 pieces on spike 16 (8 steps from end) *3 = 24
         * 6 pieces on spike 18 (5 steps from the end) *6 =30
         * total is 167
         */
        if (player == PlayerColor.WHITE) {
            for (int i = 0; i < 24; i++) {
                Spike spike = spikes.get(i);
                pips += (i + 1) * spike.getAmountOfPieces(PlayerColor.WHITE);
            }
        } else {
            int j = 0;
            for (int i = 23; i >= 0; i--, j++) {
                Spike spike = spikes.get(i);
                pips += (j + 1) * spike.getAmountOfPieces(PlayerColor.BLACK);
            }
        }
        return pips;
    }

    public ArrayList<Spike> getSpikes() {
        return spikes;
    }

    public void rollDies() {
        die1.roll();
        die2.roll();
        die1HasBeenUsed = die2HasBeenUsed = false;
    }

    public boolean rolledDouble() {
        return die1.enabled() && die2.enabled() && (die1.getValue() == die2.getValue());
    }
    
    public PlayerColor whoseTurnIsIt() {
        return currentPlayer.getColour();
    }
}
