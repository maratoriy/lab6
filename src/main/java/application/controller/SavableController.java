package application.controller;

public interface SavableController extends BasicController {
    String getSavePath();

    void setSavePath(String savePath);
}
