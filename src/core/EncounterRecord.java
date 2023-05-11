package core;

import java.util.List;

public class EncounterRecord {
    private final DTNHost fromNode;
    private final DTNHost toNode;
    private int fromSequence;
    private int toSequence;
    private double time;
    private List<Message> sentMessages;
    private List<Message> receivedMessages;
    private String fromHash;
    private String toHash;

    public EncounterRecord(DTNHost fromNode, Connection connection){
        this.fromNode = fromNode;
        this.toNode = connection.getOtherNode(fromNode);
    }

    public static EncounterRecord[] createPairER(DTNHost node1, DTNHost node2, int conId){
        // TODO: Implement this
        return new EncounterRecord[]{

        };
    }
}
