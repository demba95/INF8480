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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ca.polymtl.inf8480.tp1.shared.Configurations;
import ca.polymtl.inf8480.tp1.shared.FileContent;
import ca.polymtl.inf8480.tp1.shared.ServerInterface;
import ca.polymtl.inf8480.tp1.shared.Operations;

public class Server implements ServerInterface {
	public final String LOCALHOST = "127.0.0.1";

	private Operations operations_;
	private Random random = new Random(); // random number generator
	private int capacity_; // work capacity
	private int maliciousness_; // tendance a retourner de faux resultats entre 0 et 100
	private String serverId_;
	private int portNumber_;

	Configurations myConfig;

	public void setWorkCapacity(int q) throws RemoteException {
		this.capacity_ = q;
	}

	public void setMaliciousNess(int m) throws RemoteException {
		this.maliciousness_ = m;
	}

	public static void main(String[] args) {
		int port, capacity, maliciousness;
		if(args.length == 3) 
		{
			// String id = args[0];
			port = Integer.parseInt(args[0]);
			capacity = Integer.parseInt(args[1]);
			maliciousness = Integer.parseInt(args[2]);

		}
		else
		{
			System.out.println("You should enter: port, workCapacity, maliciousness");
			return;
		}
		Server server = new Server(port, maliciousness, capacity);
		server.run();
	}

	public Server(int port, int capacity, int maliciousness)  {
		super();
		this.myConfig.setPortNumber(port);
		this.myConfig.setQ(capacity);
		this.myConfig.setMaliciousness(maliciousness);
		this.myConfig.setServerIp(LOCALHOST);

	}

	private void run() {
		// same as provided in TP1
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, 5020);
			Registry registry = LocateRegistry.getRegistry(this.portNumber_);
			registry.rebind("server", stub);
			System.out.println("Server ready on port" + this.portNumber_ + "...");
		} catch (ConnectException e) {
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}

		this.operations_ = new Operations();

	}

	public double getRefusalRate(int nbOperations, int serverCapacity) {
		double refusalRatio = (nbOperations - serverCapacity) / (4 * serverCapacity);
		return refusalRatio * 100;
	}

	@Override
	public ArrayList<Integer> processOperations(ArrayList<FileContent> listOperations) throws RemoteException {
		int arraySize = listOperations.size();
		double chanceToDefect = this.getRefusalRate(arraySize, this.capacity_);

		// if listOperations is null or refusalRate too High (>25)return null: refuse
		// operations
		if (/*listOperations == null || */chanceToDefect > 25.0) {
			return null;
		}
		ArrayList<Integer> opResults = new ArrayList<Integer>();
		for (FileContent var : listOperations) {
			if (this.maliciousness_ > 0 && random.nextInt() <= this.maliciousness_) {
				int wrongResult = random.nextInt() % 4000;
				opResults.add(wrongResult);
			}

			else {
				switch (var.getOperation()) {
				case "pell":
					opResults.add(this.operations_.pell(var.getOperande()) % 4000);
					break;

				case "prime":
					opResults.add(this.operations_.prime(var.getOperande()) % 4000);
					break;
				}
			}

		}

		return opResults;
	}

}