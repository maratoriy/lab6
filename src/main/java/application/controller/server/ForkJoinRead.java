package application.controller.server;

import application.controller.server.client.ServerClient;
import application.controller.server.handlers.MessageHandler;
import application.controller.server.messages.ClientMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class ForkJoinRead {
    public final int MAX_CLIENTS_PER_THREAD;
    private final MessageHandler messageHandler;
    private final ForkJoinPool forkJoinPool;
    private RecursiveRead recursiveRead;

    {
        forkJoinPool = new ForkJoinPool();
    }

    public ForkJoinRead(MessageHandler messageHandler, int MAX_CLIENTS_PER_THREAD) {
        this.messageHandler = messageHandler;
        this.MAX_CLIENTS_PER_THREAD = MAX_CLIENTS_PER_THREAD;
    }


    private final List<Selector> selectors = Collections.synchronizedList(new ArrayList<>());

    public void register(ServerClient client) throws ClosedChannelException {
        selectors.sort(Comparator.comparingInt(sel -> sel.keys().size()));
        if (selectors.size() != 0) {
            client.getChannel().register(selectors.get(0), SelectionKey.OP_READ, client);
        }
    }


    public void processRead(ServerClient client) {
        if (recursiveRead == null || recursiveRead.isDone()) {
            recursiveRead = new RecursiveRead(Collections.singletonList(client), messageHandler);
            forkJoinPool.submit(recursiveRead);
        } else {
            try {
                register(client);
            } catch (ClosedChannelException e) {

            }
        }
    }

    private class RecursiveRead extends RecursiveAction {

        private final MessageHandler messageHandler;

        private final List<ServerClient> clients;


        public RecursiveRead(List<ServerClient> clients, MessageHandler messageHandler) {
            this.clients = clients;
            this.messageHandler = messageHandler;
        }


        @Override
        protected void compute() {
            try (Selector selector = Selector.open()) {
                TCPServer.log("Starting new RecursiveRead {} ", Thread.currentThread().getName());
                for (ServerClient client : clients) {
                    client.getChannel().register(selector, SelectionKey.OP_READ, client);
                }
                selectors.add(selector);
                while (selector.keys().size() > 0) {
                    selector.select(100);
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        if (key.isReadable()) {
                            ServerClient client = (ServerClient) key.attachment();
                            ClientMessage message = TCPServer.read(key);
                            if (Objects.nonNull(message)) messageHandler.handleObject(client, message);
                        }
                    }
                    if (selector.keys().size() > MAX_CLIENTS_PER_THREAD) {
                        List<SelectionKey> keyList = new ArrayList<>(selector.keys());
                        List<ServerClient> clients1 = new ArrayList<>();
                        List<ServerClient> clients2 = new ArrayList<>();
                        for (int i = 0; i < keyList.size() / 2; i++) {
                            clients1.add((ServerClient) keyList.get(i).attachment());
                        }
                        for (int i = keyList.size() / 2; i < keyList.size(); i++) {
                            clients2.add((ServerClient) keyList.get(i).attachment());
                        }
                        RecursiveRead recursiveRead1 = new RecursiveRead(clients1, messageHandler);
                        RecursiveRead recursiveRead2 = new RecursiveRead(clients2, messageHandler);
                        selectors.remove(selector);
                        selector.close();
                        TCPServer.log("Splitting current RecursiveRead {} to 2 new simple jobs", Thread.currentThread().getName());
                        ForkJoinTask.invokeAll(recursiveRead1, recursiveRead2);
                        return;
                    }
                }
                TCPServer.log("Exited RecursiveRead at {} ", Thread.currentThread().getName());
                selectors.remove(selector);

            } catch (IOException | ClassNotFoundException e) {
                TCPServer.log("Exited RecursiveRead at {} with error", Thread.currentThread().getName(), e.getMessage());
                e.printStackTrace();
            }
        }

    }

}
