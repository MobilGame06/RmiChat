package client;

import server.ChatClientInterface;
import server.ChatServerInterface;

import java.rmi.*;
import java.rmi.server.*;
import java.util.Scanner;

public class ChatClient extends UnicastRemoteObject implements ChatClientInterface {
    private String name;
    private ChatServerInterface server;
    private volatile boolean running = true;

    protected ChatClient(String name, ChatServerInterface server) throws RemoteException {
        this.name = name;
        this.server = server;
        server.registerClient(this);
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        System.out.println(message);
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public void disconnect(String reason) throws RemoteException {
        System.out.println("[System] " + reason);
        running = false;
        try {
            UnicastRemoteObject.unexportObject(this, true);
        } catch (Exception e) {
        }
        System.exit(0);
    }

    public void startChat() throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Chat gestartet. Schreibe deine Nachricht:");
        System.out.println("Befehl zum Kicken: /kick <Name>");

        while (running) {
            if (!scanner.hasNextLine()) {
                break;
            }
            String msg = scanner.nextLine();

            try {
                server.broadcastMessage(name, msg);
            } catch (RemoteException e) {
                System.out.println("[System] Verbindung zum Server verloren.");
                break;
            }
        }

        System.out.println("[System] Chat wird beendet.");
    }

    public static void main(String[] args) {
        try {
            ChatServerInterface server = (ChatServerInterface) Naming.lookup("rmi://localhost/ChatServer");
            Scanner sc = new Scanner(System.in);
            System.out.print("Dein Name: ");
            String name = sc.nextLine();

            ChatClient client = new ChatClient(name, server);
            client.startChat();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}