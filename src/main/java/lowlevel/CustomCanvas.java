package lowlevel;

import java.awt.*;
import java.awt.event.*;

import data.GuiState;
import data.PlayerColor;
import gamelogic.*;
import graphics.GameColour;
import graphics.Geometry;
import utils.MouseClickAndMoveListener;

import java.awt.image.BufferStrategy;
import java.awt.image.MemoryImageSource;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JFrame;

import static data.GuiState.*;
import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON3;

/** This class is used basically for calling the right paint methods
 *  based on state, these paint due to this class being a subclass of canvas.
 */
public class CustomCanvas extends Canvas implements MouseClickAndMoveListener, KeyListener {

    private final int maxSplashCounter;
    private final boolean drawMousePointer;
    private final boolean enableDoubleBuffering;

    private static final String SERVER_IP_ADDRESS = "localhost";
    private static final String SPECIAL_END_SYMBOL = "::"; // this signifiys to scroll bar the end is reached whislt being invisible to our customfont
    // breaks down wrapMe into a vector and prints each line after each other making sure that the text wraps
    // properly.

    private boolean HUMAN_VS_COMPUTER = false; // human is white, computer is black
    private boolean I_AM_CLIENT = false;
    private boolean I_AM_SERVER = false;

    public static final String VERSION = "v0.0.1";
    private static final boolean RELEASE_BUILD = false;

    private GameColour gameColour;
    private Geometry geometry;
    private JFrame mainWindow;
    private Board board;

    private boolean soundOn;
    private int splashCounter;
    private int infoCounter;
    private boolean gameComplete;

    // Debug
    public static boolean showBoundaryBoxes = false;
    private static boolean PAINT_STATE = false;
    private static final String DEBUG_HEADER = "Midokura Backgammon game (DEBUG MODE):";

    // -- constants
    private Color panel_colour, background_colour, roll_button_colour;

    private CustomFont fontwhite, fontblack;
    private boolean INFO = false;    // 'about box' toggle
    private Utils utils = new Utils();   // Hardware Abstraction Layer
    private GuiState state = GuiState.SPLASH_SCREEN;
    private int matchPoints;

    // this is always the current x and y vals of the mouse pointer
    private int mouseHoverX, mouseHoverY;

    private boolean NETWORK_GAME_IN_PROCESS;
    private static Sound sfxError = new Sound("/error.wav");
    private Sound sfxDiceRoll, sfxDoubleRolled, sfxGameOver;
    private Sound sfxdouble, sfxResign;
    private static Image splashScreenLogo, splashScreenLogoSmall, op, admin, pointer;
    private static Cursor transparentCursor;

    private static String robotMoveDesc = "Bot loaded.";
    private int paraYoffset;
    private Vector playerPositions; // Positions of players in network lobby

    // Garbage
    private final static int OUTLINE_FOR_CHAT_BOXES = 0;

    //prefs button x,y width and height
    private static final int prefw = 20;
    private static final int prefh = 20;
    private static long FIFTY_SECONDS = 50000L;
    private static long TEN_SECONDS = 10000L;
    private static long robotMessageSetTimeLong;
    private static int TRANSPARENCY_LEVEL = 100;

    // use these to detect if the roll button was clicked.
    private int rollButtonX;
    private int rollButtonY;
    private int rollButtonW;
    private int rollButtonH;

    static int D1lastDieRoll_toSendOverNetwork;
    static int D2lastDieRoll_toSendOverNetwork;

    //for glowy buttons
    private static final int GLOW_INCREMENTER = 15;
    private boolean glowA, glowB;

    private int doubleX;
    private int doubleY;
    private int doubleWidth;
    private int doubleHeight;

    private int resignX;
    private int resignY;
    private int resignWidth;
    private int resignHeight;

    private boolean whiteResigned;
    private boolean blackResigned;

    private static boolean DEBUG_CONSOLE = false;
    private boolean PAUSED;
    private NetworkChatClient chatClient;
    static String chatText = "";

    static final int LEFT_MOUSE_BUTTON = BUTTON1;
    static final int RIGHT_MOUSE_BUTTON = BUTTON3;

    private boolean showChallengeWindow;
    private String personToChallenge;

    // for screens with 2 buttons this is button 1
    private int buttonxA, buttonyA;
    private int buttonwA, buttonhA;

    //and button 2
    private int buttonxB, buttonyB;
    private int buttonwB, buttonhB;

    private int glowCounter = 125;

    private static boolean paintRobotMessages;
    private long playerMessageSetTimeLong; // this keeps the version on screen for a few secs at the start
    //sets the vars to allow a message to be shown to the player in bottom right for
    //a while

    private static final long SHOW_ME_LIMIT = 3000; // how long  show player message 1.5 sec
    private String message2Players = VERSION;

    private int messageWidth, messageHeight;
    private int messagex, messagey;

    //////////////////THEMES CODE/////////////////
    private static final int DEFAULT   = 0;
    private static final int METALIC   = 1;
    private static final int CLASSIC   = 2;
    private static final int FUNNYMAN  = 3;
    private static final int BUMBLEBEE = 4;
    private static final int MAX_THEMES = 4; // this should always equals the last one
    private int theme = DEFAULT;
    private String themeName;
    private boolean firstThemeSet = true; // so we dont tell players when the theme is set upon loading but we do othertimes

    private int debugMenuPos = 0;
    public static final int DEBUG_OPTION_TIME_DELAY_BETWEEN_CLICKS=0;
    public static final int DEBUG_OPTION_ROBOT_DELAY_AFTER_CLICKS=1;
    public static final int DEBUG_OPTION_paintRobotMessages=2;
    public static final int DEBUG_OPTION_FULL_AUTO_PLAY=3;
    public static final int LAST_DEBUG_OPTION=3;

    public static final int DEBUGLEFT = 1;
    public static final int DEBUGRIGHT = 2;

    public static int pointerX;
    public static int pointerY;

