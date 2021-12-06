package csp_V3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public final class MessageListener extends ThreadLoop {

    private YARNConcurrentProcess process;
    private DatagramSocket socket;

    public MessageListener(YARNConcurrentProcess process, DatagramSocket socket) {
        super(process.getName());
        this.process = process;
        this.socket = socket;
    }

    @Override
    void beforeLoop() {}

    @Override
    void inLoop() {
        byte[] rcvData = new byte[Message.MAX_SIZE];
        try {
            DatagramPacket rcvPaquet = new DatagramPacket(rcvData, rcvData.length);
            this.socket.receive(rcvPaquet);
            Message rcvMsg = Message.fromBytes(rcvPaquet.getData(), rcvPaquet.getData().length);
            this.process.receiveMessage(rcvMsg);
        } catch (SocketTimeoutException e) {
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    void afterLoop() {
        this.socket.close();
    }
    
}
