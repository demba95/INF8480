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

import ca.polymtl.inf8480.tp1.shared.FileContent;
import ca.polymtl.inf8480.tp1.shared.ServerInterface;
import ca.polymtl.inf8480.tp1.shared.Operations;

public class Server implements ServerInterface {

	private Operations operations_;
	private Random random = new Random(); // a random number generator object
	private int q_ = 0; // work capacity
	private int m_ = 0; // tendance a retourner de faux resultats

	public Operations getOperations_() {
		return this.operations_;
	}

	public void setOperations_(Operations operations_) {
		this.operations_ = operations_;
	}

	public int getWorkCapacity() {
		return this.q_;
	}

	public void setWorkCapacity(int q) throws RemoteException {
		this.q_ = q;
	}

	// public int getMaliciousNess() {
	// return this.m_;
	// }

	public void setMaliciousNess(int m) throws RemoteException {
		this.m_ = m;
	}

	public static void main(String[] args) {
		String port = "";
		if (args.length == 1) {
			port = args[0];
		} else if (args.length > 1) {
			System.out.println("Too many arguments. 0 or 1 expected");
			System.exit(-1);
		}

		Server server = new Server();
		server.run(port);
	}

	public Server() {
		super();
	}

	private void run(String port) {
		// same as provided in TP1 : Only add portNumber
		int portNumber = (int) Integer.parseInt(port);
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, portNumber);

			Registry registry = LocateRegistry.getRegistry(portNumber);
			registry.rebind("server", stub);
			System.out.println("Server ready.");
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
		double chanceToDefect = this.getRefusalRate(arraySize, this.q_);

		// if listOperations is null or refusalRate too High return null: refuse
		// operations
		if (listOperations != null || chanceToDefect > 20.0) {
			return null;
		}
		ArrayList<Integer> opResults = new ArrayList<Integer>();
		for (FileContent var : listOperations) {
			if (this.m_ > 0 && random.nextInt() <= this.m_) {
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
