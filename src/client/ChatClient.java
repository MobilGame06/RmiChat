package client;

import server.ChatClientInterface;
import server.ChatServerInterface;

import java.rmi.*;
import java.rmi.server.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ChatClient extends UnicastRemoteObject implements ChatClientInterface {
    private String name;
    private ChatServerInterface server;
    private volatile boolean running = true;

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";

    private static final String FG_RED = "\u001B[31m";
    private static final String FG_GREEN = "\u001B[32m";
    private static final String FG_YELLOW = "\u001B[33m";
    private static final String FG_MAGENTA = "\u001B[35m";
    private static final String FG_CYAN = "\u001B[36m";
    private static final String FG_WHITE = "\u001B[37m";

    private static final String SYSTEM_TAG = FG_YELLOW + BOLD + "[System]" + RESET;
    private static final String PROMPT_ARROW = FG_CYAN + "➤ " + RESET;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    protected ChatClient(String name, ChatServerInterface server) throws RemoteException {
        this.name = name;
        this.server = server;
        server.registerClient(this);
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        String time = LocalTime.now().format(TIME_FMT);
        String timeTag = DIM + "[" + time + "]" + RESET + " ";

        if (message.startsWith("[System]")) {
            String clean = message.substring("[System]".length()).trim();
            System.out.println(timeTag + SYSTEM_TAG + " " + FG_YELLOW + clean + RESET);
        } else {
            int idx = message.indexOf(":");
            if (idx > 0) {
                String sender = message.substring(0, idx);
                String msgBody = message.substring(idx + 1).trim();
                System.out.println(
                        timeTag +
                                FG_GREEN + BOLD + sender + RESET +
                                ": " +
                                FG_WHITE + msgBody + RESET
                );
            } else {
                System.out.println(timeTag + FG_WHITE + message + RESET);
            }
        }
        System.out.print(PROMPT_ARROW);
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public void disconnect(String reason) throws RemoteException {
        System.out.println(SYSTEM_TAG + " " + FG_RED + reason + RESET);
        running = false;
        try {
            UnicastRemoteObject.unexportObject(this, true);
        } catch (Exception e) {
        }
        System.exit(0);
    }

    public void startChat() throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        System.out.println(SYSTEM_TAG + " " + FG_CYAN + "Chat gestartet. Schreibe deine Nachricht:" + RESET);
        System.out.println(SYSTEM_TAG + " " + FG_MAGENTA + "Befehl zum Kicken: " + BOLD + "/kick <Name>" + RESET);

        System.out.print(PROMPT_ARROW);
        while (running) {
            if (!scanner.hasNextLine()) {
                break;
            }
            String msg = scanner.nextLine();

            try {
                server.broadcastMessage(name, msg);
            } catch (RemoteException e) {
                System.out.println(SYSTEM_TAG + " " + FG_RED + "Verbindung zum Server verloren." + RESET);
                break;
            }
            System.out.print(PROMPT_ARROW);
        }

        System.out.println(SYSTEM_TAG + " " + FG_YELLOW + "Chat wird beendet." + RESET);
    }

    public static void main(String[] args) {
        try {
            ChatServerInterface server = (ChatServerInterface) Naming.lookup("rmi://localhost/ChatServer");
            Scanner sc = new Scanner(System.in);
            System.out.print(FG_CYAN + "Dein Name: " + RESET);
            String name = sc.nextLine();

            ChatClient client = new ChatClient(name, server);
            client.startChat();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}