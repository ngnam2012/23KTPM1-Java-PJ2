package com.server;

import javax.swing.*;

public class ServerApp {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        SwingUtilities.invokeLater(() -> {
            ServerGUI serverGUI = new ServerGUI(5000);
            serverGUI.setVisible(true);
        });
    }
}