

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerHandlerImpl implements ServerHandler {
    
    static private ArrayList<ClientHandler> clientList;
    static private ArrayList<String> messageList;
    static private FileWriter logFileWriter;
    static private FileReader logFileReader;
    
    public ServerHandlerImpl(ArrayList<ClientHandler> clients, ArrayList<String> messages, FileWriter fileWriter, FileReader fileReader) {
        clientList = clients;
        messageList = messages;
        logFileWriter = fileWriter;
        logFileReader = fileReader;
    }
    
    /**
     * Allow a client to connect to the server.
     * @param client The ClientHandler that whish to log on.
     * @return True if succesful, False if not (in case of an username already taken for example).
     * @throws RemoteException if there is an error joining the client.
     */
    @Override
    public boolean connect(ClientHandler client) throws RemoteException {
        if (!usernameIsTaken(client.getUsername())){
            
            addUser(client);
            
            sendInfoMessage(client.getUsername() + " has joined.");
            
            // TODO: limit client maximum ? Check if client is banned ?
            return true;
        } else {
            System.out.println("Username is already taken.");
            return false;
        }
    }
    
    /**
     * Disconnect the supplied client.
     * @param client The client to disconnect.
     * @throws RemoteException in case the client could not be joined.
     */
    @Override
    public void disconnect(ClientHandler client) throws RemoteException {
        sendInfoMessage(client.getUsername() + " has left.");
        
        removeUser(client);
    }
    
    /**
     * Send a message to all client, but only if it contain something after being trimmed.
     * @param client The client who sent the message
     * @param message The message to send
     * @return false if the message was empty, true if it was sent.
     * @throws RemoteException in case of error joining one client.
     */
    @Override
    public boolean sendMessage(ClientHandler client, String message) throws RemoteException {
        // Prevent empty messages and bogus messages
        message = message.trim();
        if (message.isEmpty())
            return false;
        
        String formattedLogMessage = "[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "] " + client.getUsername() +": " + message ;
        
        writeToLogs(formattedLogMessage);
        System.out.println(formattedLogMessage);
        messageList.add(formattedLogMessage);
        for (ClientHandler currentClient : clientList) {
            currentClient.printMessage(formattedLogMessage);
        }
        return true;
    }
    
    /**
     * Send information messages to all clients. 
     * @param message The message to send.
     * @throws RemoteException 
     */
    public void sendInfoMessage(String message) throws RemoteException {
        String formattedLogMessage = "[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "] " + message ;
        
        writeToLogs(formattedLogMessage);
        System.out.println(formattedLogMessage);
        messageList.add(formattedLogMessage);
        for (ClientHandler currentClient : clientList) {
            currentClient.printMessage(formattedLogMessage);
        }
    }
    
    /**
     * Check if an username is already taken in the list.
     * @param username The username to check.
     * @return True if it is taken, false if it is available.
     */
    private boolean usernameIsTaken(String username) {
        for (ClientHandler currentClient : clientList){
            try {
                if (currentClient.getUsername().equals(username))
                    return true;
            } catch (RemoteException ex) {
                System.out.println("Error joining client (" + ex.getMessage() + "), ejecting him...");
                clientList.remove(currentClient);
            }
        }
        return false;
    }
    
    private void writeToLogs(String message){
        message += "\n";
        try {
            logFileWriter.write(message);
        } catch (IOException ex) {
            Logger.getLogger(ServerHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Return the current server session history.
     * @return the current server session history.
     * @throws RemoteException in case of error joining the server.
     */
    @Override
    public ArrayList<String> getHistory() throws RemoteException {
        return messageList;
    }
    
    @Override
    public ArrayList<String> getConnectedUsers() throws RemoteException {
        ArrayList<String> usernames = new ArrayList<>();
        for (ClientHandler client : clientList){
            usernames.add(client.getUsername());
        }
        
        return usernames;
    }
    
    @Override
    public ArrayList<String> getAllHistory() throws RemoteException {
        // FIXME : plusieurs getAllHistory renvoie un historique vide
        ArrayList<String> fullHistory = new ArrayList<>();
        try {
            logFileReader.reset();
            BufferedReader b = new BufferedReader(logFileReader);
            String line;
            while ((line = b.readLine()) != null){
                System.out.println("Adding " + line);
                fullHistory.add(line);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ServerHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return fullHistory;
    }
    
    private void addUser(ClientHandler client){
        clientList.add(client);
    }
    
    private void removeUser (ClientHandler client){
        clientList.remove(client);
    }
}
