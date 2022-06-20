package application.view.gui;

import application.controller.server.AuthorisationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class AuthorizationFrame extends AbstractFrame {
    private final JLabel usernameLabel = new JLabel(bundle.getString("username"));
    private final JTextField usernameField = new JTextField();
    private final JLabel passwordLabel = new JLabel(bundle.getString("password"));
    private final JPasswordField passwordField = new JPasswordField();

    private final JLabel authErrorLabel = new JLabel();
    private String authErrorLabelKey;

    private final JButton logInButton = new JButton(bundle.getString("logInButton"));
    private final JButton signUpButton = new JButton(bundle.getString("signUpButton"));

    private final AuthorisationManager authorisationManager;
    private final Consumer<String> actionOnSuccess;

    public AuthorizationFrame(AuthorisationManager authorisationManager, Consumer<String> actionOnSuccess) {
        this.authorisationManager = authorisationManager;
        this.actionOnSuccess = actionOnSuccess;
        initGUI();
    }

    @Override
    public void updateLocale() {
        setTitle(bundle.getString("authorizationFrame"));

        passwordLabel.setText(bundle.getString("password"));
        usernameLabel.setText(bundle.getString("username"));

        logInButton.setText(bundle.getString("logInButton"));
        signUpButton.setText(bundle.getString("signUpButton"));
        setAuthorizationError(authErrorLabelKey);


    }

    private void setAuthorizationError(String key) {
        if (!Objects.nonNull(key)) {
            authErrorLabelKey = null;
            authErrorLabel.setVisible(false);
        } else {
            authErrorLabelKey = key;
            String newLabel = bundle.getString(authErrorLabelKey);
            authErrorLabel.setText(newLabel.isEmpty() ? key : newLabel);
            authErrorLabel.setVisible(true);
        }
    }

    protected void initGUI() {
        super.initGUI();
        constructLayout();
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        authErrorLabel.setForeground(Color.RED);
        usernameField.setPreferredSize(new Dimension(200, 20));
        passwordField.setPreferredSize(new Dimension(200, 20));
        authErrorLabel.setVisible(false);

        addListeners();
    }

    private void constructLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbConstr = new GridBagConstraints();
        gbConstr.insets = new Insets(5, 2, 5, 2);

        gbConstr.gridx = 0;
        gbConstr.gridy = 0;
        gbConstr.anchor = GridBagConstraints.WEST;
        add(usernameLabel, gbConstr);

        gbConstr.gridx++;
        gbConstr.fill = GridBagConstraints.BOTH;
        gbConstr.anchor = GridBagConstraints.EAST;
        add(usernameField, gbConstr);

        gbConstr.gridx = 0;
        gbConstr.gridy++;
        gbConstr.fill = GridBagConstraints.NONE;
        gbConstr.anchor = GridBagConstraints.WEST;
        add(passwordLabel, gbConstr);

        gbConstr.gridx++;
        gbConstr.fill = GridBagConstraints.BOTH;
        gbConstr.anchor = GridBagConstraints.EAST;
        add(passwordField, gbConstr);

        gbConstr.gridx = 0;
        gbConstr.gridy++;
        add(signUpButton, gbConstr);

        gbConstr.gridx++;
        add(logInButton, gbConstr);

        gbConstr.gridy++;
        gbConstr.gridx = 0;
        gbConstr.gridwidth = 2;
        gbConstr.gridheight = 1;
        gbConstr.anchor = GridBagConstraints.CENTER;
        gbConstr.fill = GridBagConstraints.BOTH;
        add(authErrorLabel, gbConstr);

        setTitle(bundle.getString("authorizationFrame"));
        setSize(400, 300);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void activeAuthInterface(boolean active) {
        usernameField.setEditable(active);
        passwordField.setEditable(active);
        logInButton.setEnabled(active);
        signUpButton.setEnabled(active);
    }

    private void buttonCallback(Throwable e) {
        String errorString = e.getClass().getSimpleName();
        setAuthorizationError(errorString);
    }

    private void success() {
        dispose();
        actionOnSuccess.accept(usernameField.getText());
    }

    private void addListeners() {
        logInButton.addActionListener(event -> {
            activeAuthInterface(false);
            String login = usernameField.getText();
            String password = new String(passwordField.getPassword());

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    authorisationManager.login(login, password);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        success();
                    } catch (ExecutionException e) {
                        buttonCallback(e.getCause());
                    } catch (InterruptedException ignored) {
                    } finally {
                        activeAuthInterface(true);
                    }
                }
            }.execute();
        });
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    passwordField.grabFocus();
            }
        });
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    logInButton.doClick();
            }
        });
        signUpButton.addActionListener(event -> {
            activeAuthInterface(false);
            String login = usernameField.getText();
            String password = new String(passwordField.getPassword());

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    authorisationManager.register(login, password);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        success();
                    } catch (ExecutionException e) {
                        buttonCallback(e.getCause());
                    } catch (InterruptedException ignored) {
                    } finally {
                        activeAuthInterface(true);
                    }
                }
            }.execute();
        });
    }

}
