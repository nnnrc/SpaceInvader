package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import engine.GameState;

class InfiniteGameScreenSpeedProgressTest {

    @ParameterizedTest
    @CsvSource({
            "0, 1, 0.0",
            "1, 1, 0.05",
            "10, 1, 0.5",
            "19, 1, 0.95",
            "20, 2, 0.0",
            "21, 2, 0.05",
            "30, 2, 0.5",
            "39, 2, 0.95",
            "40, 3, 1.0",
            "41, 3, 1.0",
            "60, 3, 1.0",
            "2147483647, 3, 1.0"
    })
    void reflectsSpeedThresholdsAndProgress(final int defeatedEnemies,
            final int expectedLevel, final double expectedProgress) {
        InfiniteGameScreen screen = screen(defeatedEnemies);

        assertEquals(expectedLevel, screen.getEnemySpeedLevel());
        assertEquals(expectedProgress, screen.getEnemySpeedProgress(), 0.000001);
    }

    @Test
    void newRunStartsAtLevelOneWithAnEmptyBarAfterAMaxLevelRun() {
        InfiniteGameScreen previousRun = screen(40);
        assertEquals(3, previousRun.getEnemySpeedLevel());
        assertEquals(1.0, previousRun.getEnemySpeedProgress(), 0.000001);

        InfiniteGameScreen newRun = screen(0);
        assertEquals(1, newRun.getEnemySpeedLevel());
        assertEquals(0.0, newRun.getEnemySpeedProgress(), 0.000001);
    }

    private InfiniteGameScreen screen(final int defeatedEnemies) {
        return new InfiniteGameScreen(
                new GameState(1, 0, 3, 0, defeatedEnemies), 448, 520, 60);
    }
}
