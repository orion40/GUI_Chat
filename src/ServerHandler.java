

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 *
 * @author whoami
 */
public interface ServerHandler extends Remote{
    public boolean connect(ClientHandler client) throws RemoteException;
    public void disconnect(ClientHandler client) throws RemoteException;
    public boolean sendMessage(ClientHandler client, String message) throws RemoteException;
    public ArrayList<String> getHistory() throws RemoteException;
    public ArrayList<String> getAllHistory() throws RemoteException;
    public ArrayList<String> getConnectedUsers() throws RemoteException;
}
