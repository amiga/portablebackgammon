package gamelogic;

import java.util.List;

public interface GameState {

    /**
     *
     * @return Complete state as sequence of bytes
     */
    List<Byte> get();

    /**
     *
     * @return true if one of the players win
     */
    boolean isTerminal();
}
