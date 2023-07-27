package org.example;

import org.example.config.Config;
import org.example.threads.Reader;
import org.example.threads.Writer;

import java.io.IOException;
import java.net.Socket;

public class ChatClient {

    private final String hostname;
    private final int port;
    private String username;

    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient(Config.HOSTNAME, Config.PORT);
        chatClient.execute();
    }

    public void execute() {
        try (Socket socket = new Socket(hostname, port)) {
            Writer writer = new Writer(this, socket);
            Reader reader = new Reader(socket);
            writer.start();
            reader.start();
            writer.join();
            reader.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
