package academy.game;

public interface GuessingGame<T> {
    void playGame();

    void tryGuess(T c);

    String getState();

    boolean isGameOver();

    boolean isWin();

    void completeGame();
}
