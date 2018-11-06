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

import ca.polymtl.inf8480.tp1.shared.ProjectFile;
import ca.polymtl.inf8480.tp1.shared.ServerInterface;
import ca.polymtl.inf8480.tp1.shared.

public class Server implements ServerInterface {

	private Operations operations_;
	private Random uniqueId_; // pour assurer l'unicite
	private int q_ = 0;
	private int m_ = 0;

	public static void main(String[] args) {
		String port = "";
		if (arg.length == 1) {
			port = args[0];
		} else if (arg.length > 1) {
			System.out.println("Too many arguments. 0 or 1 expected");
			System.exit(-1);
		}

		Server server = new Server();
		server.run(port);
	}

	public Server() {
		super();
		this.uniqueId = new Random();
	}

	private void run(String port) {
		// same as provided in TP1 : Only add portNumber
		portNumber = Integer.parseInt(port);
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

	public int getNbBadResults(int nbTasks, int m) {

		return (nbTasks * m) / 100;
	}

	public void showResults(Map<Task, List<Integer>> res) {

		int r = 0;
		for (List<Integer> i : res.values()) {
			for (int j = 0; j < i.size(); j++) {
				r += i.get(j);
				r = r % 4000;
			}
		}
		System.out.println(r);
	}

}
