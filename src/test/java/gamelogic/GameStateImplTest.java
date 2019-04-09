package gamelogic;

import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GameStateImplTest {

    @Test
    @DisplayName("Default initialization")
    public void test1() {
        ConfigFactory.setProperty("configFileName", "nobody_cares");
        GameConfig config = ConfigFactory.create(GameConfig.class);
        GameState state = new GameStateImpl(config);
        Byte[] initPosition = {0,  0,0,0,0,0,5, 0,3,0,0,0,0,  5,0,0,0,0,0, 0,0,0,0,0,2};
        ArrayList<Byte> expected = new ArrayList<>(Arrays.asList(initPosition));
        expected.addAll(expected);
        expected.add((byte)0);
        expected.add((byte)0);
        expected.add((byte)0);
        assertArrayEquals(expected.toArray(new Byte[0]), state.get().toArray(new Byte[0]));
        assertFalse(state.isTerminal());

    }
}
