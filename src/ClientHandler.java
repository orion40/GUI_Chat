

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author whoami
 */
public interface ClientHandler extends Remote{
    public void printMessage(String message) throws RemoteException;
    public String getUsername() throws RemoteException;
    public void kickClient() throws RemoteException;
    public void addUserToList(String s) throws RemoteException;
    public void deleteUserFromList(String s) throws RemoteException;
}
