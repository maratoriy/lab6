package application.view.gui;


import application.view.gui.localization.ApplicationLocale;
import application.view.gui.localization.BundleManager;
import application.view.gui.localization.LocalizableComponent;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

abstract public class AbstractFrame extends JFrame implements LocalizableComponent {
    protected final static BundleManager bundle = BundleManager.getBundle("gui");
    protected final JMenu language = new JMenu(bundle.getString("changeLanguage"));


    private final List<LocalizableComponent> localizableChildren = new LinkedList<>();

    public AbstractFrame() {
        setIconImage(new ImageIcon("C:\\Users\\Lenovo\\OneDrive - ITMO UNIVERSITY\\Labs\\lab6\\src\\main\\resources\\icons\\worker.png").getImage());
    }

    protected void addChildToLocalize(LocalizableComponent component) {
        localizableChildren.add(component);
    }

    private void updateAllLocales() {
        language.setText(bundle.getString("changeLanguage"));
        localizableChildren.forEach(LocalizableComponent::updateLocale);
        updateLocale();
    }

    protected void initGUI() {
        JMenuItem ru_item = new JMenuItem("Русский");
        JMenuItem en_item = new JMenuItem("English (US)");
        JMenuItem fi_item = new JMenuItem("Suomalainen");
        JMenuItem ca_item = new JMenuItem("Català");

        language.add(ru_item);
        language.add(en_item);
        language.add(fi_item);
        language.add(ca_item);

        ru_item.addActionListener(event -> {
            bundle.setMyLocale(ApplicationLocale.RUSSIAN);
            updateAllLocales();
        });
        en_item.addActionListener(event -> {
            bundle.setMyLocale(ApplicationLocale.ENGLISH);
            updateAllLocales();
        });
        fi_item.addActionListener(event -> {
            bundle.setMyLocale(ApplicationLocale.FINNISH);
            updateAllLocales();
        });
        ca_item.addActionListener(event -> {
            bundle.setMyLocale(ApplicationLocale.CATALAN);
            updateAllLocales();
        });



        JMenuBar menuBar = new JMenuBar();
        menuBar.add(language);
        setJMenuBar(menuBar);
    }


}
