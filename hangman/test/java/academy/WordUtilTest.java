package academy;

import academy.utils.WordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

public class WordUtilTest {
    @DisplayName("Генератор должен бросать, если нет категории")
    @Test
    void testGenerateWordInvalidCategory() {
        assertThrows(IllegalArgumentException.class, () ->
            WordUtil.generateWord("несуществующая", 1)
        );
    }

    @DisplayName("Корректными являются только символы русского алфавита")
    @Test
    void testAvailableOfRussian() {
        for (int i = 0; i < WordUtil.getRussianAlphabet().length(); i++) {
            assertTrue(WordUtil.isSymbolAvailable(WordUtil.getRussianAlphabet().charAt(i)));
        }

        for (char i = '0'; i <= '9'; i++) {
            assertFalse(WordUtil.isSymbolAvailable(i));
        }

        for (char i = 'a'; i <= 'z'; i++) {
            assertFalse(WordUtil.isSymbolAvailable(i));
        }

        for (char i = 'A'; i <= 'Z'; i++) {
            assertFalse(WordUtil.isSymbolAvailable(i));
        }
    }

    @DisplayName("Для каждого слова существует подсказка")
    @Test
    void checkClueForEveryWord() {
        int stepNums = 5;

        for (String category : WordUtil.getCategoryList()) {
            for (int complexity = 1; complexity <= 3; complexity++) {
                for (int step = 0; step < stepNums; step++) {
                    String word = WordUtil.generateWord(category, complexity);
                    assertNotNull(WordUtil.getClue(word));
                }
            }
        }
    }
}
