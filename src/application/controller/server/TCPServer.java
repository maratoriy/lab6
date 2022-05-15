package application.controller.server;

import application.controller.BasicController;
import application.controller.server.client.ClientTask;
import application.controller.server.client.ServerClient;
import application.controller.server.exceptions.ServerException;
import application.controller.server.handlers.AbstractObjectHandler;
import application.controller.server.handlers.CommunicatingHandler;
import application.controller.server.handlers.ObjectHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TCPServer implements BasicController {
    static public final int BUFFER_CAPACITY = 1024 * 64;

    private State state;
    private ServerSocketChannel serverSocketChannel;
    private final InetSocketAddress address;
    private Selector selector;

    private final AbstractObjectHandler<?> firstHandler;

    {
        firstHandler = new CommunicatingHandler();
    }

    public AbstractObjectHandler<?> getFirstHandler() {
        return firstHandler;
    }

    public TCPServer(InetSocketAddress address) {
        this.address = address;
    }

    static public void log(String msg) {
        System.out.println(LocalTime.now() + " [SERVER] " + msg);
    }

    static public void log(String format, Object... args) {
        log(String.format(format, args));
    }

    public TCPServer(String hostName, int port) {
        address = new InetSocketAddress(hostName, port);
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        ServerClient serverClient = new ServerClient(BUFFER_CAPACITY, socketChannel,socketChannel.getRemoteAddress().toString());
        serverClients.add(serverClient);
        socketChannel.register(selector, SelectionKey.OP_READ, serverClient);
        log("New client connected from %s", socketChannel.getRemoteAddress());
    }


    static public void read(SelectionKey key, ObjectHandler<Object> handler) throws IOException, InterruptedException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
        int byteLen = channel.read(byteBuffer);
        if (byteLen == -1) {
            key.cancel();
            channel.close();
            return;
        }
        byteBuffer.flip();
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteBuffer.array()));
        try {
            Object object = objectInputStream.readObject();
            ServerClient serverClient = (ServerClient) key.attachment();
            TCPServer.log("Reading object \"%s\" from \"%s\"", object.toString(), serverClient.getName());
            if(handler!=null) handler.handleObject(serverClient, object);
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            if(!object.getClass().isAssignableFrom(CommunicatingHandler.Message.class)) {
                serverClient.pushForwardTask(ClientTask.writeTask(CommunicatingHandler.Message.COMPLETE));
                Thread.sleep(3);
                write(key);
            }
        } catch (ClassNotFoundException e) {
            throw new ServerException(e);
        }

    }
    static public boolean write(SelectionKey key) throws IOException, InterruptedException  {
        SocketChannel channel = (SocketChannel) key.channel();
        ServerClient client = (ServerClient) key.attachment();
        if (client.haveTask()&&client.getTopTaskType()== ClientTask.Type.WRITE) {
            Object object = ((ClientTask.WriteTask) client.pollTask()).getObject();
            client.writeNextObject(object);
            channel.write(client.getByteBuffer());
            TCPServer.log("Sending object \"%s\" to \"%s\"", object.toString(), channel.getRemoteAddress());
            return true;
        }
        return false;
    }

    static public void writeLock(SelectionKey key) throws IOException, InterruptedException {
        ServerClient client = (ServerClient) key.attachment();
//        if(client.command) { write(key); client.command = false; return; }
        if(!client.writeLock) if(write(key)) client.writeLock = true;
    }


    @Override
    public void run() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(address);
            serverSocketChannel.configureBlocking(false);

            Thread.sleep(3000);

            log("Started at %s", serverSocketChannel.getLocalAddress());

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, serverSocketChannel);

            state = State.RUNNING;
            while (state == State.RUNNING) {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (key.isAcceptable()) accept(key);
                    if (key.isWritable()) {
                        writeLock(key);
                    }
                    if (key.isReadable()) {
                        read(key, firstHandler);
                    }
                }
            }
        } catch (IOException | InterruptedException | ClosedSelectorException | CancelledKeyException e) {
            log(e.getMessage());
        }

    }

    @Override
    public boolean isRunning() {
        return state == State.RUNNING;
    }

    private final List<ServerClient> serverClients = new ArrayList<>();
    @Override
    public void close() {
        try {
            selector.wakeup();
            serverSocketChannel.close();
            state = State.CLOSING;
            serverClients.forEach(ServerClient::close);
            log("Disconnecting...");
        } catch (IOException | CancelledKeyException | ClosedSelectorException e) {
            throw new ServerException(e);
        }
    }
}
