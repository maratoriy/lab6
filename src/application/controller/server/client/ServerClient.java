package application.controller.server.client;

import application.controller.server.handlers.CommunicatingHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class ServerClient {
    private final ByteBuffer byteBuffer;
    private final String name;
    private final SocketChannel socketChannel;
    public boolean writeLock = false;
    public boolean command = false;

    private final LinkedList<ClientTask> clientTasks;


    {
        clientTasks = new LinkedList<>();
    }


    public ServerClient(int bufferCapacity, SocketChannel socketChannel,String name) {
        this.socketChannel = socketChannel;
        this.byteBuffer = ByteBuffer.allocate(bufferCapacity);
        this.name = name;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void close() {
        try {
            socketChannel.close();
        } catch (IOException e) {

        }
    }

    public String getName() {
        return name;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public boolean haveTask() {
        return !clientTasks.isEmpty();
    }

    public ClientTask.Type getTopTaskType() {
        if(!clientTasks.isEmpty())
            return clientTasks.peek().getType();
        else return null;
    }

    public ClientTask pollTask() {
        return clientTasks.poll();
    }

    public void pushBackTask(ClientTask task) {
        clientTasks.addLast(task);
    }

    public void pushForwardTask(ClientTask task) {
        clientTasks.addFirst(task);
    }

    public void writeNextObject(Object object) throws IOException {
        byteBuffer.clear();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        byteBuffer.put(byteArrayOutputStream.toByteArray());
        objectOutputStream.close();
        byteArrayOutputStream.close();
        byteBuffer.flip();
    }


    public void clearTaskSet() {
        clientTasks.clear();
    }

}
