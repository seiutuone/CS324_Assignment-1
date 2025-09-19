import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote {
    void recieveElection(int candidateId, int originId) throws RemoteException;
    void recieveLeader(int leaderId, int originId) throws RemoteException;
    void setNextNode(Node nextNode) throws RemoteException;
}