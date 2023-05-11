package core;

import util.Crypto;

import java.util.ArrayList;
import java.util.List;

public class EncounterRecord {
    private final DTNHost fromNode;
    private final DTNHost toNode;
    private int fromSequence;
    private int toSequence;
    private double time;
    private final List<Message> sentMessages;
    private final List<Message> receivedMessages;

    private String fromSignature;
    private String toSignature;

    public EncounterRecord(DTNHost fromNode, DTNHost toNode) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.sentMessages = new ArrayList<>();
        this.receivedMessages = new ArrayList<>();
    }

    public static void createEncounterRecord(Connection con, DTNHost fromNode, DTNHost toNode) {
        int connectionId = con.getConnectionId();
        if (fromNode.getIncompleteER(connectionId) != null || toNode.getIncompleteER(connectionId) != null) {
            // 如果存在，则已经创建
            return;
        }
        fromNode.putIncompleteER(connectionId, new EncounterRecord(fromNode, toNode));
        toNode.putIncompleteER(connectionId, new EncounterRecord(toNode, fromNode));
    }

    public static void finalizeEncounterRecord(Connection con, DTNHost fromNode, DTNHost toNode) {
        int connectionId = con.getConnectionId();
        if (fromNode.getIncompleteER(connectionId) == null || toNode.getIncompleteER(connectionId) == null) {
            // 如果不存在，则已经完成
            return;
        }
        int fromSequence = fromNode.getNextSequence();
        int toSequence = toNode.getNextSequence();
        double time = SimClock.getTime();
        fromNode.addEncounterRecord(fromNode.getIncompleteER(connectionId).finalizeER(fromSequence, toSequence, time));
        toNode.addEncounterRecord(toNode.getIncompleteER(connectionId).finalizeER(toSequence, fromSequence, time));
        fromNode.removeIncompleteER(connectionId);
        toNode.removeIncompleteER(connectionId);
    }

    public void addSentMessage(Message message) {
        this.sentMessages.add(message);
    }

    public void addReceivedMessage(Message message) {
        this.receivedMessages.add(message);
    }

    public EncounterRecord finalizeER(int fromSequence, int toSequence, double time) {
        // 设置剩下的属性
        this.fromSequence = fromSequence;
        this.toSequence = toSequence;
        this.time = time;

        // 拼接字符串用于计算hash
        StringBuilder builder = new StringBuilder();
        builder.append(this.fromNode.toString()).append(";");
        builder.append(this.toNode.toString()).append(";");
        builder.append(this.fromSequence).append(";");
        builder.append(this.toSequence).append(";");
        builder.append(this.time).append(";");
        for (Message message : this.sentMessages) {
            builder.append(message.toString()).append(";");
        }
        for (Message message : this.receivedMessages) {
            builder.append(message.toString()).append(";");
        }

        // 计算签名
        String content = builder.toString();
        this.fromSignature = Crypto.sign(content, fromNode.getPrivateKey());
        this.toSignature = Crypto.sign(content, toNode.getPrivateKey());

        return this;
    }

    public DTNHost getFromNode() {
        return fromNode;
    }

    public DTNHost getToNode() {
        return toNode;
    }

    public int getFromSequence() {
        return fromSequence;
    }

    public int getToSequence() {
        return toSequence;
    }

    public double getTime() {
        return time;
    }

    public List<Message> getSentMessages() {
        return sentMessages;
    }

    public List<Message> getReceivedMessages() {
        return receivedMessages;
    }

    public String getFromSignature() {
        return fromSignature;
    }

    public String getToSignature() {
        return toSignature;
    }

    public String toString() {
        return fromNode + (this.time == 0 ? " encounter " : " encountered ") + toNode + (this.time != 0 ? " at " + this.time : "");
    }
}
