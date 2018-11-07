package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.polymtl.inf8480.tp1.shared.ProjectFile;

public interface ServerInterface extends Remote {
    public ArrayList<Integer> processOperations(ArrayList<FileContent> listOperations) throws RemoteException;
    // public ArrayList<Integer>
    // processOperations(List<AbstractMap.SimpleEntry<String, Integer>>
    // listOperations)
    // throws RemoteException;
}
