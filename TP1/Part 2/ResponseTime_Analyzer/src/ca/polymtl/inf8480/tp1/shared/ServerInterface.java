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
	String connect(String login, String password) throws RemoteException;
	boolean verify(String login, String password) throws RemoteException; 

	String create(String filename) throws RemoteException;

	String listFiles() throws RemoteException;

	ProjectFile getFile(String filename, String checksum) throws RemoteException;

	List<ProjectFile> syncLocalDirectory() throws RemoteException;

	String lock(String filename, String checksum) throws IOException;

	String push(String filename, String content) throws RemoteException;
}
