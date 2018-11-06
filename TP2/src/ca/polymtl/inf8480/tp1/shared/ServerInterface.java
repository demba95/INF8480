package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;
// import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.polymtl.inf8480.tp1.shared.ProjectFile;

public interface ServerInterface extends Remote {
    ArrayList<Integer> processOperations(ArrayList<Pair> operations) throws RemoteException;
}
