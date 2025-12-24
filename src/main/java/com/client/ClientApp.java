package com.client;

import com.common.*;
import java.io.File;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.UUID;

public class ClientApp {
    private ClientConnection clientConnection;
    private DirectoryWatcher directoryWatcher;
    private final String clientId;

    public ClientApp() {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            hostname = "Client";
        }
        this.clientId = hostname + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void startWatch(String directory) {
        directoryWatcher = new DirectoryWatcher(directory, clientId, event -> {
            System.out.println(event.getChangeType().getIcon() + " " + event.getFilePath());
            clientConnection.sendFileChangeEvent(event);
        });
        new Thread(directoryWatcher).start();
    }

    private void cleanup() {
        if (directoryWatcher != null) directoryWatcher.stop();
        if (clientConnection != null) clientConnection.disconnect();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Client App!");
        System.out.println("ClientID: " + clientId + "\n");

        clientConnection = new ClientConnection(clientId);

        if (!clientConnection.connect("localhost", 5000)) {
            System.out.println("Failed to connect to the Server!");
            return;
        }
        System.out.println("Connected to Server successfully!");

        clientConnection.setOnDisconnected(() -> {
            System.out.println("Disconnected from Server!");
            if (directoryWatcher != null) directoryWatcher.stop();
        });

        while (true) {
            System.out.print("Đường dẫn thư mục: ");
            String dir = scanner.nextLine().trim();
            File folder = new File(dir);
            if (folder.exists() && folder.isDirectory()) {
                startWatch(dir);
                break;
            }
            System.out.println("Thư mục không hợp lệ!\n");
        }

        System.out.println("Nhập 0 để thoát\n");
        while (true) {
            if (scanner.hasNextInt()) {
                int cmd = scanner.nextInt();
                if (cmd == 0) break;
            } else {
                scanner.next();
            }
        }

        cleanup();
        System.out.println("Goodbye!");
    }

    public static void main(String[] args) {
        new ClientApp().start();
    }
}