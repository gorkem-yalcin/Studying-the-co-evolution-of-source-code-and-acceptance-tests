package models;

import java.util.List;

public class FileChange {

	private String changeSummary;
	private Integer additionSummaryCount;
	private Integer deletionSummaryCount;
	private Integer neutralSummaryCount;
	private String fileName;
	private String isFileDeleted;
	private String startDate;
	private String endDate;
	private String startingVersion;
	private String endVersion;
	private List<LineChange> lineChanges;
	private String commitUrl;
	private String projectName;
	private Boolean isFileCreated;

	public FileChange(String changeSummary, Integer additionSummaryCount, Integer deletionSummaryCount, Integer neutralSummaryCount, String fileName, String isFileDeleted,
			String startDate, String endDate, String startingVersion, String endVersion, List<LineChange> lineChanges, String commitUrl, String projectName,
			Boolean isFileCreated) {
		super();
		this.changeSummary = changeSummary;
		this.additionSummaryCount = additionSummaryCount;
		this.deletionSummaryCount = deletionSummaryCount;
		this.neutralSummaryCount = neutralSummaryCount;
		this.fileName = fileName;
		this.isFileDeleted = isFileDeleted;
		this.startDate = startDate;
		this.endDate = endDate;
		this.startingVersion = startingVersion;
		this.endVersion = endVersion;
		this.lineChanges = lineChanges;
		this.commitUrl = commitUrl;
		this.projectName = projectName;
		this.isFileCreated = isFileCreated;
	}

	@Override
	public String toString() {
		return "FileChange [changeSummary=" + changeSummary + ", additionSummaryCount=" + additionSummaryCount + ", deletionSummaryCount=" + deletionSummaryCount
				+ ", neutralSummaryCount=" + neutralSummaryCount + ", fileName=" + fileName + ", isFileDeleted=" + isFileDeleted + ", startDate=" + startDate + ", endDate="
				+ endDate + ", startingVersion=" + startingVersion + ", endVersion=" + endVersion + ", lineChanges=" + lineChanges + ", commitUrl=" + commitUrl + ", projectName="
				+ projectName + ", isFileCreated=" + isFileCreated + "]";
	}

	public Boolean getIsFileCreated() {
		return isFileCreated;
	}

	public void setIsFileCreated(Boolean isFileCreated) {
		this.isFileCreated = isFileCreated;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStartingVersion() {
		return startingVersion;
	}

	public void setStartingVersion(String startingVersion) {
		this.startingVersion = startingVersion;
	}

	public String getEndVersion() {
		return endVersion;
	}

	public void setEndVersion(String endVersion) {
		this.endVersion = endVersion;
	}

	public String getChangeSummary() {
		return changeSummary;
	}

	public void setChangeSummary(String changeSummary) {
		this.changeSummary = changeSummary;
	}

	public Integer getAdditionSummaryCount() {
		return additionSummaryCount;
	}

	public void setAdditionSummaryCount(Integer additionSummaryCount) {
		this.additionSummaryCount = additionSummaryCount;
	}

	public Integer getDeletionSummaryCount() {
		return deletionSummaryCount;
	}

	public void setDeletionSummaryCount(Integer deletionSummaryCount) {
		this.deletionSummaryCount = deletionSummaryCount;
	}

	public Integer getNeutralSummaryCount() {
		return neutralSummaryCount;
	}

	public void setNeutralSummaryCount(Integer neutralSummaryCount) {
		this.neutralSummaryCount = neutralSummaryCount;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getIsFileDeleted() {
		return isFileDeleted;
	}

	public void setIsFileDeleted(String isFileDeleted) {
		this.isFileDeleted = isFileDeleted;
	}

	public List<LineChange> getLineChanges() {
		return lineChanges;
	}

	public void setLineChanges(List<LineChange> lineChanges) {
		this.lineChanges = lineChanges;
	}

	public String getCommitUrl() {
		return commitUrl;
	}

	public void setCommitUrl(String commitUrl) {
		this.commitUrl = commitUrl;
	}
}
