package academy.game;

import academy.utils.WordUtil;
import java.io.Closeable;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
import static academy.utils.WordUtil.BLANK;

public class HangmanScanner implements Closeable {
    private final Scanner scanner;

    public HangmanScanner() {
        this(System.in);
    }

    public HangmanScanner(InputStream stream) {
        scanner = new Scanner(stream);
    }

    public String getCategory() {
        try {
            System.out.println("Выберите категорию:\n" + WordUtil.getCategoryListInString());

            String category = new Scanner(scanner.nextLine()).next();
            category = category.toLowerCase();

            if (!WordUtil.isCategoryAvailable(category)) {
                return WordUtil.getRandomCategory();
            }

            return category;
        } catch (NoSuchElementException e) {
            return WordUtil.getRandomCategory();
        }
    }

    public int getComplexity(String category) {
        try {
            System.out.println("Выберите сложность от 1 до " + WordUtil.getMaxComplexity(category));

            int complexity = new Scanner(scanner.nextLine()).nextInt();

            if (!WordUtil.isComplexityAvailable(complexity, category)) {
                return WordUtil.getRandomComplexity(category);
            }

            return complexity;
        } catch (NoSuchElementException e) {
            return WordUtil.getRandomComplexity(category);
        }
    }

    public char getNextChar() {
        while (true) {
            try {
                System.out.println("Введите следующую букву, если вы введете несколько будет использована первая");
                System.out.println("Если вы хотите запросить подсказку, то напишите \"подсказка\"");

                String nextLine = scanner.nextLine();

                if (nextLine.equals("подсказка")) {
                    return BLANK;
                }

                char c = nextLine.toLowerCase().charAt(0);

                if (WordUtil.isSymbolAvailable(c)) {
                    return c;
                } else {
                    System.out.println("Некорректный символ, попробуйте снова");
                }
            } catch (NoSuchElementException e) {
                System.out.println("Что-то пошло не так, попробуйте снова");
            }
        }
    }

    @Override
    public void close() {
        try {
            scanner.close();
        } catch (IllegalStateException e) {
            System.err.println("Scanner already closed");
        }
    }
}
