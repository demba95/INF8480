package ca.polymtl.inf8480.tp1.shared;

import java.io.Serializable;
import java.lang.Object;
import java.util.*;

/*This class is used to optimise pair list utilisation obtained in operations,
therefore avoid getting stucked with getKey and getValue semantics
Reference: https://stackoverflow.com/questions/4777622/creating-a-list-of-pairs-in-java

*/
public class FileContent implements Serializable {
    private String operation;
    private int operande;

    public FileContent(String l, int r) {
        this.operation = l;
        this.operande = r;
    }

    public String getOperation() {
        return operation;
    }

    public int getOperande() {
        return operande;
    }
}