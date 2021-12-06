package csp_V3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.IOException;

public class Message implements Serializable {

    static final int MAX_SIZE = 65536;

    private String tag;
    private int source_id;
    private String content;
    private int destination_id;

    public Message(int dest_id, String tag, String content) {
        this.destination_id = dest_id;
        this.tag = tag;
        this.content = content;
        if (getLength() > MAX_SIZE) throw new IllegalArgumentException("Message trop volumineux.");
    }

    private int getLength() {
        return Integer.BYTES + Integer.BYTES + this.tag.length() + this.content.length();
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baout);
        oos.writeObject(this);
        oos.flush();
        return baout.toByteArray();
    }

    public static Message fromBytes(byte[] bytes, int length) throws ClassNotFoundException, IOException {
        ByteArrayInputStream bain = new ByteArrayInputStream(bytes, 0, length);
        ObjectInputStream ois = new ObjectInputStream(bain);
        return (Message) ois.readObject();
    }

    void setSourceId(int src_id) {
        this.source_id = src_id;
    }

    public int getSourceId() {
        return this.source_id;
    }

    public int getDestinationId() {
        return this.destination_id;
    }

    public String getTag() {
        return this.tag;
    }

    public String getContent() {
        return this.content;
    }

    @Override
    public String toString() {
        return "Message (" + source_id + " ~~>> " + destination_id + ", " + tag  + ", '" + content + "')";
    }
}
