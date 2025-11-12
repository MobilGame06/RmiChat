package client;

import server.ChatClientInterface;
import server.ChatServerInterface;

import java.rmi.*;
import java.rmi.server.*;
import java.util.Scanner;

public class ChatClient extends UnicastRemoteObject implements ChatClientInterface {
    private String name;
    private ChatServerInterface server;

    protected ChatClient(String name, ChatServerInterface server) throws RemoteException {
        this.name = name;
        this.server = server;
        server.registerClient(this);
    }

    public void receiveMessage(String message) throws RemoteException {
        System.out.println(message);
    }

    public void startChat() throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Chat gestartet. Schreibe deine Nachricht:");

        while (true) {
            String msg = scanner.nextLine();
            server.broadcastMessage(name, msg);
        }
    }

    public static void main(String[] args) {
        try {
            ChatServerInterface server = (ChatServerInterface) Naming.lookup("rmi://192.168.81.223/ChatServer");
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
