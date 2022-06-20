package application.controller.server;

import application.controller.server.exceptions.ServerException;
import application.controller.server.messages.ClientMessage;
import application.controller.server.messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TCPCommunicator {
    private static final Logger logger = LoggerFactory.getLogger(
            TCPCommunicator.class);

    synchronized static public void debug(String msg) {
        logger.debug(msg);
    }

    synchronized static public void debug(String msg, Object... args) {
        logger.debug(msg, args);
    }

    private final InetSocketAddress address;
    private DataOutputStream dos;
    private DataInputStream dis;
    private Socket socket;


    public TCPCommunicator(String hostName, int port) {
        address = new InetSocketAddress(hostName, port);
    }


    public void connect() throws IOException {
        socket = new Socket();
        socket.connect(address);
        socket.setSendBufferSize(TCPServer.BUFFER_CAPACITY);
        socket.setReceiveBufferSize(TCPServer.BUFFER_CAPACITY);
        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());
    }

    public Message read() {
        try {
            Message message = (Message) readObject();
            if (message.getType() != Message.Type.PARTS) {
                debug("Received from the {}: {}", address, message);
                return message;
            } else {
                int buffers = (Integer) message.get("parts");
                debug("Preparing to receive message sliced to {} parts from {}", buffers, address);
                List<ByteBuffer> bufferList = new ArrayList<>();
                for (int i = 0; i < buffers; i++) {
                    bufferList.add(readBuffer());
                }
                message = (Message) TCPServer.deserializeBuffer(TCPServer.mergeBuffers(bufferList));
                debug("Completed receiving from the {}: {}", address, message);
                return message;
            }
        } catch (IOException | ClassNotFoundException e) {
            write(new ClientMessage(Message.Type.CLEAR));
            throw new ServerException(e);
        }
    }

    public void write(ClientMessage msg) {
        try {
            ByteBuffer byteBuffer = TCPServer.serializeToBuffer(msg);
            if (byteBuffer.limit() <= TCPServer.BUFFER_CAPACITY) {
                debug("Writing {} to {}", msg, address);
                dos.write(byteBuffer.array());
                dos.flush();
            } else {
                List<ByteBuffer> sliced = TCPServer.sliceByteBuffer(byteBuffer);
                debug("Preparing to send {}} sliced to {} parts to {}", msg, sliced.size(), address);
                dos.write(TCPServer.serializeToByteArray(new ClientMessage(Message.Type.PARTS, msg.login, msg.password).put("parts", sliced.size())));
                for (ByteBuffer iter : sliced) {
                    Message partReady = read();
                    if (partReady.getType() != Message.Type.READY) return;
                    dos.write(iter.array());
                    dos.flush();
                }
            }
        } catch (IOException e) {
            throw new ServerException(e);
        }
    }

    private ByteBuffer readBuffer() throws IOException {
        byte[] buffer = new byte[TCPServer.BUFFER_CAPACITY];
        while (dis.read(buffer) == -1) ;
        return ByteBuffer.wrap(buffer);
    }

    private Object readObject() throws IOException, ClassNotFoundException {
        return TCPServer.deserializeBuffer(readBuffer());
    }


    public void close() {
        try {
            if (socket!=null&&!socket.isClosed()) {
                socket.close();
                debug("Closing connection with {}", address);
            }
        } catch (IOException e) {

        }
    }

    public boolean isConnected() {
        return socket != null
                && !socket.isClosed()
                && socket.isConnected();
    }


}
