package com.server;

import com.common.*;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;
    private ServerSocket serverSocket;
    private ClientManager clientManager;
    private volatile boolean running = true;
    private final int port;

    public ServerGUI(int port) {
        this.port = port;
        this.clientManager = new ClientManager();

        setTitle("Directory Monitor - Server (Port: " + port + ")");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        new Thread(this::startServer).start();
    }

    private void initComponents() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("System Logs"));

        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        JScrollPane clientScroll = new JScrollPane(clientList);
        clientScroll.setPreferredSize(new Dimension(200, 0));
        clientScroll.setBorder(BorderFactory.createTitledBorder("Connected Clients"));

        setLayout(new BorderLayout());
        add(logScroll, BorderLayout.CENTER);
        add(clientScroll, BorderLayout.EAST);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            log("Server started on port " + port);

            while (running) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                setupCallbacks(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            log("Server Error: " + e.getMessage());
        }
    }

    private void setupCallbacks(ClientHandler handler) {
        handler.setOnRegister((id, h) -> {
            clientManager.addClient(id, h);
            SwingUtilities.invokeLater(() -> clientListModel.addElement(id));
            log("New Client: " + id);
        });

        handler.setOnFileChange((id, event) -> {
            String msg = String.format("[%s] %s %s: %s",
                    event.getFormattedTime(), event.getChangeType().getIcon(),
                    id, event.getFilePath());
            log(msg);
        });

        handler.setOnDisconnect(h -> {
            String id = h.getClientId();
            if (id != null) {
                clientManager.removeClient(id);
                SwingUtilities.invokeLater(() -> clientListModel.removeElement(id));
                log("Client Disconnected: " + id);
            }
        });
    }
}