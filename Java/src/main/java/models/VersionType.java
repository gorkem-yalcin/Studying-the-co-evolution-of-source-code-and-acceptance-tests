package models;

public class VersionType {

	private Integer majorVersion;
	private Integer minorVersion;
	private Integer minorestVersion;
	private String versionParseError;

	public VersionType(Integer majorVersion, Integer minorVersion, Integer minorestVersion, String versionParseError) {
		super();
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.minorestVersion = minorestVersion;
	}

	public VersionType() {
	}

	public Integer getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(Integer majorVersion) {
		this.majorVersion = majorVersion;
	}

	public Integer getMinorVersion() {
		return minorVersion;
	}

	public void setMinorVersion(Integer minorVersion) {
		this.minorVersion = minorVersion;
	}

	@Override
	public String toString() {
		return "VersionType [majorVersion=" + majorVersion + ", minorVersion=" + minorVersion + ", minorestVersion=" + minorestVersion + ", versionParseError=" + versionParseError + "]";
	}

	public Integer getMinorestVersion() {
		return minorestVersion;
	}

	public void setMinorestVersion(Integer minorestVersion) {
		this.minorestVersion = minorestVersion;
	}

	public String getVersionParseError() {
		return versionParseError;
	}

	public void setVersionParseError(String versionParseError) {
		this.versionParseError = versionParseError;
	}
}
