package application.controller.input.exceptions;

public class OverLoadedScriptException extends ScriptException {
    public OverLoadedScriptException() {
        super("Recursion of the script has been detected");
    }
}
