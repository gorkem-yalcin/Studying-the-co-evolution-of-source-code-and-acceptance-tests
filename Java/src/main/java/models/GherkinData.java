package models;

public class GherkinData {
	
	private String commitDate;
	private String version;
	private CommitType commitType;
	private String changes;

	public String getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(String commitDate) {
		this.commitDate = commitDate;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public CommitType getCommitType() {
		return commitType;
	}

	public void setCommitType(CommitType commitType) {
		this.commitType = commitType;
	}

	public String getChanges() {
		return changes;
	}

	public void setChanges(String changes) {
		this.changes = changes;
	}
}
