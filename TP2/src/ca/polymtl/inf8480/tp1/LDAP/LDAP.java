package ca.polymtl.inf8480.tp1.LDAP;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.stream.Collectors;
import java.nio.file.*;
import java.rmi.ConnectException;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.*;
import java.io.BufferedReader;
import java.rmi.RMISecurityManager;

import java.rmi.Naming;
import ca.polymtl.inf8480.tp1.shared.LDAPInterface;

public class LDAP extends UnicastRemoteObject implements LDAPInterface, Remote {

    private static final String USERS = "./users";
    public static final String PROTOCOL = "rmi://";
    public static final String ADDR = "127.0.0.1";
    public static final String ENDPOINT = "/auth";
    public static final int PORT = 5050;

    public LDAP() throws RemoteException {
        super();

    }

    public static void main(String[] args) throws RemoteException {
        LDAP ldapServer = new LDAP();
        try {
            ldapServer.run();
            System.out.println("LDAP ");

        } catch (Exception e) {
        }

    }

    public static void run() {
        RMISecurityManager security = new RMISecurityManager();
        System.setSecurityManager(security);
        try {
            LDAP proxy = new LDAP();
            LocateRegistry.createRegistry(LDAP.PORT);
            Naming.rebind(PROTOCOL + LDAP.ADDR + "->" + LDAP.PORT + "/auth", proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerClient(String user, String password) throws RemoteException {
        // from TP1

        if (this.verifyClient(user, password))
            return;
        String infos = user + "->" + password + "";
        try {
            Files.write(Paths.get(USERS), infos.getBytes(), StandardOpenOption.WRITE);
        } catch (Exception e) {
        }
    }

    public boolean verifyClient(String user, String password) throws RemoteException {
        List<String> userData;
        boolean isLegit = false;

        try (BufferedReader buffer = Files.newBufferedReader(Paths.get(USERS))) {
            userData = buffer.lines().collect(Collectors.toList());
            Iterator<String> it = userData.iterator();
            while (it.hasNext()) {
                String[] data = it.next().split("->"); // ignorer le separateur
                if (data[0].equals(user) && data[1].equals(password)) {
                    isLegit = true;
                }
            }
        } catch (Exception e) {
        }
        return isLegit;
    }

}