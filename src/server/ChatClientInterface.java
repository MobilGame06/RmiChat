package server;

import java.rmi.*;

public interface ChatClientInterface extends Remote {
    void receiveMessage(String message) throws RemoteException;
}
