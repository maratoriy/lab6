package application.controller.input;

import application.controller.input.exceptions.SystemInClosedException;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConsoleInputStrategy implements InputStrategy {
    private final Scanner scanner;

    public ConsoleInputStrategy() {
        scanner = new Scanner(System.in);
    }

    @Override
    public StrategyType getType() {
        return StrategyType.CONSOLE;
    }

    @Override
    public String getLine() {
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            throw new SystemInClosedException();
        }
    }
}
