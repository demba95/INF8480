package ca.polymtl.inf8480.tp1.client;

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
import java.security.MessageDigestSpi;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;
import ca.polymtl.inf8480.tp1.server.Server;
// import ca.polymtl.inf8480.tp1.shared.Configurations;
import ca.polymtl.inf8480.tp1.shared.FileContent;

public class Distributor {
    ArrayList<FileContent> listOfOperations;

    // private ServerInterface[] listOfServersStubs = new ServerInterface[4]; // 4
    // servers stubs because 4 is the maxNumber
    // of servers used in this work
    private ArrayList<ServerInterface> listOfServersStubs  = new ArrayList<ServerInterface>();// stub Servers;
    private ArrayList<Server> connectedServers = new ArrayList<Server>(); // connected Servers

    public final String SERVER_CONFIG_FILE = "./config";

    // Refs:
    // http://tutorials.jenkov.com/java-concurrency/creating-and-starting-threads.html
    // http://www.java2novice.com/java_thread_examples/implementing_runnable/
    // plus simple methode pour gerer les threads
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
                    if (i < nbOperations) {
                        data.add(this.listCalculs_.get(i));
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

    public static void main(String[] args) {
        String security = "";
        String fileName = "";

        if (args.length < 2) {
            System.out.println("You must define filename and running mode  ( secure or not)in arguments");
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
    }

    private ServerInterface loadServerStub(Server config) {
        ServerInterface stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(config.getIpAddress(), config.getPortNumber());
            // Ref: https://coderanch.com/t/209255/java/RMI-Naming-rebind
            stub = (ServerInterface) registry.lookup("server");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        return stub;
    }

    public void readServersConfigurations(String fileName) {
        // file format: Port Capacity(q) Malicious(m)
        try {
            InputStream in = new FileInputStream(fileName);
            InputStreamReader inRead = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(inRead);
            connectedServers.clear(); // reset server list
            // String titles = br.readLine(); // line to be ignored for titles
            for (String task; (task = br.readLine()) != null;) {
                String[] chaine = task.split("\t"); // divise ligne en 3 params separe par tabulation
                int portNumber = (int) Integer.parseInt(chaine[0]);
                int capacity = (int) Integer.parseInt(chaine[1]);
                int maliciousT = (int) Integer.parseInt(chaine[2]);

                Server curServer = new Server(portNumber, capacity, maliciousT);

                // Configurations config = new Configurations(m, portNumber, capacity);
                connectedServers.add(curServer);
            }
            br.close();
            System.out.println("Nombre de serveurs " + connectedServers.size());
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
        System.out.println("Nb connected = " + nbConnectedServers);

        int nbTacheUnit = listOfOperations.size() / nbConnectedServers; // nb de taches par serveur
        int results = 0;

        while (remaining) {
            remaining = false;
            int i = 0;
            int j = 1;
            runnableThreads.clear();
            simpleThreads.clear();
            // Use a thread for each server
            nbTacheUnit = listOfOperations.size() / nbConnectedServers; // nb de taches par serveur
            for (ServerInterface stubServer : listOfServersStubs) {
                ArrayList<FileContent> opTasks = new ArrayList<FileContent>();
                int offset = 0;
                if (j == nbConnectedServers && nbConnectedServers != 1) {
                    offset = listOfOperations.size() % nbConnectedServers;
                }
                for (; i < (nbTacheUnit * j) + offset; i++) {
                    opTasks.add(listOfOperations.get(i));
                }
                RunnableThread threadClass = new RunnableThread(opTasks, j - 1, stubServer);
                runnableThreads.add(threadClass);
                Thread thread = new Thread(threadClass);
                simpleThreads.add(thread);
                thread.start();
                j++;
            }
            listOfOperations.clear();

            try {
                results = commuteCalculResults(simpleThreads, runnableThreads, remaining);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Final result = " + results);

    }

    public int commuteCalculResults(ArrayList<Thread> threadList, ArrayList<RunnableThread> myRunnable,
            boolean remainingTasks) throws InterruptedException {
        int k = 0;
        int calculResult = 0;
        for (Thread thread : threadList) {
            thread.join();
            // Si un serveur a crash on repartit ses taches avec les autres serveurs
            if (myRunnable.get(k).calculResults == -1) {
                remainingTasks = true;
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

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        // Lire fichier config pour obtenir les infos sur les serveurs
        readServersConfigurations(SERVER_CONFIG_FILE);
        try {
            for (int counter = 0; counter < connectedServers.size(); counter++) {
                ServerInterface rmiServerStub = loadServerStub(connectedServers.get(counter));
                listOfServersStubs.add(rmiServerStub);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        // Lire le fichier des operations
        readOperationsFile(fileName);
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
