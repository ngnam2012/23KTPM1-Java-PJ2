package com.server;

import java.util. Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public void addClient(String clientId, ClientHandler handler) { clients.put(clientId, handler); }
    public void removeClient(String clientId) { clients.remove(clientId); }
    public ClientHandler getClient(String clientId) { return clients.get(clientId); }
    public Collection<ClientHandler> getAllClients() { return clients.values(); }
    public int getClientCount() { return clients.size(); }
    public String[] getClientIds() { return clients.keySet().toArray(new String[0]); }
}