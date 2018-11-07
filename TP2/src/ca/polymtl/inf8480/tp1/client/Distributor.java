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
import ca.polymtl.inf8480.tp1.shared.Configurations;
import ca.polymtl.inf8480.tp1.shared.FileContent;

//Refs: http://tutorials.jenkov.com/java-concurrency/creating-and-starting-threads.html
//http://www.java2novice.com/java_thread_examples/implementing_runnable/
//plus simple methode pour gerer les threads
public class RunnableThread implements Runnable {
    public ArrayList<FileContent> listCalculs_;
    public int calculResults = 0;
    private int nOpsEach_ = 5;
    public int count_ = 0;
    private ServerInterface stubServer_;

    public RunnableThread(ArrayList<FileContent> liste, int ops, ServerInterface stubs) {
        this.listCalculs_ = liste;
        this.stubServer_ = stubs;
        this.count_ = ops;
    }

    public void run() {

        int nbOperations = this.listCalculs_.size(); // get number of calculs to send

        while (nbOperations != 0) {
            ArrayList<FileContent> data = new ArrayList<FileContent>(); // data sent to server
            for (int i = 0; i < this.nOpsEach_; i++) {
                if (i < nbOperations()) {
                    data.add(this.Taches.get(i));
                }
            }
            // get results
            ArrayList<Integer> theResults = null;
            try {
                theResults = this.stubServer_.processOperations(data); // calculer les operations
            } catch (Exception e) {
                this.calculResults = -1;
                System.out.println("Error: " + e.getMessage());
                return;
            }

            checkResults(data, theResults);
        }
    }

    private void checkResults(ArrayList<FileContent> data, ArrayList<Integer> myResults) {
        if (myResults == null) {
            // cannot reduce
            this.nOpsEach_--;
        } else if (myResults != null) {
            // On est capable de traiter encore plus de taches par block (peut-etre)
            this.nOpsEach_++;

            for (Integer each : myResults) {
                this.calculResults = (this.calculResults + each) % 4000;
            }
            this.listCalculs_.removeAll(data); // remove selected data from what to send
        }
    }
}

public class Distributor {
    ArrayList<FileContent> listOfOperations;

    // private ServerInterface[] listOfServersStubs = new ServerInterface[4]; // 4
    // servers stubs because 4 is the maxNumber
    // of servers used in this work
    private ArrayList<Configurations> listOfServersStubs;// stub Servers;
    ArrayList<Configurations> connectedServers;// connected Servers

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

    public void readServersConfigurations(String fileName) {
        try {
            connectedServers.clear();
            BufferedReader br = new BufferedReader(fileName);
            for (String task; (task = br.readLine()) != null;) {
                String[] chaine = task.split("\t"); // divise ligne en 4 params
                String serverAddress = chaine[0];
                int portNumber = (int) Integer.parseInt(chaine[1]);
                String mode = chaine[2];
                int capacity = (int) Integer.parseInt(chaine[3]);
                connectedServers.add(new Configurations(serverAddress, mode, portNumber, capacity));
            }
            br.close();
        } catch (Exception e) {
            System.out.println(e.toString());
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
            br.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error opening file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void safeRun() {
        Boolean remaining = true;
        ArrayList<RunnableThread> runnableThreads = new ArrayList<RunnableThread>();
        ArrayList<Thread> simpleThreads = new ArrayList<Thread>();
        int nbConnectedServers = listOfServersStubs.size();

        int nbTacheUnit = listOfOperations.size() / nbConnectedServers; // nb de taches par serveur
        int results = 0;

        while (remaining) {
            remaining = false;
            int i = 0;
            int j = 1;
            runnableThreads.clear();
            simpleThreads.clear();
            // Use a thread for each server
            for (ServerInterface stubServer : listOfServersStubs) {
                ArrayList<Pair> opTasks = new ArrayList<Pair>();
                int offset = 0;
                if (j == nbConnectedServers && nbConnectedServers != 1) {
                    offset = listOfOperations.size() % nbConnectedServers;
                }
                for (; i < (nbTacheUnit * j) + offset; i++) {
                    opTasks.add(listOfOperations.get(i));
                }
                threadServeur threadClass = new threadServeur(opTasks, stub, j - 1);
                runnableThreads.add(threadClass);
                Thread thread = new Thread(threadClass);
                simpleThreads.add(thread);
                thread.start();
                j++;
            }
            listOfOperations.clear();

            try {
                results = commuteCalculResults(simpleThreads, runnableThreads);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Final result = " + results);

        // int k = 0;
        // for (Thread thread : simpleThreads) {
        // thread.join();
        // // Si un serveur a crash on repartit ses taches avec les autres serveurs
        // if (runnableThreads.get(k).getResult() == -1) {
        // remaining = true;
        // listOfOperations.addAll(runnableThreads.get(k).listCalculs_);
        // listOfServersStubs.remove(runnableThreads.get(k).count_);
        // }
        // // Si le serveur a reussi
        // else {
        // results = (results + runnableThreads.get(k).calculResults) % 4000;
        // }
        // k++;
        // }

    }

    public int commuteCalculResults(ArrayList<Thread> threadList, ArrayList<RunnableThread> myRunnable) {
        int k = 0;
        int calculResult = 0;
        for (Thread thread : threadList) {
            thread.join();
            // Si un serveur a crash on repartit ses taches avec les autres serveurs
            if (myRunnable.get(k).getResult() == -1) {
                remaining = true;
                this.listOfOperations.addAll(myRunnable.get(k).listCalculs_);
                this.listOfServersStubs.remove(myRunnable.get(k).count_);
            }
            // Si le serveur a reussi
            else {
                calculResult = (calculResult + myRunnable.get(k).calculResults) % 4000;
            }
            k++;
        }
        return calculResult;
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