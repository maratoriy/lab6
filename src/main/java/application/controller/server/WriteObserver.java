package application.controller.server;

import application.controller.server.client.ServerClient;
import application.controller.server.exceptions.ServerException;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WriteObserver extends ClientObserver.WriteObserver {
    private final ExecutorService service;
    private List<ServerClient> serverClients;

    {
        serverClients = Collections.synchronizedList(new ArrayList<>());
    }


    public WriteObserver(ExecutorService service) {
        this.service = service;
    }

    public void observeAction(ServerClient client) {
        try {
            if (!serverClients.contains(client)) {
                Future<?> writing = service.submit(createTask(client));
                    writing.get();
                serverClients.remove(client);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new ServerException(e);
        }
    }

    static private Runnable createTask(ServerClient client) {
        return () -> {
            try {
                Selector selector = Selector.open();
                SocketChannel channel = client.getChannel();
                channel.register(selector, SelectionKey.OP_WRITE, client);
                while(client.hasObjectToSend()) {
                    selector.select();
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        if (key.isWritable()) {
                            TCPServer.write(key);
                            Thread.sleep(0, 1);
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                throw new ServerException(e);
            }
        };
    }

}
