package application.controller.input.exceptions;

public class EndOfTheScriptException extends ScriptException {
    public EndOfTheScriptException() {
        super("End of the script detected!");
    }
}
