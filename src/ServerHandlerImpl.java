

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerHandlerImpl implements ServerHandler {
    
    static private ArrayList<ClientHandler> clientList;
    static private ArrayList<String> messageList;
    static private FileWriter logFileWriter;
    static private FileReader logFileReader;
    
    public ServerHandlerImpl(ArrayList<ClientHandler> clients, ArrayList<String> messages, File file) {
        clientList = clients;
        messageList = messages;
        try {
            logFileWriter = new FileWriter(file, true);
        } catch (IOException ex) {
            Logger.getLogger(ServerHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            logFileReader = new FileReader(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public boolean connect(ClientHandler client) throws RemoteException {
        if (!usernameIsTaken(client.getUsername())){
            
            addUser(client);
            
            sendInfoMessage(client, client.getUsername() + " has joined.");
            
            // TODO: limit client maximum ? Check if client is banned ?
            return true;
        } else {
            System.out.println("Username is already taken.");
            return false;
        }
    }
    
    @Override
    public boolean disconnect(ClientHandler client) throws RemoteException {
        sendInfoMessage(client, client.getUsername() + " has left.");
        
        removeUser(client);
        
        return true;
    }
    
    @Override
    public boolean sendMessage(ClientHandler client, String message) throws RemoteException {
        String formattedLogMessage = "[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "] " + client.getUsername() +": " + message ;
        
        writeToLogs(formattedLogMessage);
        System.out.println(formattedLogMessage);
        messageList.add(formattedLogMessage);
        for (ClientHandler currentClient : clientList) {
            currentClient.printMessage(formattedLogMessage);
        }
        return true;
    }
    
    public boolean sendInfoMessage(ClientHandler client, String message) throws RemoteException {
        String formattedLogMessage = "[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "] " + message ;
        
        writeToLogs(formattedLogMessage);
        System.out.println(formattedLogMessage);
        messageList.add(formattedLogMessage);
        for (ClientHandler currentClient : clientList) {
            currentClient.printMessage(formattedLogMessage);
        }
        return true;
    }
    
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
        // FIXME: les logs ne sont pas écrit correctement
        message += "\n";
        try {
            logFileWriter.write(message);
        } catch (IOException ex) {
            Logger.getLogger(ServerHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
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
            BufferedReader b = new BufferedReader(logFileReader);
            String line;
            while ((line = b.readLine()) != null){
                fullHistory.add(line);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ServerHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return fullHistory;
    }
    
    private void addUser(ClientHandler client){        
        for (ClientHandler c : clientList){
            try {
                c.addUserToList(client.getUsername());
            } catch (RemoteException ex) {
                Logger.getLogger(ServerHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Unable to join client, deleting him...");
                clientList.remove(c);
            }
        }
        
        clientList.add(client);
    }
    
    private void removeUser (ClientHandler client){
        for (ClientHandler c : clientList){
            try {
                c.deleteUserFromList(client.getUsername());
            } catch (RemoteException ex) {
                Logger.getLogger(ServerHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Unable to join client, deleting him...");
                clientList.remove(c);
            }
        }
        
        clientList.remove(client);
    }
}
