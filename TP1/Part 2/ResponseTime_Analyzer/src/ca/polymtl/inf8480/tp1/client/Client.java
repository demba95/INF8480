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

import ca.polymtl.inf8480.tp1.shared.ProjectFile;
import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Client {
    public static void main(String[] args) {
        String distantHostname = null;
        String userChoice = null;
        String fileName = null;

        // pour le service d'authentification
        String login = null;
    	String password = null;

        if (args.length > 0) {
            userChoice = args[0];
        }

        if (args.length > 1) {
            fileName = args[1];
        }

        if (args.length > 2) {
            login = args[1];
            password = args[2];
        }

        

        Client client = new Client(distantHostname);
        client.run(userChoice, fileName, login, password);
    }

    FakeServer localServer = null;
    private final String FILE_DIRECTORY = "./files/";
    private ServerInterface localServerStub = null;
    private ServerInterface distantServerStub = null;

    public Client(String distantServerHostname) {
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

    // modifier fonction run pour prendre en parametre l'action a executer et
    // eventuellement le nom du fichier
    private void run(String choice, String filename, String login, String password) {

        switch (choice) {
            // service authentification
            case "new":
                try {
                    String result = this.localServerStub.connect(login,password);
                    System.out.println(result);				
                } catch (RemoteException e) {
                    System.out.println("Cannot save client " + e.getMessage());
                }	
                break;

            case "verify":
                try {                    							 				
                    if(this.localServerStub.verify(login,password)) 
                        System.out.println("The user is legit");
                    else
                        System.out.println("Cannot authentificate user");				
                } catch (RemoteException e) {
                    System.out.println("Authentification error : " + e.getMessage());
                }	
                break;

            // interface client
            case "create":
                this.createFile(filename);
                break;

            case "list":
                this.listFiles();
                break;

            case "syncLocalDirectory":
                try {
                    this.syncLocalDirectory(this.localServerStub.syncLocalDirectory());
                } catch (Exception e) {
                }
                break;

            case "get":
                try {
                    this.getFile(filename);
                } catch (Exception e) {
                }
                break;

            case "lock":
                String userId = String.valueOf(this.createUserId());
                try {
                    String lockerId = this.localServerStub.lock(filename, userId);
                    this.lock(filename, lockerId);
                } catch (Exception e) { }
                break;

            case "push":
                String theUser = String.valueOf(this.createUserId());
                try {
                    String content = new String(this.getCurFile(filename));
                    System.out.println(this.localServerStub.push(filename, content));
                } catch (Exception e) { }
                break;

            default:
                System.out.println("La commande entrée n'est pas reconnue !");
                break;
        }

    }

    public void listFiles() {
        try {
            String result = localServerStub.listFiles();
            System.out.println(result);
        } catch (RemoteException e) {
            System.out.println("Cannot list files " + e.getMessage());
        }

    }

    public void createFile(String fileName) {
        try {
            String result = localServerStub.create(fileName);
            System.out.println(result);
        } catch (RemoteException e) {
            System.out.println("Cannot create the file " + e.getMessage());
        }

    }

    // ref: https://howtodoinjava.com/java8/java-8-write-to-file-example/
    // https://www.mkyong.com/java/how-to-write-to-file-in-java-bufferedwriter-example/
    public void download(ProjectFile file) throws IOException {

        // Files.write(Paths.get(this.FILE_DIRECTORY + file.getFileName()) ,
        // file.getFileContent().getBytes());
        if (file == null) {
            System.out.println("Le fichier n'existe pas");
        } else {
            BufferedWriter write = null;
            String curName = "";
            if (file.getFileName() != null) {
                curName = file.getFileName();
                File newFile = new File("./files/" + curName);

                try {
                    newFile.createNewFile();
                    write = new BufferedWriter(new FileWriter(newFile));
                } catch (IOException e) {
                    System.err.println("Cannot create the file");
                }

                try {
                    if (file.getFileContent() != null) {
                        write.write(file.getFileContent());
                        write.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    public void getFile(String filename) throws RemoteException, IOException {
        try {
            File local = new File("file/" + filename);
            ProjectFile distant = null;
            if (local.exists()) {
                // recupere le checksum en comparant les hash MD5
                String fileChecksum = getMd5OfFile("file/" + filename);
                // cast pour gerer le fichier et recuperer la bonne version
                distant = (ProjectFile) localServerStub.getFile(filename, fileChecksum);
            } else {
                // just look for the initial file
                distant = (ProjectFile) localServerStub.getFile(filename, "-1");
            }

            this.download(distant);
            if (distant != null) {
                System.out.println(filename + " synchronise");
            }

        } catch (RemoteException e) {
            System.out.println("Impossible to retrieve file " + e.getMessage());
        }

    }

    public void syncLocalDirectory(List<ProjectFile> files) throws RemoteException {
        try {
            // download each file in the server list of files. Duplication is checked server
            // side
            Iterator<ProjectFile> it = files.iterator();
            while (it.hasNext()) {
                this.download(it.next());
            }
            System.out.println("Dossier local synchonise");
        } catch (Exception e) {
            System.out.println("Impossible to sync directory" + e.getMessage());
        }

    }

    private ServerInterface loadServerStub(String hostname) {
        ServerInterface stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
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

    public String getUserId() throws IOException {
        String userId = null;
        InputStream istream = null;
        try {
            istream = new FileInputStream(this.FILE_DIRECTORY + "./users.txt");
            BufferedReader buffer = new BufferedReader(new InputStreamReader(istream));
            userId = buffer.readLine();
        } finally {
            istream.close();
        }
        return userId;
    }

    // Ref:
    // https://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
    public static String getMd5OfFile(String filePath) {
        String returnVal = "";
        try {
            InputStream input = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest md5Hash = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = input.read(buffer);
                if (numRead > 0) {
                    md5Hash.update(buffer, 0, numRead);
                }
            }
            input.close();

            byte[] md5Bytes = md5Hash.digest();
            for (int i = 0; i < md5Bytes.length; i++) {
                returnVal += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return returnVal.toUpperCase();
    }

    // Verifier s'il existe un id dans le repertoire courant,
    // si oui one ne fait rien ,sinon on lui alloue un nouvel id

    public int createUserId() {
        File idsFile = new File("./users.txt");
        int userId = 0;
        if (idsFile.exists()) {
            try {
                FileReader freader = new FileReader(idsFile);
                BufferedReader buffer = new BufferedReader(freader);
                String id = buffer.readLine();
                userId = Integer.parseInt(id);
            } catch (IOException e) {
                System.err.println("Cannot open userId file");
            }

        } else {
            Random numberRand = new Random();
            userId = numberRand.nextInt(1000000);
            String idToStr= Integer.toString(userId);
            try {
                FileWriter fwriter = new FileWriter(idsFile, true);
                fwriter.write(idToStr);
                fwriter.write("\n");
                fwriter.close();
            } catch (IOException e) {
                System.err.println("Cannot write on file");
            }
        }
        return userId;
    }

    public byte[] getCurFile(String name) {
        byte[] content = null;
        try {
            Path filePath = Paths.get(this.FILE_DIRECTORY + name);
            content = Files.readAllBytes(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public void lock(String fileName, String checksumId) {
        System.out.println("Tentative de locker " + fileName );
        if (!checksumId.equals(""))
            System.out.println("Succes:" + fileName + " verrouille par !" + checksumId);
        else
            System.out.println("Erreur: Fichier deja verrouille");
    }

}
