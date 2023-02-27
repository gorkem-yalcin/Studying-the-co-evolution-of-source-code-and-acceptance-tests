package models;

public class Version {

	public Version(String version, String date) {
		super();
		this.version = version;
		this.date = date;
	}

	private String version;
	private String date;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}