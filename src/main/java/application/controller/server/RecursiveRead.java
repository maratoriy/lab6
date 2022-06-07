package application.controller.server;

import application.controller.server.client.ServerClient;
import application.controller.server.handlers.MessageHandler;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class RecursiveRead extends RecursiveAction {

    private final int CRITICAL_SIZE = 4;

    private final MessageHandler messageHandler;

    private final List<ServerClient> clients;


    public RecursiveRead(List<ServerClient> clients, MessageHandler messageHandler) {
        this.clients = clients;
        this.messageHandler = messageHandler;
    }

    private static List<Selector> selectors = Collections.synchronizedList(new ArrayList<>());

    static public void register(ServerClient client) throws ClosedChannelException {
        selectors.sort(Comparator.comparingInt(sel -> sel.keys().size()));
        if(selectors.size()!=0) {
            client.getChannel().register(selectors.get(0), SelectionKey.OP_READ, client);
        }
    }

    static public void clear() {
        selectors.clear();
    }


    @Override
    protected void compute() {
        try(Selector selector = Selector.open()) {
            for(ServerClient client : clients) {
                client.getChannel().register(selector, SelectionKey.OP_READ, client);
            }
            selectors.add(selector);
            while(selector.keys().size()>0) {
                selector.select(100);
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (key.isReadable()) {
                        TCPServer.read(key, messageHandler);
                    }
                }
                if(selector.keys().size()==4) {
                    List<SelectionKey> keyList = new ArrayList<>(selector.keys());
                    List<ServerClient> clients1 = new ArrayList<>();
                    List<ServerClient> clients2 = new ArrayList<>();
                    for(int i=0;i<keyList.size()/2;i++) {
                        clients1.add((ServerClient) keyList.get(i).attachment());
                    }
                    for(int i=keyList.size()/2;i<keyList.size();i++) {
                        clients2.add((ServerClient) keyList.get(i).attachment());
                    }
                    RecursiveRead recursiveRead1 = new RecursiveRead(clients1, messageHandler);
                    RecursiveRead recursiveRead2 = new RecursiveRead(clients2, messageHandler);
                    selectors.remove(selector);
                    selector.close();
                    TCPServer.log("Splitting current {} to 2 new simple jobs", Thread.currentThread().getName());
                    ForkJoinTask.invokeAll(recursiveRead1, recursiveRead2);
                    return;
                }
            }
            selectors.remove(selector);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
