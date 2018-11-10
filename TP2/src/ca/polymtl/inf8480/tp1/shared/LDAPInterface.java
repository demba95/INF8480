package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface LDAPInterface extends Remote {
    public void registerClient(String user, String password) throws RemoteException;

    public boolean verifyClient(String user, String password) throws RemoteException;

}