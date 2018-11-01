package ca.polymtl.inf8480.tp1.server;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.stream.Collectors;
import java.nio.file.*;
import java.io.RandomAccessFile;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.*;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.*;
import java.security.MessageDigestSpi;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.UUID;

import ca.polymtl.inf8480.tp1.shared.ProjectFile;
import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Server implements ServerInterface {
	final String LOCKED = " Verrouillee par l'utilisateur ";
	final String UNLOCKED = " Non verouillee \n ";
	// attributs de la classe
	ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final String FILE_DIRECTORY = "ServerDir/";
	Map<String, ProjectFile> fileList = new HashMap<String, ProjectFile>();

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	@Override
	public String create(String filename) throws RemoteException {
		String ERROR = filename + " already exists. Choose different name";
		String SUCCESS = filename + " successfully created";
		String result = "";

		if (this.fileList.containsKey(filename)) {
			result = ERROR;
		}

		else {
			ProjectFile theFile = new ProjectFile(filename);
			rwLock.writeLock().lock(); // verouiller
			try {

				ByteBuffer buffer = ByteBuffer.allocate(1024);
				FileChannel fc = FileChannel.open(Paths.get(this.FILE_DIRECTORY + "/" + theFile.getFileName()),
						StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				fc.write(buffer);
				fc.close();
				this.fileList.put(filename, theFile);

			} catch (IOException e) {
			} finally {
				rwLock.writeLock().unlock(); // deverouiller
			}
			result = SUCCESS;
		}
		return result;
	}

	@Override
	public String listFiles() throws RemoteException {

		String theList = "";
		Collection<?> setKeys = this.fileList.keySet();
		for (Object key : setKeys) {
			this.rwLock.readLock().lock();
			try {
				theList += "* " + this.fileList.get(key).getFileName();
				boolean isFileLocked = this.fileList.get(key).isLocked();
				if (!isFileLocked == true) {
					theList += UNLOCKED;
				} else {
					theList += LOCKED + this.fileList.get(key).getUser() + "\n";
				}
			} finally {
				this.rwLock.readLock().unlock();
			}
		}

		theList += this.fileList.size() + " fichier(s)\n";
		return theList;
	}

	@Override
	public ProjectFile getFile(String filename, String checkSum) throws RemoteException {
		ProjectFile theFile = null;
		if (this.fileList.containsKey(filename)) {
			this.rwLock.readLock().lock();
			// variable temp necessaire pour eviter un Null Pointer Exception
			ProjectFile temp = this.fileList.get(filename);
			if ((checkSum.equals("-1"))
					|| (temp.getFileName().equals(filename) && !(temp.getChecksum().equals(checkSum)))) {
				try {
					byte[] content = this.getFileSize(temp.getFileName());
					String contentToStr = new String(content);
					temp.setFileContent(contentToStr);
					theFile = temp;
				} catch (Exception e) {
				}
			}
			this.rwLock.readLock().unlock();

		}
		return theFile;
	}

	// Ref: https://stackoverflow.com/questions/858980/file-to-byte-in-java
	public byte[] getFileSize(String filename) {

		byte[] fileSize = null;
		try {
			fileSize = Files.readAllBytes(Paths.get(this.FILE_DIRECTORY + filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileSize;
	}

	@Override
	public List<ProjectFile> syncLocalDirectory() {
		List<ProjectFile> newList = new ArrayList<ProjectFile>();
		for (ProjectFile file : this.fileList.values()) {
			byte[] content = this.getFileSize(file.getFileName());
			String contentToStr = new String(content);
			file.setFileContent(contentToStr);
			newList.add(file);
		}

		return newList;
	}

	@Override
	public String lock(String filename, String checksum) throws IOException { // checksum will use client ID
		ProjectFile file = new ProjectFile();
		String lockerId = "";

		//if file exists
		if(this.fileList.containsKey(filename)){
			this.rwLock.readLock().lock();
			try {
				file = this.fileList.get(filename);
			} finally {
				this.rwLock.readLock().unlock();
			}

			//if file exists and is locked
			if(file.isLocked()){
				this.rwLock.readLock().lock();
				try {			
					lockerId += "";;
				}
				finally {
					this.rwLock.readLock().unlock();
				}	
			}
						
			//if file exists and is unlocked
			else{	
				this.rwLock.writeLock().lock();
				try {			
					file.lockfile(checksum);
					lockerId += file.getUser();;
				}
				finally {
					this.rwLock.writeLock().unlock();
				}					
			}
			
		}
		else
		{
			lockerId = " File does not exist ";
		}
		System.out.println("lockerId " + lockerId );
		return lockerId;
	}

	public String push(String filename, String content) throws RemoteException {
		String ERROR = "Operation refusee : vous devez  verrouiller d'abord  le fichier";
		String SUCCESS = "Fichier envoye au serveur";
		String result = " ";
		try {
			ProjectFile file = this.fileList.get(filename);
			if (file.isLocked()) {
				Files.write(Paths.get(this.FILE_DIRECTORY + filename), content.getBytes(), StandardOpenOption.WRITE);
				file.unlockFile();
				result += SUCCESS;
			}
			else {
				result += ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public String connect(String login, String password) throws RemoteException {
		String SUCCESS = "Enregistrement reuissi";
		String ALREADY_EXISTS = "Cet usager existe deja";

		if (this.verify (login, password))
			return ALREADY_EXISTS;
		String infos=login+"->"+password+"";
		try{
		     Files.write(Paths.get("ServerDir/logins.txt"), infos.getBytes(), StandardOpenOption.WRITE);
		}catch(Exception e){ }

		return SUCCESS;
	}

	public boolean verify(String login, String password) throws RemoteException {
		List<String> userData;
		boolean isLegit = false;	
		String filename = "ServerDir/logins.txt";		

		try (BufferedReader buffer = Files.newBufferedReader(Paths.get(filename))) {
			userData = 	buffer.lines().collect(Collectors.toList());
			Iterator<String> it = userData.iterator();
		while (it.hasNext()) {
			String[] data = it.next().split("->"); // ignorer le separateur
			if (data[0].equals(login) && data[1].equals(password)) {
				isLegit = true;
			}
		}	
		}catch(Exception e){}
		return isLegit;

		
	}

}
