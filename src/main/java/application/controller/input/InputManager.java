package application.controller.input;

public class InputManager {
    private volatile InputStrategy inputStrategy;

    public InputManager(InputStrategy inputStrategy) {
        this.inputStrategy = inputStrategy;
    }

    public void setStrategy(InputStrategy inputStrategy) {
        this.inputStrategy = inputStrategy;
    }

    public StrategyType getType() {
        return inputStrategy.getType();
    }

    public String getLine() {
        return inputStrategy.getLine();
    }
}
