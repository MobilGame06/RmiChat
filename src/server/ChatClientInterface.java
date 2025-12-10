package server;

import java.rmi.*;

public interface ChatClientInterface extends Remote {
    void receiveMessage(String message) throws RemoteException;

    String getName() throws RemoteException;

    void disconnect(String reason) throws RemoteException;
}