    public CustomCanvas(JFrame mainWindow, GameColour gameColour, Geometry geometry,
                        Board board, GameConfig config) {
        log("CustomCanvas made.");
        this.maxSplashCounter = config.maxSplashCounter();
        this.drawMousePointer = config.drawMousePointer();
        this.enableDoubleBuffering = config.enableDoubleBuffering();
        this.mainWindow = mainWindow;
        this.board = board;
        this.gameColour = gameColour;
        this.geometry = geometry;

        sfxDiceRoll = new Sound("/diceroll.wav");
        sfxDoubleRolled = new Sound("/whoosh.wav");

        sfxdouble = new Sound("/double.wav");
        sfxResign = new Sound("/resign.wav");
        sfxGameOver = new Sound("/gameover.wav", true);

        this.soundOn = config.soundOn();
        this.gameComplete = false;

        addMouseListener(this);
        addMouseMotionListener( this );
        addKeyListener( this );

        setTheme(theme);
        makeColourObjects();
        loadSounds(this.soundOn);
        loadImages();
        int[] pixels = new int[16 * 16];
        Image image = Toolkit.getDefaultToolkit().createImage( new MemoryImageSource(16, 16, pixels, 0, 16));
        transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            image, new Point(0, 0), "invisibleCursor");
        loadCustomFonts();
        mainWindow.getContentPane().add(this);
    }

    /**
     * Must be called after canvas has window associated.
     */
    public void init() {
        requestFocus();
        if (enableDoubleBuffering) {
            createBufferStrategy(2);
        }
    }

    @Override
    public void paint(Graphics g_) {
        Graphics graphics = g_;
        BufferStrategy bufferStrategy = null;
        if (enableDoubleBuffering) {
            bufferStrategy = this.getBufferStrategy();
            if (bufferStrategy == null) {
                log("Buffer strategy is not yet created, waiting");
                return;
            }
            Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics = g;
        }
        geometry.setCanvasWidth(getWidth());
        geometry.setCanvasHeight(getHeight());
        paintSwitch(graphics);
        handleMousePointer(graphics);
        if (bufferStrategy != null) {
            bufferStrategy.show();
        }
    }

    public GuiState getState() {
        return state;
    }

    private void loadSounds(boolean soundOn) {
        sfxDiceRoll.loadSound(soundOn);
        sfxDoubleRolled.loadSound(soundOn);
        sfxError.loadSound(soundOn);
        sfxdouble.loadSound(soundOn);
        sfxResign.loadSound(soundOn);
        sfxGameOver.loadSound(soundOn);

        if (soundOn) {
            log("Sounds loaded");
        } else {
            log("Sounds are disabled");
        }
    }

    // Calls a different paint method based on the current state
    private void paintSwitch(Graphics g) {
        switch(state) {
            case SPLASH_SCREEN:
                paint_SPLASH_SCREEN(g);
                break;
            case OPTIONS_SCREEN_LOCAL_OR_NETWORK:
                glowButton(mouseHoverX, mouseHoverY);
                paint_OPTIONS_SCREEN_LOCAL_OR_NETWORK(g," Local Play ","Network Play","Please select");
                break;
            case OPTIONS_SCREEN_LOCAL_COMPUTER_OR_HUMAN:
                glowButton(mouseHoverX, mouseHoverY);
                paint_OPTIONS_SCREEN_LOCAL_OR_NETWORK(g,"Computer"," Human  ","Play against");
                break;
            case GAME_IN_PROGRESS:
                paint_POST_SPLASH_SCREEN(g);
                break;
            case NETWORKING_ENTER_NAME:
                paint_NETWORKING_ENTER_NAME(g);
                break;
            case NETWORKING_LOBBY:
                paint_NETWORKING_LOBBY(g);
                break;
            default://///////////////////////////////////////////////
                Utils._E("Warning state in paint unrecognised!");
                break;
        }
        drawExtras(g);
    }

    private void handleMousePointer(Graphics g) {
        boolean botIsPlaying = Bot.getFullAutoPlay() ||
            (HUMAN_VS_COMPUTER && board.getCurrentPlayer().getColour() == PlayerColor.BLACK);

        if (NETWORK_GAME_IN_PROCESS) {
            mouseHoverX = pointerX;
            mouseHoverY = pointerY;
        } else {
            if (botIsPlaying) {
                mainWindow.setCursor(transparentCursor);
                mouseHoverX = Bot.x;
                mouseHoverY = Bot.y;
            } else {
                mainWindow.setCursor(null);
            }
        }
        if (this.drawMousePointer && (NETWORK_GAME_IN_PROCESS || botIsPlaying)) {
            utils.drawImage(g, pointer, mouseHoverX, mouseHoverY + 6, this); // this 6 lines it up
        }
    }

    //draws any of the extras:
    //debug info, about box, messages to players etc
    private void drawExtras(Graphics g) {
        if (PAINT_STATE) {
            paintState(g);
        }
        if (INFO) {
            paintAboutBox(g);
        }
        if (Utils.CANVAS_LOGGING) {
            paintStringsToCanvas(g);
        }
        //all of this in aid of a loop that lasts for x amount of seconds not a cpu dependent tick,
        //could be a bit over the top for what im doign (todo optimise?)
        long playerMessageTimePassedLong = System.currentTimeMillis() - playerMessageSetTimeLong;
        if (playerMessageTimePassedLong < SHOW_ME_LIMIT ) {
            paintMessageToPlayers(g);
        }
        if (Bot.dead == false) {
            if (paintRobotMessages) {
                  //all of this in aid of a loop that lasts for x amount of seconds not a cpu dependent tick,
                  //could be a bit over the top for what im doign (todo optimise?)
                long robotMessageTimePassedLong = System.currentTimeMillis()-robotMessageSetTimeLong;
                if (robotMessageTimePassedLong > FIFTY_SECONDS) {//so we dont have a mad long getting bigger and bigger
                    robotMessageTimePassedLong = TEN_SECONDS;//so it doesnt bring message back
                    robotMessageSetTimeLong = System.currentTimeMillis() - TEN_SECONDS;
                }
                if (robotMessageTimePassedLong < SHOW_ME_LIMIT) {
                    paintRobotMessage(g);
                }
            }
        }
        if (DEBUG_CONSOLE) {
            paintDebugBox(g);
        }
    }

    private void paintAboutBox(Graphics g) {
        infoCounter++;
        if (infoCounter > maxSplashCounter) {
            infoCounter = 0;
            INFO = false;
        }

        utils.setColor(g, 0,0,0,TRANSPARENCY_LEVEL);
        utils.fillRoundRect(g, geometry.boardWidth()/4, geometry.boardHeight()/4, geometry.boardWidth()/2, geometry.boardHeight()/2);
        utils.setColor(g, Color.white);
        utils.drawRoundRect(g, geometry.boardWidth()/4, geometry.boardHeight()/4, geometry.boardWidth()/2, geometry.boardHeight()/2);

        int xabout = geometry.boardWidth() / 2;
        int yabout = (geometry.boardHeight()/4) + geometry.tinyGap();

        //paint the about box
        String printme="Forumosa Backgammon ("+VERSION+")";
        fontwhite.drawString(g, printme,xabout-fontwhite.stringWidth(printme)/2,yabout,0);
        yabout += fontblack.getHeight();
        printme="- www.forumosa.com -";fontwhite.drawString(g, printme,xabout-fontwhite.stringWidth(printme)/2,yabout,0);yabout+=fontblack.getHeight();
        printme=" ";fontwhite.drawString(g, printme,xabout-fontwhite.stringWidth(printme)/2,yabout,0);yabout+=fontblack.getHeight();
        printme="Keys:    ";fontwhite.drawString(g, printme,xabout-fontwhite.stringWidth(printme)/2,yabout,0);yabout+=fontblack.getHeight();
        printme="q = quit ";fontwhite.drawString(g, printme,xabout-fontwhite.stringWidth(printme)/2,yabout,0);yabout+=fontblack.getHeight();
        printme="t = theme";fontwhite.drawString(g, printme,xabout-fontwhite.stringWidth(printme)/2,yabout,0);yabout+=fontblack.getHeight();
        printme=" ";fontwhite.drawString(g, printme,xabout-fontwhite.stringWidth(printme)/2,yabout,0);yabout+=fontblack.getHeight();
        printme=" ";fontwhite.drawString(g, printme,xabout-fontwhite.stringWidth(printme)/2,yabout,0);yabout+=fontblack.getHeight();
        printme=" ";fontwhite.drawString(g, printme,xabout-fontwhite.stringWidth(printme)/2,yabout,0);yabout+=fontblack.getHeight();
        printme=" ";fontwhite.drawString(g, printme,xabout-fontwhite.stringWidth(printme)/2,yabout,0);yabout+=fontblack.getHeight();
        printme=" ";fontwhite.drawString(g, printme,xabout-fontwhite.stringWidth(printme)/2,yabout,0);yabout+=fontblack.getHeight();
        printme="Developed by www.garethmurfin.co.uk";fontwhite.drawString(g, printme,xabout-fontwhite.stringWidth(printme)/2,yabout,0);yabout+=fontblack.getHeight();
    }

    //paints the about box
    private void paintDebugBox(Graphics g) {
        infoCounter++;
        if (infoCounter > maxSplashCounter) {
            infoCounter = 0;
            INFO = false;
        }

        utils.setColor(g, 0, 0, 0, 125);
        int x = 10;
        int y = 10;
        utils.fillRoundRect(g, x, y, geometry.boardWidth() / 2, geometry.boardHeight() - 40);
        utils.setColor(g, Color.yellow);
        utils.drawRoundRect(g, x, y, geometry.boardWidth() / 2, geometry.boardHeight() - 40);

        x += 5;
        y += geometry.tinyGap();
        //paint the about box
        String printme = "Backgammon (" + VERSION + ") DEBUG CONSOLE";
        fontwhite.drawString(g, printme, x, y, 0);
        y += fontblack.getHeight();

        printme = "DELAY_BETWEEN_CLICKS_MILLIS:" + Bot.DELAY_BETWEEN_CLICKS_MILLIS;
        //Highlighter to indicate if its on this option
        if (debugMenuPos == 0) {
            utils.drawRoundRect(g, x, y, fontwhite.stringWidth(printme + " "), fontwhite.getHeight());
        }
        //option itself,
        fontwhite.drawString(g, printme, x, y, 0);
        y += fontblack.getHeight();


        printme = "ROBOT_DELAY_AFTER_CLICKS:" + Bot.ROBOT_DELAY_AFTER_CLICKS;
        //Highlighter to indicate if its on this option
        if (debugMenuPos == 1) {
            utils.drawRoundRect(g, x, y, fontwhite.stringWidth(printme + " "), fontwhite.getHeight());
        }
        //option itself,
        fontwhite.drawString(g, printme, x, y, 0);
        y += fontblack.getHeight();

        printme = "paintRobotMessages:" + paintRobotMessages;
        //Highlighter to indicate if its on this option
        if (debugMenuPos == 2) {
            utils.drawRoundRect(g, x, y, fontwhite.stringWidth(printme + " "), fontwhite.getHeight());
        }
        //option itself,
        fontwhite.drawString(g, printme, x, y, 0);
        y += fontblack.getHeight();

        printme = "FULL_AUTO_PLAY:" + Bot.getFullAutoPlay();
        //Highlighter to indicate if its on this option
        if (debugMenuPos == 3) {
            utils.drawRoundRect(g, x, y, fontwhite.stringWidth(printme + " "), fontwhite.getHeight());
        }
        //option itself,
        fontwhite.drawString(g, printme, x, y, 0);
        y += fontblack.getHeight();

        y += 10;

        String[] helpMessages = {"Q = QUIT",
            "P = PAUSE (bot dead? " + Bot.dead + ")",
            "D = DEBUG CONSOLE (" + DEBUG_CONSOLE + ")",
            "T = THEME (" + themeName + ")", "C = COLLISIONS (" + showBoundaryBoxes + ")",
            "L = CANVAS LOGGING (" + Utils.CANVAS_LOGGING + ")", "S = SOUND (" + soundOn + ")", "X = TEST SOUND",
            "J = JUMP TO DESTINATION: unknown",
            "F = FULL_AUTO_PLAY: unknwon"};
        for (String message : helpMessages) {
            fontwhite.drawString(g, message, x, y, 0);
            y += fontblack.getHeight();
        }
        y += 5;
        if (robotMoveDesc.length() < 20) {//avoid printing textual things, just moves.
            printme = "Bot is thinking:" + robotMoveDesc;
            fontwhite.drawString(g, printme, x, y, 0);
            y += fontblack.getHeight();
        }
    }

    private void debugOptionChanged(int direction) {
        switch (debugMenuPos) {
            //DELAY_BETWEEN_CLICKS_MILLIS
            case DEBUG_OPTION_TIME_DELAY_BETWEEN_CLICKS:
                if (direction == DEBUGLEFT) {
                    Bot.DELAY_BETWEEN_CLICKS_MILLIS -= 10;
                    log("DELAY_BETWEEN_CLICKS_MILLIS:" + Bot.DELAY_BETWEEN_CLICKS_MILLIS);
                }
                if (direction == DEBUGRIGHT) {
                    Bot.DELAY_BETWEEN_CLICKS_MILLIS += 10;
                    log("DELAY_BETWEEN_CLICKS_MILLIS:" + Bot.DELAY_BETWEEN_CLICKS_MILLIS);
                }
                break;
            //DEBUG_OPTION_ROBOT_DELAY_AFTER_CLICKS
            case DEBUG_OPTION_ROBOT_DELAY_AFTER_CLICKS:
                if (direction == DEBUGLEFT) {
                    Bot.ROBOT_DELAY_AFTER_CLICKS -= 10;
                    log("ROBOT_DELAY_AFTER_CLICKS:" + Bot.ROBOT_DELAY_AFTER_CLICKS);
                }
                if (direction == DEBUGRIGHT) {
                    Bot.ROBOT_DELAY_AFTER_CLICKS += 10;
                    log("ROBOT_DELAY_AFTER_CLICKS:" + Bot.ROBOT_DELAY_AFTER_CLICKS);
                }
                break;
            case DEBUG_OPTION_paintRobotMessages:
                if (direction == DEBUGLEFT) {
                    paintRobotMessages = !paintRobotMessages;
                    log("paintRobotMessages:" + paintRobotMessages);
                }
                if (direction == DEBUGRIGHT) {
                    paintRobotMessages = !paintRobotMessages;
                    log("paintRobotMessages:" + paintRobotMessages);
                }
                break;
            //DELAY_BETWEEN_CLICKS_MILLIS
            case DEBUG_OPTION_FULL_AUTO_PLAY:
                if (direction == DEBUGLEFT) {
                    Bot.setFullAutoPlay(!Bot.getFullAutoPlay());
                    log("FULL_AUTO_PLAY:" + Bot.getFullAutoPlay());
                }
                if (direction == DEBUGRIGHT) {
                    Bot.setFullAutoPlay(!Bot.getFullAutoPlay());
                    log("FULL_AUTO_PLAY:" + Bot.getFullAutoPlay());
                }
                break;
            default:
                Utils._E("UNKNOWN DEBUG OPTION CHANGED:" + debugMenuPos);
                break;
        }
    }

    //paints the state - for debugging.
    private void paintState(Graphics g) {
        fontblack.drawString(g, state.name(),20,20,0);
    }

    private void loadImages() {
        log("Attempting to loadImages()");
        splashScreenLogo = utils.loadImage("/midokura-logo.png");
        splashScreenLogoSmall = utils.loadImage("/midokura-logo-small.png");
        pointer = utils.loadImage("/pointer.png");
        op = utils.loadImage("/op.png");
        admin = utils.loadImage("/admin.png");
    }

    ///////// ALL PAINT STATE METHODS //////////////////////
    private void paint_SPLASH_SCREEN(Graphics g) {
        utils.backGround(g, Color.WHITE, getWidth(), getHeight());
        utils.drawImage(g, splashScreenLogo, getWidth()/2, getHeight()/2,this);
        utils.setColor(g, Color.BLACK);
        if (showBoundaryBoxes) {
             int ydebug = 10;
             int xdebug = 10;
             fontblack.drawString(g, DEBUG_HEADER, xdebug, ydebug,0);
             ydebug += fontblack.getHeight();
             fontblack.drawString(g, VERSION, xdebug, ydebug,0);
             ydebug += fontblack.getHeight();
             fontblack.drawString(g, splashCounter + "/" + maxSplashCounter, xdebug, ydebug,0);
        }
        if (++splashCounter > maxSplashCounter) {
            log("Splash done.");
            state = OPTIONS_SCREEN_LOCAL_OR_NETWORK;
        }
    }

    private void paint_POST_SPLASH_SCREEN(Graphics g) {
        utils.backGround(g, background_colour, getWidth(), getHeight()); // paint entire background
        utils.setColor(g, Color.WHITE);

        int boardWidth = (getWidth() / geometry.panelSizeFraction()) * (geometry.panelSizeFraction() - 1);
        int boardHeight = getHeight();
        board.paint(g, boardWidth, boardHeight, !gameComplete(), mouseHoverX, mouseHoverY);

        //paint the message panel to the right with players name etc
        utils.setColor(g, panel_colour);
        utils.fillRect(g, boardWidth, geometry.borderWidth(),
            geometry.panelWidth(), boardHeight - (geometry.borderWidth() * 2));

        //draw the preferences button
        final int prefx = preferencesButtonX();
        final int prefy = preferencesButtonY();

        //draw a circle with an 'i' inside.
        utils.setColor(g, Color.blue);
        utils.fillCircle(g, prefx, prefy, prefw, prefh);
        utils.setColor(g, Color.white);
        utils.drawCircle(g, prefx, prefy, prefw, prefh);
        fontwhite.drawString(g, "i", prefx + 4, prefy + 2, 0);

        if (showBoundaryBoxes) {
            utils.setColor(g, Color.RED);
            utils.drawRect(g, prefx, prefy, prefw, prefh);
        }

        board.drawBlackPieceContainer(g, mouseHoverX, mouseHoverY);
        board.drawWhitePieceContainer(g, mouseHoverX, mouseHoverY);
        board.drawBarPieces(g);

        // draw panel text:
        int xpos = boardWidth + geometry.tinyGap();
        drawHUDtext(g, xpos);
        board.drawPieceStuckToMouse(g, mouseHoverX, mouseHoverY);
        if (!gameComplete() && board.turnOver()) {
            log("---- THIS TURN IS OVER ----");
            checkIfGameIsOver();
            if (!gameComplete()) {
                nextTurn();
            }
        }
    }

    private void checkIfGameIsOver() {
        String gameCompleteString = "White has won the game!";
        if (board.gameIsOver()) {
            gameComplete = true;
        }
        if (board.whoseTurnIsIt() == PlayerColor.BLACK) {
            gameCompleteString = "Black has won the game!";
        }

        if (whiteResigned || blackResigned) {
            String looser = whiteResigned ? "White" : "Black";
            String winner = whiteResigned ? "Black" : "White";
            gameCompleteString = String.format("%s has resigned. %s has won!", looser, winner);
        }

        if (gameComplete || whiteResigned || blackResigned) {
            sfxGameOver.playSound();
            tellPlayers(gameCompleteString);
        }
    }

    // draw all of the text on the panel
    private void drawHUDtext(Graphics g, int xpos) {
        int ypos = geometry.borderWidth() + geometry.tinyGap();
        //draw black players score at top
        String printme = "White (" + board.getWhitePlayer().getName() + ")";
        if (board.whoseTurnIsIt() == PlayerColor.WHITE) {
            printme += "*";
        }
        fontwhite.drawString(g, printme, xpos, ypos, 0);
        ypos += fontwhite.getHeight();

        printme = "Pips: " + board.calculatePips(PlayerColor.WHITE);
        fontwhite.drawString(g, printme, xpos, ypos, 0);
        ypos += fontwhite.getHeight();
        printme = "Score: " + board.getBlackPlayer().score;
        fontwhite.drawString(g, printme, xpos, ypos, 0);

        //draw white players score at bot
        ypos = geometry.boardHeight() - 9 - (geometry.borderWidth() * 2) - (fontwhite.getHeight() * 2);
        printme = "Brown (" + board.getBlackPlayer().getName() + ")";
        if (board.whoseTurnIsIt() == PlayerColor.BLACK) {
            printme += "*";
        }
        fontwhite.drawString(g, printme, xpos, ypos, 0);
        ypos += fontwhite.getHeight();
        printme = "Pips: " + board.calculatePips(PlayerColor.BLACK);
        fontwhite.drawString(g, printme, xpos, ypos, 0);
        ypos += fontwhite.getHeight();
        printme = "Score: " + board.getWhitePlayer().score;
        fontwhite.drawString(g, printme, xpos, ypos, 0);

        int xposTmp = -1;
        ypos = (geometry.boardHeight() / 2) - ((fontwhite.getHeight() * 4) / 2);
        printme = "Match Points: " + matchPoints;
        int widthOfPrintMe = (fontwhite.stringWidth(printme));
        xposTmp = (geometry.boardWidth() + geometry.panelWidth() / 2) - ((widthOfPrintMe / 2) + geometry.tinyGap());
        fontwhite.drawString(g, printme, xposTmp, ypos, 0);
        ypos += fontwhite.getHeight();
        utils.setColor(g, roll_button_colour);

        //---- draw buttons
        ///////// double button
        printme = "Double";
        widthOfPrintMe = (fontwhite.stringWidth(printme));
        xposTmp = (geometry.boardWidth() + geometry.panelWidth() / 2) - ((widthOfPrintMe / 2) + geometry.tinyGap());
        utils.setColor(g, roll_button_colour);
        ypos += 10;
        utils.drawRoundRect(g, xposTmp - 10, ypos, widthOfPrintMe + 20, (fontwhite.getHeight()));
        fontwhite.drawString(g, printme, xposTmp, ypos + 1, 0);
        ////
        doubleX = xposTmp - 10;
        doubleY = ypos;
        doubleWidth = widthOfPrintMe + 20;
        doubleHeight = (fontwhite.getHeight());
        if (showBoundaryBoxes) {
            utils.setColor(g, Color.red);
            utils.drawRect(g, doubleX, doubleY, doubleWidth, doubleHeight);
        }

        // draw the 'Roll' button
        printme = "Roll";
        widthOfPrintMe = (fontwhite.stringWidth(printme));

        if (!gameComplete() && board.showRollButton()) {
            //draw in centre:
            xposTmp = ((geometry.boardWidth() / 2)) - widthOfPrintMe / 2;

            utils.setColor(g, roll_button_colour);
            utils.fillRoundRect(g, xposTmp - 10, ypos, widthOfPrintMe + 20, (fontwhite.getHeight()));

            if (HUMAN_VS_COMPUTER && board.whoseTurnIsIt() == PlayerColor.BLACK || Bot.getFullAutoPlay()) {
                Board.setBotDestination((xposTmp - 10) + (widthOfPrintMe + 20) / 2,
                    ypos + (fontwhite.getHeight()) / 2, "PRESS ROLL BUTTON");
            }

            rollButtonX = xposTmp - 10;
            rollButtonY = ypos;
            rollButtonW = widthOfPrintMe + 20;
            rollButtonH = (fontwhite.getHeight());
            if (showBoundaryBoxes) {
                utils.setColor(g, Color.RED);
                utils.drawRect(g, rollButtonX, rollButtonY, rollButtonW, rollButtonH);
            }
            fontblack.drawString(g, printme, xposTmp, ypos + 1, 0);
            ypos += fontwhite.getHeight();

        } else {
            // still knock y down so other buttons draw inline.
            ypos += fontwhite.getHeight();
        }

        ///////// resign button
        printme = "Resign";
        widthOfPrintMe = (fontwhite.stringWidth(printme));
        xposTmp = (geometry.boardWidth() + geometry.panelWidth() / 2) - ((widthOfPrintMe / 2) + geometry.tinyGap());
        utils.setColor(g, roll_button_colour);
        ypos += 10;
        utils.drawRoundRect(g, xposTmp - 10, ypos, widthOfPrintMe + 20, (fontwhite.getHeight()));
        fontwhite.drawString(g, printme, xposTmp, ypos + 1, 0);

        resignX = xposTmp - 10;
        resignY = ypos;
        resignWidth = widthOfPrintMe + 20;
        resignHeight = (fontwhite.getHeight());

        if (showBoundaryBoxes) {
            utils.setColor(g, Color.red);
            utils.drawRect(g, resignX, resignY, resignWidth, resignHeight);
        }
    }

    // simply sets glow to true if the mouse is over the button
    // glow is a boolean used to make the button glow when pointer is over it.
    private void glowButton(int x, int y) {
        glowA = (x >= buttonxA && x <= buttonxA + buttonwA) &&
            (y >= buttonyA && y <= buttonyA + buttonhA);
        glowB = (x >= buttonxB && x <= buttonxB + buttonwB) &&
            (y >= buttonyB && y <= buttonyB + buttonhB);

        if (glowA || glowB) {
            glowCounter += GLOW_INCREMENTER;
            glowCounter = Math.min(glowCounter, 355);
        }
    }

    // this deals with touching the 'virtual' buttons
    // a mouse event is passed in to grab the x,y values from
    private void touchedButton(int x, int y) {
        switch (state) {
            ///////////////////////////////////////////
            case OPTIONS_SCREEN_LOCAL_OR_NETWORK:
                //check buttons (local player, network play)
                checkAndDealWithTopButtonPressed_localplay(x, y);
                checkAndDealWithBotButtonPressed_networkplay(x, y);
                break;
            //////////////////////////////////////
            case OPTIONS_SCREEN_LOCAL_COMPUTER_OR_HUMAN:
                //check buttons (computer player, human player)
                checkAndDealWithTopButtonPressed_computerPlayer(x, y);
                checkAndDealWithTopButtonPressed_humanPlayer(x, y);
                break;
            //////////////////////////////////////
            case GAME_IN_PROGRESS:
                if (board.showRollButton()) {
                    checkAndDealWithRollDiceButton(x, y);
                }
                // Other in game buttons go here, like double up, resign etc.
                break;
        }
    }

    //works out if the bottom button is pressed (in this state the 'computer player' button)
    //and deals with it
    private void checkAndDealWithTopButtonPressed_computerPlayer(int x, int y) {
        if (x >= buttonxA && x <= buttonxA + buttonwA) {
            if (y >= buttonyA && y <= buttonyA + buttonhA) {
                log("Selected COMPUTER on OPTIONS_SCREEN_LOCAL_COMPUTER_OR_HUMAN");
                HUMAN_VS_COMPUTER = true;
                log("CPU OPPONENT PRIMED.");
                startGame();
            }
        }
    }

    //works out if the bottom button is pressed (in this state the 'human player' button)
    //and deals with it
    private void checkAndDealWithTopButtonPressed_humanPlayer(int x,int y) {
        //check if bottom button is pressed (human player)
        if (x >= buttonxB && x <= buttonxB + buttonwB) {
            if (y >= buttonyB && y <= buttonyB + buttonhB) {
                log("Selected HUMAN on OPTIONS_SCREEN_LOCAL_COMPUTER_OR_HUMAN");
                startGame();
                HUMAN_VS_COMPUTER = false;
                log("THE WEAKLING WOULD RATHER FACE A HUMAN.");
            }
        }
    }

     //works out if the bottom button is pressed (in this state the 'network play' button)
     //and deals with it
    private void checkAndDealWithBotButtonPressed_networkplay(int x, int y) {
        //check if bottom button is pressed (NETWORK)
        if (x >= buttonxB && x <= buttonxB + buttonwB) {
            if (y >= buttonyB && y <= buttonyB + buttonhB) {
                log("Selected NETWORK PLAY on OPTIONS_SCREEN_LOCAL_OR_NETWORK");
                state = NETWORKING_ENTER_NAME;
            }
        }
    }

    //works out if the top button is pressed (in this state the 'local play' button)
    //and deals with it
    private void checkAndDealWithTopButtonPressed_localplay(int x, int y) {
        if (x >= buttonxA && x <= buttonxA + buttonwA) {
            if (y >= buttonyA && y <= buttonyA + buttonhA) {
                log("Selected LOCAL PLAY on OPTIONS_SCREEN_LOCAL_OR_NETWORK");
                state = OPTIONS_SCREEN_LOCAL_COMPUTER_OR_HUMAN;
            }
        }
    }
  
    //detects if the roll dice button has been pressed, and if so, reacts
    //accordingly
    private void checkAndDealWithRollDiceButton(int x, int y) {
        if (x >= rollButtonX && x <= rollButtonX + rollButtonW) {
            if (y >= rollButtonY && y <= rollButtonY + rollButtonH) {
                log("Roll Dice button clicked.");
                sfxDiceRoll.playSound();
                dealWithOrdinaryRolls();
            }
        }
    }

    private void dealWithOrdinaryRolls() {
        if (board.whoseTurnIsIt() == PlayerColor.WHITE) {
            log("white will roll both die now.");
        } else if (board.whoseTurnIsIt() == PlayerColor.BLACK) {
            log("black will roll both die now.");
        }
        playerRolls(board.whoseTurnIsIt());
    }

    private void playerRolls(PlayerColor player) {
        board.rollDies();
        int val = board.die1.getValue();
        int val2 = board.die2.getValue();

        D1lastDieRoll_toSendOverNetwork = val;
        GameNetworkClient.SENDCLICK_AND_DIEVALUE1 = true; // tells it to send a click over network
        D2lastDieRoll_toSendOverNetwork = val2;
        GameNetworkClient.SENDCLICK_AND_DIEVALUE2 = true; // tells it to send a click over network

        String playerStr = player == PlayerColor.WHITE ? "White" : "Black";
        log(String.format("####################################%s rolled:%d, %d", playerStr, val, val2));
        tellPlayers(String.format("%s rolled:%d-%d", playerStr, val, val2));

        if (board.rolledDouble()) {
            log(String.format("%s Double!", playerStr));
            tellPlayers(String.format("%s rolled:%d-%d (Double)", playerStr, val, val2));
            sfxDoubleRolled.playSound();
        }
    }

    private void paint_OPTIONS_SCREEN_LOCAL_OR_NETWORK(Graphics g, String buttonAstr, String buttonBstr, String question) {
        utils.backGround(g, Color.WHITE, getWidth(), getHeight());
        String printme = question;
        int widthOfPrintMe;
        int xposTmp;
        int ypos = (getHeight() / 2) - fontblack.getHeight() * 5;

        widthOfPrintMe = (fontblack.stringWidth(printme));
        xposTmp = (getWidth() / 2) - ((widthOfPrintMe / 2));
        fontblack.drawString(g, printme, xposTmp, ypos + 1, 0);
        ypos += fontblack.getHeight() * 2;
        printme = buttonAstr;

        widthOfPrintMe = fontblack.stringWidth(printme);
        xposTmp = (getWidth() / 2) - ((widthOfPrintMe / 2));

        //make button glow if pointer is over it
        if (glowA) {
            if (glowCounter < 255) {
                utils.setColor(g, new Color(glowCounter, 0, 0));
            } else {
                utils.setColor(g, new Color(255, 0, 0));
            }
            utils.fillRoundRect(g, xposTmp - 10, ypos, widthOfPrintMe + 20, (fontblack.getHeight()));
            glowA = false;
        }
        utils.setColor(g, Color.black);
        utils.drawRoundRect(g, xposTmp - 10, ypos, widthOfPrintMe + 20, (fontblack.getHeight()));

        String robotMoveDesc = "Bot Loaded. Answer: (" + question + ") ";
        Board.setBotDestination((xposTmp - 10) + (widthOfPrintMe + 20) / 2, ypos + (fontblack.getHeight() / 2),
            "Bot Loaded. Answer: (" + question + ") ");
        tellRobot(false, robotMoveDesc);

        /////for collision of button
        buttonxA = xposTmp - 10;
        buttonyA = ypos;
        buttonwA = widthOfPrintMe + 20;
        buttonhA = (fontblack.getHeight());
        if (showBoundaryBoxes) {
            utils.setColor(g, Color.red);
            utils.drawRect(g, buttonxA, buttonyA, buttonwA, buttonhA);
        }
        fontblack.drawString(g, printme, xposTmp, ypos + 1, 0);
        printme = "or";
        ypos += (fontblack.getHeight() * 2);
        widthOfPrintMe = (fontblack.stringWidth(printme));
        xposTmp = (getWidth() / 2) - ((widthOfPrintMe / 2));
        fontblack.drawString(g, printme, xposTmp, ypos + 1, 0);

        ///////// 'network' button
        printme = buttonBstr; // "Network Play";
        widthOfPrintMe = (fontblack.stringWidth(printme));
        xposTmp = (getWidth() / 2) - ((widthOfPrintMe / 2));
        utils.setColor(g, Color.BLACK);
        ypos += fontblack.getHeight() * 2;

        //make button glow if pointer is over it
        if (glowB) {
            if (glowCounter < 255) {
                utils.setColor(g, new Color(0, 0, glowCounter));
            } else {
                utils.setColor(g, new Color(0, 0, 255));
            }

            utils.fillRoundRect(g, xposTmp - 10, ypos, widthOfPrintMe + 20, (fontblack.getHeight()));
            glowB = false;
        }
        utils.setColor(g, Color.black);
        utils.drawRoundRect(g, xposTmp - 10, ypos, widthOfPrintMe + 20, (fontblack.getHeight()));
        fontblack.drawString(g, printme, xposTmp, ypos + 1, 0);

        /////for collision of button
        buttonxB = xposTmp - 10;
        buttonyB = ypos;
        buttonwB = widthOfPrintMe + 20;
        buttonhB = (fontblack.getHeight());
        if (showBoundaryBoxes) {
            utils.setColor(g, Color.red);
            utils.drawRect(g, buttonxB, buttonyB, buttonwB, buttonhB);
        }
        //draw a little version of the logo in the bottom right
        utils.drawImage(g, splashScreenLogoSmall, getWidth() - ((splashScreenLogoSmall.getWidth(this) / 2) + 20),
            getHeight() - splashScreenLogoSmall.getHeight(this), this);
    }

    private void paint_NETWORKING_ENTER_NAME(Graphics g) {
        String printme="Enter your name:";
        int xposTmp=0;
        int ypos =(getHeight()/2)-fontblack.getHeight()*5;

        int widthOfPrintMe = fontblack.stringWidth(printme);
        xposTmp=(getWidth()/2)-((widthOfPrintMe/2));
        fontblack.drawString(g, printme, xposTmp , ypos+1, 0);
        ypos+=fontblack.getHeight()*2;


        printme="Enter your name:";
        xposTmp=(getWidth()/2)-((widthOfPrintMe/2));
        utils.drawRect(g, xposTmp, ypos, fontblack.stringWidth(printme), fontblack.getHeight());

        printme=NetworkChatClient.nick;
        widthOfPrintMe=(fontblack.stringWidth(printme));
        xposTmp=(getWidth()/2)-((widthOfPrintMe/2));
        fontblack.drawString(g, printme, xposTmp , ypos+1, 0);
    }

    private void paint_NETWORKING_LOBBY(Graphics g) {
        TRANSPARENCY_LEVEL=255;
        utils.setColor(g, 0,0,0,TRANSPARENCY_LEVEL);
        utils.fillRect(g,0,0,getWidth(),getHeight());
        int SMALLGAP=5;

        int x = SMALLGAP;
        int y = SMALLGAP + fontblack.getHeight()*2;

        int BORDER=10;
        int WIDTH_OF_MESSAGE_TEXT  = ((getWidth()-getWidth()/6)-BORDER);
        int HEIGHT_OF_MESSAGE_TEXT = ((getHeight()-getHeight()/8)-(BORDER+y+SMALLGAP))+2;
        int WIDTH_OF_USERLIST  = getWidth()-(WIDTH_OF_MESSAGE_TEXT+BORDER+SMALLGAP);
        int HEIGHT_OF_USERLIST = HEIGHT_OF_MESSAGE_TEXT+1;
        int WIDTH_OF_ENTERTEXT_BOX = HEIGHT_OF_MESSAGE_TEXT;
        int HEIGHT_OF_ENTERTEXT_BOX = getHeight() - (HEIGHT_OF_MESSAGE_TEXT + BORDER + y + SMALLGAP);
        int HEIGHT_OF_TOPIC_AND_NEWS_BOX=getHeight()-(HEIGHT_OF_MESSAGE_TEXT + HEIGHT_OF_ENTERTEXT_BOX + SMALLGAP * 5);
        y = SMALLGAP;

        ///////////////////////////////////////////

        x = x + WIDTH_OF_MESSAGE_TEXT + SMALLGAP;
        //info box top right (amount of users and ops)
        utils.setColor(g, 255,255,255,TRANSPARENCY_LEVEL);
        utils.fillRoundRect(g, x, y, WIDTH_OF_USERLIST, HEIGHT_OF_TOPIC_AND_NEWS_BOX);
        utils.setColor(g, OUTLINE_FOR_CHAT_BOXES);
        utils.drawRoundRect(g, x, y, WIDTH_OF_USERLIST, HEIGHT_OF_TOPIC_AND_NEWS_BOX);
        fontblack.drawString(g, ""+NetworkChatClient.userList.size()+" users", x+SMALLGAP , y+(SMALLGAP*2)-2, 0);
        ////////////////////////////////////////////

        x=SMALLGAP;
        y=SMALLGAP+fontblack.getHeight()*2;

        Shape s = g.getClip();
        g.setClip(x-2, y+3, WIDTH_OF_MESSAGE_TEXT+5, HEIGHT_OF_MESSAGE_TEXT);

        //message text
        utils.setColor(g, 255,255,255,TRANSPARENCY_LEVEL);
        utils.fillRoundRect(g, x, y, WIDTH_OF_MESSAGE_TEXT, HEIGHT_OF_MESSAGE_TEXT);
        utils.setColor(g, OUTLINE_FOR_CHAT_BOXES);
        utils.drawRoundRect(g, x, y, WIDTH_OF_MESSAGE_TEXT, HEIGHT_OF_MESSAGE_TEXT);

        int listY=y+SMALLGAP;
        int topofChatBox=listY;

        Enumeration e = null;
        if (NetworkChatClient.messageText!=null) {
         y=SMALLGAP+fontblack.getHeight()*2;
         y+=paraYoffset;//scrolls it
         e = NetworkChatClient.messageText.elements();
         boolean flip = false;
         while (e.hasMoreElements()) {
             if (y>(getHeight()-topofChatBox-HEIGHT_OF_ENTERTEXT_BOX)+fontblack.getHeight()) {
                 paraYoffset--;//smooth scrolling
             }
             int ydiff=y;
             int yorig=y;
             String message = (String) e.nextElement();
             y=drawMeWrapped(g,x,y,message,fontblack,false,false,true,HEIGHT_OF_ENTERTEXT_BOX-15,false);
             flip=!flip;
             if (flip)
             {
                 ydiff=y-ydiff;
                 //utils.setColor(g,0xFFFFFF);
                 utils.setColor(g, 255,255,255,TRANSPARENCY_LEVEL);
                 utils.fillRoundRect(g,x+1,yorig-1,WIDTH_OF_ENTERTEXT_BOX-1,ydiff);
                 y=yorig;
                y=drawMeWrapped(g,x,y,message,fontblack,false,false,true,WIDTH_OF_ENTERTEXT_BOX-15,false);
             }
             else
             {

                 ydiff=y-ydiff;
               //  utils.setColor(g,0xd5d5d5);
                 utils.setColor(g, 100,100,100,TRANSPARENCY_LEVEL);
                 utils.fillRoundRect(g,x+1,yorig-1,WIDTH_OF_ENTERTEXT_BOX-1,ydiff);
                 y=yorig;
                y=drawMeWrapped(g,x,y,message,fontblack,false,false,true,WIDTH_OF_ENTERTEXT_BOX-15,false);

             }
         }
        }
        g.setClip(s);

        x = SMALLGAP;
        y = SMALLGAP;

        // header for topic and live news/////////draw here so it covers over scrolled text from messages
        utils.setColor(g, 255,255,255,TRANSPARENCY_LEVEL);
        utils.fillRoundRect(g, x, y, WIDTH_OF_MESSAGE_TEXT, HEIGHT_OF_TOPIC_AND_NEWS_BOX);
        utils.setColor(g, OUTLINE_FOR_CHAT_BOXES);
        utils.drawRoundRect(g, x, y, WIDTH_OF_MESSAGE_TEXT, HEIGHT_OF_TOPIC_AND_NEWS_BOX);

        fontblack.drawString(g, NetworkChatClient.topic, x+SMALLGAP , y+(SMALLGAP*2)-2, 0);

        x += WIDTH_OF_MESSAGE_TEXT+SMALLGAP;
        y = 2+SMALLGAP+fontblack.getHeight()*2;

        utils.setColor(g, 255,255,255,TRANSPARENCY_LEVEL);
        utils.fillRoundRect(g, x, y, WIDTH_OF_USERLIST, HEIGHT_OF_USERLIST);
        utils.setColor(g, OUTLINE_FOR_CHAT_BOXES);
        utils.drawRoundRect(g, x, y, WIDTH_OF_USERLIST, HEIGHT_OF_USERLIST);

        listY = y + SMALLGAP;
        e = NetworkChatClient.userList.elements();
        playerPositions = new Vector();
        while (e.hasMoreElements()) {
         String user = (String) e.nextElement();
         int xval=0;
         if (user.equals("ChanServ")) {
             utils.drawImage(g, op,x+SMALLGAP+3 , listY+fontblack.getHeight()/2, this);
             xval=x+3+SMALLGAP*2;
             fontblack.drawString(g, user,  xval, listY, 0); listY+=fontblack.getHeight();
         } else if (user.equals("Admin")) {
             utils.drawImage(g, admin,x+SMALLGAP+3 , listY+fontblack.getHeight()/2, this);
             xval=x+3+SMALLGAP*2;
             fontblack.drawString(g, user, xval , listY, 0); listY+=fontblack.getHeight();
         } else {
             xval=x+SMALLGAP ;
            fontblack.drawString(g, user, xval, listY, 0); listY+=fontblack.getHeight();
         }
         //so we know if we clicked on one
         playerPositions.addElement(new PlayerPos(xval,listY-fontblack.getHeight(),fontblack.stringWidth(user),fontblack.getHeight(),user));
        }

        x = SMALLGAP;
        y += HEIGHT_OF_USERLIST + SMALLGAP + 2;

        //enter text box
        utils.setColor(g, 255,255,255,TRANSPARENCY_LEVEL);
        utils.fillRoundRect(g, x, y, WIDTH_OF_ENTERTEXT_BOX, HEIGHT_OF_ENTERTEXT_BOX);
        utils.setColor(g, OUTLINE_FOR_CHAT_BOXES);
        utils.drawRoundRect(g, x, y, WIDTH_OF_ENTERTEXT_BOX, HEIGHT_OF_ENTERTEXT_BOX);

        if (chatText != null) {
            drawMeWrapped(g,x,y,chatText,fontblack,false,false,true,WIDTH_OF_ENTERTEXT_BOX,false);
        }

        //// 2 buttons in bottom right
        x += WIDTH_OF_MESSAGE_TEXT + SMALLGAP;
        //button 1
        //utils.setColor(g, 0xFFFFFF);
        utils.setColor(g, 255,255,255,TRANSPARENCY_LEVEL);
        utils.fillRoundRect(g, x, y, WIDTH_OF_USERLIST, (HEIGHT_OF_ENTERTEXT_BOX-SMALLGAP)/2);
        utils.setColor(g, OUTLINE_FOR_CHAT_BOXES);
        utils.drawRoundRect(g, x, y, WIDTH_OF_USERLIST, (HEIGHT_OF_ENTERTEXT_BOX-SMALLGAP)/2);
        String printme="Options";
        fontblack.drawString(g, printme, (x)+(fontblack.stringWidth(printme)/2) , y+(SMALLGAP*2)-3, 0);

        y+=(HEIGHT_OF_ENTERTEXT_BOX+SMALLGAP)/2;
        //button 2
        //  utils.setColor(g, 0xFFFFFF);
        utils.setColor(g, 255,255,255,TRANSPARENCY_LEVEL);
        utils.fillRoundRect(g, x, y, WIDTH_OF_USERLIST, (HEIGHT_OF_ENTERTEXT_BOX-SMALLGAP)/2);
        utils.setColor(g, OUTLINE_FOR_CHAT_BOXES);
        utils.drawRoundRect(g, x, y, WIDTH_OF_USERLIST, (HEIGHT_OF_ENTERTEXT_BOX-SMALLGAP)/2);
        printme="Leave";
        fontblack.drawString(g, printme, (x)+15+(fontblack.stringWidth(printme)/2) , y+(SMALLGAP*2)-3, 0);
        /////////////////////////

        if (showChallengeWindow) {
            utils.setColor(g, 0, 0, 0, TRANSPARENCY_LEVEL);
            utils.fillRoundRect(g, geometry.boardWidth() / 4, geometry.boardHeight() / 4, geometry.boardWidth() / 2, geometry.boardHeight() / 2);
            utils.setColor(g, Color.white);
            utils.drawRoundRect(g, geometry.boardWidth() / 4, geometry.boardHeight() / 4, geometry.boardWidth() / 2, geometry.boardHeight() / 2);

            int xabout = geometry.boardWidth() / 2;
            int yabout = (geometry.boardHeight() / 4) + geometry.tinyGap();

            //paint the about box
            printme = "Challenge this player?";
            fontwhite.drawString(g, printme, xabout - fontwhite.stringWidth(printme) / 2, yabout, 0);
            yabout += fontblack.getHeight();
            printme = personToChallenge;
            fontwhite.drawString(g, printme, xabout - fontwhite.stringWidth(printme) / 2, yabout, 0);
            yabout += fontblack.getHeight();
            printme = "IP:";
            fontwhite.drawString(g, printme, xabout - fontwhite.stringWidth(printme) / 2, yabout, 0);
        }
    }

    // gets called each frame to repaint
    public void update(Graphics g) {
        paint(g);
    }

    // wrapper around system out to console
    private static void log(String s) {
        Utils.log(String.format("thread-%s CustomCanvas{}:%s", Thread.currentThread().getName(), s));
    }

    /**
    * left click is used for everything, apart from cancelling which is done with
    * right button, for instance once a piece is stuck to the pointer right click
    * will return it to its original position (cancel the move)
    */
    //////// INPUT METHODS ////////////////
    @Override
    public void mouseClicked(MouseEvent e) {
        if (state == NETWORKING_ENTER_NAME) {
            if (e.getY() > getHeight() - 50) {
                NetworkChatClient.theURL = NetworkChatClient.localURL;
                System.out.println("swapped to local url");
            }
            return;
        }
        if (state == NETWORKING_LOBBY) {
            //check who they clicked on
            Enumeration ee = playerPositions.elements();
            while (ee.hasMoreElements()) {
                PlayerPos pos = (PlayerPos) ee.nextElement();
                if (e.getX() >= pos.x && e.getX() <= pos.x + pos.width &&
                    e.getY() >= pos.y && e.getY() <= pos.y + pos.height
                    ) {
                    log("Clicked on:" + pos.name);
                    showChallengeWindow = true;
                    personToChallenge = pos.name;
                    String theirIP = personToChallenge.substring(personToChallenge.indexOf("@") + 1, personToChallenge.length());
                    if (theirIP.isEmpty()) {
                        theirIP = SERVER_IP_ADDRESS;
                    }
                    log("IP TO CONNECT TO:" + theirIP);
                    GameNetworkClient client = new GameNetworkClient(this, theirIP);
                    Thread t = new Thread(client);
                    t.start();
                }
            }
            return;
        }
        if (gameComplete && e.getButton() == LEFT_MOUSE_BUTTON) {
            board.RESET_ENTIRE_GAME_VARS(soundOn);
            RESET_ENTIRE_GAME_VARS();
            state = SPLASH_SCREEN;
            return;
        }
        // so our mouse doesnt influence anything
        if (Bot.getFullAutoPlay() || (!Bot.dead && HUMAN_VS_COMPUTER && board.whoseTurnIsIt() == PlayerColor.BLACK) ) {
        } else {
            log("mouseClicked " + e.getX() + "," + e.getY());
            try {
                mouseClickedX(e.getX(), e.getY(), e.getButton());
            } catch (Exception exeption) {
                Utils._E(String.format("%s CustomCanvas ERROR %s", Thread.currentThread().getName(), exeption));
            }
        }
    }

    void mouseClickedX(int x, int y, int buttonPressed) throws Exception {
        splashCounter = maxSplashCounter + 1; // turn off splash if its on
        if (buttonPressed == RIGHT_MOUSE_BUTTON) {
            log("RIGHT BUTTON PRESSED");
            board.unstickPieceFromMouse();
            return;
        }

        switch(state) {
            case OPTIONS_SCREEN_LOCAL_OR_NETWORK:
                touchedButton(x, y);
                break;
            case OPTIONS_SCREEN_LOCAL_COMPUTER_OR_HUMAN:
                touchedButton(x, y);
                break;
            case GAME_IN_PROGRESS:
                touchedButton(x, y);
                checkIfPrefsButtonClickedOn(x, y);
                if (!board.showRollButton()) {
                    board.checkIfPieceClickedOn(x, y);
                    board.checkIfSpikeClickedOn(x, y);
                    board.checkIfPieceContainerClickedOn(x, y);
                    checkIfDoubleClickedOn(x, y);
                    checkIfResignClickedOn(x, y);
                }
                break;
        }
    }

    private void RESET_ENTIRE_GAME_VARS() {
        message2Players = VERSION;
        gameComplete = false;
        whiteResigned = false;
        blackResigned = false;

        // so it doesnt continuing playing on its own
        HUMAN_VS_COMPUTER = false;
        Bot.dead = true;
        splashCounter = 0;
        loadSounds(soundOn);
    }

    void nextTurn() {
        if (board.getCurrentPlayer().getColour() ==  PlayerColor.WHITE) {
            tellPlayers("Black's turn to roll.");
        } else {
            tellPlayers("White's turn to roll.");
        }
        board.nextTurn();
        Bot.dead = !Bot.getFullAutoPlay() && HUMAN_VS_COMPUTER &&
            (board.getCurrentPlayer().getColour() == PlayerColor.WHITE);
    }

    //checks if the preferences button is pressed and deals with it if so
    private void checkIfPrefsButtonClickedOn(int x, int y) {
        int prefx = preferencesButtonX();
        int prefy = preferencesButtonY();
        if (x >= prefx && x <= prefx + prefw) {
            if (y >= prefy && y <= prefy + prefh) {
                log("Prefs button clicked.");
                INFO = !INFO;
                if (INFO) {
                    tellPlayers("About");
                }
            }
        }
    }

    private void checkIfDoubleClickedOn(int x,int y) {
        int myX = doubleX;
        int myY = doubleY;
        int myWidth = doubleWidth;
        int myHeight = doubleHeight;
        if (x >= myX && x < (myX + myWidth)) {
            if (y > myY && y < (myY + myHeight)) {
                log("DOUBLE CLICKED ON!");
                sfxdouble.playSound();
                if (board.whoseTurnIsIt() == PlayerColor.WHITE) {
                    tellPlayers("White just doubled.");
                } else {
                    tellPlayers("Black just doubled.");
                }
            }
        }
    }

    private void checkIfResignClickedOn(int x, int y) {
        int myX = resignX;
        int myY = resignY;
        int myWidth = resignWidth;
        int myHeight = resignHeight;
        if (x >= myX && x < (myX+myWidth)) {
            if (y > myY && y < (myY+myHeight)) {
                log("RESIGN CLICKED ON!");
                sfxResign.playSound();
                gameComplete = true;
                if (board.whoseTurnIsIt() == PlayerColor.WHITE) {
                    whiteResigned = true;
                } else {
                    blackResigned = true;
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
         //so our mouse doesnt influence anything
        if (Bot.getFullAutoPlay() || (!Bot.dead && HUMAN_VS_COMPUTER && board.whoseTurnIsIt()==PlayerColor.BLACK) ) {
            //log("mouse wont respond");
        } else {
            if (NETWORK_GAME_IN_PROCESS) {
                if ((I_AM_CLIENT && board.whoseTurnIsIt() == PlayerColor.WHITE) ||
                    (I_AM_SERVER && board.whoseTurnIsIt() == PlayerColor.BLACK)) {
                    pointerX = e.getX();
                    pointerY = e.getY();
                }
            } else {
                //stick these bck in as local play was broke without--
                pointerX = e.getX();
                pointerY = e.getY();
                mouseHoverX = pointerX;
                mouseHoverY = pointerY;
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        log("keyPressed");

        //TEXT ENTRY IN LOBBY
        if (state == NETWORKING_LOBBY) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                chatClient.send();
                chatText = "";
            }
            String letter = "" + e.getKeyChar();
            chatText += letter;
            return;
        }

        //////////////NAME ENTRY////////
        if (state == NETWORKING_ENTER_NAME) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                state = NETWORKING_LOBBY;
                chatClient = new NetworkChatClient(this);
            }
            if ((e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_SHIFT || e.getKeyCode() == KeyEvent.VK_CAPS_LOCK)
                && NetworkChatClient.nick.length() > 0) {
                NetworkChatClient.nick = NetworkChatClient.nick.substring(0, NetworkChatClient.nick.length() - 1);
            } else {
                String letter = "" + e.getKeyChar();
                if (letter.equals(" ") || NetworkChatClient.nick.length() > 10) {
                } else {
                    NetworkChatClient.nick += letter;
                }
            }
            return;
        }

        if (e.getKeyChar() == 'q' || e.getKeyChar() == 'Q') {//QUIT
            System.exit(0);
        }
        if (e.getKeyChar() == 'f' || e.getKeyChar() == 'F') {//QUIT
            Bot.setFullAutoPlay(!Bot.getFullAutoPlay());
            HUMAN_VS_COMPUTER = !HUMAN_VS_COMPUTER;
            Bot.dead = !Bot.getFullAutoPlay();
            log("Bot.dead:" + Bot.dead);
            paintRobotMessages = Bot.getFullAutoPlay();
            log("FULL_AUTO_PLAY:" + Bot.getFullAutoPlay());
            if (Bot.getFullAutoPlay()) {
                tellRobot(true, "Bot turned on.");
            } else {
                tellRobot(true, "Bot turned off.");
            }
        }

        if (e.getKeyChar() == 'p' || e.getKeyChar() == 'P') {//QUIT
            //PAUSE
            PAUSED = !PAUSED;
            Bot.dead = PAUSED;
            log("PAUSED:" + PAUSED);
        }

        if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {//QUIT
            //PAUSE
            soundOn = !soundOn;
            log("soundOn:" + soundOn);
            loadSounds(soundOn);
            board.loadSounds(soundOn);
        }

        if (DEBUG_CONSOLE && e.getKeyChar() == 'x' || e.getKeyChar() == 'X') {
            sfxError.playSound();
        }

        if (!RELEASE_BUILD && e.getKeyChar() == 'c' || e.getKeyChar() == 'C') {
            showBoundaryBoxes = !showBoundaryBoxes;
        }
        if (!RELEASE_BUILD && e.getKeyChar() == 'l' || e.getKeyChar() == 'L') {
            Utils.CANVAS_LOGGING = !Utils.CANVAS_LOGGING;
        }

        if (!RELEASE_BUILD && e.getKeyChar() == 'd' || e.getKeyChar() == 'D') {
            DEBUG_CONSOLE = !DEBUG_CONSOLE;
        }

        if (e.getKeyChar() == 't' || e.getKeyChar() == 'T') {
            theme++;
            setTheme(theme);
        }
        if (DEBUG_CONSOLE) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                log("UP");
                debugMenuPos--;
                if (debugMenuPos < 0)
                    debugMenuPos = 0;

            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                log("DOWN");
                debugMenuPos++;
                if (debugMenuPos > LAST_DEBUG_OPTION)
                    debugMenuPos = LAST_DEBUG_OPTION;

            }
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                log("LEFT");
                debugOptionChanged(DEBUGLEFT);
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                log("RIGHT");
                debugOptionChanged(DEBUGRIGHT);
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    private void setTheme(int theme_) {
        theme = theme_;
        log("theme:" + theme);
        if (theme > MAX_THEMES) {
            theme = DEFAULT;
        }

        switch (theme) {
            case DEFAULT:
                log("THEME SET TO DEFAULT");
                themeName = "default";
                if (!firstThemeSet)
                    tellPlayers("Theme set to " + themeName);
                themecolours = defaultms;
                break;
            case METALIC:
                log("THEME SET TO METALIC");
                themeName = "metalic";
                if (!firstThemeSet)
                    tellPlayers("Theme set to " + themeName);
                themecolours = metalic;
                break;
            case CLASSIC:
                log("THEME SET TO CLASSIC");
                themeName = "classic";
                if (!firstThemeSet)
                    tellPlayers("Theme set to " + themeName);
                themecolours = classic;
                break;
            case FUNNYMAN:
                log("THEME SET TO FUNNYMAN");
                themeName = "funnyman";
                if (!firstThemeSet)
                    tellPlayers("Theme set to " + themeName);
                themecolours = funnyman;
                break;
            case BUMBLEBEE:
                log("THEME SET TO BUMBLEBEE");
                themeName = "bumblebee";
                if (!firstThemeSet)
                    tellPlayers("Theme set to " + themeName);
                themecolours = bumblebee;
                break;
            default:
                Utils._E("theme is out of range!");
        }
        firstThemeSet = false;
        //assigns each colour from the one specified
        gameColour.setBackgroundColour(themecolours[0]);
        gameColour.setRollButtonColour(themecolours[2]);
        gameColour.setPanelColour(themecolours[1]);
        gameColour.setBoardColour(themecolours[3]);
        gameColour.setBarColour(themecolours[4]);
        for (int i = 0; i < themecolours.length; i++) {
            switch (i) {
                case 5:
                    Spike.BLACK_SPIKE_COLOUR = themecolours[i];
                    break;
                case 6:
                    Spike.WHITE_SPIKE_COLOUR = themecolours[i];
                    break;
                case 7:
                    Piece.WHITE_PIECE_COLOUR = themecolours[i];
                    break;
                case 8:
                    Piece.BLACK_PIECE_COLOUR = themecolours[i];
                    break;
                case 9:
                    Piece.WHITE_PIECE_INNER_COLOUR = themecolours[i];
                    break;
                case 10:
                    Piece.BLACK_PIECE_INNER_COLOUR = themecolours[i];
                    break;
                case 11:
                    Die.DIE_COLOUR = themecolours[i];
                    break;
                case 12:
                    Die.DOT_COLOUR = themecolours[i];
                    break;
                default:
                    if (i > 12)
                        Utils._E("theme state error, should not exceed 12!");
            }
        }
        makeColourObjects();
        board.makeColourObjects();
        Piece.makeColourObjects();
        Die.makeColourObjects();
        log("Theme is loaded now and working.");
    }

    private void makeColourObjects() {
        panel_colour = new Color(gameColour.getPanelColour());
        background_colour = new Color(gameColour.getBackgroundColour());
        roll_button_colour = new Color(gameColour.getRollButtonColour());
    }

    private void loadCustomFonts() {
        boolean land = false;
        int gap = 10;
        int GAP = 3;
        try {
            Utils.log("loading fonts:");
            fontwhite = CustomFont.getFont(utils.loadImage("/whitefont.png"),
                land, 32, 93, GAP, gap, this);
            fontblack = CustomFont.getFont(utils.loadImage("/blackfont.png"),
                land, 32, 93, GAP, gap, this);
        } catch (Exception e) {
            Utils._E("== error loading fonts " + e.getMessage());
        }
    }

    //for debugging, paints sytem.out to screen
    private void paintStringsToCanvas(Graphics g) {
        Enumeration e = Utils.systemOuts.elements();
        int x = 3;
        int y = 3;
        while (e.hasMoreElements()) {
             String printthis = (String)e.nextElement();
             fontwhite.drawString(g, printthis, x, y, 0);y+=fontwhite.getHeight();
        }
    }

    public void tellPlayers(String s) {
        playerMessageSetTimeLong = System.currentTimeMillis();
        message2Players = s;
    }

    static void robotExplain(String s) {
         robotMessageSetTimeLong = System.currentTimeMillis();
         robotMoveDesc = s;
    }

    void paintMessageToPlayers(Graphics g) {
        utils.setColor(g, 0, 0, 0, TRANSPARENCY_LEVEL);
        if (gameComplete) {
            messageWidth = fontwhite.stringWidth(message2Players + "  ");
            messagex = (geometry.boardWidth() / 2) - messageWidth / 2;
            messageHeight = fontwhite.getHeight();
            messagey = (geometry.boardHeight() / 2) - messageHeight / 2;

            utils.fillRoundRect(g, messagex, messagey, messageWidth, messageHeight);
            utils.setColor(g, Color.WHITE);
            utils.drawRoundRect(g, messagex, messagey, messageWidth, messageHeight);
            //draw message in middle of screen
            fontwhite.drawString(g, message2Players, messagex + 7, messagey + 1, 0);
        } else {
            messageWidth = fontwhite.stringWidth(message2Players + "  ");
            messagex = 10;
            messagey = geometry.boardHeight() - (fontwhite.getHeight() + geometry.tinyGap());
            messageHeight = fontwhite.getHeight();
            utils.fillRoundRect(g, messagex, messagey, messageWidth, messageHeight);
            utils.setColor(g, Color.WHITE);
            utils.drawRoundRect(g, messagex, messagey, messageWidth, messageHeight);
            //draw message in bottom left.
            fontwhite.drawString(g, message2Players, messagex + 7, messagey + 1, 0);
        }
    }

    void paintRobotMessage(Graphics g) {
        utils.setColor(g, 0, 0, 0, TRANSPARENCY_LEVEL);
        messageWidth = fontwhite.stringWidth(robotMoveDesc + "  ");
        messagex = geometry.boardWidth() - (messageWidth + 10);
        messagey = 10;//(fontwhite.getHeight()+geometry.tinyGap());
        messageHeight = fontwhite.getHeight();

        utils.fillRoundRect(g, messagex, messagey, messageWidth, messageHeight);
        utils.setColor(g, Color.RED);
        utils.drawRoundRect(g, messagex, messagey, messageWidth, messageHeight);
        fontwhite.drawString(g, robotMoveDesc, messagex + 7, messagey + 1, 0);
    }

    /////////////////////ADJUST COLOURS HERE ////////////////////////////
    //themes: specify colours for colour themes
    private static int themecolours[]; // this gets assigned in constructor

    // DEFAULT VALUES (ms xp backgammon colours)
    static int defaultms[] = {
        0x993300,
        0x000000,
        0xffcc66,
        0x000000,
        0x993300,
        Spike.BLACK_SPIKE_COLOUR,
        Spike.WHITE_SPIKE_COLOUR,
        Piece.WHITE_PIECE_COLOUR,
        Piece.BLACK_PIECE_COLOUR,
        Piece.WHITE_PIECE_INNER_COLOUR,
        Piece.BLACK_PIECE_INNER_COLOUR,
        Die.DIE_COLOUR,
        Die.DOT_COLOUR
    };

    public static int metalic[] = {
        /*BACKGROUND_COLOUR*/               0xffffff,
        /*PANEL_COLOUR*/                    0x828284,
        /*ROLL_BUTTON_COLOUR*/              0xffffff,
        /*Board.BOARD_COLOUR*/              0x9b9b9b,
        /*Board.BAR_COLOUR*/                0x8b898c,
        /*Spike.BLACK_SPIKE_COLOUR*/        0xc7c8cd,
        /*Spike.WHITE_SPIKE_COLOUR*/        0xa3a4a8,
        /*Piece.WHITE_PIECE_COLOUR*/        0xedf0f5,
        /*Piece.BLACK_PIECE_COLOUR*/        0x1a1a22,
        /*Piece.WHITE_PIECE_INNER_COLOUR*/  0xffffff,
        /*Piece.BLACK_PIECE_INNER_COLOUR*/  0xffffff,
        /*Die.DIE_COLOUR*/                  0x807875,
        /*Die.DOT_COLOUR*/                  0xe5e0da
    };

    public static int classic[] = {
        /*BACKGROUND_COLOUR*/               0x2c632a,
        /*PANEL_COLOUR*/                    0x002001,
        /*ROLL_BUTTON_COLOUR*/              0xfe1e1c,
        /*Board.BOARD_COLOUR*/              0xf4ebca,
        /*Board.BAR_COLOUR*/                0x245223,
        /*Spike.BLACK_SPIKE_COLOUR*/        0x99643c,
        /*Spike.WHITE_SPIKE_COLOUR*/        0xed974c,
        /*Piece.WHITE_PIECE_COLOUR*/        0xfefbf2,
        /*Piece.BLACK_PIECE_COLOUR*/        0x363b3f,
        /*Piece.WHITE_PIECE_INNER_COLOUR*/  0xffffff,
        /*Piece.BLACK_PIECE_INNER_COLOUR*/  0xffffff,
        /*Die.DIE_COLOUR*/                  0xfe1e1c,
        /*Die.DOT_COLOUR*/                  0xfffdfe
    };


    public static int funnyman[] = {
        /*BACKGROUND_COLOUR*/               0x661913,
        /*PANEL_COLOUR*/                    0x210d0c,
        /*ROLL_BUTTON_COLOUR*/              0xffffff,
        /*Board.BOARD_COLOUR*/              0x9d581d,
        /*Board.BAR_COLOUR*/                0x490f0e,
        /*Spike.BLACK_SPIKE_COLOUR*/        0x290d0a,
        /*Spike.WHITE_SPIKE_COLOUR*/        0x6e1213,
        /*Piece.WHITE_PIECE_COLOUR*/        0x4e3113,
        /*Piece.BLACK_PIECE_COLOUR*/        0x841b25,
        /*Piece.WHITE_PIECE_INNER_COLOUR*/  0xffffff,
        /*Piece.BLACK_PIECE_INNER_COLOUR*/  0xffffff,
        /*Die.DIE_COLOUR*/                  0xffffff,
        /*Die.DOT_COLOUR*/                  0x791216
    };

    public static int bumblebee[] = {
        /*BACKGROUND_COLOUR*/               0x202427,
        /*PANEL_COLOUR*/                    0x3a3a3a,
        /*ROLL_BUTTON_COLOUR*/              0xe4ff00,
        /*Board.BOARD_COLOUR*/              0x50555b,
        /*Board.BAR_COLOUR*/                0x545454,
        /*Spike.BLACK_SPIKE_COLOUR*/        0x030504,
        /*Spike.WHITE_SPIKE_COLOUR*/        0xe4ff00,
        /*Piece.WHITE_PIECE_COLOUR*/        0xb1995d,
        /*Piece.BLACK_PIECE_COLOUR*/        0x404443,
        /*Piece.WHITE_PIECE_INNER_COLOUR*/  0xffffff,
        /*Piece.BLACK_PIECE_INNER_COLOUR*/  0xffffff,
        /*Die.DIE_COLOUR*/                  0x000000,
        /*Die.DOT_COLOUR*/                  0xe4ff00
    };

    ///ROBOT STUFF
    public static void tellRobot(boolean b, String s) {
        if (s != null) {
            robotExplain(s);
        }
    }

    private int drawMeWrapped(Graphics g,int x, int y, String wrapMe, CustomFont font, boolean backdrop,
                             boolean outline, boolean justifyleft, int width, boolean justifyRight) {
        if (wrapMe == null) {
            log("drawMeWrapped received a null string");
        }
        //////these texts need to be wrapped as they could be long
        Vector textLinesForWrappingTMP = separateTextNEW(wrapMe, width, getHeight(), font);

        int stringHeight = y;
        int Xtmp=x;
        for (int i = 0; i < textLinesForWrappingTMP.size(); i++) {
            if (stringHeight >= 0) {
                String printme = (String)textLinesForWrappingTMP.elementAt(i);
                if (justifyleft) {
                    Xtmp=x;
                } else if (justifyRight) {
                } else {
                    Xtmp=(geometry.boardWidth()/2)-(font.stringWidth(printme)/2);
                }
                // this is a bit of a hack but a legacy form the custom font days
                // check if the end of the text is reached and control users ability to scroll with bools.
                // so we dont let them keep scrolling
                boolean allowScrollingDOWN = printme.indexOf(SPECIAL_END_SYMBOL) == -1;
                if (!allowScrollingDOWN) {
                    log("DONT ALLOW ASCROLL SINCE SPECIAL END SYMBOL DETECTED allowScrollingDOWN:" + allowScrollingDOWN);
                    //ok now remove the special end sybol so it doesnt print
                    printme=printme.substring(0,printme.indexOf(SPECIAL_END_SYMBOL));
                }
                fontblack.drawString( g,printme,Xtmp,stringHeight,0);
            }
            stringHeight+=(font.getHeight());//-5);
        }
        y = stringHeight;
        return y;
    }

    private static Vector separateTextNEW(String string, int width, int height, CustomFont font) {
        Vector lines = new Vector();
        String aline = "";
        StringTokenizer st = new StringTokenizer(string, " ");
        String s;
        while (st.hasMoreElements()) {
            s = st.nextToken(); // if its not null s failed to get used last time
            s = s.trim();
            if (s.equals("[p]") || s.equals("[br]") || s.equals("[br2]") || s.equals("[br][br]")) {
                //<p>"))
                // [p] [br] [br][br] ALL WORK LIKE HTMLS <P>
                // [br2] works like HTMLS <BR>
                lines.addElement(aline);
                if (!s.equals("[br2]")) {//dont add a empty line if its a br2
                    lines.addElement(" ");
                }
                aline = "";
            } else {
                if (font.stringWidth(aline + " " + s) < width) {
                    aline += s + " ";
                    s = null;
                } else {
                    lines.addElement(aline);
                    aline = "";
                }
                if (!st.hasMoreElements()) {
                    //if its finished just add last bit now to new line
                    if (s != null && (!s.equals("[p]") || s.equals("[br2]") || s.equals("[br]") || s.equals("[br][br]"))) {
                        lines.addElement(aline + "" + s);
                    } else {
                        lines.addElement(aline.trim());
                    }
                } else {
                    if (s != null) {
                        if (s.equals("[p]") || s.equals("[br]") || s.equals("[br2]") || s.equals("[br][br]")) {
                            lines.addElement(aline);
                            if (!s.equals("[br2]")) {//dont add a empty line if its a br2
                                lines.addElement(" ");
                            }
                            aline = "";
                        } else {
                            aline += "" + s + " ";
                        }
                    }
                }
            }
        }
        return lines;
    }

    private int preferencesButtonX() {
        return getWidth() - (geometry.borderWidth() + prefw + geometry.tinyGap() / 2);
    }

    private int preferencesButtonY() {
        return geometry.borderWidth();
    }

    void onHumanPlayerConnectedToServer() {
        log("Human player connected to server");
        Bot.dead = true;
        startGame();
        NETWORK_GAME_IN_PROCESS = true;
        I_AM_CLIENT = true;
        NetworkChatClient.KEEP_LOBBY_GOING = false;
    }

    void startGame() {
        int val = Utils.getRand(0, 999_999);
        String playerStr = "White";
        Player player = board.getWhitePlayer();
        if (val >= 500_000) {
            player = board.getBlackPlayer();
            playerStr = "Black";
            if (HUMAN_VS_COMPUTER) {
                Bot.dead = false;
            }
        }
        board.setCurrentPlayer(player);
        tellPlayers(String.format("%s won the roll off", playerStr));
        state = GAME_IN_PROGRESS;
    }

    void startNetworkGame() {
        NETWORK_GAME_IN_PROCESS = true;
        Bot.dead = true;
        I_AM_SERVER = true;
        NetworkChatClient.KEEP_LOBBY_GOING = false;
        startGame();
    }

    static void playErrorSound() {
        sfxError.playSound();
    }

    public boolean gameComplete() {
        return this.gameComplete;
    }

    public boolean humanVsComputer() {
        return HUMAN_VS_COMPUTER;
    }
}
