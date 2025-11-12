package server;

import java.rmi.*;

public interface ChatServerInterface extends Remote {
    void registerClient(ChatClientInterface client) throws RemoteException;
    void broadcastMessage(String name, String message) throws RemoteException;
}
