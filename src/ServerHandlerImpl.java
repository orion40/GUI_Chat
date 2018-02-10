

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
            String formatted_message = "[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "] " + client.getUsername() + " has joined.";
            
            messageHandler(client, formatted_message);
            
            clientList.add(client);
            // TODO: limit client maximum ? Check if client is banned ?
            return true;
        } else {
            System.out.println("Username is already taken.");
            return false;
        }
    }
    
    @Override
    public boolean disconnect(ClientHandler client) throws RemoteException {
        String formatted_message = "[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "] " + client.getUsername() + " has left.";
       
        messageHandler(client, formatted_message);
        
        clientList.remove(client);
        return true;
    }
    
    @Override
    public boolean sendMessage(ClientHandler client, String message) throws RemoteException {
        String formatted_message = "[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "] " + client.getUsername() + ": " + message;
        
        messageList.add(formatted_message);
        writeToLogs(formatted_message);
        System.out.println(formatted_message);
        
        for (ClientHandler currentClient : clientList) {
                currentClient.printMessage(formatted_message);
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
        ArrayList<String> fullHistory = new ArrayList<>();
        fullHistory.ensureCapacity(50000);
        try {
            BufferedReader b = new BufferedReader(logFileReader);
            String line;
            while ((line = b.readLine()) != null){
                fullHistory.add(line);
                System.out.println("fullHistory size: " + fullHistory.size());
            }
                
        } catch (IOException ex) {
            Logger.getLogger(ServerHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return fullHistory;
    }
    
    private void messageHandler(ClientHandler client, String formatted_message) throws RemoteException{
        sendMessage(client, formatted_message);
        
        System.out.println(formatted_message);
        messageList.add(formatted_message);
        writeToLogs(formatted_message);
    }
}
