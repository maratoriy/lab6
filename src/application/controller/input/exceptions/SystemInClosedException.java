package application.controller.input.exceptions;

import application.controller.exceptions.CriticalErrorException;

public class SystemInClosedException extends CriticalErrorException {
    public SystemInClosedException() {
        super("System.in closed!");
    }
}
