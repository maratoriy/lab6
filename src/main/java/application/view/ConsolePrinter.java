package application.view;

public class ConsolePrinter {

    static public boolean printBlock = false;

    static public void request(String msg) {
        if (!printBlock) print(msg);
    }

    static public void print(String msg) {
        System.out.print(msg);
    }

    static public void print(Object obj) {
        print(obj.toString());
    }

    static public void printError(String err) {
        System.out.println(err);
    }
}
