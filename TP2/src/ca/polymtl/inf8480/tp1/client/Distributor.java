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
import ca.polymtl.inf8480.tp1.shared.FilePair;

public class Distributor {
	// Liste d'operations a effectuer
	ArrayList<FilePair> listOperations;
	// Liste de reponses
    ArrayList<Integer> results = null;
    

    public static void main(String[] args) 
    {
        String runningMode = "";
        String fileName = "";

        if (args.length < 2) 
        {
            System.out.println("You must define filename and running mode in arguments");
            System.exit(-1);
        }

        else 
        {
            fileName = args[0];
            runningMode = args[1];
        }
        Distributor distributor = new Distributor();
        distributor.run(fileName, runningMode);
    }

    FakeServer localServer = null;
    private final String FILE_DIRECTORY = "./files/";
    private ServerInterface localServerStub = null;
    private ServerInterface distantServerStub = null;

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

    public void readFile(String fileName) {

    }

    public void safeRun() {

    }

    public void nonSafeRun() {

    }


    private void run(String fileName, String runningMode) {
		long startTime = System.nanoTime();
			
		// Lire le fichier des operations
		readFile(fileName);
        // Executer le bon mode selon le parametre specifie
        if(runningMode.equals("s")){
			safeRun();
		}
		if(runningMode.equals("n")){
			nonSafeRun();
		}
		// Calculer et afficher temps execution operation
		System.out.println("Temps écoulé: " + (System.nanoTime() - startTime)  + " ns");
    }

}
