package academy;

import academy.game.HangmanScanner;
import academy.utils.WordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HangmanScannerTest {
    private InputStream getInputStreamByInput(String input) {
        return new ByteArrayInputStream(input.getBytes());
    }

    @DisplayName("Категория вводится корректно")
    @Test
    void testGetCategory() {
        HangmanScanner scanner = new HangmanScanner(getInputStreamByInput("растения\n"));
        assertEquals("растения", scanner.getCategory());
    }

    @DisplayName("Сложность вводится корректно")
    @Test
    void testGetComplexity() {
        HangmanScanner scanner = new HangmanScanner(getInputStreamByInput("2\n"));
        assertEquals(2, scanner.getComplexity("растения"));
    }

    @DisplayName("Сканнер правильно сканирует символы")
    @Test
    void testGetNextChar() {
        HangmanScanner scanner = new HangmanScanner(getInputStreamByInput("а\n"));
        assertEquals('а', scanner.getNextChar());
    }

    @DisplayName("Из введенной строки выбирается первый символ")
    @Test
    void testGetNextCharInString() {
        HangmanScanner scanner = new HangmanScanner(getInputStreamByInput("а тест !\n"));
        assertEquals('а', scanner.getNextChar());
    }

    @DisplayName("При некорректном вводе категории выбирается случайная")
    @Test
    void testIncorrectCategory() {
        HangmanScanner scanner1 = new HangmanScanner(getInputStreamByInput("\n"));
        assertDoesNotThrow(scanner1::getCategory);

        HangmanScanner scanner2 = new HangmanScanner(getInputStreamByInput("no cat\n"));
        assertDoesNotThrow(scanner2::getCategory);

        HangmanScanner scanner3 = new HangmanScanner(getInputStreamByInput("no category\n"));
        assertTrue(WordUtil.getCategoryList().contains(scanner3.getCategory()));
    }

    @DisplayName("При вводе некорректной сложности выбирается случайная сложность")
    @Test
    void testIncorrectComplexity() {
        HangmanScanner scanner1 = new HangmanScanner(getInputStreamByInput("\n"));
        assertDoesNotThrow(() -> scanner1.getComplexity("растения"));

        HangmanScanner scanner2 = new HangmanScanner(getInputStreamByInput("no such complexity\n"));
        assertDoesNotThrow(() -> scanner2.getComplexity("еда"));

        HangmanScanner scanner3 = new HangmanScanner(getInputStreamByInput("no complex\n"));
        int complexity = scanner3.getComplexity("еда");
        assertTrue(1 <= complexity && complexity <= 3);
    }

    @DisplayName("Должны корректно обрабатываться оба регистра")
    @Test
    void testRegister() {
        HangmanScanner scanner1 = new HangmanScanner(getInputStreamByInput("РаСтЕнИя\n"));
        assertEquals("растения", scanner1.getCategory());

        HangmanScanner scanner2 = new HangmanScanner(getInputStreamByInput("И\n"));
        assertEquals('и', scanner2.getNextChar());
    }
}
