package application.controller.commands;

import application.view.gui.localization.BundleManager;

abstract public class AbstractCommand implements Command {
    private final BundleManager bundleManager = BundleManager.getBundle("gui");

    private final String name;


    public AbstractCommand(String commandName) {
        this.name = commandName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return bundleManager.getString(name + "Description");
    }

}
