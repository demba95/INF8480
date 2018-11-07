package ca.polymtl.inf8480.tp1.client;

import java.io.File;
import java.io.IOException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.UUID;
import java.nio.file.Paths;
import java.nio.file.*;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.security.MessageDigestSpi;
import java.security.MessageDigest;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;
import ca.polymtl.inf8480.tp1.shared.FileContent;

public class Distributor {
    ArrayList<FileContent> listOfOperations;
    ArrayList<Integer> operationResults;

    public static void main(String[] args) {
        String security = "";
        String fileName = "";

        if (args.length < 2) {
            System.out.println("You must define filename and running mode in arguments");
            System.exit(-1);
        }

        else {
            fileName = args[0];
            security = args[1];
        }
        Distributor distributor = new Distributor();
        distributor.run(fileName, security);
    }

    public Distributor() {
        super();

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        localServer = new FakeServer();
        localServerStub = loadServerStub("127.0.0.1");

        if (distantServerHostname != null) {
            distantServerStub = loadServerStub(distantServerHostname);
        }
    }

    public void readOperationsFile(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            for (String task; (task = br.readLine()) != null;) {
                String[] chaine = task.split(" ");
                FileContent theTask = new FileContent(chaine[0], (int) Integer.parseInt(chaine[1]));
                listOfOperations.add(theTask);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error opening file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void safeRun() {

    }

    public void nonSafeRun() {

    }

    private void run(String fileName, String runningMode) {
        long startTime = System.currentTimeMillis();

        // Lire le fichier des operations
        readFile(fileName);
        // Executer le bon mode selon le parametre specifie
        if (runningMode.equals("s")) {
            safeRun();
        }
        if (runningMode.equals("n")) {
            nonSafeRun();
        }
        // Calculer et afficher temps execution operation
        System.out.println("Elapsed Time: " + (System.currentTimeMillis() - startTime) + " ms");
    }

}
