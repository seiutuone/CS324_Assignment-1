import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public interface PeerRegister extends Node {
    void register(int id) throws RemoteException, AlreadyBoundException, NotBoundException;
}