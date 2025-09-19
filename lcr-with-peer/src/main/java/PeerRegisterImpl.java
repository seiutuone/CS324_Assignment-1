import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class PeerRegisterImpl extends UnicastRemoteObject implements PeerRegister {
    private final int id;
    private int leaderId;
    private Node nextNode;
    private boolean isLeader;
    private boolean hasVoted;
    private boolean isAlive;
    public Registry registry;

    private ArrayList<Integer> peers;
    private boolean electionInProgress;

    protected PeerRegisterImpl() throws RemoteException {
        this.id = 0;
        isLeader = false;
        isAlive = true;
        peers = new ArrayList<>();
        peers.add(id);
        electionInProgress = false;

        this.registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
    }

    @Override
    public synchronized void register(int id) throws RemoteException, NotBoundException {
        System.out.println("Attempting to register Node " + id);
        peers.add(id);
        System.out.println(peers);
        if (electionInProgress) {
            throw new RemoteException("PR: Election in progress. Cannot register.");
        }

        if (peers.size() > 1) {
            Integer previousNodeID = peers.get(peers.size() - 2);
            Node node = (Node) registry.lookup("Node" + id);
            Node previousNode = (Node) registry.lookup("Node" + previousNodeID);
            previousNode.setNextNode(node);
        }
        System.out.println("PR: Node " + id + " registered.");
    }

    @Override
    public void recieveElection(int candidateId, int originId) throws RemoteException {
        electionInProgress = true;
        System.out.println("PR: Election is occurring");
        nextNode.recieveElection(candidateId, originId);
    }

    @Override
    public void recieveLeader(int leaderId, int originId) throws RemoteException {
        this.leaderId = leaderId;
        System.out.println("PR: Leader is " + leaderId);
        electionInProgress = false;
        nextNode.recieveLeader(leaderId, originId);
    }

    @Override
    public void setNextNode(Node nextNode) throws RemoteException {
        this.nextNode = nextNode;
    }

    public static void main(String[] args) throws RemoteException {
        PeerRegisterImpl peerRegister = new PeerRegisterImpl();

        try {
            peerRegister.registry.bind("Node0", peerRegister);
        } catch (AlreadyBoundException e) {
            System.err.println("Peer register is already binded.");
        }
        System.out.println("PR: Peer register node running . . .");

        // Keep running
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}