package academy.game;

import academy.utils.WordUtil;
import java.util.HashSet;
import java.util.Set;
import static academy.utils.WordUtil.BLANK;

public class HangmanGame implements GuessingGame<Character> {
    private final String word;
    private final StringBuilder guessedCharacters;
    private Integer attempts;
    private final Set<Character> wasUsedLetters = new HashSet<>();
    private final HangmanWriter hangmanWriter = new HangmanWriter();
    private final GuessType type;
    private HangmanScanner hangmanScanner;

    private static final Character CLOSED_LETTER = '*';

    public HangmanGame() {
        this.type = GuessType.INTERACTIVE;

        this.hangmanScanner = new HangmanScanner();

        String category = hangmanScanner.getCategory();
        System.out.println("Ваша категория: " + category);

        int complexity = hangmanScanner.getComplexity(category);
        this.attempts = WordUtil.getAttemptsFromComplexity(complexity);
        System.out.println("Выбранная сложность: " + complexity + ", у вас есть " + this.attempts + " попыток");

        this.word = WordUtil.generateWord(category, complexity);

        this.guessedCharacters = new StringBuilder();
        guessedCharacters.append(CLOSED_LETTER.toString().repeat(word.length()));

        System.out.println(getState());
    }

    public HangmanGame(String word, Integer maxAttempts) {
        this.word = word;
        this.attempts = maxAttempts;
        this.type = GuessType.NON_INTERACTIVE;

        this.guessedCharacters = new StringBuilder();
        guessedCharacters.append(CLOSED_LETTER.toString().repeat(word.length()));
    }

    @Override
    public void playGame() {
        while (!isGameOver()) {
            char nextChar = hangmanScanner.getNextChar();

            if (nextChar != BLANK) {
                tryGuess(nextChar);
            } else {
                System.out.println("Подсказка: " + WordUtil.getClue(word));
            }
        }
    }

    @Override
    public void tryGuess(Character c) {
        boolean guessed = false;
        wasUsedLetters.add(c);

        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == c && guessedCharacters.charAt(i) == CLOSED_LETTER) {
                guessed = true;
                guessedCharacters.setCharAt(i, c);
            }
        }

        if (!guessed) {
            attempts--;
        }

        if (type == GuessType.INTERACTIVE) {
            if (!guessed) {
                hangmanWriter.loseWrite(attempts);
            } else {
                hangmanWriter.rightWrite();
            }

            System.out.println("Текущее состояние слова:\n" + guessedCharacters);
            System.out.println("Число оставшихся попыток: " + attempts);

            System.out.println("Возможные оставшиеся буквы");
            for (int i = 0; i < WordUtil.getRussianAlphabet().length(); i++) {
                char current = WordUtil.getRussianAlphabet().charAt(i);
                System.out.print(wasUsedLetters.contains(current) ? '#' : current);
            }
            System.out.println();
        }
    }

    @Override
    public String getState() {
        return guessedCharacters.toString();
    }

    @Override
    public boolean isWin() {
        int countClosed = 0;

        for (int i = 0; i < guessedCharacters.length(); i++) {
            if (guessedCharacters.charAt(i) == CLOSED_LETTER) {
                countClosed++;
            }
        }

        return countClosed == 0;
    }

    @Override
    public void completeGame() {
        if (isWin()) {
            System.out.println("Вы победили!");
        } else {
            System.out.println("Вы проиграли, у вас было слово " + word);
        }

        hangmanScanner.close();
    }

    @Override
    public boolean isGameOver() {
        return attempts <= 0 || isWin();
    }
}
