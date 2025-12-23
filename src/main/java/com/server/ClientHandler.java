package com.server;


import com.common.*;

import java.io.*;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
public class ClientHandler implements Runnable{
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String clientId;
    private volatile boolean running = true;

    private Consumer<ClientHandler> onDisconnect;
    private BiConsumer<String, FileChangeEvents> onFileChange;
    private BiConsumer<String, ClientHandler> onRegister;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            while (running) {
                Message msg = (Message) in.readObject();
                handleMessage(msg);
            }
        } catch (Exception e) {
            // Client disconnected
        } finally {
            if (onDisconnect != null) onDisconnect.accept(this);
            close();
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case Constants.MSG_REGISTER:
                clientId = (String) msg.getData();
                if (onRegister != null) onRegister.accept(clientId, this);
                break;
            case Constants.MSG_FILE_CHANGE:
                FileChangeEvents event = (FileChangeEvents) msg.getData();
                if (onFileChange != null) onFileChange.accept(clientId, event);
                break;
            case Constants.MSG_DISCONNECT:
                running = false;
                break;
        }
    }

    public synchronized void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {}
    }

    public void requestWatch(String directory) {
        sendMessage(new Message(Constants.MSG_WATCH_REQUEST, directory, "SERVER"));
    }

    public void close() {
        running = false;
        try { if (socket != null) socket.close(); } catch (IOException e) {}
    }

    public void setOnDisconnect(Consumer<ClientHandler> cb) { this.onDisconnect = cb; }
    public void setOnFileChange(BiConsumer<String, FileChangeEvents> cb) { this.onFileChange = cb; }
    public void setOnRegister(BiConsumer<String, ClientHandler> cb) { this.onRegister = cb; }

    public String getClientId() { return clientId; }
    public String getClientAddress() { return socket.getInetAddress().getHostAddress(); }
}
