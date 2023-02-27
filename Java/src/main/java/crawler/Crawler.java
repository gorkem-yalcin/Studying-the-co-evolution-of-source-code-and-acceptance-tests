package crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import models.CommitType;
import models.FileChange;
import models.LineChange;
import models.ProjectCommitData;
import models.Repository;
import models.Version;
import models.VersionType;
import util.Constants;
import util.ExcelUtil;

public class Crawler {
	private static Integer TOTAL_TLOC = 0;
	private static Integer TOTAL_ELOC = 0;

	public void crawlProjectList(String programmingLanguage, Integer leastStarCount) {
		String crawlUrl = getUrlList(programmingLanguage, leastStarCount);
		Document document;
		try {
			new File("logs.txt").createNewFile();
			BufferedWriter myWriter = new BufferedWriter(new FileWriter("logs.txt"));
			document = Jsoup.connect(crawlUrl).timeout(0).userAgent(Constants.USER_AGENT).get();

			Elements repositoryCrawlList = document.getElementsByClass("repo-list-item hx_hit-repo d-flex flex-justify-start py-4 public source");
			Integer pageNumber = 1;
			List<Repository> repositoryList = new ArrayList<>();
			while (!repositoryCrawlList.isEmpty()) {
				System.out.println("Page number: " + pageNumber);
				for (Element repository : repositoryCrawlList) {

					Elements repositoryUrlElements = repository.getElementsByClass("v-align-middle");
					String repositoryUrl = "-";
					if (!isEmpty(repositoryUrlElements)) {
						Elements repositoryUrlHrefElements = repositoryUrlElements.get(0).getElementsByAttribute("href");
						if (!isEmpty(repositoryUrlHrefElements)) {
							repositoryUrl = Constants.GITHUB_PREFIX + repositoryUrlHrefElements.get(0).ownText();
						}
					}

					Elements repositoryNameElements = repository.getElementsByClass("mb-1");
					String repositoryName = "-";
					if (!isEmpty(repositoryNameElements)) {
						repositoryName = repositoryNameElements.get(0).ownText();
					} else {
						myWriter.write("Name: " + repository.getAllElements() + "\n");
						System.out.println("Name: " + repository.getAllElements());
					}

					Elements repositoryStarCountElements = repository.getElementsByClass("Link--muted");
					String repositoryStarCount = "-";
					if (!isEmpty(repositoryStarCountElements)) {
						repositoryStarCount = repositoryStarCountElements.get(0).ownText();
					} else {
						myWriter.write("Star: " + repository.getAllElements() + "\n");
						System.out.println("Star: " + repository.getAllElements());
					}

					repositoryList.add(new Repository(repositoryName, repositoryUrl, repositoryStarCount));
				}
				pageNumber++;
				crawlUrl = new StringBuilder(crawlUrl).append("&p=").append(pageNumber).toString();
				Thread.sleep(15000);
				document = Jsoup.connect(crawlUrl).timeout(0).userAgent(Constants.USER_AGENT).get();
				crawlUrl = crawlUrl.substring(0, crawlUrl.lastIndexOf("&p"));
				repositoryCrawlList = document.getElementsByClass("repo-list-item hx_hit-repo d-flex flex-justify-start py-4 public source");

			}
			Integer count = 0;
			for (Repository repository : repositoryList) {
				Thread.sleep(15000);
				count++;
				System.out.println("Repo count: " + count);
				try {
					document = Jsoup.connect(repository.getUrl()).timeout(0).userAgent(Constants.USER_AGENT).get();
				} catch (Exception e) {
					Thread.sleep(20000);
					try {
						System.out.println("Second try");
						document = Jsoup.connect(repository.getUrl()).timeout(0).userAgent(Constants.USER_AGENT).get();
					} catch (Exception e2) {
						Thread.sleep(20000);
						System.out.println("Third try");
						document = Jsoup.connect(repository.getUrl()).timeout(0).userAgent(Constants.USER_AGENT).get();
					}
				}
				String issues = "##";
				if (!isEmpty(document.getElementsByAttributeValue("id", "issues-tab")) && !isEmpty(document.getElementsByAttributeValue("id", "issues-tab").get(0).getElementsByAttribute("title"))) {
					issues = document.getElementsByAttributeValue("id", "issues-tab").get(0).getElementsByAttribute("title").get(0).ownText();
				} else {
					myWriter.write("Issues: " + document.getAllElements() + "\n");
					System.out.println("Issues: " + document.getAllElements());
				}
				repository.setIssues(issues);
				String tags = "##";
				if (!isEmpty(document.getElementsByClass("ml-3 Link--primary no-underline")) && !isEmpty(document.getElementsByClass("ml-3 Link--primary no-underline").get(0).getElementsByTag("strong"))) {
					tags = document.getElementsByClass("ml-3 Link--primary no-underline").get(0).getElementsByTag("strong").get(0).ownText();
				} else {
					myWriter.write("Tags: " + document.getAllElements() + "\n");
					System.out.println("Tags: " + document.getAllElements());
				}
				repository.setTags(tags);
				Map<String, String> languagePercentageMap = new HashMap<>();
				Elements languagePercentageElements = document.getElementsByClass("d-inline-flex flex-items-center flex-nowrap Link--secondary no-underline text-small mr-3");
				for (Element languagePercentageElement : languagePercentageElements) {
					if (!isEmpty(languagePercentageElement.getElementsByClass("color-text-primary text-bold mr-1")) && languagePercentageElement.getElementsByTag("span").size() > 1) {
						languagePercentageMap.put(languagePercentageElement.getElementsByClass("color-text-primary text-bold mr-1").get(0).ownText(), languagePercentageElement.getElementsByTag("span").get(1).ownText());
					} else {
						myWriter.write("Language Percentage: " + document.getAllElements() + "\n");
						System.out.println("Language Percentage: " + document.getAllElements());
					}

				}
				repository.setLanguagePercentageMap(languagePercentageMap);
				Map<String, String> languageFileCountMap = new HashMap<>();
				String codeFileCountSearchUrl = repository.getUrl() + "/search?l=" + Constants.GHERKIN;
				Thread.sleep(15000);
				try {
					document = Jsoup.connect(codeFileCountSearchUrl).timeout(0).userAgent(Constants.USER_AGENT).get();
				} catch (Exception e) {
					Thread.sleep(20000);
					try {
						System.out.println("Second try second connect");
						document = Jsoup.connect(codeFileCountSearchUrl).timeout(0).userAgent(Constants.USER_AGENT).get();
					} catch (Exception e2) {
						Thread.sleep(20000);
						System.out.println("Third try");
						document = Jsoup.connect(codeFileCountSearchUrl).timeout(0).userAgent(Constants.USER_AGENT).get();
					}
				}
				Elements otherLanguages = document.getElementsByClass("filter-item");
				for (Element otherLanguage : otherLanguages) {
					if (!isEmpty(otherLanguage.getElementsByClass("filter-item")) && !isEmpty(otherLanguage.getElementsByClass("filter-item").get(0).getElementsByTag("span"))) {
						languageFileCountMap.put(otherLanguage.getElementsByClass("filter-item").get(0).ownText(), otherLanguage.getElementsByClass("filter-item").get(0).getElementsByTag("span").get(0).ownText());
					} else {
						myWriter.write("Language Count: " + document.getAllElements() + "\n");
						System.out.println("Language Count: " + document.getAllElements());
					}
				}
				if (!isEmpty(document.getElementsByAttributeValue("data-search-type", "Code"))) {
					languageFileCountMap.put("Gherkin", document.getElementsByAttributeValue("data-search-type", "Code").get(0).ownText());
				} else {
					myWriter.write("Gherkin: " + document.getAllElements() + "\n");
					System.out.println("Gherkin: " + document.getAllElements());
				}

				repository.setLanguageFileCountMap(languageFileCountMap);
			}
			myWriter.close();
			ExcelUtil.createRepositoryExcel(repositoryList, "Projects");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	private String getUrlList(String programmingLanguage, Integer leastStarCount) {
		String url = Constants.GITHUB_SEARCH_URL;
		url = programmingLanguage != null ? url + "language:" + programmingLanguage + "+" : url;
		url = leastStarCount > 0 ? url + "stars:>=" + leastStarCount : url;
		return url;
	}

	public List<ProjectCommitData> crawlProjectGherkinData(List<String> urlList, List<String> projectNameList) {
		List<ProjectCommitData> projectCommitDataList = new ArrayList<>();
		for (Integer x = 0; x < urlList.size(); x++) {
			String mainUrl = urlList.get(x);
			String crawlUrl = mainUrl + "/tags/";
			Document document;
			try {
				new File("logsGherkinData.txt").createNewFile();
				BufferedWriter myWriter = new BufferedWriter(new FileWriter("logsGherkinData.txt"));

				document = Jsoup.connect(crawlUrl).timeout(0).userAgent(Constants.USER_AGENT).get();

				List<Version> versionList = new ArrayList<>();

				Elements versionCrawlList = document.getElementsByClass("Box-row position-relative d-flex ");

				for (Element version : versionCrawlList) {
					versionList.add(new Version(version.getElementsByClass("flex-auto min-width-0 pr-2 pb-1 commit-title").get(0).getElementsByAttribute("href").get(0).ownText(),
							version.getElementsByClass("d-inline-block mt-1 mr-2 color-fg-muted").get(0).getElementsByTag("relative-time").get(0).attr("datetime")));
				}

				if (!isEmpty(document.getElementsMatchingOwnText("Next")) && isEmpty(document.getElementsContainingOwnText("Next").get(0).getElementsByClass("disabled"))) {

					while (isEmpty(document.getElementsMatchingOwnText("Next").get(0).getElementsByClass("disabled"))) {
						System.out.println("Current URL: " + crawlUrl);
						crawlUrl = document.getElementsContainingOwnText("Next").get(0).attr("href");
						if (!crawlUrl.startsWith("https://github.com")) {
							crawlUrl = "https://github.com" + crawlUrl;
						}
						Thread.sleep(15000);
						document = Jsoup.connect(crawlUrl).timeout(0).userAgent(Constants.USER_AGENT).get();
						versionCrawlList = document.getElementsByClass("Box-row position-relative d-flex ");

						for (Element version : versionCrawlList) {
							versionList.add(new Version(version.getElementsByClass("flex-auto min-width-0 pr-2 pb-1 commit-title").get(0).getElementsByAttribute("href").get(0).ownText(),
									version.getElementsByClass("d-inline-block mt-1 mr-2 color-fg-muted").get(0).getElementsByTag("relative-time").get(0).attr("datetime")));
						}
					}
				}
				Thread.sleep(15000);
				System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
				FirefoxProfile profile = new FirefoxProfile();
				profile.setPreference("layout.css.devPixelsPerPx", "0.1");
				FirefoxOptions fo = new FirefoxOptions();
				fo.setProfile(profile);
				List<FileChange> fileChangeList = new ArrayList<>();
				WebDriver driver = new FirefoxDriver(fo);
				driver.manage().timeouts().implicitlyWait(90, TimeUnit.SECONDS);
				for (Integer i = 0; i < versionList.size() - 1; i++) {
					String newVersion = versionList.get(i).getVersion();
					String oldVersion = versionList.get(i + 1).getVersion();
					String updatedUrl = mainUrl + "/compare/" + oldVersion + "..." + newVersion;
					System.out.println(updatedUrl);
					driver.get(updatedUrl);
					Thread.sleep(15000);
					Elements elements = Jsoup.parse(driver.getPageSource()).getElementsByAttributeValue("data-file-type", ".feature");
					if (elements.isEmpty()) {
						elements = Jsoup.parse(driver.getPageSource()).getElementsByAttributeValue("data-file-type", ".feature");
					}

					for (Integer j = 0; j < elements.size(); j += 2) {
						Element featureFileChange = elements.get(j);
						String changeSummary = featureFileChange.getElementsByClass("diffstat tooltipped tooltipped-e").get(0).attr("aria-label");
						Integer additionSummaryCount = featureFileChange.getElementsByClass("diffstat-block-added").size();
						Integer deletionSummaryCount = featureFileChange.getElementsByClass("diffstat-block-deleted").size();
						Integer neutralSummaryCount = featureFileChange.getElementsByClass("diffstat-block-neutral").size();
						String fileName = featureFileChange.getElementsByTag("clipboard-copy").get(0).attr("value");
						String isFileDeleted = featureFileChange.attr("data-file-deleted");
						Boolean isFileCreated = getIsFileCreated(changeSummary, featureFileChange);
						List<LineChange> lineChanges = new ArrayList<>();
						Elements removalLineChanges = featureFileChange.getElementsByClass("blob-code blob-code-deletion js-file-line");
						Elements removalLineNumberElements = featureFileChange.getElementsByClass("blob-num blob-num-deletion js-linkable-line-number");
						for (Integer k = 0; k < removalLineChanges.size(); k++) {
							Element removal = removalLineChanges.get(k);
							String lineNumber = removalLineNumberElements.get(k).attr("data-line-number");
							Elements allTextElements = removal.getAllElements();
							for (Element textElement : allTextElements) {
								LineChange lineChange = new LineChange();
								lineChange.setChange(textElement.ownText());
								lineChange.setCommitType(CommitType.REMOVAL);
								lineChange.setLineNumber(lineNumber);
								lineChanges.add(lineChange);
							}
						}
						Elements additionLineChanges = featureFileChange.getElementsByClass("blob-code blob-code-addition js-file-line");
						Elements additionLineNumberElements = featureFileChange.getElementsByClass("blob-num blob-num-addition js-linkable-line-number js-code-nav-line-number");
						for (Integer l = 0; l < additionLineChanges.size(); l++) {
							Element addition = additionLineChanges.get(l);
							String lineNumber = additionLineNumberElements.get(l).attr("data-line-number");
							Elements allTextElements = addition.getAllElements();
							for (Element textElement : allTextElements) {
								LineChange lineChange = new LineChange();
								lineChange.setChange(textElement.ownText());
								lineChange.setCommitType(CommitType.ADDITION);
								lineChange.setLineNumber(lineNumber);
								lineChanges.add(lineChange);
							}
						}
						String projectName = projectNameList.get(x);
						FileChange fc = new FileChange(changeSummary, additionSummaryCount, deletionSummaryCount, neutralSummaryCount, fileName, isFileDeleted, versionList.get(i).getDate(), versionList.get(i + 1).getDate(), oldVersion, newVersion,
								lineChanges, updatedUrl, projectName, isFileCreated);
						fileChangeList.add(fc);
						myWriter.write(fc.toString());
						System.out.println(fc);
					}
				}
				ProjectCommitData pcd = new ProjectCommitData();
				pcd.setFileChangeList(fileChangeList);
				ExcelUtil.createFileChangeExcel(fileChangeList, " Gherkin Files");
				projectCommitDataList.add(pcd);

				driver.quit();

				myWriter.close();
				// ExcelUtil.createGherkidDataExcel(repositoryList, "Projects");
			} catch (

			Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		return projectCommitDataList;

	}

	private Boolean getIsFileCreated(String changeSummary, Element featureFileChange) {
		Boolean isFileCreated = false;
		if (changeSummary != null && isOnlyAdditionString(changeSummary)) {
			String lastEdited = "";
			if (!featureFileChange.getElementsByAttribute("data-hunk").isEmpty()) {
				lastEdited = featureFileChange.getElementsByAttribute("data-hunk").get(featureFileChange.getElementsByAttribute("data-hunk").size() - 1).getElementsByAttribute("data-line-number").get(0).attr("data-line-number");
			}
			isFileCreated = lastEdited.equals(changeSummary.split(" ")[0]);
		}
		return isFileCreated;
	}

	private boolean isOnlyAdditionString(String changeSummary) {
		List<String> changeSummaryList = Arrays.asList(changeSummary.split(" "));
		if (changeSummaryList.contains("additions")) {
			Integer additions = 0;
			if (changeSummaryList.indexOf("additions") != -1) {
				additions = Integer.parseInt(changeSummaryList.get(changeSummaryList.indexOf("additions") - 1).replace(",", ""));// String can come as 6,773, parsing error
			}
			Integer deletions = 0;
			if (changeSummaryList.indexOf("deletions") != -1) {
				deletions = Integer.parseInt(changeSummaryList.get(changeSummaryList.indexOf("deletions") - 1).replace(",", ""));
			}
			Integer total = Integer.parseInt(changeSummaryList.get(0).replace(",", ""));
			if (additions.equals(total) && deletions.equals(0)) {
				return true;
			}
		}
		return false;
	}

	public List<ProjectCommitData> crawl(List<String> projectUrlList, List<String> projectNameList) throws IOException, InterruptedException {
		List<ProjectCommitData> projectCommitDataList = new ArrayList<>();
		for (Integer x = 0; x < projectUrlList.size(); x++) {
			Integer totalChangedFileCount = 0;
			TOTAL_ELOC = 0;
			TOTAL_TLOC = 0;
			String mainUrl = projectUrlList.get(x);
			String crawlUrl = mainUrl + "/tags/";
			Document document;
			new File("logsGherkinData.txt").createNewFile();
			BufferedWriter myWriter = new BufferedWriter(new FileWriter("logsGherkinData.txt"));

			document = Jsoup.connect(crawlUrl).timeout(0).userAgent(Constants.USER_AGENT).get();

			List<Version> versionList = new ArrayList<>();

			Elements versionCrawlList = document.getElementsByClass("Box-row position-relative d-flex ");

			for (Element version : versionCrawlList) {
				versionList.add(new Version(version.getElementsByClass("flex-auto min-width-0 pr-2 pb-1 commit-title").get(0).getElementsByAttribute("href").get(0).ownText(),
						version.getElementsByClass("d-inline-block mt-1 mr-2 color-fg-muted").get(0).getElementsByTag("relative-time").get(0).attr("datetime")));
			}

			if (!isEmpty(document.getElementsMatchingOwnText("Next")) && isEmpty(document.getElementsContainingOwnText("Next").get(0).getElementsByClass("disabled"))) {

				while (isEmpty(document.getElementsMatchingOwnText("Next").get(0).getElementsByClass("disabled"))) {
					System.out.println("Current URL: " + crawlUrl);
					crawlUrl = document.getElementsContainingOwnText("Next").get(0).attr("href");
					if (!crawlUrl.startsWith("https://github.com")) {
						crawlUrl = "https://github.com" + crawlUrl;
					}
					Thread.sleep(15000);
					document = Jsoup.connect(crawlUrl).timeout(0).userAgent(Constants.USER_AGENT).get();
					versionCrawlList = document.getElementsByClass("Box-row position-relative d-flex ");

					for (Element version : versionCrawlList) {
						versionList.add(new Version(version.getElementsByClass("flex-auto min-width-0 pr-2 pb-1 commit-title").get(0).getElementsByAttribute("href").get(0).ownText(),
								version.getElementsByClass("d-inline-block mt-1 mr-2 color-fg-muted").get(0).getElementsByTag("relative-time").get(0).attr("datetime")));
					}
				}
			}
			Collections.reverse(versionList);

			String firstVersionURL = mainUrl + "/releases/tag/" + versionList.get(0).getVersion();
			document = Jsoup.connect(firstVersionURL).timeout(0).userAgent(Constants.USER_AGENT).get();

			Elements downloadLinkElements = document.getElementsByAttribute("href");
			for (Element downloadLinkElement : downloadLinkElements) {
				if (downloadLinkElement.getElementsByAttributeValueContaining("href", ".zip") != null && !isEmpty(downloadLinkElement.getElementsByAttributeValueContaining("href", ".zip"))) {
					String downloadUrl = "https://github.com" + downloadLinkElement.getElementsByAttributeValueContaining("href", ".zip").get(0).attr("href");
					URL url = new URL(downloadUrl);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					InputStream in = connection.getInputStream();
					ZipInputStream zipIn = new ZipInputStream(in);
					FileOutputStream fos = new FileOutputStream(projectNameList.get(x) + ".zip");
					try (ZipOutputStream zipOut = new ZipOutputStream(fos)) {
						ZipEntry entry = zipIn.getNextEntry();
						while (entry != null) {
							zipOut.putNextEntry(entry);
							byte[] bytes = new byte[1024];
							int length;
							while ((length = zipIn.read(bytes)) >= 0) {
								zipOut.write(bytes, 0, length);
							}
							System.out.println(entry.getName());
							if (!entry.isDirectory()) {
								System.out.println("===File===");
							} else {
								System.out.println("===Directory===");
							}
							zipIn.closeEntry();
							entry = zipIn.getNextEntry();
						}
					}
				}
			}
			String fileZip = projectNameList.get(x) + ".zip";
			File destDir = new File(projectNameList.get(x));
			byte[] buffer = new byte[1024];
			try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))) {
				ZipEntry zipEntry = zis.getNextEntry();
				while (zipEntry != null) {
					while (zipEntry != null) {
						File newFile = newFile(destDir, zipEntry);
						if (zipEntry.isDirectory()) {
							if (!newFile.isDirectory() && !newFile.mkdirs()) {
								throw new IOException("Failed to create directory " + newFile);
							}
						} else {
							// fix for Windows-created archives
							File parent = newFile.getParentFile();
							if (!parent.isDirectory() && !parent.mkdirs()) {
								throw new IOException("Failed to create directory " + parent);
							}

							// write file content
							try (FileOutputStream fos = new FileOutputStream(newFile)) {
								int len;
								while ((len = zis.read(buffer)) > 0) {
									fos.write(buffer, 0, len);
								}
								fos.close();
							}
						}
						zipEntry = zis.getNextEntry();
					}
				}
			}

			File directory = new File(projectNameList.get(x));
			showFiles(directory.listFiles());

			Thread.sleep(15000);
			System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
			FirefoxProfile profile = new FirefoxProfile();
			profile.setPreference("layout.css.devPixelsPerPx", "0.1");
			FirefoxOptions fo = new FirefoxOptions();
			fo.setProfile(profile);
			List<FileChange> fileChangeList = new ArrayList<>();
			WebDriver driver = new FirefoxDriver(fo);
			List<Map<String, Object>> versionLineChangeMapList = new ArrayList<>();

			Integer cumulativeAddedProductLinePerVersion = 0;
			Integer cumulativeRemovedProductLinePerVersion = 0;
			Integer cumulativeAddedGherkinLinePerVersion = 0;
			Integer cumulativeRemovedGherkinLinePerVersion = 0;
			Integer cumulativeRemovedScenarioPerVersion = 0;
			Integer cumulativeRemovedWhenPerVersion = 0;
			Integer cumulativeRemovedThenPerVersion = 0;
			Integer cumulativeRemovedGivenPerVersion = 0;
			Integer cumulativeAddedScenarioPerVersion = 0;
			Integer cumulativeAddedWhenPerVersion = 0;
			Integer cumulativeAddedThenPerVersion = 0;
			Integer cumulativeAddedGivenPerVersion = 0;

			Integer cumulativeSingularAddedProductLinePerVersion = 0;
			Integer cumulativeSingularRemovedProductLinePerVersion = 0;
			Integer cumulativeSingularAddedGherkinLinePerVersion = 0;
			Integer cumulativeSingularRemovedGherkinLinePerVersion = 0;
			Integer cumulativeSingularRemovedScenarioPerVersion = 0;
			Integer cumulativeSingularRemovedWhenPerVersion = 0;
			Integer cumulativeSingularRemovedThenPerVersion = 0;
			Integer cumulativeSingularRemovedGivenPerVersion = 0;
			Integer cumulativeSingularAddedScenarioPerVersion = 0;
			Integer cumulativeSingularAddedWhenPerVersion = 0;
			Integer cumulativeSingularAddedThenPerVersion = 0;
			Integer cumulativeSingularAddedGivenPerVersion = 0;

			Integer singularUpdatedProductionCodePerVersion = 0;
			Integer singularUpdatedGherkinCodePerVersion = 0;

			Integer isGherkinNotProduction = 0;
			Integer isGherkinAndProduction = 0;
			Integer isProductionAndNotGherkin = 0;
			Integer isSame = 0;

			Integer majorMinorVersionCount = 0;
			Integer majorVersionCount = 0;
			Integer minorVersionCount = 0;
			Integer patchVersionCount = 0;

			Integer productionCodeUpdatedCountForMajorMinor = 0;
			Integer testCodeUpdatedCountForMajorMinor = 0;

			for (Integer i = 0; i < versionList.size() - 1; i++) {
				String newVersion = versionList.get(i).getVersion();
				String oldVersion = versionList.get(i + 1).getVersion();
				String updatedUrl = mainUrl + "/compare/" + newVersion + "..." + oldVersion;
				System.out.println(updatedUrl);
				driver.get(updatedUrl);
				Thread.sleep(15000);
				Elements elements = Jsoup.parse(driver.getPageSource()).getElementsByAttribute("data-file-type");
				if (elements.isEmpty()) {
					elements = Jsoup.parse(driver.getPageSource()).getElementsByAttribute("data-file-type");
				}
				BufferedWriter htmlWriter = new BufferedWriter(new FileWriter("html.txt"));
				htmlWriter.write(elements.toString());
				htmlWriter.close();

				Boolean isProductionCodeUpdated = false;
				Boolean isGherkinCodeUpdated = false;

				Boolean isAddedProductLinePerVersionUpdated = false;
				Boolean isRemovedProductLinePerVersionUpdated = false;
				Boolean isAddedGherkinLinePerVersionUpdated = false;
				Boolean isRemovedGherkinLinePerVersionUpdated = false;
				Boolean isRemovedScenarioPerVersionUpdated = false;
				Boolean isRemovedWhenPerVersionUpdated = false;
				Boolean isRemovedThenPerVersionUpdated = false;
				Boolean isRemovedGivenPerVersionUpdated = false;
				Boolean isAddedScenarioPerVersionUpdated = false;
				Boolean isAddedWhenPerVersionUpdated = false;
				Boolean isAddedThenPerVersionUpdated = false;
				Boolean isAddedGivenPerVersionUpdated = false;

				Integer addedProductLinePerVersion = 0;
				Integer removedProductLinePerVersion = 0;
				Integer addedGherkinLinePerVersion = 0;
				Integer removedGherkinLinePerVersion = 0;
				Integer removedScenarioPerVersion = 0;
				Integer removedWhenPerVersion = 0;
				Integer removedThenPerVersion = 0;
				Integer removedGivenPerVersion = 0;
				Integer addedScenarioPerVersion = 0;
				Integer addedWhenPerVersion = 0;
				Integer addedThenPerVersion = 0;
				Integer addedGivenPerVersion = 0;
				for (Integer j = 0; j < elements.size(); j += 2) {
					Element fileChange = elements.get(j);
					String changeSummary = fileChange.getElementsByClass("diffstat tooltipped tooltipped-e").get(0).attr("aria-label");
					Integer additionSummaryCount = fileChange.getElementsByClass("diffstat-block-added").size();
					Integer deletionSummaryCount = fileChange.getElementsByClass("diffstat-block-deleted").size();
					Integer neutralSummaryCount = fileChange.getElementsByClass("diffstat-block-neutral").size();
					String fileName = fileChange.getElementsByTag("clipboard-copy").get(0).attr("value");
					String isFileDeleted = fileChange.attr("data-file-deleted");
					Boolean isFileCreated = getIsFileCreated(changeSummary, fileChange);
					List<LineChange> lineChanges = new ArrayList<>();
					Elements removalLineChanges = fileChange.getElementsByClass("blob-code blob-code-deletion  js-file-line");
					Elements removalLineNumberElements = fileChange.getElementsByClass("blob-num blob-num-deletion js-linkable-line-number");
					for (Integer k = 0; k < removalLineChanges.size(); k++) {
						Element removal = removalLineChanges.get(k);
						String lineNumber = removalLineNumberElements.get(k).attr("data-line-number");
						Elements allTextElements = removal.getAllElements();
						for (Element textElement : allTextElements) {
							LineChange lineChange = new LineChange();
							lineChange.setChange(textElement.ownText());
							if (textElement.ownText().toLowerCase().contains("then")) {
								removedThenPerVersion++;
								cumulativeRemovedThenPerVersion++;
								isRemovedThenPerVersionUpdated = true;
							}
							if (textElement.ownText().toLowerCase().contains("when")) {
								removedWhenPerVersion++;
								cumulativeRemovedWhenPerVersion++;
								isRemovedWhenPerVersionUpdated = true;
							}
							if (textElement.ownText().toLowerCase().contains("given")) {
								removedGivenPerVersion++;
								cumulativeRemovedGivenPerVersion++;
								isRemovedGivenPerVersionUpdated = true;
							}
							if (textElement.ownText().toLowerCase().contains("scenario")) {
								removedScenarioPerVersion++;
								cumulativeRemovedScenarioPerVersion++;
								isRemovedScenarioPerVersionUpdated = true;
							}
							lineChange.setCommitType(CommitType.REMOVAL);
							lineChange.setLineNumber(lineNumber);
							lineChanges.add(lineChange);
						}
					}

					Elements additionLineChanges = fileChange.getElementsByClass("blob-code blob-code-addition  js-file-line");
					Elements additionLineNumberElements = fileChange.getElementsByClass("blob-num blob-num-addition js-linkable-line-number js-code-nav-line-number js-blob-rnum");
					for (Integer l = 0; l < additionLineChanges.size(); l++) {
						Element addition = additionLineChanges.get(l);
						String lineNumber = additionLineNumberElements.size() > l ? additionLineNumberElements.get(l).attr("data-line-number") : "";
						Elements allTextElements = addition.getAllElements();
						for (Element textElement : allTextElements) {
							LineChange lineChange = new LineChange();
							lineChange.setChange(textElement.ownText());
							lineChange.setCommitType(CommitType.ADDITION);
							if (fileChange.attr("data-file-type").equals(".feature")) {
								if (!textElement.getElementsContainingOwnText("then").isEmpty()) {
									addedThenPerVersion++;
									cumulativeAddedThenPerVersion++;
									isAddedThenPerVersionUpdated = true;
								}
								if (!textElement.getElementsContainingOwnText("when").isEmpty()) {
									addedWhenPerVersion++;
									cumulativeAddedWhenPerVersion++;
									isAddedWhenPerVersionUpdated = true;
								}
								if (!textElement.getElementsContainingOwnText("given").isEmpty()) {
									addedGivenPerVersion++;
									cumulativeAddedGivenPerVersion++;
									isAddedGivenPerVersionUpdated = true;
								}
								if (!textElement.getElementsContainingOwnText("scenario").isEmpty()) {
									addedScenarioPerVersion++;
									cumulativeAddedScenarioPerVersion++;
									isAddedScenarioPerVersionUpdated = true;
								}
							}
							lineChange.setLineNumber(lineNumber);
							lineChanges.add(lineChange);
						}
					}
					if (fileChange.attr("data-file-type").equals(".feature")) {
						removedGherkinLinePerVersion += removalLineChanges.size();
						addedGherkinLinePerVersion += additionLineChanges.size();
						cumulativeAddedGherkinLinePerVersion += additionLineChanges.size();
						cumulativeRemovedGherkinLinePerVersion += removalLineChanges.size();
						if (!additionLineChanges.isEmpty()) {
							isAddedGherkinLinePerVersionUpdated = true;
						}
						if (!removalLineChanges.isEmpty()) {
							isRemovedGherkinLinePerVersionUpdated = true;
						}
					} else {
						removedProductLinePerVersion += removalLineChanges.size();
						addedProductLinePerVersion += additionLineChanges.size();
						cumulativeAddedProductLinePerVersion += additionLineChanges.size();
						cumulativeRemovedProductLinePerVersion += removalLineChanges.size();
						if (!additionLineChanges.isEmpty()) {
							isAddedProductLinePerVersionUpdated = true;
						}
						if (!removalLineChanges.isEmpty()) {
							isRemovedProductLinePerVersionUpdated = true;
						}
					}
					String projectName = projectNameList.get(x);
					FileChange fc = new FileChange(changeSummary, additionSummaryCount, deletionSummaryCount, neutralSummaryCount, fileName, isFileDeleted, versionList.get(i).getDate(), versionList.get(i + 1).getDate(), oldVersion, newVersion,
							lineChanges, updatedUrl, projectName, isFileCreated);
					totalChangedFileCount++;
					fileChangeList.add(fc);
					myWriter.write(fc.toString());
					System.out.println(fc);
				}

				TOTAL_ELOC = TOTAL_ELOC + addedProductLinePerVersion - removedProductLinePerVersion;
				TOTAL_TLOC = TOTAL_TLOC + addedGherkinLinePerVersion - removedGherkinLinePerVersion;

				cumulativeSingularAddedProductLinePerVersion = isAddedProductLinePerVersionUpdated ? cumulativeSingularAddedProductLinePerVersion + 1 : cumulativeSingularAddedProductLinePerVersion;
				cumulativeSingularRemovedProductLinePerVersion = isRemovedProductLinePerVersionUpdated ? cumulativeSingularRemovedProductLinePerVersion + 1 : cumulativeSingularRemovedProductLinePerVersion;
				cumulativeSingularAddedGherkinLinePerVersion = isAddedGherkinLinePerVersionUpdated ? cumulativeSingularAddedGherkinLinePerVersion + 1 : cumulativeSingularAddedGherkinLinePerVersion;
				cumulativeSingularRemovedGherkinLinePerVersion = isRemovedGherkinLinePerVersionUpdated ? cumulativeSingularRemovedGherkinLinePerVersion + 1 : cumulativeSingularRemovedGherkinLinePerVersion;
				cumulativeSingularRemovedScenarioPerVersion = isRemovedScenarioPerVersionUpdated ? cumulativeSingularRemovedScenarioPerVersion + 1 : cumulativeSingularRemovedScenarioPerVersion;
				cumulativeSingularRemovedWhenPerVersion = isRemovedWhenPerVersionUpdated ? cumulativeSingularRemovedWhenPerVersion + 1 : cumulativeSingularRemovedWhenPerVersion;
				cumulativeSingularRemovedThenPerVersion = isRemovedThenPerVersionUpdated ? cumulativeSingularRemovedThenPerVersion + 1 : cumulativeSingularRemovedThenPerVersion;
				cumulativeSingularRemovedGivenPerVersion = isRemovedGivenPerVersionUpdated ? cumulativeSingularRemovedGivenPerVersion + 1 : cumulativeSingularRemovedGivenPerVersion;
				cumulativeSingularAddedScenarioPerVersion = isAddedScenarioPerVersionUpdated ? cumulativeSingularAddedScenarioPerVersion + 1 : cumulativeSingularAddedScenarioPerVersion;
				cumulativeSingularAddedWhenPerVersion = isAddedWhenPerVersionUpdated ? cumulativeSingularAddedWhenPerVersion + 1 : cumulativeSingularAddedWhenPerVersion;
				cumulativeSingularAddedThenPerVersion = isAddedThenPerVersionUpdated ? cumulativeSingularAddedThenPerVersion + 1 : cumulativeSingularAddedThenPerVersion;
				cumulativeSingularAddedGivenPerVersion = isAddedGivenPerVersionUpdated ? cumulativeSingularAddedGivenPerVersion + 1 : cumulativeSingularAddedGivenPerVersion;

				isProductionCodeUpdated = isAddedProductLinePerVersionUpdated || isRemovedProductLinePerVersionUpdated;
				isGherkinCodeUpdated = isRemovedGherkinLinePerVersionUpdated || isAddedGherkinLinePerVersionUpdated || isRemovedScenarioPerVersionUpdated || isRemovedWhenPerVersionUpdated || isRemovedThenPerVersionUpdated
						|| isRemovedGivenPerVersionUpdated || isAddedScenarioPerVersionUpdated || isAddedWhenPerVersionUpdated || isAddedThenPerVersionUpdated || isAddedGivenPerVersionUpdated;
				singularUpdatedProductionCodePerVersion = isProductionCodeUpdated ? singularUpdatedProductionCodePerVersion + 1 : singularUpdatedProductionCodePerVersion;
				singularUpdatedGherkinCodePerVersion = isGherkinCodeUpdated ? singularUpdatedGherkinCodePerVersion + 1 : singularUpdatedGherkinCodePerVersion;

				isGherkinNotProduction = !isProductionCodeUpdated && isGherkinCodeUpdated ? isGherkinNotProduction + 1 : isGherkinNotProduction;
				isGherkinAndProduction = isProductionCodeUpdated && isGherkinCodeUpdated ? isGherkinAndProduction + 1 : isGherkinAndProduction;
				isProductionAndNotGherkin = isProductionCodeUpdated && !isGherkinCodeUpdated ? isProductionAndNotGherkin + 1 : isProductionAndNotGherkin;
				isSame = !isProductionCodeUpdated && !isGherkinCodeUpdated ? isSame + 1 : isSame;

				if ("major release".equalsIgnoreCase(getUpdateType(oldVersion, newVersion))) {
					majorVersionCount++;
					majorMinorVersionCount++;
					if (isProductionCodeUpdated) {
						productionCodeUpdatedCountForMajorMinor++;
					}
					if (isGherkinCodeUpdated) {
						testCodeUpdatedCountForMajorMinor++;
					}
				} else if ("minor release".equalsIgnoreCase(getUpdateType(oldVersion, newVersion))) {
					minorVersionCount++;
					majorMinorVersionCount++;
					if (isProductionCodeUpdated) {
						productionCodeUpdatedCountForMajorMinor++;
					}
					if (isGherkinCodeUpdated) {
						testCodeUpdatedCountForMajorMinor++;
					}
				} else if ("patch release".equalsIgnoreCase(getUpdateType(oldVersion, newVersion))) {
					patchVersionCount++;
				}

				Map<String, Object> changeCountForVersion = new HashMap<>();
				changeCountForVersion.put("version", newVersion + " - " + oldVersion);
				changeCountForVersion.put("additionProduct", addedProductLinePerVersion);
				changeCountForVersion.put("removalProduct", removedProductLinePerVersion);
				changeCountForVersion.put("additionGherkin", addedGherkinLinePerVersion);
				changeCountForVersion.put("removalGherkin", removedGherkinLinePerVersion);

				changeCountForVersion.put("addedScenarioPerVersion", addedScenarioPerVersion);
				changeCountForVersion.put("addedGivenPerVersion", addedGivenPerVersion);
				changeCountForVersion.put("addedWhenPerVersion", addedWhenPerVersion);
				changeCountForVersion.put("addedThenPerVersion", addedThenPerVersion);

				changeCountForVersion.put("removedThenPerVersion", removedThenPerVersion);
				changeCountForVersion.put("removedWhenPerVersion", removedWhenPerVersion);
				changeCountForVersion.put("removedGivenPerVersion", removedGivenPerVersion);
				changeCountForVersion.put("removedScenarioPerVersion", removedScenarioPerVersion);

				changeCountForVersion.put("cumulativeAddedProductLinePerVersion", cumulativeAddedProductLinePerVersion);
				changeCountForVersion.put("cumulativeRemovedProductLinePerVersion", cumulativeRemovedProductLinePerVersion);
				changeCountForVersion.put("cumulativeAddedGherkinLinePerVersion", cumulativeAddedGherkinLinePerVersion);
				changeCountForVersion.put("cumulativeRemovedGherkinLinePerVersion", cumulativeRemovedGherkinLinePerVersion);
				changeCountForVersion.put("cumulativeRemovedScenarioPerVersion", cumulativeRemovedScenarioPerVersion);
				changeCountForVersion.put("cumulativeRemovedWhenPerVersion", cumulativeRemovedWhenPerVersion);
				changeCountForVersion.put("cumulativeRemovedThenPerVersion", cumulativeRemovedThenPerVersion);
				changeCountForVersion.put("cumulativeRemovedGivenPerVersion", cumulativeRemovedGivenPerVersion);
				changeCountForVersion.put("cumulativeAddedScenarioPerVersion", cumulativeAddedScenarioPerVersion);
				changeCountForVersion.put("cumulativeAddedWhenPerVersion", cumulativeAddedWhenPerVersion);
				changeCountForVersion.put("cumulativeAddedThenPerVersion", cumulativeAddedThenPerVersion);
				changeCountForVersion.put("cumulativeAddedGivenPerVersion", cumulativeAddedGivenPerVersion);

				changeCountForVersion.put("cumulativeSingularAddedProductLinePerVersion", cumulativeSingularAddedProductLinePerVersion);
				changeCountForVersion.put("cumulativeSingularRemovedProductLinePerVersion", cumulativeSingularRemovedProductLinePerVersion);
				changeCountForVersion.put("cumulativeSingularAddedGherkinLinePerVersion", cumulativeSingularAddedGherkinLinePerVersion);
				changeCountForVersion.put("cumulativeSingularRemovedGherkinLinePerVersion", cumulativeSingularRemovedGherkinLinePerVersion);
				changeCountForVersion.put("cumulativeSingularRemovedScenarioPerVersion", cumulativeSingularRemovedScenarioPerVersion);
				changeCountForVersion.put("cumulativeSingularRemovedWhenPerVersion", cumulativeSingularRemovedWhenPerVersion);
				changeCountForVersion.put("cumulativeSingularRemovedThenPerVersion", cumulativeSingularRemovedThenPerVersion);
				changeCountForVersion.put("cumulativeSingularRemovedGivenPerVersion", cumulativeSingularRemovedGivenPerVersion);
				changeCountForVersion.put("cumulativeSingularAddedScenarioPerVersion", cumulativeSingularAddedScenarioPerVersion);
				changeCountForVersion.put("cumulativeSingularAddedWhenPerVersion", cumulativeSingularAddedWhenPerVersion);
				changeCountForVersion.put("cumulativeSingularAddedThenPerVersion", cumulativeSingularAddedThenPerVersion);
				changeCountForVersion.put("cumulativeSingularAddedGivenPerVersion", cumulativeSingularAddedGivenPerVersion);

				changeCountForVersion.put("singularUpdatedProductionCodePerVersion", singularUpdatedProductionCodePerVersion);
				changeCountForVersion.put("singularUpdatedGherkinCodePerVersion", singularUpdatedGherkinCodePerVersion);

				changeCountForVersion.put("testToProductionCodeForVersion", Double.valueOf(singularUpdatedGherkinCodePerVersion) / Double.valueOf(singularUpdatedProductionCodePerVersion));
				changeCountForVersion.put("testToAllCodeForVersion", Double.valueOf(singularUpdatedGherkinCodePerVersion) / Double.valueOf(i + 1));
				changeCountForVersion.put("endVersion", oldVersion);
				changeCountForVersion.put("startVersion", newVersion);
				changeCountForVersion.put("updateType", getUpdateType(oldVersion, newVersion));
				changeCountForVersion.put("formattedVersionDifference", getFormattedVersionDifference(oldVersion, newVersion));
				changeCountForVersion.put("formattedStartVersion", getFormattedVersion(newVersion));
				changeCountForVersion.put("formattedEndVersion", getFormattedVersion(oldVersion));

				changeCountForVersion.put("isGherkinNotProduction", isGherkinNotProduction);
				changeCountForVersion.put("isGherkinAndProduction", isGherkinAndProduction);
				changeCountForVersion.put("isProductionNotGherkin", isProductionAndNotGherkin);
				changeCountForVersion.put("isSame", isSame);
				changeCountForVersion.put("isGherkinNotProductionPercentage", Double.valueOf(isGherkinNotProduction) / (i + 1.0));
				changeCountForVersion.put("isGherkinAndProductionPercentage", Double.valueOf(isGherkinAndProduction) / (i + 1.0));
				changeCountForVersion.put("isProductionNotGherkinPercentage", Double.valueOf(isProductionAndNotGherkin) / (i + 1.0));
				changeCountForVersion.put("isSamePercentage", Double.valueOf(isSame) / (i + 1.0));

				changeCountForVersion.put("totalELOC", TOTAL_ELOC);
				changeCountForVersion.put("totalTLOC", TOTAL_TLOC);

				changeCountForVersion.put("majorVersionCount", majorVersionCount);
				changeCountForVersion.put("minorVersionCount", minorVersionCount);
				changeCountForVersion.put("patchVersionCount", patchVersionCount);
				changeCountForVersion.put("majorMinorVersionCount", majorMinorVersionCount);
				changeCountForVersion.put("totalVersionCount", i);

				changeCountForVersion.put("totalChangedFileCount", totalChangedFileCount);

				changeCountForVersion.put("productionUpdated", isProductionCodeUpdated ? 1 : 0);
				changeCountForVersion.put("gherkinUpdated", isGherkinCodeUpdated ? 1 : 0);

				changeCountForVersion.put("productionCodeUpdatedForMajorMinor", productionCodeUpdatedCountForMajorMinor);
				changeCountForVersion.put("testCodeUpdatedForMajorMinor", testCodeUpdatedCountForMajorMinor);

				versionLineChangeMapList.add(changeCountForVersion);
			}
			ProjectCommitData pcd = new ProjectCommitData();
			pcd.setFileChangeList(fileChangeList);
			ExcelUtil.createFileChangeExcel(fileChangeList, versionLineChangeMapList);
			projectCommitDataList.add(pcd);

			driver.quit();

			myWriter.close();
			// ExcelUtil.createGherkidDataExcel(repositoryList, "Projects");
		}
		return projectCommitDataList;
	}

	private String getFormattedVersion(String newVersion) {
		List<Integer> newVersionDotIndexList = new ArrayList<>();
		VersionType version = getVersionTypeFromVersionString(newVersion, newVersionDotIndexList);
		if (version.getVersionParseError().equals("")) {
			return version.getMajorVersion() + "." + version.getMinorVersion() + "." + version.getMinorestVersion();
		}
		return newVersion;
	}

	private String getFormattedVersionDifference(String oldVersion, String newVersion) {
		List<Integer> newVersionDotIndexList = new ArrayList<>();
		List<Integer> oldVersionDotIndexList = new ArrayList<>();
		VersionType oldVersionType = getVersionTypeFromVersionString(oldVersion, newVersionDotIndexList);
		VersionType newVersionType = getVersionTypeFromVersionString(newVersion, oldVersionDotIndexList);
		if (oldVersionType.getVersionParseError().equals("") && newVersionType.getVersionParseError().equals("")) {
			return oldVersionType.getMajorVersion() + "." + oldVersionType.getMinorVersion() + "." + oldVersionType.getMinorestVersion() + " - " + newVersionType.getMajorVersion() + "." + newVersionType.getMinorVersion() + "."
					+ newVersionType.getMinorestVersion();
		}
		return oldVersion + " - " + newVersion;
	}

	private static String getUpdateType(String oldVersion, String newVersion) {
		String updateType = "unsupported versions";
		List<Integer> newVersionDotIndexList = new ArrayList<>();
		List<Integer> oldVersionDotIndexList = new ArrayList<>();
		VersionType oldVersionType = getVersionTypeFromVersionString(oldVersion, newVersionDotIndexList);
		VersionType newVersionType = getVersionTypeFromVersionString(newVersion, oldVersionDotIndexList);
		System.out.println(oldVersionType);
		System.out.println(newVersionType);
		if (oldVersionType.getVersionParseError().equals("") && newVersionType.getVersionParseError().equals("")) {
			if (!newVersionType.getMajorVersion().equals(oldVersionType.getMajorVersion())) {
				updateType = "major release";
			} else if (!newVersionType.getMinorVersion().equals(oldVersionType.getMinorVersion())) {
				updateType = "minor release";
			} else if (!newVersionType.getMinorestVersion().equals(oldVersionType.getMinorestVersion())) {
				updateType = "patch release";
			} else {
				updateType = "same version?";
			}
		}
		return updateType;
	}

	private static VersionType getVersionTypeFromVersionString(String version, List<Integer> dotIndexList) {
		String versionParseError = "";
		VersionType versionType = new VersionType();
		version = version.replaceAll("[^\\d.]", "");
		if (!version.isBlank()) {
			if (version.charAt(0) == '.') {
				version = version.substring(1);
			}
			if (version.charAt(version.length() - 1) == '.') {
				version = version.substring(0, version.length() - 2);
			}
			for (int i = 0; i < version.length(); i++) {
				if (version.charAt(i) == '.') {
					dotIndexList.add(i);
				}
			}
			if (!dotIndexList.isEmpty()) {
				if ((dotIndexList.get(0) == 0 || dotIndexList.get(0) == version.length()) || (dotIndexList.size() > 1 && (dotIndexList.get(1) == 0 || dotIndexList.get(1) == version.length()))) {
					versionParseError = "first or last character is a dot";
				} else {
					if (dotIndexList.size() > 1 && Math.abs(dotIndexList.get(0) - dotIndexList.get(1)) == 1) {
						versionParseError = "adjacent dots";
					} else {
						String[] splittedVersions = version.split("\\.");
						String majorVersion = splittedVersions.length > 0 ? splittedVersions[0] : "0";
						String minorVersion = splittedVersions.length > 1 ? splittedVersions[1] : "0";
						String minorestVersion = splittedVersions.length > 2 ? splittedVersions[2] : "0";
						versionType = new VersionType(Integer.parseInt(majorVersion), Integer.parseInt(minorVersion), Integer.parseInt(minorestVersion), versionParseError);
					}
				}
			} else {
				versionParseError = "no dots";
			}
		} else {
			versionParseError = "unsupported version";
		}
		versionType.setVersionParseError(versionParseError);
		return versionType;
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

	public static void showFiles(File[] files) {
		try {
			for (File file : files) {
				if (file.isDirectory()) {
					System.out.println("Directory: " + file.getAbsolutePath());
					showFiles(file.listFiles());
				} else {
					if (file.getName().endsWith(".feature")) {
						TOTAL_TLOC += (int) Files.lines(file.toPath()).count();
					} else {
						TOTAL_ELOC += (int) Files.lines(file.toPath()).count();
					}
					System.out.println("File: " + file.getAbsolutePath());
				}
			}
		} catch (IOException | java.io.UncheckedIOException e) {

		}
	}
}
