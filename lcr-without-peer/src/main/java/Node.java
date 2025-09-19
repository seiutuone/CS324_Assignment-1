import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote {
    void receiveElection(int candidateId) throws RemoteException;
    void receiveLeader(int leaderId) throws RemoteException;
}