package application.controller;

public interface BasicController {
    void run();

    void close();

    boolean isRunning();

    enum State {
        RUNNING,
        CLOSING
    }
}
