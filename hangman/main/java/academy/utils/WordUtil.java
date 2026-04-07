package academy.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class WordUtil {
    private static final Map<String, Map<Integer, List<String>>> words = new HashMap<>();
    private static final Map<Integer, Integer> complexityToMaxAttempts = new HashMap<>();
    private static final Map<String, String> wordToClue = new HashMap<>();
    private static final Random random = new Random();
    private static final String russianAlphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    public static final Character BLANK = 0;

    static {
        complexityToMaxAttempts.put(1, 8);
        complexityToMaxAttempts.put(2, 7);
        complexityToMaxAttempts.put(3, 6);

        try (InputStream inputStream = WordUtil.class.getResourceAsStream("/WordList.txt")) {
            assert inputStream != null;
            try (Scanner scanner = new Scanner(inputStream)) {
                readWordList(scanner);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load word list", e);
        }
    }

    public static void readWordList(Scanner scanner) {
        String currentCategory = null;
        Integer currentNumber = null;
        Map<Integer, List<String>> currentCategoryMap = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.equals("category")) {
                if (scanner.hasNextLine()) {
                    currentCategory = scanner.nextLine().trim();
                    currentCategory = currentCategory.toLowerCase();
                    currentCategoryMap = new HashMap<>();
                    words.put(currentCategory, currentCategoryMap);
                    currentNumber = null;
                }

                continue;
            }

            if (currentCategory == null) {
                continue;
            }

            try {
                currentNumber = Integer.parseInt(line);
                currentCategoryMap.putIfAbsent(currentNumber - 1, new ArrayList<>());
            } catch (NumberFormatException e) {
                String[] splitResult = line.split(";");
                String word = splitResult[0].trim().toLowerCase();
                String clue = splitResult[1].trim().toLowerCase();

                wordToClue.put(word, clue);

                if (currentNumber != null) {
                    currentCategoryMap.get(currentNumber - 1).add(word);
                }
            }
        }
    }

    public static String getClue(String word) {
        return wordToClue.get(word);
    }

    public static int getAttemptsFromComplexity(int complexity) {
        return complexityToMaxAttempts.get(complexity);
    }

    public static String generateWord(String category, int complexity) {
        if (words.containsKey(category)) {
            if (complexity - 1 < words.get(category).size()) {
                List<String> strings = words.get(category).get(complexity - 1);
                return strings.get(Math.abs(random.nextInt()) % strings.size());
            } else {
                throw new IllegalArgumentException("No such available complexity: " + complexity);
            }
        } else {
            throw new IllegalArgumentException("No such category: " + category);
        }
    }

    public static String getRussianAlphabet() {
        return russianAlphabet;
    }

    public static boolean isCategoryAvailable(String category) {
        return words.containsKey(category);
    }

    public static String getCategoryListInString() {
        return words.keySet().toString();
    }

    public static List<String> getCategoryList() {
        return new ArrayList<>(words.keySet());
    }

    public static String getRandomCategory() {
        List<String> categoryList = new ArrayList<>(words.keySet());
        return categoryList.get(Math.abs(random.nextInt()) % categoryList.size());
    }

    public static boolean isComplexityAvailable(int complexity, String category) {
        return words.containsKey(category) && complexity - 1 < words.get(category).size();
    }

    public static int getMaxComplexity(String category) {
        return words.get(category).size();
    }

    public static int getRandomComplexity(String category) {
        return Math.abs(random.nextInt()) % words.get(category).size() + 1;
    }

    public static boolean isSymbolAvailable(Character c) {
        return russianAlphabet.contains(Character.toString(c));
    }
}
