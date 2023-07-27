package org.example.threads;

import org.example.ChatClient;
import org.example.config.Config;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Writer extends Thread {

    private final ChatClient chatClient;
    private final Socket socket;

    public Writer(ChatClient chatClient, Socket socket) {
        this.chatClient = chatClient;
        this.socket = socket;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        try (PrintWriter out = new PrintWriter(socket.getOutputStream())) {
            processUsername(scanner, out);
            processGroupName(scanner, out);
            String message;
            while (true) {
                message = scanner.nextLine();
                sendMessage(out, message);
                if (message.equals(Config.LEAVE_MESSAGE)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processGroupName(Scanner scanner, PrintWriter out) {
        System.out.println("Enter the name of the group you want to join:");
        String groupName = scanner.nextLine();
        sendMessage(out, groupName);
    }

    private void processUsername(Scanner scanner, PrintWriter out) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        chatClient.setUsername(username);
        sendMessage(out, username);
    }

    private void sendMessage(PrintWriter out, String message) {
        out.println(message);
        out.flush();
    }
}
