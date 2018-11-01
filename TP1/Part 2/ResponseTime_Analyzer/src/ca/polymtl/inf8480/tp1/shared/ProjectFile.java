package ca.polymtl.inf8480.tp1.shared;

import java.io.Serializable;

public class ProjectFile implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private String content;
	private String checksum;
	private String user;
	private Boolean isLocked;

	public ProjectFile() {
		name = null;
		content = null;
		user = null;
		checksum = null;
		isLocked = false;
	}

	public ProjectFile(String _name) {
		name = _name;
		content = null;
		checksum = null;
		user = null;
		isLocked = false;
	}

	public ProjectFile(String _name, String _content) {
		name = _name;
		content = _content;
		user = null;
		checksum = null;
		isLocked = false;
	}

	public String getFileContent() {
		return this.content;
	}

	public void setFileContent(String fileContent) {
		this.content = fileContent;
	}

	public String getFileName() {
		return this.name;
	}

	public void setFileName(String fileName) {
		this.name = fileName;
	}

	public String getChecksum() {
		return this.checksum;
	}

	public void unlockFile() {
		this.setUser(null);
		this.isLocked = false;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String client) {
		this.user = client;
		this.isLocked = true;
		this.checksum = client;
	}

	public boolean isLocked() {
		return (this.isLocked == true && this.getUser() != null);
	}

	public void lockfile(String client) {
		this.setUser(client);
	}
}
