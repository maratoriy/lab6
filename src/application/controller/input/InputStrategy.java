package application.controller.input;

public interface InputStrategy {
    String getLine();

    StrategyType getType();
}
