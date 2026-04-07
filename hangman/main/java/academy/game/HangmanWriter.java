package academy.game;

import java.util.ArrayList;
import java.util.List;

public class HangmanWriter {
    private static final List<String> hangmanRenders = new ArrayList<>();
    private int hangmanPosition = 0;

    static {
        hangmanRenders.add(
            """
                  +---+
                  |   |
                      |
                      |
                      |
                      |
                =========""");

        hangmanRenders.add(
            """
                  +---+
                  |   |
                  O   |
                      |
                      |
                      |
                =========""");

        hangmanRenders.add(
            """
                  +---+
                  |   |
                  O   |
                  |   |
                      |
                      |
                =========""");

        hangmanRenders.add(
            """
                  +---+
                  |   |
                  O   |
                 /|   |
                      |
                      |
                =========""");

        hangmanRenders.add(
            """
                  +---+
                  |   |
                  O   |
                 /|\\  |
                      |
                      |
                =========""");

        hangmanRenders.add(
            """
                  +---+
                  |   |
                  O   |
                 /|\\  |
                 /    |
                      |
                =========""");

        hangmanRenders.add(
            """
                  +---+
                  |   |
                  O   |
                 /|\\  |
                 / \\  |
                      |
                =========""");
    }

    void loseWrite(int attempts) {
        if (attempts > 0) {
            if (attempts < hangmanRenders.size() - 1) {
                hangmanPosition++;
            }

            System.out.println(hangmanRenders.get(hangmanPosition));
        } else {
            System.out.println(hangmanRenders.getLast());
        }
    }

    void rightWrite() {
        System.out.println(hangmanRenders.get(hangmanPosition));
    }
}
