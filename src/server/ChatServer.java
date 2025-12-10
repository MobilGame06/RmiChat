package server;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;

public class ChatServer extends UnicastRemoteObject implements ChatServerInterface {
    private final Map<String, ChatClientInterface> clients = new HashMap<>();

    protected ChatServer() throws RemoteException {
        super();
    }

    public synchronized void registerClient(ChatClientInterface client) throws RemoteException {
        String name = client.getName();
        clients.put(name, client);
        System.out.println("Neuer Client verbunden: " + name);

        String joinMsg = "[System] " + name + " hat den Chat betreten.";
        for (ChatClientInterface c : clients.values()) {
            c.receiveMessage(joinMsg);
        }
    }

    public synchronized void broadcastMessage(String name, String message) throws RemoteException {
        if (message.startsWith("/kick ")) {
            String targetName = message.substring(6).trim();
            if (targetName.isEmpty()) {
                ChatClientInterface sender = clients.get(name);
                if (sender != null) {
                    sender.receiveMessage("[System] Benutzung: /kick <Name>");
                }
                return;
            }

            ChatClientInterface kicked = clients.remove(targetName);
            if (kicked != null) {
                String info = "[System] " + targetName + " wurde von " + name + " gekickt.";
                System.out.println(info);
                for (ChatClientInterface c : clients.values()) {
                    c.receiveMessage(info);
                }

                try {
                    kicked.disconnect("Du wurdest aus dem Chat gekickt.");
                } catch (RemoteException e) {
                    System.out.println("Fehler beim Disconnect von " + targetName + ": " + e.getMessage());
                }
            } else {
                ChatClientInterface sender = clients.get(name);
                if (sender != null) {
                    sender.receiveMessage("[System] Benutzer '" + targetName + "' nicht gefunden.");
                }
            }
            return;
        }

        String fullMessage = name + ": " + message;
        System.out.println("Sende: " + fullMessage);
        for (ChatClientInterface client : clients.values()) {
            client.receiveMessage(fullMessage);
        }
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            ChatServer server = new ChatServer();
            Naming.rebind("rmi://localhost/ChatServer", server);
            System.out.println("ChatServer läuft...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}