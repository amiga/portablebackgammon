package graphics;

public class Geometry {

    private int canvasWidth;
    private int canvasHeight;

    public Geometry(int canvasWidth, int canvasHeight) {
        this.canvasHeight = canvasHeight;
        this.canvasWidth = canvasWidth;
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public void setCanvasWidth(int canvasWidth) {
        this.canvasWidth = canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public void setCanvasHeight(int canvasHeight) {
        this.canvasHeight = canvasHeight;
    }

    // Fraction of the canvas width game panel at the right size occupies
    public int panelSizeFraction() {
        return 5;
    }

    public int boardWidth() {
        int initWidth = getCanvasWidth() / panelSizeFraction() * (panelSizeFraction() - 1);
        int spikeArea = initWidth - 2 * borderWidth() - centralBarWidth();
        return initWidth - spikeArea % 12;
    }

    public int borderWidth() {
        return canvasWidth / 80;
    }

    public int boardHeight() {
        return getCanvasHeight();
    }

    public int centralBarWidth() {
        return borderWidth() * 2;
    }

    public int panelWidth() {
        return getCanvasWidth() - boardWidth() - borderWidth();
    }

    public int spikeWidth() {
        return (boardWidth() - (borderWidth() * 4)) / 12;
    }

    public int spikeHeight() {
        int halfBoard = (boardHeight() - borderWidth() * 2) / 2;
        return halfBoard - halfBoard / 10;
    }

    public int tinyGap() {
        return 5;
    }

    public int pieceDiameter() {
        return (boardHeight() - 2 * borderWidth()) / 14;
    }

    public int dieSize() {
        return pieceDiameter();
    }

    public int dieDotDiameter() {
        return  dieSize() / 5;
    }

    public int miniDieSize() {
        return dieSize() - 6;
    }

    public int pieceRadius() {
        return pieceDiameter() / 2;
    }

    public int containerWidth() {
        return panelWidth() / 3;
    }

    public int containerHeight() {
        return containerSubSize() * 15;
    }

    public int containerSubSize() {
        return boardHeight() / 70;
    }

    public int panelFontHeight() {
        return 20;
    }

    public int containerX() {
        return boardWidth() + ((panelWidth() / 4) - (containerWidth() / 2));
    }

    public int containerMargin() {
        return panelFontHeight() * 3 + borderWidth() * 2 + tinyGap();
    }

    public int whiteContainerY() {
        return containerMargin();
    }

    public int blackContainerY() {
        return boardHeight() - (containerHeight() + containerMargin());
    }
}
