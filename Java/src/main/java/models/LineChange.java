package models;

public class LineChange {

	private CommitType commitType;
	private String change;
	private String lineNumber;

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public String toString() {
		return "LineChange [commitType=" + commitType + ", change=" + change + ", lineNumber=" + lineNumber + "]";
	}

	public CommitType getCommitType() {
		return commitType;
	}

	public void setCommitType(CommitType commitType) {
		this.commitType = commitType;
	}

	public String getChange() {
		return change;
	}

	public void setChange(String change) {
		this.change = change;
	}
}
