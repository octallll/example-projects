package academy;

import academy.game.HangmanGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HangmanGameTest {
    private HangmanGame game;

    @BeforeEach
    void setUp() {
        game = new HangmanGame("тест", 3);
    }

    @DisplayName("В начале никакие символы не открыты")
    @Test
    void testInitialState() {
        assertEquals("****", game.getState());
        assertFalse(game.isWin());
        assertFalse(game.isGameOver());
    }

    @DisplayName("Учитываются все вхождения символов")
    @Test
    void testCorrectGuess() {
        game.tryGuess('т');
        assertEquals("т**т", game.getState());
        assertFalse(game.isWin());
    }

    @DisplayName("Игра выиграна после всех введенных символов слова")
    @Test
    void testWinCondition() {
        game.tryGuess('т');
        game.tryGuess('е');
        game.tryGuess('с');
        assertTrue(game.isWin());
        assertTrue(game.isGameOver());
    }

    @DisplayName("Игра не выиграна после неправильно угаданного символа")
    @Test
    void testIncorrectGuess() {
        game.tryGuess('а');
        assertFalse(game.isWin());
    }

    @DisplayName("Игра заканчивается по истечению попыток")
    @Test
    void testGameOver() {
        game.tryGuess('а');
        game.tryGuess('б');
        game.tryGuess('в');
        assertTrue(game.isGameOver());
        assertFalse(game.isWin());
    }

    @DisplayName("При попытке предоположить символ, который уже вводился состояние игры не меняется")
    @Test
    void testDuplicateGuess() {
        game.tryGuess('т');
        String firstState = game.getState();
        game.tryGuess('т');
        assertEquals(firstState, game.getState());
    }
}
