package org.example;

import org.example.config.Config;
import org.example.threads.Handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer {

    private final int port;
    private final List<Handler> handlerList;
    private final Map<String, List<String>> chatHistory;

    public ChatServer(int port) {
        this.port = port;
        handlerList = new ArrayList<>();
        chatHistory = new HashMap<>();
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(Config.PORT);
        chatServer.startServer();
    }

    public void startServer() {
        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                Socket socket = serverSocket.accept();
                System.out.println("New user has connected");
                Handler handler = new Handler(this, socket);
                handlerList.add(handler);
                handler.start();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public Optional<List<String>> getGroupChatHistory(String groupName) {
        return Optional.ofNullable(chatHistory.get(groupName));
    }

    public void addMessageToGroupHistory(String groupName, String newMessage) {
        if (!chatHistory.containsKey(groupName)) {
            synchronized (chatHistory) {
                if (!chatHistory.containsKey(groupName)) {
                    List<String> groupHistory = new LinkedList<>();
                    groupHistory.add(newMessage);
                    chatHistory.put(groupName, groupHistory);
                }else {
                    chatHistory.get(groupName).add(newMessage);
                }
            }
        }else {
            chatHistory.get(groupName).add(newMessage);
        }
    }

    public void removeUser(Handler handlerToRemove, String usernameToRemove) {
        handlerList.remove(handlerToRemove);
        System.out.printf("%s left\n", usernameToRemove);
    }

    public void sendGroupChatHistory(Handler handler, String chatHistory) {
        handler.sendMessage(chatHistory);
    }

    public void sendMessageToClients(Handler handler, String message) {
        handlerList.stream()
                .filter(currHandler -> handler.getGroupName().equals(currHandler.getGroupName()))
                .forEach(currHandler -> {
                    if (handler != currHandler) {
                        currHandler.sendMessage(message);
                    }
                });
    }
}
