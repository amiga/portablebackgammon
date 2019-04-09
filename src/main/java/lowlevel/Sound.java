package lowlevel;

import java.io.InputStream;
import javax.sound.sampled.*;

public class Sound {

    private Clip clip;
    private String fileName;
    private boolean playOnce = false;

    private static class CloseOnStopListener implements LineListener {

        private final DataLine line;
        CloseOnStopListener(DataLine line) {
            this.line = line;
        }

        @Override
        public void update(LineEvent event) {
            if (event.getType() == LineEvent.Type.STOP) {
                line.close();
            }
        }
    }

    public Sound(String filename, boolean playOnce) {
        this.fileName = filename;
        this.playOnce = playOnce;
    }

    public Sound(String filename) {
        this(filename, false);
    }

    public void playSound() {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void loadSound(boolean soundOn) {
        if (clip != null) {
            clip.close();
            clip = null;
        }
        if (soundOn) {
            try {
                InputStream stream = this.getClass().getResourceAsStream(fileName);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(stream);
                AudioFormat audioFormat = audioInputStream.getFormat();
                DataLine.Info dataLineInfo = new DataLine.Info(Clip.class, audioFormat);
                clip = (Clip) AudioSystem.getLine(dataLineInfo);
                if (playOnce) {
                    clip.addLineListener(new CloseOnStopListener(clip));
                }
                clip.open(audioInputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
