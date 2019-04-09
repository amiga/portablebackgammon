package lowlevel;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Shape;
import java.awt.image.ImageObserver;

class CustomFont {

    private final static int availWidth = 50000;// this is for limiting, if its 1000 we`re not limiting it now
    private final static int availHeight = 50000;

    private ImageObserver imageObserver;
    private int height;
    private int width;
    private Image image;
    private boolean isLandscape;
    private int charHeight;
    private int charWidth;
    private int compensator;
    private int gap;

    static CustomFont getFont(Image i, boolean landscape, int compensator_,
                              int amountOfLetters_, int widthBetweenPixelsCompensator_, int gap_,
                              ImageObserver imageObserver) {
        System.out.println("font created.");
        return new CustomFont(i, landscape, compensator_, amountOfLetters_,
            widthBetweenPixelsCompensator_, gap_, imageObserver);
    }

    private CustomFont(Image inImage, boolean landscape, int compensator_,
                       int amountOfLetters_, int widthBetweenPixelsCompensator_,
                       int gap_, ImageObserver imageObserver) {
        this.imageObserver = imageObserver;
        isLandscape = false;
        charHeight = 0;
        charWidth = 0;
        compensator = 33;
        compensator = compensator_;
        isLandscape = landscape;
        image = inImage;
        gap = gap_;
        try {
            height = image.getHeight(imageObserver) + 2;
            charHeight = height;
            width = image.getWidth(imageObserver) / amountOfLetters_;
            charWidth = width - widthBetweenPixelsCompensator_;
            Utils.log("charWidth:" + charWidth);
        } catch(Throwable t) {
            t.printStackTrace();
            throw new IllegalArgumentException("Specified font is invalid: " + t);
        }
    }

    private int charsWidth(char ch[], int offset, int length)
    {
        return length * charWidth;
    }

    private int getWidth() {
        return charWidth;
    }

    int getHeight() {
        return height - 1;
    }

    int stringWidth(String str) {
        return str.length() * getWidth();
    }

    void drawChars(Graphics g, char data[], int offset, int length, int x, int y, int anchor) {
        if(isLandscape) {
            if((anchor & 8) != 0) {
                x += height * length;
            } else {
                if((anchor & 1) != 0)
                    x += (height * length) / 2;
            }
            if((anchor & 0x20) != 0) {
                y -= height;
            } else {
                if((anchor & 2) != 0)
                    y += height / 2;
            }
        } else {
            if((anchor & 8) != 0) {
                x -= charsWidth(data, offset, length);
            } else {
                if((anchor & 1) != 0)
                    x -= charsWidth(data, offset, length) / 2;
            }
            if((anchor & 0x20) != 0) {
                y -= height;
            } else {
                if((anchor & 2) != 0)
                    y -= height / 2;
            }
        }
        for(int i = 0; i < length; i++) {
            char c = data[offset + i];
            drawCharInternal(g, c, x, y, 0x10 | 4, i);
            if(isLandscape)
                x -= charHeight;
            else
                x += charWidth;
        }
    }

    private void drawCharInternal(Graphics g, char character, int x, int y, int anchor, int charsAlong) {
        Shape s = g.getClip();
        int comps = compensator;
        if(isLandscape) {
            if(character != ' ') {
               g.drawImage(image, width * (character - compensator), 0,
                   width, height, 0, y, x - charsAlong * gap - height, anchor, imageObserver);
         
            }
        } else {
            if(character == ' '){
                return;
            }
			if ( x > availWidth || y > availHeight + 15 ) {//15 buffer period //specific to SCROLL PROJECT
			} else {
			    g.setClip(x, y, width, height);
				g.drawImage(image, x - width * (character - comps), y, imageObserver);
				g.setClip(s);
			}
        }
    }

    void drawString(Graphics g, String str, int x, int y, int anchor) {
        drawChars(g, str.toCharArray(), 0, str.length(), x, y, anchor);
    }
}
