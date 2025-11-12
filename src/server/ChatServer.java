package server;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;

public class ChatServer extends UnicastRemoteObject implements ChatServerInterface {
    private final List<ChatClientInterface> clients = new ArrayList<>();

    protected ChatServer() throws RemoteException {
        super();
    }

    public synchronized void registerClient(ChatClientInterface client) throws RemoteException {
        clients.add(client);
        System.out.println("Neuer Client verbunden!");
    }

    public synchronized void broadcastMessage(String name, String message) throws RemoteException {
        String fullMessage = name + ": " + message;
        System.out.println("Sende: " + fullMessage);
        for (ChatClientInterface client : clients) {
            client.receiveMessage(fullMessage);
        }
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099); // Standard-Port
            ChatServer server = new ChatServer();
            Naming.rebind("rmi://192.168.81.223/ChatServer", server);
            System.out.println("ChatServer läuft...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
