package application.controller.actions;

import application.controller.input.InputManager;
import application.model.collection.server.AuthorisationManager;
import application.view.ConsolePrinter;

public class LoginAction implements Action {
    private final InputManager inputManager;
    private final AuthorisationManager authorisationManager;

    public LoginAction(InputManager inputManager, AuthorisationManager authorisationManager) {
        this.inputManager = inputManager;
        this.authorisationManager = authorisationManager;
    }

    @Override
    public void act() {
        boolean result = false;
        do {
            ConsolePrinter.print("Do you want to login/register?: ");
            String respond = inputManager.getLine();
            if(respond.equals("login")||respond.equals("register")) {
                String password;
                String login;
                do {
                    ConsolePrinter.print("Enter login (greater or equal than 2 chars): ");
                    login = inputManager.getLine();
                    result = login.length()>=2;
                } while (!result);
                result = false;
                do {
                    ConsolePrinter.print("Enter password (lower or equal than 12 chars): ");
                    password = inputManager.getLine();
                    result = password.length()<=12;
                } while (!result);
                result = false;
                if(respond.equals("login")) {
                    result = authorisationManager.login(login, password);
                    if(!result) ConsolePrinter.print("Invalid login or password!\n");
                } else {
                    result = authorisationManager.register(login, password);
                    if(!result) ConsolePrinter.print("Couldn't register user!\n");
                }
                if(result) ConsolePrinter.print("Successful!\n");
            }
        } while (!result);
    }
}
