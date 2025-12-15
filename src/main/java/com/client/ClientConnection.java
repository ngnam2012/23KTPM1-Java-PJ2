package com.client;


import com.common.*;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;
public class ClientConnection {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final String clientId;
    private Consumer<Message> onMessageReceived;
    private Runnable onDisconnected;
    private volatile boolean connected = false;
    ClientConnection(String clientId) {
        this.clientId = clientId;
    }
    public void setOnMessageReceived(Consumer<Message> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }
    public void setOnDisconnected(Runnable onDisconnected) {
        this.onDisconnected = onDisconnected;
    }
    public boolean isConnected() {
        return connected;
    }
    public String getClientId() {
        return clientId;
    }


    public synchronized void sendMessage(Message message) {
        if(!connected) return;
        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (IOException e) {
            handleDisconnect();
        }
    }

    private void startListening() {
        Thread listener = new Thread(() -> {
            while(connected) {
                try {
                    Message message = (Message) in.readObject();
                    if(onMessageReceived != null) {
                        onMessageReceived.accept(message);
                    }
                } catch(Exception e) {
                    if(connected) handleDisconnect();
                    break;
                }
            }
        });
        listener.setDaemon(true);
        listener.start();
    }
    private void handleDisconnect() {
        connected = false;
        if(onDisconnected != null) onDisconnected.run();
    }
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            sendMessage(new Message(Constants.MSG_REGISTER, clientId, clientId));
            startListening();
            return true;
        } catch(IOException e) {
            return false;
        }
    }
    public void sendFileChangeEvent(FileChangeEvents event) {
        sendMessage(new Message(Constants.MSG_FILE_CHANGE, event, clientId));
    }

    public void closeResources() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }

            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {}
    }

    public void disconnect() {
        if (!connected) return;
        connected = false;
        try {
            if (out != null) {
                out.writeObject(new Message(Constants.MSG_DISCONNECT, null, clientId));
                out.flush();
            }
        } catch (IOException e) {
            closeResources();
        }
    }
}
