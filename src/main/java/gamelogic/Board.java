package gamelogic;
import java.awt.*;
import java.util.*;

import data.DieType;
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
    public Die die1, die2;
    private Spike theBarWHITE;
    private Spike theBarBLACK;
    private Vector whitePiecesSafelyInContainer = new Vector(15);
    private Vector blackPiecesSafelyInContainer = new Vector(15);
    private boolean diesRolled = false;

    private Utils utils = new Utils();
    private Sound sfxNoMove;
    private Sound sfxKilled;
    private Sound sfxPutPieceInContainer;

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

    private SpikePair SPtheMoveToMake; // stores the move they will make

    public Board(GameColour gameColour, Geometry geometry, GameConfig config) {
        this.gameColour = gameColour;
        this.geometry = geometry;
        sfxNoMove = new Sound("/nomove.wav");
        sfxKilled = new Sound("/killed.wav");
        sfxPutPieceInContainer = new Sound("/pieceputaway.wav");
        loadSounds(config.soundOn());
        whitePlayer = new Player(PlayerColor.WHITE,"Player1");
        blackPlayer = new Player(PlayerColor.BLACK,"Player2");
        currentPlayer = whitePlayer;
        spikes = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            spikes.add(new Spike(geometry, i));
        }
        die1 = new Die(geometry);
        die2 = new Die(geometry);
        theBarWHITE = new Spike(geometry, 25);
        theBarBLACK = new Spike(geometry, 25);
        makeColourObjects();
        initialiseBoard(INIT_CONFIGURATION);
        log("Board made");
    }

    public void loadSounds(boolean soundOn) {
        sfxNoMove.loadSound(soundOn);
        sfxKilled.loadSound(soundOn);
        sfxPutPieceInContainer.loadSound(soundOn);
    }

    public void makeColourObjects() {
        board_colour = new Color(gameColour.getBoardColor());
        bar_colour = new Color(gameColour.getBarColor());
        for (Spike spike: spikes) {
            spike.makeColourObjects();
        }
    }

    void initialiseBoard(int mode) {
        log("mode: BOARD_NEW_GAME");
        initialiseBoardForNewGame(whiteInitPositions, blackInitPositioin);
        if (mode == DEBUG_PIECES_IN_THEIR_HOME) {
            int[] whiteHome = {
                0, 5, 5, 5, 0, 0,
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0
            };
            int[] blackHome = {
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0,
                0, 2, 11, 2, 0, 0
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
        int whitePiecesOnBoard = 0;
        for (int i = 0; i < whiteInitPositions.length; ++i) {
            whitePiecesOnBoard += whiteInitPositions[i];
            for (int j = 0; j < whiteInitPositions[i]; ++j) {
                Piece newPiece = new Piece(geometry, whitePlayer);
                spikes.get(i).addPiece(newPiece);
            }
        }
        int blackPiecesOnBoard = 0;
        for (int i = 0; i < blackInitPositions.length; ++i) {
            blackPiecesOnBoard += blackInitPositions[i];
            for (int j = 0; j < blackInitPositions[i]; ++j) {
                Piece newPiece = new Piece(geometry, blackPlayer);
                spikes.get(i).addPiece(newPiece);
            }
        }
        for (int j = whitePiecesOnBoard; j < 15; ++j) {
            theBarWHITE.addPiece(new Piece(geometry, whitePlayer));
        }
        for (int j = blackPiecesOnBoard; j < 15; ++j) {
            theBarBLACK.addPiece(new Piece(geometry, blackPlayer));
        }
    }
    
    public void paint(Graphics g, int boardWidth, int boardHeight, boolean gameInProgress,
                      int mouseX, int mouseY) {
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
           spike.paint(g, this);
        }

        paintDice(g, boardWidth, boardHeight);

        // draw the potential moves for whoevers go it is
        if (gameInProgress && !showRollButton()) {
            checkConsistent();
            if (haveToMovePieceFromBar(currentPlayer.getColour())) {
                pulsatePotentialSpikesFromBar();
            } else {
                pulsatePotentialSpikes(mouseX, mouseY);
            }
            if (!turnOver()) {
                calculatePotentialMoves();
            }
        }
    }

    private void paintDice(Graphics g, int WIDTH, int HEIGHT) {
        if (!showRollButton()) {
            int diex = (geometry.borderWidth() + ((WIDTH/4)*3)) + geometry.dieSize();
            int diey = (geometry.borderWidth() + (HEIGHT/2)) - geometry.dieSize();
            if (die1.enabled()) {
                die1.paint(g, diex, diey);
            }

            diex += geometry.dieSize() + geometry.tinyGap();
            if (die2.enabled()) {
                die2.paint(g, diex, diey);
            }
        }
    }

    private void pulsatePotentialSpikesFromBar() {
        ArrayList<Spike> spikesAllowedToMoveToFromBar = new ArrayList<>();
        boolean cantGetOfBarWithDie1 = !canWeGetOffTheBarWithThisDie(die1, DieType.DIE1, spikesAllowedToMoveToFromBar);
        if (!cantGetOfBarWithDie1) {
            getOffTheBarOptions(spikesAllowedToMoveToFromBar);
        } else {
            log("NO OPTIONS FOR GETTING OFF BAR WITH DIE1");
        }
        boolean cantGetOfBarWithDie2 = !canWeGetOffTheBarWithThisDie(die2, DieType.DIE2, spikesAllowedToMoveToFromBar);
        if (!cantGetOfBarWithDie2) {
            getOffTheBarOptions(spikesAllowedToMoveToFromBar);
        } else {
            log("NO OPTIONS FOR GETTING OFF BAR WITH DIE2");
        }

        if (cantGetOfBarWithDie1 && cantGetOfBarWithDie2) {
            log("NO OPTIONS FROM BAR NEXT TURN!!!!!!!!!!!!!!");
            die1.noOptions();
            die2.noOptions();
        }
    }

    private void getOffTheBarOptions(ArrayList<Spike> spikesAllowedToMoveToFromBar) {
        Spike destinationSpike = spikesAllowedToMoveToFromBar.get(0);
        Spike theBarPieces = whoseTurnIsIt() == PlayerColor.WHITE ? theBarWHITE : theBarBLACK;
        log("White pieces on bar potential pick ups: " + theBarPieces.pieces.size());
        if (pieceStuckToMouse() != null) {
            log("Destination PLACE ON AVAIL SPIKE:" + destinationSpike.getSpikeNumber());
            Point spikeMiddle = destinationSpike.getMiddlePoint();
            setBotDestination(spikeMiddle.x, spikeMiddle.y, "PLACE FROM BAR ONTO AVAIL SPIKES");
        } else {
            log("DESTINATION FOR BOT, PIECE ON BAR......");
            Piece p = (Piece) theBarPieces.pieces.firstElement();
            setBotDestination(p.getCenterX(), p.getCenterY(),"DESTINATION FOR BOT, PIECE ON BAR......");
        }
    }

    // given the die this method will add spikes to spikesAllowedToMoveToFromBar
    // for current player so that the spikes available will flash and be ready to have a piece added to them
    private boolean canWeGetOffTheBarWithThisDie(Die die, DieType whichDie, ArrayList<Spike> spikesAllowedToMoveToFromBar) {
        Spike barSpike = currentPlayer.isWhite() ? theBarWHITE : theBarBLACK;
        if (barSpike.isEmpty()) {
            return false;
        }
        int destinationSpikeId = whoseTurnIsIt() == PlayerColor.BLACK ? die.getValue() - 1 : 24 - die.getValue();
        if (destinationSpikeId >= 0 && destinationSpikeId < spikes.size()) {
            Spike destinationSpike = spikes.get(destinationSpikeId);
            if (destinationSpike.pieces.size() <= 1 ||
                doesThisSpikeBelongToPlayer(destinationSpike, whoseTurnIsIt())) {
                destinationSpike.flash(whichDie);
                if (spikesAllowedToMoveToFromBar != null && !spikesAllowedToMoveToFromBar.contains(destinationSpike)) {
                    destinationSpike.store_this_die(die);
                    spikesAllowedToMoveToFromBar.add(destinationSpike);
                }
                return true;
            }
        }
        return false;
    }

    public void RESET_ENTIRE_GAME_VARS(boolean soundOn) {
        loadSounds(soundOn);
        currentPlayer = whitePlayer;
        theBarBLACK.pieces.clear();
        theBarWHITE.pieces.clear();
        whitePiecesSafelyInContainer.clear();
        blackPiecesSafelyInContainer.clear();
        SPtheMoveToMake = null;
        diesRolled = false;
        if (die1.enabled()) {
            die1.noOptions();
        }
        if (die2.enabled()) {
            die2.noOptions();
        }
        initialiseBoard(INIT_CONFIGURATION);
    }

    // pulsates the spikes while piece is stuck to mouse or hovering over some spike
    private void pulsatePotentialSpikes(int mouseX, int mouseY) {
        int sourceSpikeId;
        Piece pieceStuckToMouse = pieceStuckToMouse();
        if (pieceStuckToMouse != null) {
            sourceSpikeId = pieceStuckToMouse.sourceSpikeId();
        } else {
            Spike activeSpike = grabSpikeHoveringOver(mouseX, mouseY);
            if (activeSpike != null) {
                sourceSpikeId = activeSpike.getSpikeNumber();
            } else {
                sourceSpikeId = -2;
            }
        }
        if (sourceSpikeId == -1) {
            ArrayList<Spike> possibleSpikes = this.spikesToMoveToFromBar(whoseTurnIsIt());
            for (Spike spike: possibleSpikes) {
                int neededValue = currentPlayer.isWhite() ? spike.getPosition() : 24 - spike.getPosition();
                spike.flash(neededValue == die1.getValue() ? DieType.DIE1 : DieType.DIE2);
            }
        } else if (sourceSpikeId >= 0){
            ArrayList<Integer> possibleSpikes = reachableSpikes(spikes.get(sourceSpikeId), currentPlayer, die1, die2);
            for (Integer spikeId: possibleSpikes) {
                int neededValue = Math.abs(sourceSpikeId - spikeId);
                DieType dieType = DieType.DIE1;
                if (neededValue == die1.getValue()) {
                    dieType = DieType.DIE1;
                } else if (neededValue == die2.getValue()) {
                    dieType = DieType.DIE2;
                } else if (neededValue == die1.getValue() + die2.getValue()) {
                    dieType = DieType.DIE1AND2;
                }
                if (spikeId != currentPlayer.containerId()) {
                    spikes.get(spikeId).flash(dieType);
                }
            }
        }
    }

    void checkConsistent() {
        final int whitePieces = calculateAmountOfPiecesOnBoard(PlayerColor.WHITE) +
            whitePiecesSafelyInContainer.size() + theBarWHITE.pieces.size();
        if (whitePieces != 15) {
            throw new RuntimeException("PIECES NOT EQUAL TO 15 FOR WHITE its " + whitePieces);
        }
        final int blackPieces = calculateAmountOfPiecesOnBoard(PlayerColor.BLACK) +
            blackPiecesSafelyInContainer.size() + theBarBLACK.pieces.size();
        if (blackPieces != 15) {
            throw new RuntimeException("PIECES NOT EQUAL TO 15 FOR BLACK its " + blackPieces);
        }
    }

    /**
     * Returns list of ids of reachable spikes from given spike for given player.
     * If current spike does not belongs to the player an empty list is returned.
     * @param currentSpike the spike to calculate reachable spikes from
     * @param player the player to check.
     * @param die1 die1
     * @param die2 die2
     * @return The list of spike ids (0 to 23 for normal spikes, -1 or 24 for container ids)
     */
    private ArrayList<Integer> reachableSpikes(Spike currentSpike, Player player, Die die1, Die die2) {
        ArrayList<Integer> result = new ArrayList<>();
        if (currentSpike.getAmountOfPieces(player.getColour()) > 0) {
            boolean die1AnOption = die1.enabled() && checkValidPotentialMove(currentSpike, die1.getValue());
            if (die1AnOption) {
                result.add(player.getDestinationSpikeId(currentSpike, die1.getValue()));
            }

            boolean die2AnOption = die2.enabled() && checkValidPotentialMove(currentSpike, die2.getValue());
            if (die2AnOption) {
                Integer die2Destination = player.getDestinationSpikeId(currentSpike, die2.getValue());
                if (die2Destination == player.containerId()) {
                    if (die2.getValue() == die1.getValue() || result.isEmpty()) {
                            result.add(die2Destination);
                    }
                } else {
                    result.add(die2Destination);
                }
            }

            boolean die1and2AnOption = die1.enabled() && die2.enabled() && (die1AnOption || die2AnOption)
                && !result.contains(player.containerId()) &&
                checkValidPotentialMove(currentSpike, die1.getValue() + die2.getValue());
            if (die1and2AnOption) {
                result.add(player.getDestinationSpikeId(currentSpike, die1.getValue() + die2.getValue()));
            }
        }
        return result;
    }

    private int calculateAmountOfPiecesOnBoard(PlayerColor player) {
        int piecesOnBoard = 0;
        for (Spike spike: spikes) {
            piecesOnBoard += spike.getAmountOfPieces(player);
        }
        return piecesOnBoard;
    }

    private int calculateAmountOfPiecesInHomeArea(Player player) {
        int piecesInHomeArea = 0;
        for (Spike spike: spikes) {
            if (!spike.pieces.isEmpty() &&
                spike.getSpikeNumber() >= player.homeSpikeStart() && spike.getSpikeNumber() <= player.homeSpikeEnd()) {
                Piece piece = (Piece) spike.pieces.firstElement();
                if (piece.getColour() == player.getColour()) {
                    piecesInHomeArea += spike.pieces.size();
                }
            }
        }
        return piecesInHomeArea;
    }

    private void pulsatePotentialSpike(Spike spike, DieType whichDice) {
        //this makes the spike colour pulse nicely to indicate its an option
        spike.flash(whichDice);
    }

    /**
     * Checks if current player can make a valid move from given spike using given die.
     * @param currentSpike Spike under consideration.
     * @param dieRoll value of the die.
     * @return true if the  move is possible, false otherwise
     */
    private boolean checkValidPotentialMove(Spike currentSpike, int dieRoll) {
        int potentialSpike = currentPlayer.getDestinationSpike(currentSpike, dieRoll);
        if (potentialSpike < whitePlayer.containerId() && lastNotEmptyWhiteSpike().getPosition() == currentSpike.getPosition()) {
            potentialSpike = whitePlayer.containerId();
        }
        if (potentialSpike > blackPlayer.containerId() && lastNotEmptyBlackSpike().getPosition() == currentSpike.getPosition()) {
            potentialSpike = blackPlayer.containerId();
        }
        if (potentialSpike == currentPlayer.containerId() && allPiecesAreHome(currentPlayer)) {
            return true;
        }
        return (potentialSpike >= FIRST_SPIKE) && (potentialSpike <= LAST_SPIKE) &&
            (isThisSpikeEmpty(spikes.get(potentialSpike)) ||
                doesThisSpikeBelongToPlayer(spikes.get(potentialSpike), whoseTurnIsIt()));
    }

    private Spike doesThisSpikeBelongToPlayer(int mouseHoverX, int mouseHoverY) {
        Spike hoverSpike = grabSpikeHoveringOver(mouseHoverX, mouseHoverY);
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

    private Spike grabSpikeHoveringOver(int mouseHoverX, int mouseHoverY) {
       for (Spike currentSpike: spikes) {
           if (currentSpike.userClickedOnThis(mouseHoverX, mouseHoverY)) {
               return currentSpike;
           }
        }
        return null;
    }

    // ALSO RETURNS TRUE IF THERE IS ONLY ONE ENEMY PIECE ON THE SPIKE
    private boolean isThisSpikeEmpty(Spike checkme) {
        return  checkme.isBar() ? checkme.pieces.isEmpty() : checkme.pieces.size() <= 1;
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

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    private static void log(String s) {
        Utils.log(String.format("thread-%s Board{}:%s", Thread.currentThread().getName(), s));
    }

    void calculatePotentialMoves() {
        if (pieceStuckToMouse() == null && SPtheMoveToMake == null) {
            log("_______________________RECALCULATE MOVES die1:" + die1.disabled() + " die2:" + die2.disabled());
            Vector spikePairs = getValidOptions();
            log("Potential moves: " + spikePairs.size());
            if (spikePairs.size() > 0) {
                String botOptions = "";
                Enumeration ee = spikePairs.elements();
                while (ee.hasMoreElements()) {
                    SpikePair sp = (SpikePair) ee.nextElement();
                    botOptions += sp.pickMyPiece.getName() + "->" + sp.dropPiecesOnMe.getName() + " ";
                }
                log("valid options: " + botOptions);
                SPtheMoveToMake = (SpikePair) spikePairs.elementAt(Utils.getRand(0, spikePairs.size() - 1));
                if (SPtheMoveToMake.dropPiecesOnMe.isContainer()) {
                    log("SPECIAL CASE randomly chose to go to spike:" +
                        SPtheMoveToMake.pickMyPiece.getName() + " and drop off at CONTAINER");
                    CustomCanvas.tellRobot(true, "->" + SPtheMoveToMake.pickMyPiece.getName() + "->Container");
                    Spike takeMyPiece = SPtheMoveToMake.pickMyPiece;
                    Piece firstPiece = ((Piece) takeMyPiece.pieces.firstElement());
                    setBotDestination(firstPiece.getCenterX(), firstPiece.getCenterY(), "TAKE A PIECE TO CONTAINER");
                } else {
                    log("-randomly chose to go to spike:" +
                        SPtheMoveToMake.pickMyPiece + " and drop off at spike:" +
                        SPtheMoveToMake.dropPiecesOnMe);
                    CustomCanvas.tellRobot(true, "->" + SPtheMoveToMake.pickMyPiece +
                        "->" + SPtheMoveToMake.dropPiecesOnMe);
                    Spike takeMyPiece = SPtheMoveToMake.pickMyPiece;
                    Piece firstPiece = ((Piece) takeMyPiece.pieces.firstElement());
                    int goToX = firstPiece.getCenterX();
                    int goToY = firstPiece.getCenterY();
                    setBotDestination(goToX, goToY, "RANDOMLY CHOOSE A PIECE");
                    log("***************PIECE IM LOOKING FOR IS AT: " + goToX + "," + goToY);
                }
            }
        } else if (pieceStuckToMouse() != null) {
            // Point robot the final destination after it took the piece
            theyWantToPlaceAPiece();
        }
    }

    /**
     * Checks the currently available die and populates spikePairs vector with
     * valid (sourceSpike, destinationSpike) pairs. Does not take bar spike into account.
     * Bar spike is handled separately before, so bot destination should be set at this point.
     */
    private Vector getValidOptions() {
        log("Getting valid spike pairs");
        Vector spikePairs = new Vector(5);
        int diceRoll = -1;
        Die die = die1;
        if (die1.enabled()) {
            diceRoll = die1.getValue();
            log("using DIE1 value " + diceRoll);
        } else if (die2.enabled()) {
            diceRoll = die2.getValue();
            die = die2;
            log("using DIE2 value " + diceRoll);
        }
        if (diceRoll > 0) {
            for (Spike spike : spikes) {
                if (spike.getAmountOfPieces(currentPlayer.getColour()) > 0) {
                    Integer potentialSpike = currentPlayer.getDestinationSpikeId(spike, diceRoll);
                    ArrayList<Integer> reachableSpikes = reachableSpikes(spike, currentPlayer, die1, die2);
                    if (reachableSpikes.contains(potentialSpike)) {
                        Spike destinationSpike = potentialSpike == currentPlayer.containerId() ?
                            new Spike(geometry, -1) : spikes.get(potentialSpike);
                        spikePairs.add(new SpikePair(spike, destinationSpike));
                    }
                }
            }
            if (spikePairs.isEmpty()) {
                if (!haveToMovePieceFromBar(currentPlayer.getColour())) {
                    die.noOptions();
                    sfxNoMove.playSound();
                }
            }
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
        Piece pieceStuckToMouse = pieceStuckToMouse();
        if (pieceStuckToMouse != null && pieceStuckToMouse.sourceSpikeId() < 0) {
            log("dont do anythign til we place this");
            return;
        }
        if (SPtheMoveToMake == null) {
            log("DOUBLE RECALC.>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            return;
        }
        if (!Bot.getFullAutoPlay() && whoseTurnIsIt() == PlayerColor.WHITE) {
            return;
        }
        Spike dropOnMe = SPtheMoveToMake.dropPiecesOnMe;
        if (dropOnMe != null) {
            if (dropOnMe.isContainer()) {
                int pieceContainerX = geometry.containerX();
                int pieceContainerY = (whoseTurnIsIt() == PlayerColor.WHITE) ? geometry.whiteContainerY() :
                    geometry.blackContainerY();
                setBotDestination(pieceContainerX + geometry.containerWidth() / 2,
                    pieceContainerY + geometry.containerHeight() / 2,"PIECE CONTAINER DESTINATION");
            } else {
                Point middlePoint = dropOnMe.getMiddlePoint();
                setBotDestination(middlePoint.x, middlePoint.y, "NORMAL CASE DROP ON SPIKE A");
            }
        }
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player player) {
        currentPlayer = player;
    }

    public void nextTurn() {
        if (currentPlayer.getColour() == PlayerColor.WHITE) {
            currentPlayer = blackPlayer;
            log("BLACKS TURN");
        } else {
            currentPlayer = whitePlayer;
            log("WHITES TURN");
        }
        assert die1.disabled() && die2.disabled();
        diesRolled = false;
        SPtheMoveToMake = null;
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
        if (die1.getValue() == die2.getValue()) {
            die1.doubleRoll();
            die2.doubleRoll();
        }
        diesRolled = true;
    }

    public boolean rolledDouble() {
        return die1.enabled() && die2.enabled() && (die1.getValue() == die2.getValue());
    }
    
    public PlayerColor whoseTurnIsIt() {
        return currentPlayer.getColour();
    }

    public void drawBlackPieceContainer(Graphics g, int mouseX, int mouseY) {
        boolean allPiecesAreHome = allPiecesAreHome(blackPlayer);
        boolean pulsateBlackContainer = allPiecesAreHome && pulsateContainer(blackPlayer, activeSpikeId(mouseX, mouseY))
            && !currentPlayer.isWhite();
        drawPieceContainer(g, geometry.blackContainerY(), pulsateBlackContainer, allPiecesAreHome,
            blackPiecesSafelyInContainer.size());
    }

    public void drawWhitePieceContainer(Graphics g, int mouseX, int mouseY) {
        boolean allPiecesAreHome = allPiecesAreHome(whitePlayer);
        boolean pulsateWhiteContainer = allPiecesAreHome && pulsateContainer(whitePlayer, activeSpikeId(mouseX, mouseY)) &&
            currentPlayer.isWhite();
        drawPieceContainer(g, geometry.whiteContainerY(), pulsateWhiteContainer, allPiecesAreHome,
            whitePiecesSafelyInContainer.size());
    }

    private void drawPieceContainer(Graphics g, int topY, boolean pulsateContainer, boolean allPiecesAreHome,
                                    int piecesOnContainer) {
        if (allPiecesAreHome) {
            utils.setColor(g, Color.GREEN);
            if (pulsateContainer) {
                utils.setColor(g, Color.YELLOW);
            }
        } else {
            utils.setColor(g, Color.WHITE);
        }
        final int myX = geometry.containerX();
        int myY = topY;
        for (int i = 0; i < 15; i++) {
            myY += geometry.containerSubSize();
            if (i < piecesOnContainer) {
                Color originalColor = utils.getColor();
                utils.setColor(g, Color.ORANGE);
                utils.fillRect(g, myX, myY, geometry.containerWidth(), geometry.containerSubSize());
                utils.setColor(g, originalColor);
            }
            utils.drawRect(g, myX, myY, geometry.containerWidth(), geometry.containerSubSize());
        }
    }

    private boolean allPiecesAreHome(Player player) {
        int inContainer = player.isWhite() ?  whitePiecesSafelyInContainer.size() :
            blackPiecesSafelyInContainer.size();
        return calculateAmountOfPiecesInHomeArea(player) +  inContainer == 15;
    }

    private Spike lastNotEmptyWhiteSpike() {
        for (int i = 23; i >= 0; --i) {
            if (spikes.get(i).getAmountOfPieces(PlayerColor.WHITE) > 0)
                return spikes.get(i);
        }
        return null;
    }

    private Spike lastNotEmptyBlackSpike() {
        for (Spike spike: spikes) {
            if (spike.getAmountOfPieces(PlayerColor.BLACK) > 0)
                return spike;
        }
        return null;
    }

    private Spike lastNotEmptySpike() {
        return currentPlayer.isWhite() ? lastNotEmptyWhiteSpike() : lastNotEmptyBlackSpike();
    }

    /**
     * Calculates which dies get current piece to container, assuming that such die combination exist
     * @param player player
     * @param sourceSpikeId source spike id from where piece was picked
     * @return Die type
     * @throws Exception if source spike does not belong to player of container cannot be reached from it.
     */
    DieType whichDieGetsUsToPieceContainer(Player player, int sourceSpikeId) throws Exception {
        int exactValue = player.isWhite() ? sourceSpikeId - player.containerId() : player.containerId() - sourceSpikeId;
        int maxDieValue = Math.max(die1.getValue(), die2.getValue());
        Spike sourceSpike = spikes.get(sourceSpikeId);
        if (sourceSpike.getAmountOfPieces(player.getColour()) == 0) {
            throw new Exception(String.format("Source spike does not belongs to %s player", player.getColour()));
        }

        ArrayList<Integer> reachableFromSource = reachableSpikes(sourceSpike, player, die1, die2);
        if (!reachableFromSource.contains(player.containerId())) {
            throw new  Exception(String.format("Container cannot be reached from spike %s die1: %d, die2: %d",
                sourceSpike.getName(), die1.getValue(), die2.getValue()));
        }
        if (die1.getValue() == exactValue)
            return DieType.DIE1;
        if (die2.getValue() == exactValue)
            return DieType.DIE2;
        if (maxDieValue > exactValue) {
            return die1.getValue() >= die2.getValue() ? DieType.DIE1 : DieType.DIE2;
        }
        return DieType.DIE1AND2;
    }

    ArrayList<Spike> spikesToMoveToFromBar(PlayerColor playerColor) {
        ArrayList<Spike> result = new ArrayList<>();
        canWeGetOffTheBarWithThisDie(die1, DieType.DIE1, result);
        canWeGetOffTheBarWithThisDie(die2, DieType.DIE2, result);
        return result;
    }

    private boolean haveToMovePieceFromBar(PlayerColor playerColor) {
        return (playerColor == PlayerColor.WHITE && theBarWHITE.pieces.size() > 0) ||
        (playerColor == PlayerColor.BLACK && theBarBLACK.pieces.size() > 0);
    }

    boolean pulsateContainer(Player player, int sourceSpikeId) {
        return sourceSpikeId >= 0 &&
            reachableSpikes(spikes.get(sourceSpikeId), player, die1, die2).contains(player.containerId());
    }

    public void drawBarPieces(Graphics g) {
        int pieceOnBarY = (geometry.boardHeight() / 2) - geometry.pieceDiameter();
        Enumeration eW = theBarWHITE.pieces.elements();
        while (eW.hasMoreElements()) {
            Piece p = (Piece) eW.nextElement();
            if (!p.stickToMouse()) {
                p.paint(g,
                    (geometry.boardWidth() / 2) - geometry.pieceDiameter() / 2,
                    pieceOnBarY -= geometry.pieceDiameter());
            }
        }
        pieceOnBarY = (geometry.boardHeight() / 2);
        Enumeration eB = theBarBLACK.pieces.elements();
        while (eB.hasMoreElements()) {
            Piece p = (Piece) eB.nextElement();
            if (!p.stickToMouse()) {
                p.paint(g,
                    (geometry.boardWidth() / 2) - geometry.pieceDiameter() / 2,
                    pieceOnBarY += geometry.pieceDiameter());
            }
        }
    }

    public void drawPieceStuckToMouse(Graphics g, int mouseX, int mouseY) {
        Piece piece = pieceStuckToMouse();
        if (piece != null) {
            piece.drawPieceOnMouse(g, mouseX, mouseY);
        }
    }

    /**
     * Returns id of currently active spike.
     * @return Either id of the spike the piece is dragged from or id of the spike the mouse points on.
     */
    private int activeSpikeId(int mouseX, int mouseY) {
        int spikeId = -1;
        Piece pieceStuckToMouse = pieceStuckToMouse();
        if (pieceStuckToMouse != null)
            spikeId = pieceStuckToMouse.sourceSpikeId();
        else {
            Spike spike = grabSpikeHoveringOver(mouseX, mouseY);
            if (spike != null && spike.getAmountOfPieces(whoseTurnIsIt()) > 0) {
                spikeId = spike.getSpikeNumber();
            }
        }
        return spikeId;
    }

    public void checkIfPieceContainerClickedOn(int x, int y) throws Exception {
        final int myX = geometry.containerX();
        final int myY = currentPlayer.isWhite() ? geometry.whiteContainerY() : geometry.blackContainerY();
        if (x >= myX && x < (myX + geometry.containerWidth())) {
            if (y > myY && y < (myY + geometry.containerHeight())) {
                log(String.format("%s CONTAINER CLICKED ON", whoseTurnIsIt()));
                Piece pieceStuckToMouse = pieceStuckToMouse();
                if (pieceStuckToMouse != null && pieceStuckToMouse.sourceSpikeId() >= 0) {
                    DieType correctDie = whichDieGetsUsToPieceContainer(currentPlayer,
                        pieceStuckToMouse.sourceSpikeId());
                    if (pulsateContainer(getCurrentPlayer(), pieceStuckToMouse.sourceSpikeId())) {
                        log(String.format("%s put in container", whoseTurnIsIt()));
                        placePieceRemoveOldOneAndSetDieToUsed(currentPlayer.containerId(), correctDie);
                    }
                }
            }
        }
    }

    public void checkIfSpikeClickedOn(int x, int y) {
        Piece pieceStuckToMouse = pieceStuckToMouse();
        for (Spike spike: spikes) {
            if (spike.userClickedOnThis(x, y)) {
                log("Spike was clicked on (" + spike.getSpikeNumber() + ")");
                if (pieceStuckToMouse != null) {
                    if (pieceStuckToMouse.sourceSpikeId() < 0) {
                        ArrayList<Spike> spikesAllowedToMoveToFromBar = spikesToMoveToFromBar(whoseTurnIsIt());
                        log("barPieceStuckOnouse spikesAllowedToMoveToFromBar.size()" + spikesAllowedToMoveToFromBar.size());
                        for (Spike sp : spikesAllowedToMoveToFromBar) {
                            if (spike.getSpikeNumber() == sp.getSpikeNumber()) {
                                log("WE CAN DROP OFF AT THIS SPIKE " + sp.getSpikeNumber());
                                if (whoseTurnIsIt() == PlayerColor.WHITE) {
                                    log("WHITE PIECE REMOVED FROM BAR");
                                    theBarWHITE.removePiece(pieceStuckToMouse);
                                    // IF this spike contains an enemy piece Kill it
                                    if (sp.getAmountOfPieces(PlayerColor.BLACK) == 1) {
                                        log("WHITE KILLED A BLACK WHILE GETTING OFF BAR");
                                        Piece piece = (Piece) sp.pieces.firstElement();
                                        theBarBLACK.addPiece(piece);
                                        sp.removePiece(piece);
                                        sfxKilled.playSound();
                                    }
                                }
                                if (whoseTurnIsIt() == PlayerColor.BLACK) {
                                    log("BLACK PIECE REMOVED FROM BAR");
                                    theBarBLACK.removePiece(pieceStuckToMouse);
                                    if (sp.getAmountOfPieces(PlayerColor.WHITE) == 1) {
                                        Piece piece = (Piece) sp.pieces.firstElement();
                                        theBarWHITE.addPiece(piece);
                                        log("BLACK KILLED A WHITE WHILE GETTING OFF BAR");
                                        sp.removePiece((Piece) sp.pieces.firstElement());
                                        sfxKilled.playSound();
                                    }
                                }
                                sp.addPiece(pieceStuckToMouse);
                                unstickPieceFromMouse();
                                Die theDieThatGotUsHere = sp.get_stored_die();
                                if (theDieThatGotUsHere.getValue() == die1.getValue()) {
                                    log("DIE1 USED GETTING OFF BAR " + die1.getValue());
                                    die1.disable();
                                } else {
                                    log("DIE2 USED GETTING OFF BAR " + die2.getValue());
                                    die2.disable();
                                }
                                return;
                            }
                        }
                    } else {
                        Spike sourceSpike = spikes.get(pieceStuckToMouse.sourceSpikeId());
                        ArrayList<Integer> reachable = reachableSpikes(sourceSpike, currentPlayer, die1, die2);
                        Integer idx = reachable.indexOf(spike.getSpikeNumber());
                        if (idx >= 0) {
                            int neededValue = Math.abs(spike.getPosition() - sourceSpike.getPosition());
                            assert neededValue > 0;
                            DieType dieType = DieType.DIE1AND2;
                            if (neededValue == die1.getValue())
                                dieType = DieType.DIE1;
                            else if (neededValue == die2.getValue())
                                dieType = DieType.DIE2;
                            else
                                assert neededValue == die1.getValue() + die2.getValue();
                            placePieceRemoveOldOneAndSetDieToUsed(reachable.get(idx), dieType);
                        }
                    }
                }
            }
        }
    }

    private Piece pieceStuckToMouse() {
        for (Spike spike: spikes) {
            for (Object elem: spike.pieces) {
                Piece piece = (Piece) elem;
                if (piece.stickToMouse())
                    return piece;
            }
        }
        for (Object elem: theBarWHITE.pieces) {
            Piece piece = (Piece) elem;
            if (piece.stickToMouse())
                return piece;
        }
        for (Object elem: theBarBLACK.pieces) {
            Piece piece = (Piece) elem;
            if (piece.stickToMouse())
                return piece;
        }
        return null;
    }

    public void unstickPieceFromMouse() {
        Piece pieceStuckToMouse = pieceStuckToMouse();
        if (pieceStuckToMouse != null) {
            pieceStuckToMouse.unstickFromMouse();
        }
        SPtheMoveToMake = null;
    }

    // removes piece from the spike it came from, adds it to the new one just clicked on,
    // and sets the die that did this to used
    private void placePieceRemoveOldOneAndSetDieToUsed(int destinationSpikeId, DieType dieToSetUnused) {
        log("placePieceRemoveOldOneAndSetDieToUsed dieToSetUnused:" + dieToSetUnused +
            " destinationSpikeId: " + destinationSpikeId);
        Piece pieceStuckToMouse = pieceStuckToMouse();
        assert pieceStuckToMouse != null;
        if (pieceStuckToMouse.sourceSpikeId() >= 0)
            spikes.get(pieceStuckToMouse.sourceSpikeId()).removePiece(pieceStuckToMouse);

        if (destinationSpikeId == currentPlayer.containerId()) {
            Vector container = (whoseTurnIsIt() == PlayerColor.WHITE) ? whitePiecesSafelyInContainer :
            blackPiecesSafelyInContainer;
            container.add(pieceStuckToMouse);
            log(String.format("%s Container HAS HAD ONE ADDED TO IT, NEW SIZE: %d", whoseTurnIsIt(), container.size()));
            sfxPutPieceInContainer.playSound();
        } else {
            Spike destinationSpike = spikes.get(destinationSpikeId);
            PlayerColor thisColor = currentPlayer.getColour();
            PlayerColor otherColor = currentPlayer.isWhite() ? PlayerColor.BLACK : PlayerColor.WHITE;
            Spike piecesOnBar = currentPlayer.isWhite() ? theBarBLACK : theBarWHITE;
            if (destinationSpike.getAmountOfPieces(otherColor) > 0) {
                log(String.format("%s KILLED A %s", thisColor, otherColor));
                Piece firstPiece = (Piece) destinationSpike.pieces.firstElement();
                destinationSpike.removePiece(firstPiece);
                destinationSpike.addPiece(pieceStuckToMouse);
                piecesOnBar.addPiece(firstPiece);
                sfxKilled.playSound();
            } else {
                destinationSpike.addPiece(pieceStuckToMouse);
            }
        }
        if (dieToSetUnused == DieType.DIE1) {
            die1.disable();
            log("die1HasBeenUsed A.");
        } else if (dieToSetUnused == DieType.DIE2) {
            die2.disable();
            log("die2HasBeenUsed A.");
        } else {
            die1.disable();
            die2.disable();
            log("Both dies have been used");
        }
        unstickPieceFromMouse();
    }

    public void checkIfPieceClickedOn(int x,int y) {
        if (pieceStuckToMouse() != null) {
            return;
        }
        Spike piecesOnTheBar = (whoseTurnIsIt() == PlayerColor.WHITE) ? theBarWHITE : theBarBLACK;
        Enumeration e = piecesOnTheBar.pieces.elements();
        ArrayList<Spike> possibleDestinationsFromBar = new ArrayList<>();
        if (piecesOnTheBar.pieces.size() > 0)
            possibleDestinationsFromBar = spikesToMoveToFromBar(whoseTurnIsIt());
        while (!possibleDestinationsFromBar.isEmpty() && e.hasMoreElements()) {
            Piece p = (Piece) e.nextElement();
            if (p.userClickedOnThis(x, y)) {
                log("PIECE ON THE BAR CLICKED ON.");
                p.stickToMouse(-1);
                return;
            }
        }

        for (Spike spike: spikes) {
            Enumeration pieces_e = spike.pieces.elements();
            while (pieces_e.hasMoreElements()) {
                Piece piece = (Piece) pieces_e.nextElement();
                if (piece.userClickedOnThis(x, y)) {
                    if (allowPieceToStickToMouse(spike) && piece.getColour() == whoseTurnIsIt()) {
                        log("PICKED UP PIECE: " + piece.getColour());
                        piece.stickToMouse(spike.getSpikeNumber());
                    }
                    log("Piece was clicked on (" + piece + ") board.allowPieceToStickToMouse: true " +
                        "board.whoseTurnIsIt:" + whoseTurnIsIt());
                    return;
                }
            }
        }
    }

    public boolean gameIsOver() {
        return whitePiecesSafelyInContainer.size() == 15 || blackPiecesSafelyInContainer.size() == 15;
    }

    public boolean turnOver() {
        return diesRolled && die1.disabled() && die2.disabled();
    }

    private boolean allowPieceToStickToMouse(Spike activeSpike) {
        return !reachableSpikes(activeSpike, currentPlayer, die1, die2).isEmpty();
    }

    public boolean showRollButton() {
        return die1.disabled() && die2.disabled();
    }
}
