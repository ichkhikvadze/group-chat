package org.example.threads;

import org.example.ChatServer;
import org.example.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class Handler extends Thread {

    private final ChatServer chatServer;
    private final Socket socket;
    private String groupName;
    private PrintWriter out;

    public Handler(ChatServer chatServer, Socket socket) {
        this.chatServer = chatServer;
        this.socket = socket;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream());
            String username = in.readLine();
            groupName = in.readLine();
            String serverMessage = String.format("New User %s has connected", username);
            chatServer.sendMessageToClients(this, serverMessage);
            List<String> groupChatMessageList = chatServer.getGroupChatHistory(groupName)
                    .orElse(Collections.emptyList());
            String groupChatHistory = getGroupChatHistory(groupChatMessageList);
            chatServer.sendGroupChatHistory(this, groupChatHistory);

            String clientMessage;
            while (true) {
                clientMessage = in.readLine();
                if (clientMessage.equals(Config.LEAVE_MESSAGE)) {
                    break;
                }
                serverMessage = String.format("%s: %s", username, clientMessage);
                chatServer.addMessageToGroupHistory(groupName, serverMessage);
                chatServer.sendMessageToClients(this, serverMessage);
            }
            chatServer.removeUser(this, username);
            serverMessage = String.format("%s has left", username);
            chatServer.sendMessageToClients(this, serverMessage);
            socket.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getGroupChatHistory(List<String> groupChatHistory) {
        if (groupChatHistory.isEmpty()) {
            return "";
        }
        return groupChatHistory.stream()
                .reduce("\n", (history, currMessage) -> history += currMessage + "\n");
    }

    public void sendMessage(String message) {
        out.println(message);
        out.flush();
    }
}
