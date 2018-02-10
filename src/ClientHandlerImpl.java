

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.rmi.RemoteException;
import javafx.collections.ObservableList;


public class ClientHandlerImpl implements ClientHandler {
    private String clientName;
    private Boolean kicked = false;
    private ObservableList<String> messageList;
    
    public ClientHandlerImpl(String name, ObservableList<String> list){
        clientName = name;
        messageList = list;
    }

    @Override
    public void printMessage(String message) throws RemoteException {
        messageList.add(message);
        System.out.println(message);
    }

    @Override
    public String getUsername() throws RemoteException {
        return clientName;
    }

    @Override
    public void kickClient() throws RemoteException {
        kicked = true;
        System.out.println("You have been kicked by the server.");
    }
    
    public Boolean isKicked(){
        return kicked;
    }
    
}
