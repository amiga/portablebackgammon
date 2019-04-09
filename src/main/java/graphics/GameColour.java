package graphics;


public class GameColour {

    private static int DEFAULT_BACKGROUND_COLOUR = 0x993300;
    private static int DEFAULT_BOARD_COLOUR = 0x000000;
    private static int DEFAULT_BAR_COLOUR = 0x993300;
    private static int DEFAULT_PANEL_COLOUR = 0x000000;
    private static int DEFAULT_ROLL_BUTTON_COLOUR = 0xffcc66;

    private int backgroundColour;
    private int boardColour;
    private int barColour;
    private int panelColour;
    private int rollButtonColour;

    public GameColour() {
        backgroundColour = DEFAULT_BACKGROUND_COLOUR;
        boardColour = DEFAULT_BOARD_COLOUR;
        barColour = DEFAULT_BAR_COLOUR;
        panelColour = DEFAULT_PANEL_COLOUR;
        rollButtonColour = DEFAULT_ROLL_BUTTON_COLOUR;
    }

    public int getBackgroundColour() {
        return backgroundColour;
    }

    public int getBoardColor() {
        return boardColour;
    }

    public int getBarColor() {
        return barColour;
    }

    public int getPanelColour() {
        return panelColour;
    }

    public void setBackgroundColour(int backgroundColour) {
        this.backgroundColour = backgroundColour;
    }

    public void setBoardColour(int boardColour) {
        this.boardColour = boardColour;
    }

    public void setBarColour(int barColour) {
        this.barColour = barColour;
    }

    public void setPanelColour(int panelColour) {
        this.panelColour = panelColour;
    }

    public int getRollButtonColour() {
        return rollButtonColour;
    }

    public void setRollButtonColour(int rollButtonColour) {
        this.rollButtonColour = rollButtonColour;
    }
}
