package application.view.gui.panels;

import application.controller.ConsoleApplicationController;
import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.controller.commands.exceptions.CommandException;
import application.controller.input.InputStrategy;
import application.controller.input.ScriptInputStrategy;
import application.controller.input.StrategyType;
import application.controller.input.exceptions.EndOfTheScriptException;
import application.model.collection.CollectionItem;
import application.model.data.worker.Worker;
import application.view.console.ConsolePrinter;
import application.view.gui.AbstractPanel;
import application.view.gui.ApplicationEvent;
import application.view.gui.ApplicationMediator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;

public class FooterPanel<T extends Worker> extends AbstractPanel<T> {
    private final JTextArea textArea = new JTextArea(8, 100);
    private final JTextField commandLine = new JTextField();
    private final JButton enterCommandButton = new JButton(bundle.getString("enterCommandButton"));
    private final JButton executeScriptButton = new JButton(bundle.getString("executeScriptButton"));
    private final JScrollPane scroll = new JScrollPane();

    private final SynchronousQueue<String> inputQueue = new SynchronousQueue<>();
    private final ConsoleApplicationController<? extends CollectionItem> consoleApplicationController;
    private final JFileChooser fileChooser = new JFileChooser();

    public FooterPanel(ApplicationMediator<T> applicationMediator) {
        super(applicationMediator);
        ConsolePrinter.printConsumer = this::addLine;
        consoleApplicationController = new ConsoleApplicationController<>(applicationMediator.getCollectionManager());
        consoleApplicationController.getCommandManager().addCommand(new ClearConsole());
        InputStrategy consoleGuiInputStrategy = new InputStrategy() {
            @Override
            public String getLine() {
                try {
                    return inputQueue.take();
                } catch (InterruptedException e) {
                    throw new CommandException("Interrupted console app");
                }
            }

            @Override
            public StrategyType getType() {
                return StrategyType.CONSOLE;
            }
        };
        consoleApplicationController.setDefaultStrategy(consoleGuiInputStrategy);
        Thread consoleThread = new Thread(consoleApplicationController::run);
        consoleThread.setDaemon(true);
        consoleThread.start();

        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setMargin(new Insets(1, 10, 1, 5));
        textArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setBorder(new TitledBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY), bundle.getString("commandLine")));
        scroll.setViewportView(textArea);

        initGUI();
    }

    @Override
    public void block() {
        commandLine.setEnabled(false);
        enterCommandButton.setEnabled(false);
        executeScriptButton.setEnabled(false);
    }

    public void unblock() {
        commandLine.setEnabled(true);
        enterCommandButton.setEnabled(true);
        executeScriptButton.setEnabled(true);
    }

    @Override
    protected void constructLayouts() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbConstr = new GridBagConstraints();
        gbConstr.insets = new Insets(5, 2, 5, 2);
        gbConstr.weighty = 1.0;

        gbConstr.gridx = 0;
        gbConstr.gridwidth = 3;
        gbConstr.weightx = 2;
        gbConstr.gridy = 0;
        gbConstr.fill = GridBagConstraints.BOTH;
        gbConstr.anchor = GridBagConstraints.CENTER;
        add(scroll, gbConstr);


        gbConstr.gridwidth = 1;
        gbConstr.ipady = 10;
        gbConstr.gridy++;
        gbConstr.fill = GridBagConstraints.BOTH;
        gbConstr.anchor = GridBagConstraints.EAST;
        add(commandLine, gbConstr);

        gbConstr.gridx++;
        gbConstr.gridwidth = 1;
        gbConstr.weightx = 0.25;
        gbConstr.fill = GridBagConstraints.BOTH;
        gbConstr.anchor = GridBagConstraints.WEST;
        add(enterCommandButton, gbConstr);

        gbConstr.gridx++;
        gbConstr.gridwidth = 1;
        gbConstr.weightx = 0.25;
        add(executeScriptButton, gbConstr);
    }

    @Override
    public void updateLocale() {
        enterCommandButton.setText(bundle.getString("enterCommandButton"));
        executeScriptButton.setText(bundle.getString("executeScriptButton"));
        ((TitledBorder) scroll.getBorder()).setTitle(bundle.getString("commandLine"));
    }

    protected void addLine(String line) {
        if (!line.isEmpty())
            textArea.append(line);
    }

    @Override
    public void close() {
        try {
            inputQueue.put("skip");
        } catch (InterruptedException ignored) {}
        consoleApplicationController.close();
    }

    @Override
    protected void addListeners() {
        commandLine.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String last = commandLine.getText();
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    enterCommandButton.doClick();
                if (e.getKeyCode() == KeyEvent.VK_UP)
                    commandLine.setText(consoleApplicationController.getCommandManager().up(last));
                if (e.getKeyCode() == KeyEvent.VK_DOWN)
                    commandLine.setText(consoleApplicationController.getCommandManager().down(last));
            }
        });
        enterCommandButton.addActionListener(e -> {
            if (!commandLine.getText().equals(""))
                addLine(commandLine.getText() + "\n");
            inputQueue.offer(commandLine.getText());
            commandLine.setText("");
        });
        executeScriptButton.addActionListener(event -> {
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File chooserSelectedFile = fileChooser.getSelectedFile();
                applicationMediator.triggerEvent(ApplicationEvent.block());
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        try {
                            ConsolePrinter.printBlock = true;
                            ScriptInputStrategy inputStrategy = new ScriptInputStrategy(chooserSelectedFile);
                            while(true) {
                                inputQueue.put(inputStrategy.getLine());
                            }
                        } catch (EndOfTheScriptException e) {
                            applicationMediator.triggerEvent(ApplicationEvent.info("endOfTheScript"));
                        } catch (RuntimeException e) {
                            applicationMediator.triggerEvent(ApplicationEvent.error(e));
                        } catch (InterruptedException ignored) {}
                        return null;
                    }

                    @Override
                    protected void done()  {
                        ConsolePrinter.printBlock = false;
                        applicationMediator.triggerEvent(ApplicationEvent.unblock());
                    }
                }.execute();

            }
        });
    }

    private class ClearConsole extends AbstractCommand {
        public ClearConsole() {
            super("/clear");
        }

        @Override
        public void execute(CommandParameters params) {
            textArea.setText("");
        }
    }

}
