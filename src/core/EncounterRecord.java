package core;

import util.Crypto;

import java.util.ArrayList;
import java.util.List;

public class EncounterRecord {
    private final DTNHost thisNode;
    private final DTNHost peerNode;
    private int thisSequence;
    private int peerSequence;
    private double time;
    private final List<Message> sentMessages;
    private final List<Message> receivedMessages;

    private String thisSignature;
    private String peerSignature;

    public EncounterRecord(DTNHost thisNode, DTNHost peerNode) {
        this.thisNode = thisNode;
        this.peerNode = peerNode;
        this.sentMessages = new ArrayList<>();
        this.receivedMessages = new ArrayList<>();
    }

    public static void createEncounterRecord(Connection con, DTNHost thisNode, DTNHost peerNode) {
        int connectionId = con.getConnectionId();
        if (thisNode.getIncompleteER(connectionId) != null || peerNode.getIncompleteER(connectionId) != null) {
            // 如果存在，则已经创建
            return;
        }
        thisNode.putIncompleteER(connectionId, new EncounterRecord(thisNode, peerNode));
        peerNode.putIncompleteER(connectionId, new EncounterRecord(peerNode, thisNode));
    }

    public static void finalizeEncounterRecord(Connection con, DTNHost thisNode, DTNHost peerNode) {
        int connectionId = con.getConnectionId();
        if (thisNode.getIncompleteER(connectionId) == null || peerNode.getIncompleteER(connectionId) == null) {
            // 如果不存在，则已经完成
            return;
        }
        if (thisNode.getIncompleteER(connectionId).getSentMessages().size() != 0
                || thisNode.getIncompleteER(connectionId).getReceivedMessages().size()!=0) {
            // 如果有发送和接收消息，才有意义，需要创建记录
            int fromSequence = thisNode.getNextSequence();
            int toSequence = peerNode.getNextSequence();
            double time = SimClock.getTime();
            thisNode.addEncounterRecord(thisNode.getIncompleteER(connectionId).finalizeER(fromSequence, toSequence, time));
            peerNode.addEncounterRecord(peerNode.getIncompleteER(connectionId).finalizeER(toSequence, fromSequence, time));
        }
        thisNode.removeIncompleteER(connectionId);
        peerNode.removeIncompleteER(connectionId);
    }

    public void addSentMessage(Message message) {
        this.sentMessages.add(message);
    }

    public void addReceivedMessage(Message message) {
        this.receivedMessages.add(message);
    }

    public EncounterRecord finalizeER(int thisSequence, int peerSequence, double time) {
        // 设置剩下的属性
        this.thisSequence = thisSequence;
        this.peerSequence = peerSequence;
        this.time = time;

        // 拼接字符串用于计算hash
        StringBuilder builder = new StringBuilder();
        builder.append(this.thisNode.toString()).append(";");
        builder.append(this.peerNode.toString()).append(";");
        builder.append(this.thisSequence).append(";");
        builder.append(this.peerSequence).append(";");
        builder.append(this.time).append(";");
        for (Message message : this.sentMessages) {
            builder.append(message.toString()).append(";");
        }
        for (Message message : this.receivedMessages) {
            builder.append(message.toString()).append(";");
        }

        // 计算签名
        String content = builder.toString();
        this.thisSignature = Crypto.sign(content, thisNode.getPrivateKey());
        this.peerSignature = Crypto.sign(content, peerNode.getPrivateKey());

        return this;
    }

    public DTNHost getThisNode() {
        return thisNode;
    }

    public DTNHost getPeerNode() {
        return peerNode;
    }

    public int getThisSequence() {
        return thisSequence;
    }

    public int getPeerSequence() {
        return peerSequence;
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

    public String getThisSignature() {
        return thisSignature;
    }

    public String getPeerSignature() {
        return peerSignature;
    }

    public String toString() {
        if (this.time == 0)
            return thisNode + " encounter "  + peerNode;
        else
            return thisNode + " encountered " + peerNode + " at " + this.time + ", sending " + this.sentMessages.size() + " messages and receiving " + this.receivedMessages.size() + " messages.";
    }
}
