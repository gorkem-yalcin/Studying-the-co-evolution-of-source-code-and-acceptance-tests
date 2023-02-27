package models;

import java.util.Map;

public class Repository {

	private String name;
	private String url;
	private String starCount;
	private String tags;
	private String issues;
	private Map<String, String> languageFileCountMap;
	private Map<String, String> languagePercentageMap;

	public Repository(String name, String url, String repositoryStarCount) {
		super();
		this.name = name;
		this.url = url;
		this.starCount = repositoryStarCount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStarCount() {
		return starCount;
	}

	public void setStarCount(String starCount) {
		this.starCount = starCount;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getIssues() {
		return issues;
	}

	public void setIssues(String issues) {
		this.issues = issues;
	}

	public Map<String, String> getLanguageFileCountMap() {
		return languageFileCountMap;
	}

	public void setLanguageFileCountMap(Map<String, String> languageFileCountMap) {
		this.languageFileCountMap = languageFileCountMap;
	}

	public Map<String, String> getLanguagePercentageMap() {
		return languagePercentageMap;
	}

	public void setLanguagePercentageMap(Map<String, String> languagePercentageMap) {
		this.languagePercentageMap = languagePercentageMap;
	}

}
