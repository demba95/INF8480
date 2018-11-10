package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.polymtl.inf8480.tp1.shared.FileContent;

public interface ServerInterface extends Remote {
    public ArrayList<Integer> processOperations(ArrayList<FileContent> listOperations) throws RemoteException;
}
