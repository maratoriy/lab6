package application.view.console;

import java.util.function.Consumer;

public class ConsolePrinter {

    static public Consumer<String> printConsumer = System.out::println;
    static volatile public boolean printBlock = false;

    static public void request(String msg) {
        if (!printBlock) print(msg);
    }

    static public void print(String msg) {
        printConsumer.accept(msg);
    }

    static public void print(Object obj) {
        print(obj.toString());
    }

    static public void printError(String err) {
        print(err + "\n");
    }
}
