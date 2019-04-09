package gamelogic;

import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({ "file:${configFileName}",
    "classpath:gamelogic/backgammon.properties" })
public interface GameConfig extends Config {
    int boardSize();

    int totalCheckers();

    boolean soundOn();

    @Key("gui.maxSplashCounter")
    int maxSplashCounter();

    @Key("gui.drawMousePointer")
    boolean drawMousePointer();

    @Key("gui.enableDoubleBuffering")
    boolean enableDoubleBuffering();

    @Key("game.alwaysRollDouble")
    boolean alwaysRollDouble();
}
