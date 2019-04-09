package gamelogic;

import java.util.ArrayList;
import java.util.List;

public class GameStateImpl implements GameState {

    private int die1, die2;
    private List<Byte> player1Checkers;
    private List<Byte> player2Checkers;
    private Byte secondMoveAtDouble;
    private Byte winPoints;
    private boolean player1Turn;
    private final int boardSize;
    private final int totalCheckers;

    GameStateImpl(GameConfig config) {
        this.boardSize = config.boardSize();
        this.totalCheckers = config.totalCheckers();
        this.player1Turn = true;
        this.winPoints = 1;
        this.secondMoveAtDouble = 0;
        this.player1Checkers = new ArrayList<>(boardSize + 1);
        this.player2Checkers = new ArrayList<>(boardSize + 1);
        initCheckers(player1Checkers);
        initCheckers(player2Checkers);
    }

    @Override
    public List<Byte> get() {
        int stateSize = player1Checkers.size() + player2Checkers.size() + 2 + 1;
        ArrayList<Byte> state = new ArrayList<>(stateSize);
        state.addAll(player1Checkers);
        state.addAll(player2Checkers);
        state.add((byte)die1);
        state.add((byte)die2);
        state.add(secondMoveAtDouble);
        return state;
    }

    @Override
    public boolean isTerminal() {
        return player1Checkers.get(0) == totalCheckers || player2Checkers.get(0) == totalCheckers;
    }

    private void initCheckers(List<Byte> checkers) {
        if (totalCheckers != 15 || boardSize != 24) {
            throw new RuntimeException("Unsupported configuration");
        }
        for (int i = 0; i <= boardSize; ++i) {
            checkers.add((byte) 0);
        }
        checkers.set(24, (byte)2);
        checkers.set(13, (byte)5);
        checkers.set(8, (byte)3);
        checkers.set(6, (byte)5);
    }
}
