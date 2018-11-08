package ca.polymtl.inf8480.tp1.shared;

import java.io.Serializable;

public class Configurations implements Serializable {
    private int portNumber_;
    private String serverIp_;
    private int mode_;
    private int q_;

    public int getPortNumber() {
        return this.portNumber_;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber_ = portNumber;
    }

    public String getServerIp() {
        return this.serverIp_;
    }

    public void setServerIp(String serverIp) {
        this.serverIp_ = serverIp;
    }

    public int getMode() {
        return this.mode_;
    }

    public void setMode_(int mode) {
        this.mode_ = mode;
    }

    public int getQ() {
        return this.q_;
    }

    public void setQ(int q) {
        this.q_ = q;
    }

    public Configurations(String serverIp, int mode, int portNumber, int q) {
        this.serverIp_ = serverIp;
        this.portNumber_ = portNumber;
        this.mode_ = mode;
        this.q_ = q;
    }

    public Configurations() {
        this.serverIp_ = null;
        this.portNumber_ = 0;
        this.mode_ = 0;
        this.q_ = 0;
        // Configurations("", 0, 0, 0);
    }

}