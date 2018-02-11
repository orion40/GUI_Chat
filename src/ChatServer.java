

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author whoami
 */
public class ChatServer {
    static private ArrayList<ClientHandler> clientList;
    static private ArrayList<String> messageList;
    
    public static void main(String [] args) {
        boolean isRunning = true;
        
        clientList = new ArrayList<>();
        messageList = new ArrayList<>();
        
        try {
            // Create log file
            File serverLogs = new File("chat.log");
            FileWriter serverLogsWriter = new FileWriter(serverLogs, true);
            FileReader serverLogsReader = new FileReader(serverLogs);
            String openMessage = "[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "] " + "Server started.\n";
            serverLogsWriter.write(openMessage);
            
            // Create interrupt handler for closing the server with ^C
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Interrupt detected, kicking users...");
                for (ClientHandler client : clientList){
                    try {
                        client.kickClient();
                    } catch (RemoteException ex) {
                        System.out.println("Unable to send kick signal to remote host.");
                    }
                    clientList.remove(client);
                }
                
                try {
                    serverLogsWriter.write("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "] " + "Server shutdown.\n");
                    serverLogsWriter.close();
                    serverLogsReader.close();
                } catch (IOException ex) {
                    System.out.println("Error closing log file.");
                }
            }));
            
            // Create a Hello remote object
            ServerHandlerImpl serverHandler = new ServerHandlerImpl(clientList, messageList, serverLogsWriter, serverLogsReader);
            ServerHandler serverHandlerStub = (ServerHandler) UnicastRemoteObject.exportObject(serverHandler, 0);
            
            // Register the remote object in RMI registry with a given identifier
            Registry registry= LocateRegistry.getRegistry();
            registry.bind("ServerHandler", serverHandlerStub);
            System.out.println ("Server ready");
            
            /*
            while(isRunning){
            
            }
            */
            
        } catch (FileNotFoundException ex) {
            System.out.println("Unabled to open log file!");
            System.exit(1);
        } catch (Exception e) {
            // TODO: log errors into log file instead of outputing to stdout
            System.err.println("Error on server :" + e) ;
            e.printStackTrace();
        }
    }
}
