

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.rmi.RemoteException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;


public class ClientHandlerImpl implements ClientHandler {
    private final String clientName;
    private final SimpleBooleanProperty kicked;
    private final ObservableList<String> messageList;
    private final ObservableList<String> userList;
    
    public ClientHandlerImpl(String name, ObservableList<String> mList, ObservableList<String> uList, SimpleBooleanProperty isKicked){
        clientName = name;
        messageList = mList;
        userList = uList;
        kicked = isKicked;
    }

    /**
     * Add a message to the client observable message list.
     * @param message The message to add.
     * @throws RemoteException when the client could not be joined.
     */
    @Override
    public void printMessage(String message) throws RemoteException {
        messageList.add(message);
    }

    /**
     * Return the client username.
     * @return the client username.
     * @throws RemoteException when the client could not be joined. 
     */
    @Override
    public String getUsername() throws RemoteException {
        return clientName;
    }

    /**
     * Allow a server to kick the client and to inform him.
     * @throws RemoteException when the client could not be joined.
     */
    @Override
    public void kickClient() throws RemoteException {
        messageList.add("You have been kicked by the server.");
        kicked.set(true);
    }
    
    /**
     * Return the kicked property.
     * @return the kicked property
     * @throws RemoteException when the client could not be joined.
     */
    public SimpleBooleanProperty isKicked() throws RemoteException{
        return kicked;
    }    

    /**
     * Add an username to the current list of users.
     * @param s the username to add.
     * @throws RemoteException when the client could not be joined.
     */
    @Override
    public void addUserToList(String s) throws RemoteException {
        // FIXME: error when adding to list when 2 clients are connected
        System.out.println("Adding " + s);
        userList.add(s);
    }

    /**
     * Remove an username from the current list of users.
     * @param s the username to delete.
     * @throws RemoteException when the client could not be joined.
     */
    @Override
    public void deleteUserFromList(String s) throws RemoteException {
        userList.remove(s);
    }
}
