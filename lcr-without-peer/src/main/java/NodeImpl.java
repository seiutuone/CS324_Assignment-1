import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class NodeImpl extends UnicastRemoteObject implements Node {
    private final int id;
    private Node next;
    private int leader = -1;

    public NodeImpl(int id) throws RemoteException {
        this.id = id;
        System.out.println("Node " + id + " created.");
    }

    public void setNext(Node next) {
        this.next = next;
        System.out.println("Node " + id + " connected to next node in ring topology.");
    }

    public int getLeader() {
        return leader;
    }

    public void initiateElection() throws RemoteException {
        System.out.println("Node " + id + " initiating election with ID " + id);
        next.receiveElection(id);
    }

    @Override
    public void receiveElection(int candidateId) throws RemoteException {
        System.out.println("Node " + id + " received ELECTION(" + candidateId + ")");

        if (candidateId == id) {
            // My ID completed the circuit - I'm elected as leader
            System.out.println("Node " + id + " elected as leader!");
            leader = id;
            // Inform all participants by sending LEADER message
            next.receiveLeader(id);
        } else if (candidateId > id) {
            // Forward the election message with larger ID
            next.receiveElection(candidateId);
        }
        // If candidateId < id, drop the message (do nothing)
    }

    @Override
    public void receiveLeader(int leaderId) throws RemoteException {
        System.out.println("Node " + id + " received LEADER(" + leaderId + ")");
        leader = leaderId;

        if (id != leaderId) {
            // Forward leader message to inform all participants
            next.receiveLeader(leaderId);
        } else {
            // Leader message completed the circuit at leader node
            System.out.println("Leader message completed the circuit. Node " + id + " is confirmed as the leader.");
        }
    }
}