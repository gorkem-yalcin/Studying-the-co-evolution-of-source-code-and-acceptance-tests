package models;

import java.util.List;

public class ProjectCommitData {
	
	private List<FileChange> fileChangeList;
	private String projectName;

	public List<FileChange> getFileChangeList() {
		return fileChangeList;
	}

	public void setFileChangeList(List<FileChange> fileChangeList) {
		this.fileChangeList = fileChangeList;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
}
