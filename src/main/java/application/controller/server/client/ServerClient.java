package application.controller.server.client;

import application.controller.server.ClientObserver;
import application.controller.server.Message;
import application.controller.server.TCPServer;
import application.controller.server.exceptions.ServerException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;

public class ServerClient {
    private final ByteBuffer byteBuffer;
    private final SocketChannel channel;

    private Queue<Message> objectsToSend;
    private Queue<ByteBuffer> buffersToSend;

    private BlockingDeque<Object> receivedObjects;
    private boolean receivingParts = false;
    private int partsToReceive;
    private int receivedParts;
    private List<ByteBuffer> receivedBuffers;
    private final String name = "Undefined";

    private final List<ClientObserver> hooks;

    {
        buffersToSend = new ArrayDeque<>();
        objectsToSend = new ArrayDeque<>();
        receivedBuffers = new ArrayList<>();
        receivedObjects = new LinkedBlockingDeque<>();
        hooks = new ArrayList<>();
    }

    public ServerClient(int bufferCapacity, SocketChannel channel) {
        this.byteBuffer = ByteBuffer.allocate(bufferCapacity);
        this.channel = channel;
    }


    public void receiveObject(Object object) {
        TCPServer.log("Received object {} from {}", object.getClass().getSimpleName(),getName());
        receivedObjects.add(object);
    }

    public String getName() {
        return name;
    }

    public boolean hasObjectToSend() {
        return !objectsToSend.isEmpty()||!buffersToSend.isEmpty();
    }

    public Object pollReceivedObject() {
        try {
            return receivedObjects.takeFirst();
        } catch (InterruptedException e) {
            throw new ServerException(e);
        }
    }

    public void addHook(ClientObserver observer) {
        hooks.add(observer);
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void startReceivingParts(int partsToReceive) {
        TCPServer.log("Started receiving message sliced by {} parts from {}", partsToReceive,getName());
        receivingParts = true;
        this.partsToReceive = partsToReceive;
        receivedParts = 0;
    }

    private Message stopReceivingParts() {
        TCPServer.log("Stopped receiving message by {} parts from {}", partsToReceive, getName());
        receivingParts = false;
        try {
            return (Message) TCPServer.deserializeBuffer(TCPServer.mergeBuffers(receivedBuffers));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            receivedBuffers.clear();
        }
        return null;
    }

    public Message receive(ByteBuffer buffer) throws ClassNotFoundException, IOException {
//        TCPServer.log("Received ByteBuffer from {}", getName());
        if(!receivingParts) {
            return (Message) TCPServer.deserializeBuffer(buffer);
        } else {
            receivedParts++;
            receivedBuffers.add(buffer);
            if(receivedParts==partsToReceive) return stopReceivingParts();
        }
        return null;
    }

    public void sendObject(Message object) {
        objectsToSend.add(object);
        update(ClientObserver.Type.WRITE);
    }



    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public boolean prepareByteBuffer() throws IOException {
        if(buffersToSend.size()>0) {
            byteBuffer.clear();
            byteBuffer.put(buffersToSend.poll());
            byteBuffer.flip();
            return true;
        } else {
            return moveObjectQueue();
        }

    }

    private void update(ClientObserver.Type type) {
        hooks.forEach(iter -> iter.observe(type, this));
    }


    private boolean moveObjectQueue() throws IOException {
        if(objectsToSend.size()>0) {
            Message objectToSend = objectsToSend.poll();
            ByteBuffer buffer = TCPServer.serializeToBuffer(objectToSend);
            if(buffer.limit()>=TCPServer.BUFFER_CAPACITY) {
                List<ByteBuffer> byteBufferList = TCPServer.sliceByteBuffer(buffer);
                TCPServer.log("Preparing to send {} \"{}\" sliced to {} parts", getName(), objectToSend.toString(), byteBufferList.size());
                buffersToSend.add(TCPServer.serializeToBuffer(new Message(Message.Type.PARTS).put("parts", byteBufferList.size())));
                buffersToSend.addAll(byteBufferList);
            } else {
                TCPServer.log("Send {} to {}", objectToSend.toString(), getName());
                buffersToSend.add(buffer);
            }
            byteBuffer.clear();
            byteBuffer.put(buffersToSend.poll());
            byteBuffer.flip();
            return true;
        } else {
            return false;
        }
    }




}
