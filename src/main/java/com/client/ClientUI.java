package com.client;

import com.common.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ClientUI extends JFrame {
    private JTextField pathField;
    private JButton btnBrowse;
    private JButton btnStart;
    private JTextArea logArea;

    private ClientConnection clientConnection;
    private DirectoryWatcher directoryWatcher;
    private final String clientId;

    public ClientUI(String clientId) {
        this.clientId = clientId;

        setTitle("Client Monitor: " + clientId);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        connectToServer();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topPanel.add(new JLabel("Directory: "), BorderLayout.WEST);

        pathField = new JTextField();
        pathField.setEditable(false);
        topPanel.add(pathField, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        btnBrowse = new JButton("Browse");
        btnStart = new JButton("Start");
        btnStart.setEnabled(false);

        btnPanel.add(btnBrowse);
        btnPanel.add(btnStart);
        topPanel.add(btnPanel, BorderLayout.EAST);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("File Logs"));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnBrowse.addActionListener(e -> chooseDirectory());
        btnStart.addActionListener(e -> startWatching());
    }

    private void connectToServer() {
        clientConnection = new ClientConnection(clientId);
        boolean connected = clientConnection.connect("localhost", 5000);

        if (connected) {
            log("Connected to Server (localhost:5000)");
            clientConnection.setOnDisconnected(() -> {
                log("Disconnected from Server!");
                SwingUtilities.invokeLater(() -> btnStart.setEnabled(false));
            });
        } else {
            log("Connection Failed!");
            btnBrowse.setEnabled(false);
        }
    }

    private void chooseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            pathField.setText(selectedFolder.getAbsolutePath());
            if (clientConnection != null && clientConnection.isConnected()) {
                btnStart.setEnabled(true);
            }
        }
    }

    private void startWatching() {
        String dir = pathField.getText();
        if (dir.isEmpty()) return;

        btnStart.setEnabled(false);
        btnBrowse.setEnabled(false);

        log("Monitoring started: " + dir);

        directoryWatcher = new DirectoryWatcher(dir, clientId, event -> {
            SwingUtilities.invokeLater(() ->
                    log(event.getChangeType().getIcon() + " " + event.getFilePath())
            );
            clientConnection.sendFileChangeEvent(event);
        });

        new Thread(directoryWatcher).start();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public void dispose() {
        if (directoryWatcher != null) directoryWatcher.stop();
        if (clientConnection != null) clientConnection.disconnect();
        super.dispose();
    }
}