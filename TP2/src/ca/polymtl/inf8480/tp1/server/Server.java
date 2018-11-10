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
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.*;
import java.security.MessageDigestSpi;
import java.security.MessageDigest;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// import ca.polymtl.inf8480.tp1.shared.Configurations;
import ca.polymtl.inf8480.tp1.shared.FileContent;
import ca.polymtl.inf8480.tp1.shared.ServerInterface;
import ca.polymtl.inf8480.tp1.shared.Operations;

public class Server implements ServerInterface {
	public final String LOCALHOST = "127.0.0.1";
	public final String SERVER_FILE = "./config";

	private Operations operations_;
	private Random random = new Random(); // random number generator
	private int capacity_; // work capacity

	public int getCapacity() {
		return this.capacity_;
	}

	private int maliciousness_; // tendance a retourner de faux resultats entre 0 et 100

	public int getMaliciousness() {
		return this.maliciousness_;
	}

	private String ipAddress_;

	public String getIpAddress() {
		return this.ipAddress_;
	}

	private int portNumber_;

	public int getPortNumber() {
		return this.portNumber_;
	}

	public static void main(String[] args) {
		int port, capacity, maliciousness;
		if (args.length == 3) {
			// String id = args[0];
			port = Integer.parseInt(args[0]);
			capacity = Integer.parseInt(args[1]);
			maliciousness = Integer.parseInt(args[2]);

		} else {
			System.out.println("You should enter: port, workCapacity, maliciousness");
			return;
		}
		Server curServer = new Server(port, capacity, maliciousness);
		curServer.run(curServer);
	}

	public Server(int port, int capacity, int maliciousness) {
		super();
		// this.myConfig.setPortNumber(port);
		// this.myConfig.setCapacity(capacity);
		// this.myConfig.setMaliciousness(maliciousness);
		// this.myConfig.setServerIp(LOCALHOST);
		this.portNumber_ = port;
		this.capacity_ = capacity;
		this.maliciousness_ = maliciousness;
		this.ipAddress_ = LOCALHOST;

	}

	private void run(Server server) {
		// same as provided in TP1
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		writeServerToFile(server);

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, 5030);
			Registry registry = LocateRegistry.getRegistry(server.getPortNumber());
			registry.rebind("server", stub);
			System.out.println("Server ready on port " + server.getPortNumber());
		} catch (ConnectException e) {
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}

		this.operations_ = new Operations();

	}

	public void writeServerToFile(Server server) {
		try {
			String currentConfig = server.getPortNumber() + "\t" + server.getCapacity() + "\t"
					+ server.getMaliciousness();
			BufferedReader br = Files.newBufferedReader(Paths.get(SERVER_FILE));
			if (!br.lines().collect(Collectors.toList()).contains(currentConfig)) {
				PrintWriter writer = new PrintWriter(new FileWriter(SERVER_FILE, true));
				writer.println(currentConfig);
				writer.close();
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}

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
		if (listOperations == null || chanceToDefect > 25.0) {
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