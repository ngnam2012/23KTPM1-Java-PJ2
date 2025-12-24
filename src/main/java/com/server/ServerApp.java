package com.server;

import com.common.*;

import java.io. IOException;
import java.net. ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerApp {

    private ServerSocket serverSocket;
    private ClientManager clientManager;
    private volatile boolean running = true;

    public ServerApp() {
        clientManager = new ClientManager();
    }

    public void start() {
        System.out. println("DIRECTORY MONITOR - SERVER\n");

        startCommandThread();

        try {
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            System.out. println("Server đang lắng nghe port " + Constants.SERVER_PORT);
            System.out.println("Nhập 'help' để xem lệnh\n");

            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("Client mới:  " + socket.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(socket);
                setupCallbacks(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            if (running) System.err.println("Lỗi:  " + e.getMessage());
        }
    }

    private void startCommandThread() {
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (running) {
                String cmd = sc.nextLine().trim().toLowerCase();
                handleCommand(cmd);
            }
        }).start();
    }

    private void handleCommand(String cmd) {
        String[] parts = cmd.split("\\s+");
        switch (parts[0]) {
            case "help":
                System.out.println("\nLỆNH:  help | list | watch <id> <dir> | exit\n");
                break;
            case "list":
                String[] ids = clientManager.getClientIds();
                System.out.println("\nCLIENTS (" + ids.length + "):");
                for (String id :  ids) {
                    System. out.println("  • " + id);
                }
                System.out.println();
                break;
            case "exit":
            case "quit":
                System.out.println("Tạm biệt!");
                System.exit(0);
                break;
        }
    }

    private void setupCallbacks(ClientHandler handler) {
        handler.setOnRegister((id, h) -> {
            clientManager. addClient(id, h);
            System.out.println("Client đăng ký:  " + id);
        });

        handler.setOnFileChange((id, event) -> {
            System.out.println(String.format("[%s] %s %s:  %s",
                    event.getFormattedTime(), event.getChangeType().getIcon(),
                    id, event.getFilePath()));
        });

        handler. setOnDisconnect(h -> {
            String id = h.getClientId();
            if (id != null) {
                clientManager.removeClient(id);
                System.out.println("Client ngắt:  " + id);
            }
        });
    }

    public static void main(String[] args) {
        new ServerApp().start();
    }
}