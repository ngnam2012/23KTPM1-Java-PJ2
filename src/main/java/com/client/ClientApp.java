package com.client;

import javax.swing.*;
import java.net.InetAddress;
import java.util.UUID;

public class ClientApp {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            hostname = "Client";
        }
        String uniqueId = hostname + "-" + UUID.randomUUID().toString().substring(0, 8);

        SwingUtilities.invokeLater(() -> {
            ClientUI ui = new ClientUI(uniqueId);
            ui.setVisible(true);
        });
    }
}