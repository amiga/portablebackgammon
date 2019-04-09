package gamelogic;

import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class GameConfigTest {

    @Test
    @DisplayName("Default values are loaded from classpath props")
    void test1() {
        ConfigFactory.setProperty(
            "configFileName", "somenonexistingconfig.config");
        GameConfig config = ConfigFactory.create(GameConfig.class);
        assertEquals(24, config.boardSize());
        assertFalse(config.soundOn());
        assertTrue(config.drawMousePointer());
        assertEquals(50, config.maxSplashCounter());
        assertTrue(config.enableDoubleBuffering());
    }

    @Test
    @DisplayName("Config file takes precedence over classpath file")
    void test2() throws Exception {
        File tempFile = File.createTempFile("testbackgammon-", ".properties");
        Files.write(tempFile.toPath(), "boardSize=101".getBytes());
        ConfigFactory.setProperty("configFileName", tempFile.getAbsolutePath());
        GameConfig config = ConfigFactory.create(GameConfig.class);
        assertEquals(101, config.boardSize());
    }
}
