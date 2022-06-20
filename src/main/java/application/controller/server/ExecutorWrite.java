package application.controller.server;

import application.controller.server.client.ServerClient;
import application.controller.server.exceptions.ServerException;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorWrite extends ClientObserver.WriteObserver {
    private final ExecutorService service;
    private final List<ServerClient> serverClients;

    {
        serverClients = Collections.synchronizedList(new ArrayList<>());
    }


    public ExecutorWrite(int size) {
        this.service = Executors.newFixedThreadPool(size);
        for (int i = 0; i < size; i++) {
            try {
                selectors.add(Selector.open());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void subscribeClient(ServerClient client) {
        client.addHook(this);
    }

    public void observeAction(ServerClient client) {
        try {
            if (!serverClients.contains(client)) {
                Future<?> writing = service.submit(createTask(client));
                writing.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new ServerException(e);
        }
    }

    private final List<Selector> selectors = Collections.synchronizedList(new ArrayList<>());

    private Runnable createTask(ServerClient client) {
        return () -> {
            try {
                Selector selector = selectors.remove(0);
                SocketChannel channel = client.getChannel();
                serverClients.add(client);
                Optional<SelectionKey> optionalSelectionKey = selector.keys().stream().filter(item -> item.channel().equals(channel)).findFirst();
                SelectionKey foundKey;
                if (!optionalSelectionKey.isPresent()) {
                    foundKey = channel.register(selector, SelectionKey.OP_WRITE, client);
                } else {
                    foundKey = optionalSelectionKey.get().interestOps(SelectionKey.OP_WRITE);
                }
                while (client.hasObjectToSend()) {
                    selector.select();
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        if (key.isWritable()) {
                            TCPServer.write(key);
                        }
                    }
                }
                foundKey.interestOps(0);
                selectors.add(selector);
                serverClients.remove(client);
            } catch (IOException e) {
                e.printStackTrace();
                TCPServer.log("Writing task {} failed due to {}", Thread.currentThread(), e.getMessage());
            } catch (CancelledKeyException | NoSuchElementException e) {
                e.printStackTrace();
            }
        };
    }

}
