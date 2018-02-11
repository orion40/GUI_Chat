

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.rmi.RemoteException;
import java.util.ArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;


public class ClientHandlerImpl implements ClientHandler {
    private String clientName;
    private SimpleBooleanProperty kicked;
    private ObservableList<String> messageList;
    private ObservableList<String> userList;
    
    public ClientHandlerImpl(String name, ObservableList<String> mList, ObservableList<String> uList, SimpleBooleanProperty isKicked){
        clientName = name;
        messageList = mList;
        userList = uList;
        kicked = isKicked;
    }

    @Override
    public void printMessage(String message) throws RemoteException {
        messageList.add(message);
    }

    @Override
    public String getUsername() throws RemoteException {
        return clientName;
    }

    @Override
    public void kickClient() throws RemoteException {
        messageList.add("You have been kicked by the server.");
        kicked.set(true);
    }
    
    public SimpleBooleanProperty isKicked(){
        return kicked;
    }    

    @Override
    public void addUserToList(String s) throws RemoteException {
        // FIXME: error when adding to list when 2 clients are connected
        System.out.println("Adding " + s);
        userList.add(s);
    }

    @Override
    public void deleteUserFromList(String s) throws RemoteException {
        userList.remove(s);
    }
}
