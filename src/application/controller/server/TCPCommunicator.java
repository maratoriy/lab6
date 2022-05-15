package application.controller.server;

import application.controller.BasicController;
import application.controller.server.client.ClientTask;
import application.controller.server.client.ServerClient;
import application.controller.server.exceptions.ServerException;
import application.controller.server.handlers.AbstractObjectHandler;
import application.controller.server.handlers.CommunicatingHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class TCPCommunicator implements BasicController {
    private final InetSocketAddress address;
    private SocketChannel socketChannel;
    private Selector selector;
    private ServerClient appClient;
    private SelectionKey key;

    private final AbstractObjectHandler<?> firstHandler;

    {
        firstHandler = new CommunicatingHandler();
    }

    public AbstractObjectHandler<?> getFirstHandler() {
        return firstHandler;
    }

    public TCPCommunicator(String hostName, int port) {
        address = new InetSocketAddress(hostName, port);
    }


    @Override
    public boolean isRunning() {
        return socketChannel!=null&&socketChannel.isConnected();
    }

    public void connect() throws IOException {
        socketChannel = SocketChannel.open(address);
        socketChannel.configureBlocking(false);

        selector = Selector.open();

        appClient = new ServerClient(TCPServer.BUFFER_CAPACITY, socketChannel, socketChannel.getRemoteAddress().toString());
        key = socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, appClient);
    }

    private State state;
    @Override
    public void run() {
        try {
            state = State.RUNNING;
            appClient.pushForwardTask(ClientTask.writeTask(CommunicatingHandler.Message.STARTED));
            TCPServer.write(key);
            Thread.sleep(100);
            while(state==State.RUNNING&&appClient.haveTask()) {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (key.isWritable()) {
                        TCPServer.writeLock(key);
                    }
                    if (key.isReadable()) {
                        TCPServer.read(key, firstHandler);
                    }
                }
            }
        } catch (IOException | InterruptedException | CancelledKeyException e) {
            close();
            throw new ServerException("Disconnected");
        }
    }


    public ServerClient getAppClient() {
        return appClient;
    }

    @Override
    public void close() {
        state = State.CLOSING;
        if(selector!=null) selector.wakeup();
        try {
            if (socketChannel != null) socketChannel.close();
        } catch (IOException e ){
            throw new ServerException(e);
        }
        socketChannel = null;
    }

}
