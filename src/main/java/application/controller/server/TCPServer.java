package application.controller.server;

import application.controller.BasicController;
import application.controller.server.client.ServerClient;
import application.controller.server.exceptions.ServerException;
import application.controller.server.handlers.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class TCPServer implements BasicController {
    static public final int BUFFER_CAPACITY = 1024*4;
    private static final Logger logger = LoggerFactory.getLogger(
            TCPServer.class);

    private State state;
    private ServerSocketChannel serverSocketChannel;
    private final InetSocketAddress address;
    private Selector selector;
    private MessageHandler messageHandler;

    private final ClientObserver.WriteObserver writeObserver;

    {
        writeObserver = new WriteObserver(Executors.newFixedThreadPool(16));
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public TCPServer(InetSocketAddress address) {
        this.address = address;
    }

    synchronized static public void log(String msg) {
        logger.info(msg);
    }

    synchronized static public void log(String msg, Object... args) {
        logger.info(msg, args);
    }

    public TCPServer(String hostName, int port) {
        address = new InetSocketAddress(hostName, port);
    }


    @Override
    public void run() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(address);
            serverSocketChannel.configureBlocking(false);

            log("Started at {}", serverSocketChannel.getLocalAddress());

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, serverSocketChannel);

            ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
            ForkJoinTask<?> currentTask = null;

            state = State.RUNNING;
            while (state == State.RUNNING) {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (key.isAcceptable()) {
                        ServerClient client = accept(key);
                        if(currentTask==null||currentTask.isDone()) {
                            RecursiveRead.clear();
                            TCPServer.log("Started new RecursiveRead!");
                            currentTask = forkJoinPool.submit(new RecursiveRead(Collections.singletonList(client), messageHandler));
                        }
                    }
                }
            }
        } catch (IOException | ClosedSelectorException | CancelledKeyException e) {
            TCPServer.log(e.getMessage());
        }

    }

    private ServerClient accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannelQueue.add(socketChannel);
        ServerClient client = new ServerClient(BUFFER_CAPACITY, socketChannel);
        RecursiveRead.register(client);
        client.addHook(writeObserver);
        log("New client connected from {}", socketChannel.getRemoteAddress());
        return client;
    }

    static private void disconnect(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
        String name = ((ServerClient) key.attachment()).getName();
        log("Client {} disconnected", name);
    }

    static public void read(SelectionKey key, MessageHandler messageHandler) throws IOException, ClassNotFoundException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
        int byteLen = channel.read(byteBuffer);
        if (byteLen == -1) {
            disconnect(key);
            return;
        }
        byteBuffer.flip();
        ServerClient serverClient = (ServerClient) key.attachment();
        Message message;
        message = serverClient.receive(byteBuffer);
        if(message==null) return;
        TCPServer.log("Received object {} from {}", message, channel.getRemoteAddress());
        messageHandler.handleObject(serverClient, message);
    }



    static public void write(SelectionKey key) throws IOException {
        ServerClient client = (ServerClient) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        write(channel, client);
    }

    static public void write(SocketChannel channel, ServerClient client) throws IOException {
        if (client.prepareByteBuffer()) {
            channel.write(client.getByteBuffer());
        }
    }

    static public Object deserializeBuffer(ByteBuffer byteBuffer) throws IOException, ClassNotFoundException {
        return deserializeByteArray(byteBuffer.array());
    }


    static public Object deserializeByteArray(byte[] array)  throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(array))) {
            return objectInputStream.readObject();
        }
    }

    //сериализация объекта в массив байтов
    static public byte[] serializeToByteArray(Object object) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        }
    }

    //сериализация объекта в буфер
    static public ByteBuffer serializeToBuffer(Object object) throws IOException {
        return ByteBuffer.wrap(serializeToByteArray(object));
    }

    static public List<ByteBuffer> sliceByteBuffer(ByteBuffer bufferToSlice) {
        if (bufferToSlice.limit() <= BUFFER_CAPACITY)
            return Collections.singletonList(bufferToSlice);
        else {
            List<ByteBuffer> bufferList = new ArrayList<>();
            int bufferToSliceLimit = bufferToSlice.limit();
            int number = (int) Math.ceil((double) bufferToSlice.limit() / (double) BUFFER_CAPACITY);
            for (int i = 0, position = 0; i < number; i++) {
                int size = (i != number - 1) ? BUFFER_CAPACITY : bufferToSliceLimit - (number - 1) * BUFFER_CAPACITY;
                byte[] curr = new byte[size];
                System.arraycopy(bufferToSlice.array(), position, curr, 0, curr.length);
                position += size;
                bufferList.add(ByteBuffer.wrap(curr));
            }
            return bufferList;
        }
    }

    static public ByteBuffer mergeBuffers(List<ByteBuffer> byteBuffers) {
        if (byteBuffers == null || byteBuffers.size() == 0) {
            return ByteBuffer.allocate(0);
        } else if (byteBuffers.size() == 1) {
            return byteBuffers.get(0);
        } else {
            ByteBuffer fullContent = ByteBuffer.allocate(
                    byteBuffers.stream()
                            .mapToInt(Buffer::limit)
                            .sum()
            );
            byteBuffers.forEach(fullContent::put);
            fullContent.flip();
            return fullContent;
        }
    }

    private final Queue<SocketChannel> socketChannelQueue = new ArrayDeque<>();

    @Override
    public boolean isRunning() {
        return state == State.RUNNING;
    }

    @Override
    public void close() {
        try {
            selector.wakeup();
            for (SocketChannel socketChannel:
                 socketChannelQueue) {
                socketChannel.close();
            }
            serverSocketChannel.close();
            state = State.CLOSING;
            log("Disconnecting...");
        } catch (IOException | CancelledKeyException | ClosedSelectorException e) {
            throw new ServerException(e);
        }
    }
}
