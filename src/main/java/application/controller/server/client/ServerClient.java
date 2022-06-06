package application.controller.server.client;

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

public class ServerClient {
    private final ByteBuffer byteBuffer;


    private Queue<ByteBuffer> buffersToSend;
    private Queue<Message> objectsToSend;
    private List<ByteBuffer> receivedBuffers;
    private BlockingDeque<Object> receivedObjects;
    private final String name = "Undefined";
    {
        buffersToSend = new ArrayDeque<>();
        objectsToSend = new ArrayDeque<>();
        receivedBuffers = new ArrayList<>();
        receivedObjects = new LinkedBlockingDeque<>();
    }

    public ServerClient(int bufferCapacity) {
        this.byteBuffer = ByteBuffer.allocate(bufferCapacity);
    }

    public void receiveObject(Object object) {
        TCPServer.log("Received object {} from {}", object.getClass().getSimpleName(),getName());
        receivedObjects.add(object);
    }

    public String getName() {
        return name;
    }

    public Object pollReceivedObject() {
        try {
            return receivedObjects.takeFirst();
        } catch (InterruptedException e) {
            throw new ServerException(e);
        }
    }

    private boolean receivingParts = false;
    private int partsToReceive;
    private int receivedParts;

    public boolean isReceivingParts() {
        return receivingParts;
    }

    public void startReceivingParts(int partsToReceive) {
        TCPServer.log("Started receiving message sliced by {} parts from {}", partsToReceive,getName());
        receivingParts = true;
        this.partsToReceive = partsToReceive;
        receivedParts = 0;
    }

    public Message stopReceivingParts() {
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

    public Message receivePart(ByteBuffer buffer) throws ClassNotFoundException, IOException {
//        TCPServer.log("Received ByteBuffer from {}", getName());
        if(!receivingParts) {
            receivedObjects.add(TCPServer.deserializeBuffer(buffer));
        } else {
            receivedParts++;
            receivedBuffers.add(buffer);
            if(receivedParts==partsToReceive) return stopReceivingParts();
        }
        return null;
    }

    public void addObjectToSend(Message object) {
        objectsToSend.add(object);
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
