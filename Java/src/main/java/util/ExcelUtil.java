package util;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import models.FileChange;
import models.Repository;

public class ExcelUtil {

	public static final String RESOURCES_FOLDER = "resources\\";

	public static void createFileChangeExcel(List<FileChange> fileChangeList, String postfix) throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			CreationHelper helper = wb.getCreationHelper();
			String sheetName = !isEmpty(fileChangeList) ? fileChangeList.get(0).getProjectName() + postfix : "Redacted";
			XSSFSheet sheet = wb.createSheet(sheetName);

			XSSFFont linkFont = wb.createFont();
			linkFont.setUnderline(XSSFFont.U_SINGLE);
			linkFont.setColor(IndexedColors.BLUE.getIndex());

			XSSFCellStyle slightlyGreenCellStyle = wb.createCellStyle();
			java.awt.Color slightlyGreen = new java.awt.Color(175, 255, 175);
			slightlyGreenCellStyle.setFont(linkFont);
			slightlyGreenCellStyle.setFillForegroundColor(new XSSFColor(slightlyGreen, new DefaultIndexedColorMap()));
			slightlyGreenCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle slightlyRedCellStyle = wb.createCellStyle();
			java.awt.Color slightlyRed = new java.awt.Color(255, 150, 150);
			slightlyRedCellStyle.setFont(linkFont);
			slightlyRedCellStyle.setFillForegroundColor(new XSSFColor(slightlyRed, new DefaultIndexedColorMap()));
			slightlyRedCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle greenCellStyle = wb.createCellStyle();
			java.awt.Color green = new java.awt.Color(0, 255, 0);
			greenCellStyle.setFont(linkFont);
			greenCellStyle.setFillForegroundColor(new XSSFColor(green, new DefaultIndexedColorMap()));
			greenCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle redCellStyle = wb.createCellStyle();
			java.awt.Color red = new java.awt.Color(255, 0, 0);
			redCellStyle.setFont(linkFont);
			redCellStyle.setFillForegroundColor(new XSSFColor(red, new DefaultIndexedColorMap()));
			redCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle yellowCellStyle = wb.createCellStyle();
			java.awt.Color yellow = new java.awt.Color(255, 255, 0);
			yellowCellStyle.setFont(linkFont);
			yellowCellStyle.setFillForegroundColor(new XSSFColor(yellow, new DefaultIndexedColorMap()));
			yellowCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle transparentCellStyle = wb.createCellStyle();
			java.awt.Color transaprent = new Color(255, 255, 255, 0);
			transparentCellStyle.setFont(linkFont);
			transparentCellStyle.setFillForegroundColor(new XSSFColor(transaprent, new DefaultIndexedColorMap()));
			transparentCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			List<String> versionList = new ArrayList<>();
			Set<String> fileSet = new HashSet<>();
			for (FileChange fileChange : fileChangeList) {
				if (!versionList.contains(fileChange.getEndVersion())) {
					versionList.add(fileChange.getEndVersion());
				}
				if (!versionList.contains(fileChange.getStartingVersion())) {
					versionList.add(fileChange.getStartingVersion());
				}
				fileSet.add(fileChange.getFileName());
			}
			Integer columnCount = versionList.size();
			Integer rowCount = fileSet.size();

			Row versionRow = sheet.createRow(0);
			Cell cell;
			Collections.reverse(versionList);
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				cell = versionRow.createCell((short) columnIndex + 1);
				cell.setCellValue(versionList.get(columnIndex));
			}
			List<String> fileList = new ArrayList<>(fileSet);
			for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
				Row fileRow = sheet.createRow(rowIndex + 1);
				cell = fileRow.createCell(0);
				cell.setCellValue(fileList.get(rowIndex));
				cell = sheet.getRow(rowIndex + 1).createCell(1);
				cell.setCellStyle(greenCellStyle);
				cell.setCellValue("Created");
			}
			List<Integer> createdFileRowIndexList = new ArrayList<>();
			Set<String> fileNameSet = new HashSet<>();
			for (FileChange fileChange : fileChangeList) {
				Integer columnIndex = versionList.indexOf(fileChange.getEndVersion()) + 1;
				Integer rowIndex = fileList.indexOf(fileChange.getFileName()) + 1;
				cell = sheet.getRow(rowIndex).createCell(columnIndex);
				XSSFHyperlink link = (XSSFHyperlink) helper.createHyperlink(HyperlinkType.URL);
				link.setAddress(fileChange.getCommitUrl());
				cell.setHyperlink(link);
				cell.setCellValue(fileChange.getChangeSummary());
				if (fileChange.getIsFileDeleted().equals("true")) {
					cell.setCellStyle(redCellStyle);
				} else if (fileChange.getIsFileCreated()) {
					cell.setCellStyle(greenCellStyle);
					createdFileRowIndexList.add(rowIndex);
				} else if (fileChange.getAdditionSummaryCount() > 0 && fileChange.getDeletionSummaryCount() == 0) {
					cell.setCellStyle(slightlyGreenCellStyle);
				} else if (fileChange.getAdditionSummaryCount() == 0 && fileChange.getDeletionSummaryCount() > 0) {
					cell.setCellStyle(slightlyRedCellStyle);
				} else if (fileChange.getAdditionSummaryCount() > 0 && fileChange.getDeletionSummaryCount() > 0) {
					cell.setCellStyle(yellowCellStyle);
				}
				fileNameSet.add(fileChange.getFileName());
			}
			for (Integer createdFileRowIndex : createdFileRowIndexList) {
				cell = sheet.getRow(createdFileRowIndex).getCell(1);
				cell.setCellStyle(null);
				cell.setCellValue("");
			}
			for (int i = 0; i < columnCount + 1; i++) {
				sheet.autoSizeColumn(i);
			}
			try (FileOutputStream fileOut = new FileOutputStream(!fileChangeList.isEmpty() ? fileChangeList.get(0).getProjectName() + ".xlsx" : "redacted" + Math.random() + ".xlsx")) {
				wb.write(fileOut);
			}
		}
	}

	public static void createRepositoryExcel(List<Repository> repositoryList, String fileName) throws IOException {
		try (Workbook wb = new XSSFWorkbook()) {
			Sheet sheet = wb.createSheet("new sheet");
			Row row = sheet.createRow(0);

			row.createCell(0).setCellValue("#");
			row.createCell(1).setCellValue("Name");
			row.createCell(2).setCellValue("URL");
			row.createCell(3).setCellValue("Star Count");
			row.createCell(4).setCellValue("Tag Count");
			row.createCell(5).setCellValue("Issues");

			List<String> uniqueLanguages = getUniqueLanguagesFromMap(repositoryList);
			Integer index = 6;
			for (String uniqueLanguage : uniqueLanguages) {
				row.createCell(index).setCellValue(uniqueLanguage);
				index++;
			}

			row.createCell(index).setCellValue("--");
			index++;

			for (String uniqueLanguage : uniqueLanguages) {
				row.createCell(index).setCellValue(uniqueLanguage);
				index++;
			}
			Integer id = 1;
			for (Repository repository : repositoryList) {
				insertProductRow(sheet, repository, id, uniqueLanguages);
				id++;
			}
			try (OutputStream fileOut = new FileOutputStream(fileName + ".xlsx")) {
				wb.write(fileOut);
			}
		}
	}

	private static void insertProductRow(Sheet sheet, Repository repository, Integer id, List<String> uniqueLanguages) {
		Row dataRow = sheet.createRow(id);
		dataRow.createCell(0).setCellValue(id);
		dataRow.createCell(1).setCellValue(repository.getName());
		dataRow.createCell(2).setCellValue(repository.getUrl());
		dataRow.createCell(3).setCellValue(repository.getStarCount());
		dataRow.createCell(4).setCellValue(repository.getTags());
		dataRow.createCell(5).setCellValue(repository.getIssues());
		Integer index = 6;
		List<String> languages = new ArrayList<>(repository.getLanguageFileCountMap().keySet());
		for (String language : languages) {
			dataRow.createCell(index + uniqueLanguages.indexOf(language)).setCellValue(repository.getLanguageFileCountMap().get(language));
		}
		index += uniqueLanguages.size() + 1;
		for (String language : languages) {
			dataRow.createCell(index + uniqueLanguages.indexOf(language)).setCellValue(repository.getLanguagePercentageMap().get(language));
		}
	}

	private static List<String> getUniqueLanguagesFromMap(List<Repository> repositoryList) {
		Set<String> uniqueLanguages = new HashSet<>();
		for (Repository repository : repositoryList) {
			uniqueLanguages.addAll(repository.getLanguageFileCountMap().keySet());
		}
		return new ArrayList<>(uniqueLanguages);
	}

	private static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	public static void createFileChangeExcel(List<FileChange> fileChangeList, List<Map<String, Object>> versionLineChangeMapList) throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			CreationHelper helper = wb.getCreationHelper();
			XSSFSheet sheet = wb.createSheet("All Files History");
			XSSFSheet sheet2 = wb.createSheet("All Specific");
			XSSFSheet sheet3 = wb.createSheet("Singular Specific");
			XSSFSheet sheet4 = wb.createSheet("Singular Test - Production");
			XSSFSheet sheet5 = wb.createSheet("All Version Test Percentage");
			XSSFSheet sheet6 = wb.createSheet("Major Minor Test Percentage");
			XSSFSheet sheet7 = wb.createSheet("Total TLOC - ELOC");
			XSSFSheet sheet8 = wb.createSheet("Production - Gherkin Correlation");

			XSSFFont linkFont = wb.createFont();
			linkFont.setUnderline(XSSFFont.U_SINGLE);
			linkFont.setColor(IndexedColors.BLUE.getIndex());

			XSSFCellStyle slightlyGreenCellStyle = wb.createCellStyle();
			java.awt.Color slightlyGreen = new java.awt.Color(175, 255, 175);
			slightlyGreenCellStyle.setFont(linkFont);
			slightlyGreenCellStyle.setFillForegroundColor(new XSSFColor(slightlyGreen, new DefaultIndexedColorMap()));
			slightlyGreenCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle slightlyRedCellStyle = wb.createCellStyle();
			java.awt.Color slightlyRed = new java.awt.Color(255, 150, 150);
			slightlyRedCellStyle.setFont(linkFont);
			slightlyRedCellStyle.setFillForegroundColor(new XSSFColor(slightlyRed, new DefaultIndexedColorMap()));
			slightlyRedCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle greenCellStyle = wb.createCellStyle();
			java.awt.Color green = new java.awt.Color(0, 255, 0);
			greenCellStyle.setFont(linkFont);
			greenCellStyle.setFillForegroundColor(new XSSFColor(green, new DefaultIndexedColorMap()));
			greenCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle redCellStyle = wb.createCellStyle();
			java.awt.Color red = new java.awt.Color(255, 0, 0);
			redCellStyle.setFont(linkFont);
			redCellStyle.setFillForegroundColor(new XSSFColor(red, new DefaultIndexedColorMap()));
			redCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle yellowCellStyle = wb.createCellStyle();
			java.awt.Color yellow = new java.awt.Color(255, 255, 0);
			yellowCellStyle.setFont(linkFont);
			yellowCellStyle.setFillForegroundColor(new XSSFColor(yellow, new DefaultIndexedColorMap()));
			yellowCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle transparentCellStyle = wb.createCellStyle();
			java.awt.Color transaprent = new Color(255, 255, 255, 0);
			transparentCellStyle.setFont(linkFont);
			transparentCellStyle.setFillForegroundColor(new XSSFColor(transaprent, new DefaultIndexedColorMap()));
			transparentCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			List<String> versionList = new ArrayList<>();
			Set<String> fileSet = new HashSet<>();
			for (FileChange fileChange : fileChangeList) {
				if (!versionList.contains(fileChange.getEndVersion())) {
					versionList.add(fileChange.getEndVersion());
				}
				if (!versionList.contains(fileChange.getStartingVersion())) {
					versionList.add(fileChange.getStartingVersion());
				}
				fileSet.add(fileChange.getFileName());
			}
			Integer columnCount = versionList.size();
			Integer rowCount = fileSet.size();

			Row versionRow = sheet.createRow(0);
			Row sheet2VersionRow = sheet2.createRow(0);
			Row sheet3VersionRow = sheet3.createRow(0);
			Row sheet5VersionRow = sheet5.createRow(0);
			Row sheet6VersionRow = sheet6.createRow(0);
			Row sheet7VersionRow = sheet7.createRow(0);
			Cell cell = null;
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				cell = sheet2VersionRow.createCell((short) columnIndex + 1);
				cell.setCellValue(versionList.get(columnIndex));
				cell = versionRow.createCell((short) columnIndex + 1);
				cell.setCellValue(versionList.get(columnIndex));
				cell = sheet3VersionRow.createCell((short) columnIndex + 1);
				cell.setCellValue(versionList.get(columnIndex));
				cell = sheet5VersionRow.createCell((short) columnIndex + 1);
				cell.setCellValue(versionList.get(columnIndex));
				cell = sheet6VersionRow.createCell((short) columnIndex + 1);
				cell.setCellValue(versionList.get(columnIndex));
				cell = sheet7VersionRow.createCell((short) columnIndex + 1);
				cell.setCellValue(versionList.get(columnIndex));
			}

			Row sheet4VersionRow = sheet4.createRow(0);
			cell = sheet4VersionRow.createCell((short) 0);
			for (int versionColumnIndex = 0; versionColumnIndex < columnCount - 1; versionColumnIndex++) {
				cell = sheet4VersionRow.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("formattedVersionDifference")));
			}

			List<String> fileList = new ArrayList<>(fileSet);
			for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
				Row fileRow = sheet.createRow(rowIndex + 1);
				cell = fileRow.createCell(0);
				cell.setCellValue(fileList.get(rowIndex));
				cell = sheet.getRow(rowIndex + 1).createCell(1);
				cell.setCellStyle(greenCellStyle);
				cell.setCellValue("Created");
			}
			List<Integer> createdFileRowIndexList = new ArrayList<>();
			Set<String> fileNameSet = new HashSet<>();
			for (FileChange fileChange : fileChangeList) {
				Integer columnIndex = versionList.indexOf(fileChange.getEndVersion()) + 1;
				Integer rowIndex = fileList.indexOf(fileChange.getFileName()) + 1;
				cell = sheet.getRow(rowIndex).createCell(columnIndex);
				XSSFHyperlink link = (XSSFHyperlink) helper.createHyperlink(HyperlinkType.URL);
				link.setAddress(fileChange.getCommitUrl());
				cell.setHyperlink(link);
				cell.setCellValue(fileChange.getChangeSummary());
				if (fileChange.getIsFileDeleted().equals("true")) {
					cell.setCellStyle(redCellStyle);
				} else if (fileChange.getIsFileCreated()) {
					cell.setCellStyle(greenCellStyle);
					createdFileRowIndexList.add(rowIndex);
				} else if (fileChange.getAdditionSummaryCount() > 0 && fileChange.getDeletionSummaryCount() == 0) {
					cell.setCellStyle(slightlyGreenCellStyle);
				} else if (fileChange.getAdditionSummaryCount() == 0 && fileChange.getDeletionSummaryCount() > 0) {
					cell.setCellStyle(slightlyRedCellStyle);
				} else if (fileChange.getAdditionSummaryCount() > 0 && fileChange.getDeletionSummaryCount() > 0) {
					cell.setCellStyle(yellowCellStyle);
				}
				fileNameSet.add(fileChange.getFileName());

			}

			int i = 2;
			for (int m = 0; m < 300; m++) {
				Row sheet3Row = sheet3.createRow(m + 1);
				Row sheet4Row = sheet4.createRow(m + 1);
				Row row = sheet2.createRow(m + 1);
				Row sheet5Row = sheet5.createRow(m + 1);
				Row sheet6Row = sheet6.createRow(m + 1);
				Row sheet7Row = sheet7.createRow(m + 1);
				Row sheet8Row = sheet8.createRow(m + 1);
				for (int k = 0; k < columnCount; k++) {
					row.createCell((short) i);
					sheet3Row.createCell((short) i);
					sheet4Row.createCell((short) i);
					sheet5Row.createCell((short) i);
					sheet6Row.createCell((short) i);
					sheet7Row.createCell((short) i);
					sheet8Row.createCell((short) i);
				}
			}

			Integer majorMinorCount = 0;
			for (Map<String, Object> versionFileChangeCount : versionLineChangeMapList) {
				cell = sheet2.getRow(35 + 1).createCell(i);
				cell.setCellValue("Product line additions: " + versionFileChangeCount.get("additionProduct"));
				cell = sheet2.getRow(35 + 2).createCell(i);
				cell.setCellValue("Product line removals: " + versionFileChangeCount.get("removalProduct"));
				cell = sheet2.getRow(35 + 3).createCell(i);
				cell.setCellValue("Gherkin line additions: " + versionFileChangeCount.get("additionGherkin"));
				cell = sheet2.getRow(35 + 4).createCell(i);
				cell.setCellValue("Gherkin line removals: " + versionFileChangeCount.get("removalGherkin"));

				cell = sheet2.getRow(35 + 5).createCell(i);
				cell.setCellValue("Added scenario: " + versionFileChangeCount.get("addedScenarioPerVersion"));
				cell = sheet2.getRow(35 + 6).createCell(i);
				cell.setCellValue("Added given: " + versionFileChangeCount.get("addedGivenPerVersion"));
				cell = sheet2.getRow(35 + 7).createCell(i);
				cell.setCellValue("Added when: " + versionFileChangeCount.get("addedWhenPerVersion"));
				cell = sheet2.getRow(35 + 8).createCell(i);
				cell.setCellValue("Added then: " + versionFileChangeCount.get("addedThenPerVersion"));

				cell = sheet2.getRow(35 + 9).createCell(i);
				cell.setCellValue("Removed then: " + versionFileChangeCount.get("removedThenPerVersion"));
				cell = sheet2.getRow(35 + 10).createCell(i);
				cell.setCellValue("Removed when: " + versionFileChangeCount.get("removedWhenPerVersion"));
				cell = sheet2.getRow(35 + 11).createCell(i);
				cell.setCellValue("Removed given: " + versionFileChangeCount.get("removedGivenPerVersion"));
				cell = sheet2.getRow(35 + 12).createCell(i);
				cell.setCellValue("Removed scenario: " + versionFileChangeCount.get("removedScenarioPerVersion"));

				cell = sheet2.getRow(35 + 14).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeAddedProductLinePerVersion"))));
				cell = sheet2.getRow(35 + 15).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeRemovedProductLinePerVersion"))));
				cell = sheet2.getRow(35 + 16).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeAddedGherkinLinePerVersion"))));
				cell = sheet2.getRow(35 + 17).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeRemovedGherkinLinePerVersion"))));

				cell = sheet2.getRow(35 + 18).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeAddedScenarioPerVersion"))));
				cell = sheet2.getRow(35 + 19).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeAddedGivenPerVersion"))));
				cell = sheet2.getRow(35 + 20).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeAddedWhenPerVersion"))));
				cell = sheet2.getRow(35 + 21).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeAddedThenPerVersion"))));

				cell = sheet2.getRow(35 + 22).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeRemovedThenPerVersion"))));
				cell = sheet2.getRow(35 + 23).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeRemovedWhenPerVersion"))));
				cell = sheet2.getRow(35 + 24).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeRemovedGivenPerVersion"))));
				cell = sheet2.getRow(35 + 25).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeRemovedScenarioPerVersion"))));

				cell = sheet3.getRow(35 + 1).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeSingularAddedProductLinePerVersion"))));
				cell = sheet3.getRow(35 + 2).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeSingularRemovedProductLinePerVersion"))));
				cell = sheet3.getRow(35 + 3).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeSingularAddedGherkinLinePerVersion"))));
				cell = sheet3.getRow(35 + 4).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeSingularRemovedGherkinLinePerVersion"))));

				cell = sheet3.getRow(35 + 5).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeSingularAddedScenarioPerVersion"))));
				cell = sheet3.getRow(35 + 6).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeSingularAddedWhenPerVersion"))));
				cell = sheet3.getRow(35 + 7).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeSingularAddedThenPerVersion"))));
				cell = sheet3.getRow(35 + 8).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeSingularAddedGivenPerVersion"))));

				cell = sheet3.getRow(35 + 9).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeSingularRemovedScenarioPerVersion"))));
				cell = sheet3.getRow(35 + 10).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeSingularRemovedWhenPerVersion"))));
				cell = sheet3.getRow(35 + 11).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeSingularRemovedThenPerVersion"))));
				cell = sheet3.getRow(35 + 12).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("cumulativeSingularRemovedGivenPerVersion"))));

				cell = sheet4.getRow(35 + 2).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("singularUpdatedProductionCodePerVersion"))));
				cell = sheet4.getRow(35 + 3).createCell(i);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("singularUpdatedGherkinCodePerVersion"))));

				if (String.valueOf(versionFileChangeCount.get("updateType")).equalsIgnoreCase("major release") || String.valueOf(versionFileChangeCount.get("updateType")).equalsIgnoreCase("minor release")) {
					cell = sheet8.getRow(majorMinorCount + 1).createCell(0);
					cell.setCellValue(String.valueOf(versionFileChangeCount.get("version")));
					cell = sheet8.getRow(majorMinorCount + 1).createCell(1);
					cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("productionUpdated"))));
					cell = sheet8.getRow(majorMinorCount + 1).createCell(2);
					cell.setCellValue(Double.parseDouble(String.valueOf(versionFileChangeCount.get("gherkinUpdated"))));
					cell = sheet8.getRow(majorMinorCount + 1).createCell(3);
					cell.setCellValue(String.valueOf(versionFileChangeCount.get("updateType")));

					majorMinorCount++;
				}
				cell = sheet8.getRow(majorMinorCount).createCell(4);
				cell.setCellValue(String.valueOf(versionFileChangeCount.get("totalVersionCount")));
				i++;
			}
			cell = sheet8.getRow(majorMinorCount + 1).createCell(0);
			cell.setCellFormula("COUNTA(A2:A" + (majorMinorCount + 1) + ")");
			cell = sheet8.getRow(majorMinorCount + 1).createCell(1);
			cell.setCellFormula("SUM(B2:B" + (majorMinorCount + 1) + ")");
			cell = sheet8.getRow(majorMinorCount + 1).createCell(2);
			cell.setCellFormula("SUM(C2:C" + (majorMinorCount + 1) + ")");

			cell = sheet8.getRow(majorMinorCount + 2).createCell(1);
			cell.setCellFormula("B" + (majorMinorCount + 2) + "/A" + (majorMinorCount + 2));
			cell = sheet8.getRow(majorMinorCount + 2).createCell(2);
			cell.setCellFormula("C" + (majorMinorCount + 2) + "/A" + (majorMinorCount + 2));
			cell = sheet8.getRow(0).createCell(1);
			cell.setCellValue("Production update");
			cell = sheet8.getRow(0).createCell(2);
			cell.setCellValue("Test update");
			cell = sheet2.getRow(35 + 13).createCell(1);
			cell.setCellValue("Cumulatives:");
			cell = sheet4.getRow(35 + 1).createCell(1);
			cell.setCellValue("Cumulatives of production and test:");
			for (Integer createdFileRowIndex : createdFileRowIndexList) {
				cell = sheet.getRow(createdFileRowIndex).getCell(1);
				cell.setCellStyle(null);
				cell.setCellValue("");
			}

			XSSFDrawing drawing = sheet2.createDrawingPatriarch();
			XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 2, 12, 32);

			XSSFChart chart = drawing.createChart(anchor);
			chart.setTitleText("Specific production and test code updates");
			chart.setTitleOverlay(false);

			XDDFChartLegend legend = chart.getOrAddLegend();
			legend.setPosition(LegendPosition.TOP_RIGHT);

			XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
			bottomAxis.setTitle("Versions");
			XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
			leftAxis.setTitle("Code updates");

			XDDFDataSource<String> versions = XDDFDataSourcesFactory.fromStringCellRange(sheet2, new CellRangeAddress(0, 0, 2, versionList.size()));

			XDDFNumericalDataSource<Double> productLineAdditions = XDDFDataSourcesFactory.fromNumericCellRange(sheet2, new CellRangeAddress(35 + 14, 35 + 14, 2, versionList.size()));

			XDDFNumericalDataSource<Double> productLineRemovals = XDDFDataSourcesFactory.fromNumericCellRange(sheet2, new CellRangeAddress(35 + 15, 35 + 15, 2, versionList.size()));

			XDDFNumericalDataSource<Double> gherkinLineAdditions = XDDFDataSourcesFactory.fromNumericCellRange(sheet2, new CellRangeAddress(35 + 16, 35 + 16, 2, versionList.size()));

			XDDFNumericalDataSource<Double> gherkinLineRemovals = XDDFDataSourcesFactory.fromNumericCellRange(sheet2, new CellRangeAddress(35 + 17, 35 + 17, 2, versionList.size()));

			XDDFNumericalDataSource<Double> scenarioAdditions = XDDFDataSourcesFactory.fromNumericCellRange(sheet2, new CellRangeAddress(35 + 18, 35 + 18, 2, versionList.size()));

			XDDFNumericalDataSource<Double> givenAdditions = XDDFDataSourcesFactory.fromNumericCellRange(sheet2, new CellRangeAddress(35 + 19, 35 + 19, 2, versionList.size()));

			XDDFNumericalDataSource<Double> whenAdditions = XDDFDataSourcesFactory.fromNumericCellRange(sheet2, new CellRangeAddress(35 + 20, 35 + 20, 2, versionList.size()));

			XDDFNumericalDataSource<Double> thenAdditions = XDDFDataSourcesFactory.fromNumericCellRange(sheet2, new CellRangeAddress(35 + 21, 35 + 21, 2, versionList.size()));

			XDDFNumericalDataSource<Double> thenRemovals = XDDFDataSourcesFactory.fromNumericCellRange(sheet2, new CellRangeAddress(35 + 22, 35 + 22, 2, versionList.size()));

			XDDFNumericalDataSource<Double> whenRemovals = XDDFDataSourcesFactory.fromNumericCellRange(sheet2, new CellRangeAddress(35 + 23, 35 + 23, 2, versionList.size()));

			XDDFNumericalDataSource<Double> givenRemovals = XDDFDataSourcesFactory.fromNumericCellRange(sheet2, new CellRangeAddress(35 + 24, 35 + 24, 2, versionList.size()));

			XDDFNumericalDataSource<Double> scenarioRemovals = XDDFDataSourcesFactory.fromNumericCellRange(sheet2, new CellRangeAddress(35 + 25, 35 + 25, 2, versionList.size()));

			XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

			XDDFLineChartData.Series productLineAdditionSeries = (XDDFLineChartData.Series) data.addSeries(versions, productLineAdditions);
			productLineAdditionSeries.setTitle("productLineAdditions", null);
			productLineAdditionSeries.setSmooth(false);
			productLineAdditionSeries.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series productLineRemovalSeries = (XDDFLineChartData.Series) data.addSeries(versions, productLineRemovals);
			productLineRemovalSeries.setTitle("productLineRemovals", null);
			productLineRemovalSeries.setSmooth(false);
			productLineRemovalSeries.setMarkerSize((short) 6);
			productLineRemovalSeries.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series gherkinLineAdditionSeries = (XDDFLineChartData.Series) data.addSeries(versions, gherkinLineAdditions);
			gherkinLineAdditionSeries.setTitle("gherkinLineAdditions", null);
			gherkinLineAdditionSeries.setSmooth(false);
			gherkinLineAdditionSeries.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series gherkinLineRemovalSeries = (XDDFLineChartData.Series) data.addSeries(versions, gherkinLineRemovals);
			gherkinLineRemovalSeries.setTitle("gherkinLineRemovals", null);
			gherkinLineRemovalSeries.setSmooth(false);
			gherkinLineRemovalSeries.setMarkerSize((short) 6);
			gherkinLineRemovalSeries.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series scenarioAdditionSeries = (XDDFLineChartData.Series) data.addSeries(versions, scenarioAdditions);
			scenarioAdditionSeries.setTitle("scenarioAdditions", null);
			scenarioAdditionSeries.setSmooth(false);
			scenarioAdditionSeries.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series givenAdditionSeries = (XDDFLineChartData.Series) data.addSeries(versions, givenAdditions);
			givenAdditionSeries.setTitle("givenAdditions", null);
			givenAdditionSeries.setSmooth(false);
			givenAdditionSeries.setMarkerSize((short) 6);
			givenAdditionSeries.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series whenAdditionSeries = (XDDFLineChartData.Series) data.addSeries(versions, whenAdditions);
			whenAdditionSeries.setTitle("whenAdditions", null);
			whenAdditionSeries.setSmooth(false);
			whenAdditionSeries.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series thenAdditionSeries = (XDDFLineChartData.Series) data.addSeries(versions, thenAdditions);
			thenAdditionSeries.setTitle("thenAdditions", null);
			thenAdditionSeries.setSmooth(false);
			thenAdditionSeries.setMarkerSize((short) 6);
			thenAdditionSeries.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series thenRemovalSeries = (XDDFLineChartData.Series) data.addSeries(versions, thenRemovals);
			thenRemovalSeries.setTitle("thenRemovals", null);
			thenRemovalSeries.setSmooth(false);
			thenRemovalSeries.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series whenRemovalSeries = (XDDFLineChartData.Series) data.addSeries(versions, whenRemovals);
			whenRemovalSeries.setTitle("whenRemovals", null);
			whenRemovalSeries.setSmooth(false);
			whenRemovalSeries.setMarkerSize((short) 6);
			whenRemovalSeries.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series givenRemovalSeries = (XDDFLineChartData.Series) data.addSeries(versions, givenRemovals);
			givenRemovalSeries.setTitle("givenRemovals", null);
			givenRemovalSeries.setSmooth(false);
			givenRemovalSeries.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series scenarioRemovalSeries = (XDDFLineChartData.Series) data.addSeries(versions, scenarioRemovals);
			scenarioRemovalSeries.setTitle("scenarioRemovals", null);
			scenarioRemovalSeries.setSmooth(false);
			scenarioRemovalSeries.setMarkerSize((short) 6);
			scenarioRemovalSeries.setMarkerStyle(MarkerStyle.NONE);

			chart.plot(data);

			XSSFDrawing drawingSingular = sheet3.createDrawingPatriarch();
			XSSFClientAnchor anchorSingular = drawingSingular.createAnchor(0, 0, 0, 0, 1, 2, 33, 32);

			XSSFChart chartSingular = drawingSingular.createChart(anchorSingular);
			chartSingular.setTitleText("Specific singular source and test code updates\n If one type of code update was done in a version, +1 for that category");
			chartSingular.setTitleOverlay(false);

			XDDFChartLegend legendSingular = chartSingular.getOrAddLegend();
			legendSingular.setPosition(LegendPosition.TOP_RIGHT);

			XDDFCategoryAxis bottomAxisSingular = chartSingular.createCategoryAxis(AxisPosition.BOTTOM);
			bottomAxisSingular.setTitle("Versions");
			XDDFValueAxis leftAxisSingular = chartSingular.createValueAxis(AxisPosition.LEFT);
			leftAxisSingular.setTitle("Code updates");

			XDDFDataSource<String> versionsSingular = XDDFDataSourcesFactory.fromStringCellRange(sheet3, new CellRangeAddress(35 + 0, 35 + 0, 2, versionList.size()));

			XDDFNumericalDataSource<Double> productLineAdditionsSingular = XDDFDataSourcesFactory.fromNumericCellRange(sheet3, new CellRangeAddress(35 + 1, 35 + 1, 2, versionList.size()));

			XDDFNumericalDataSource<Double> productLineRemovalsSingular = XDDFDataSourcesFactory.fromNumericCellRange(sheet3, new CellRangeAddress(35 + 2, 35 + 2, 2, versionList.size()));

			XDDFNumericalDataSource<Double> gherkinLineAdditionsSingular = XDDFDataSourcesFactory.fromNumericCellRange(sheet3, new CellRangeAddress(35 + 3, 35 + 3, 2, versionList.size()));

			XDDFNumericalDataSource<Double> gherkinLineRemovalsSingular = XDDFDataSourcesFactory.fromNumericCellRange(sheet3, new CellRangeAddress(35 + 4, 35 + 4, 2, versionList.size()));

			XDDFNumericalDataSource<Double> scenarioAdditionsSingular = XDDFDataSourcesFactory.fromNumericCellRange(sheet3, new CellRangeAddress(35 + 5, 35 + 5, 2, versionList.size()));

			XDDFNumericalDataSource<Double> givenAdditionsSingular = XDDFDataSourcesFactory.fromNumericCellRange(sheet3, new CellRangeAddress(35 + 6, 35 + 6, 2, versionList.size()));

			XDDFNumericalDataSource<Double> whenAdditionsSingular = XDDFDataSourcesFactory.fromNumericCellRange(sheet3, new CellRangeAddress(35 + 7, 35 + 7, 2, versionList.size()));

			XDDFNumericalDataSource<Double> thenAdditionsSingular = XDDFDataSourcesFactory.fromNumericCellRange(sheet3, new CellRangeAddress(35 + 8, 35 + 8, 2, versionList.size()));

			XDDFNumericalDataSource<Double> thenRemovalsSingular = XDDFDataSourcesFactory.fromNumericCellRange(sheet3, new CellRangeAddress(35 + 9, 35 + 9, 2, versionList.size()));

			XDDFNumericalDataSource<Double> whenRemovalsSingular = XDDFDataSourcesFactory.fromNumericCellRange(sheet3, new CellRangeAddress(35 + 10, 35 + 10, 2, versionList.size()));

			XDDFNumericalDataSource<Double> givenRemovalsSingular = XDDFDataSourcesFactory.fromNumericCellRange(sheet3, new CellRangeAddress(35 + 11, 35 + 11, 2, versionList.size()));

			XDDFNumericalDataSource<Double> scenarioRemovalsSingular = XDDFDataSourcesFactory.fromNumericCellRange(sheet3, new CellRangeAddress(35 + 12, 35 + 12, 2, versionList.size()));

			XDDFLineChartData dataSingular = (XDDFLineChartData) chartSingular.createData(ChartTypes.LINE, bottomAxisSingular, leftAxisSingular);

			XDDFLineChartData.Series productLineAdditionSeriesSingular = (XDDFLineChartData.Series) dataSingular.addSeries(versionsSingular, productLineAdditionsSingular);
			productLineAdditionSeriesSingular.setTitle("productLineAdditionSeriesSingular", null);
			productLineAdditionSeriesSingular.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series productLineRemovalSeriesSingular = (XDDFLineChartData.Series) dataSingular.addSeries(versionsSingular, productLineRemovalsSingular);
			productLineRemovalSeriesSingular.setTitle("productLineRemovalSeriesSingular", null);
			productLineRemovalSeriesSingular.setMarkerSize((short) 6);
			productLineRemovalSeriesSingular.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series gherkinLineAdditionSeriesSingular = (XDDFLineChartData.Series) dataSingular.addSeries(versionsSingular, gherkinLineAdditionsSingular);
			gherkinLineAdditionSeriesSingular.setTitle("gherkinLineAdditionSeriesSingular", null);
			gherkinLineAdditionSeriesSingular.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series gherkinLineRemovalSeriesSingular = (XDDFLineChartData.Series) dataSingular.addSeries(versionsSingular, gherkinLineRemovalsSingular);
			gherkinLineRemovalSeriesSingular.setTitle("gherkinLineRemovalSeriesSingular", null);
			gherkinLineRemovalSeriesSingular.setMarkerSize((short) 6);
			gherkinLineRemovalSeriesSingular.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series scenarioAdditionSeriesSingular = (XDDFLineChartData.Series) dataSingular.addSeries(versionsSingular, scenarioAdditionsSingular);
			scenarioAdditionSeriesSingular.setTitle("scenarioAdditionSeriesSingular", null);
			scenarioAdditionSeriesSingular.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series givenAdditionSeriesSingular = (XDDFLineChartData.Series) dataSingular.addSeries(versionsSingular, givenAdditionsSingular);
			givenAdditionSeriesSingular.setTitle("givenAdditionSeriesSingular", null);
			givenAdditionSeriesSingular.setMarkerSize((short) 6);
			givenAdditionSeriesSingular.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series whenAdditionSeriesSingular = (XDDFLineChartData.Series) dataSingular.addSeries(versionsSingular, whenAdditionsSingular);
			whenAdditionSeriesSingular.setTitle("whenAdditionSeriesSingular", null);
			whenAdditionSeriesSingular.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series thenAdditionSeriesSingular = (XDDFLineChartData.Series) dataSingular.addSeries(versionsSingular, thenAdditionsSingular);
			thenAdditionSeriesSingular.setTitle("thenAdditionSeriesSingular", null);
			thenAdditionSeriesSingular.setMarkerSize((short) 6);
			thenAdditionSeriesSingular.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series thenRemovalSeriesSingular = (XDDFLineChartData.Series) dataSingular.addSeries(versionsSingular, thenRemovalsSingular);
			thenRemovalSeriesSingular.setTitle("thenRemovalSeriesSingular", null);
			thenRemovalSeriesSingular.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series whenRemovalSeriesSingular = (XDDFLineChartData.Series) dataSingular.addSeries(versionsSingular, whenRemovalsSingular);
			whenRemovalSeriesSingular.setTitle("whenRemovalSeriesSingular", null);
			whenRemovalSeriesSingular.setMarkerSize((short) 6);
			whenRemovalSeriesSingular.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series givenRemovalSeriesSingular = (XDDFLineChartData.Series) dataSingular.addSeries(versionsSingular, givenRemovalsSingular);
			givenRemovalSeriesSingular.setTitle("givenRemovalSeriesSingular", null);
			givenRemovalSeriesSingular.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series scenarioRemovalSeriesSingular = (XDDFLineChartData.Series) dataSingular.addSeries(versionsSingular, scenarioRemovalsSingular);
			scenarioRemovalSeriesSingular.setTitle("scenarioRemovalSeriesSingular", null);
			scenarioRemovalSeriesSingular.setMarkerSize((short) 6);
			scenarioRemovalSeriesSingular.setMarkerStyle(MarkerStyle.NONE);

			chartSingular.plot(dataSingular);

			XSSFDrawing drawingSimple = sheet4.createDrawingPatriarch();
			XSSFClientAnchor anchorSimple = drawingSimple.createAnchor(0, 0, 0, 0, 1, 2, 15, 15);

			XSSFChart chartSimple = drawingSimple.createChart(anchorSimple);
			chartSimple.setTitleText(fileChangeList.get(0).getProjectName());
			chartSimple.setTitleOverlay(false);

			XDDFChartLegend legendSimple = chartSimple.getOrAddLegend();
			legendSimple.setPosition(LegendPosition.TOP_RIGHT);

			XDDFCategoryAxis bottomAxisSimple = chartSimple.createCategoryAxis(AxisPosition.BOTTOM);
			bottomAxisSimple.setTitle("Versions");
			XDDFValueAxis leftAxisSimple = chartSimple.createValueAxis(AxisPosition.LEFT);
			leftAxisSimple.setTitle("Update count");

			XDDFDataSource<String> versionsSimple = XDDFDataSourcesFactory.fromStringCellRange(sheet4, new CellRangeAddress(0, 0, 1, versionList.size() - 1));

			XDDFNumericalDataSource<Double> singularUpdatedProductionCodePerVersion = XDDFDataSourcesFactory.fromNumericCellRange(sheet4, new CellRangeAddress(37, 37, 2, versionList.size()));

			XDDFNumericalDataSource<Double> singularUpdatedGherkinCodePerVersion = XDDFDataSourcesFactory.fromNumericCellRange(sheet4, new CellRangeAddress(38, 38, 2, versionList.size()));

			XDDFLineChartData dataSimple = (XDDFLineChartData) chartSimple.createData(ChartTypes.LINE, bottomAxisSimple, leftAxisSimple);

			XDDFLineChartData.Series productLineAdditionSeriesSimple = (XDDFLineChartData.Series) dataSimple.addSeries(versionsSimple, singularUpdatedProductionCodePerVersion);
			productLineAdditionSeriesSimple.setTitle("ELOC Updates", null);
			productLineAdditionSeriesSimple.setSmooth(false);
			productLineAdditionSeriesSimple.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series productLineRemovalSeriesSimple = (XDDFLineChartData.Series) dataSimple.addSeries(versionsSimple, singularUpdatedGherkinCodePerVersion);
			productLineRemovalSeriesSimple.setTitle("TLOC Updates", null);
			productLineRemovalSeriesSimple.setSmooth(false);
			productLineRemovalSeriesSimple.setMarkerStyle(MarkerStyle.NONE);

			chartSimple.plot(dataSimple);

			Row secondVersionRow = sheet5.createRow(35);
			cell = secondVersionRow.createCell((short) 0);
			cell.setCellValue("Version comparison");
			Row thirdVersionRow = sheet5.createRow(36);
			cell = thirdVersionRow.createCell((short) 0);
			cell.setCellValue("Update type");
			Row fourthVersionRow = sheet5.createRow(37);
			cell = fourthVersionRow.createCell((short) 0);
			cell.setCellValue("Percentage of source code updates with test code");
			for (int versionColumnIndex = 0; versionColumnIndex < columnCount - 1; versionColumnIndex++) {
				cell = secondVersionRow.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("version")));
				cell = thirdVersionRow.createCell((short) versionColumnIndex + 1);
				String updateType = String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("updateType"));
				if (updateType.equalsIgnoreCase("major release")) {
					cell.setCellStyle(greenCellStyle);
				} else if (updateType.equalsIgnoreCase("minor release")) {
					cell.setCellStyle(yellowCellStyle);
				} else if (updateType.equalsIgnoreCase("patch release")) {
					cell.setCellStyle(slightlyRedCellStyle);
				}
				cell.setCellValue(updateType);
				cell = fourthVersionRow.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("testToAllCodeForVersion")).length() > 5
						? Double.parseDouble(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("testToAllCodeForVersion")).substring(0, 5))
						: Double.parseDouble(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("testToAllCodeForVersion"))));
			}

			XSSFDrawing drawingAllVersions = sheet5.createDrawingPatriarch();
			XSSFClientAnchor anchorAllVersions = drawingSimple.createAnchor(0, 0, 0, 0, 1, 1, 12, 32);

			XSSFChart chartAllVersions = drawingAllVersions.createChart(anchorAllVersions);
			chartAllVersions.setTitleText("All Versions' Test Updates Percentage with Production Updates");
			chartAllVersions.setTitleOverlay(false);

			XDDFChartLegend legendAllVersions = chartAllVersions.getOrAddLegend();
			legendAllVersions.setPosition(LegendPosition.TOP_RIGHT);

			XDDFCategoryAxis bottomAxisAllVersions = chartAllVersions.createCategoryAxis(AxisPosition.BOTTOM);
			bottomAxisAllVersions.setTitle("Versions");
			XDDFValueAxis leftAxisAllVersions = chartAllVersions.createValueAxis(AxisPosition.LEFT);
			leftAxisAllVersions.setTitle("Percentage of test updates when there is a source code update");

			XDDFDataSource<String> versionsAllVersions = XDDFDataSourcesFactory.fromStringCellRange(sheet2, new CellRangeAddress(35, 35, 1, versionList.size() - 1));

			XDDFNumericalDataSource<Double> testForProductionPercentagePerVersion = XDDFDataSourcesFactory.fromNumericCellRange(sheet5, new CellRangeAddress(37, 37, 1, versionList.size() - 1));

			XDDFLineChartData dataAllVersions = (XDDFLineChartData) chartAllVersions.createData(ChartTypes.LINE, bottomAxisAllVersions, leftAxisAllVersions);
			dataAllVersions.setVaryColors(false);
			XDDFLineChartData.Series productLineAdditionSeriesAllVersions = (XDDFLineChartData.Series) dataAllVersions.addSeries(versionsAllVersions, testForProductionPercentagePerVersion);
			productLineAdditionSeriesAllVersions.setTitle("testForProductionPercentagePerVersion", null);
			productLineAdditionSeriesAllVersions.setMarkerStyle(MarkerStyle.NONE);

			chartAllVersions.plot(dataAllVersions);

			secondVersionRow = sheet6.createRow(34);
			cell = secondVersionRow.createCell((short) 0);
			cell.setCellValue("Only for minor and major updates");
			secondVersionRow = sheet6.createRow(36);
			cell = secondVersionRow.createCell((short) 0);
			cell.setCellValue("Version comparison");
			thirdVersionRow = sheet6.createRow(37);
			cell = thirdVersionRow.createCell((short) 0);
			cell.setCellValue("Update type");
			fourthVersionRow = sheet6.createRow(38);
			cell = fourthVersionRow.createCell((short) 0);
			cell.setCellValue("Percentage of source code updates with test code");
			int lastUpdatedCell = 0;
			for (int versionColumnIndex = 0; versionColumnIndex < columnCount - 1; versionColumnIndex++) {
				String updateType = String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("updateType"));
				if (updateType.equalsIgnoreCase("major release") || updateType.equalsIgnoreCase("minor release")) {
					cell = secondVersionRow.createCell((short) lastUpdatedCell + 1);
					cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("version")));
					cell = thirdVersionRow.createCell((short) lastUpdatedCell + 1);
					if (updateType.equalsIgnoreCase("major release")) {
						cell.setCellStyle(greenCellStyle);
					} else if (updateType.equalsIgnoreCase("minor release")) {
						cell.setCellStyle(yellowCellStyle);
					}
					cell.setCellValue(updateType);
					cell = fourthVersionRow.createCell((short) lastUpdatedCell + 1);
					cell.setCellValue(Double.parseDouble(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("testCodeUpdatedForMajorMinor")))
							/ Double.parseDouble(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("majorMinorVersionCount"))));
					lastUpdatedCell++;
				}
			}
			if (lastUpdatedCell != 0) {
				XSSFDrawing drawingMajorMinorVersions = sheet6.createDrawingPatriarch();
				XSSFClientAnchor anchorMajorMinorVersions = drawingSimple.createAnchor(0, 0, 0, 0, 1, 2, 16, 32);

				XSSFChart chartMajorMinorVersions = drawingMajorMinorVersions.createChart(anchorMajorMinorVersions);
				chartMajorMinorVersions.setTitleText("Major Minor Versions' Test Update Percentage with Source Code Updates");
				chartMajorMinorVersions.setTitleOverlay(false);

				XDDFChartLegend legendMajorMinorVersions = chartMajorMinorVersions.getOrAddLegend();
				legendMajorMinorVersions.setPosition(LegendPosition.TOP_RIGHT);

				XDDFCategoryAxis bottomAxisMajorMinorVersions = chartMajorMinorVersions.createCategoryAxis(AxisPosition.BOTTOM);
				bottomAxisMajorMinorVersions.setTitle("Versions");
				XDDFValueAxis leftAxisMajorMinorVersions = chartMajorMinorVersions.createValueAxis(AxisPosition.LEFT);
				leftAxisMajorMinorVersions.setTitle("Percentage of test updates when there is a source code update");

				XDDFDataSource<String> versionsMajorMinorVersions = XDDFDataSourcesFactory.fromStringCellRange(sheet6, new CellRangeAddress(36, 36, 1, lastUpdatedCell));

				XDDFNumericalDataSource<Double> testForProductionPercentageForMajorMinorPerVersion = XDDFDataSourcesFactory.fromNumericCellRange(sheet6, new CellRangeAddress(38, 38, 1, lastUpdatedCell));

				XDDFLineChartData dataMajorMinorVersions = (XDDFLineChartData) chartMajorMinorVersions.createData(ChartTypes.LINE, bottomAxisMajorMinorVersions, leftAxisMajorMinorVersions);
				dataMajorMinorVersions.setVaryColors(false);

				XDDFLineChartData.Series productLineAdditionSeriesMajorMinorVersions = (XDDFLineChartData.Series) dataMajorMinorVersions.addSeries(versionsMajorMinorVersions, testForProductionPercentageForMajorMinorPerVersion);
				productLineAdditionSeriesMajorMinorVersions.setTitle("testForProductionPercentagePerVersion", null);
				productLineAdditionSeriesMajorMinorVersions.setMarkerStyle(MarkerStyle.NONE);

				chartMajorMinorVersions.plot(dataMajorMinorVersions);
			}

			Row isGherkinNotProductionVersionRow = sheet6.createRow(40);
			Row isGherkinNotProductionVersionPercentageRow = sheet6.createRow(41);
			Row isGherkinAndProductionVersionRow = sheet6.createRow(42);
			Row isGherkinAndProductionVersionPercentageRow = sheet6.createRow(43);
			Row isProductionNotGherkinVersionRow = sheet6.createRow(44);
			Row isProductionNotGherkinVersionPercentageRow = sheet6.createRow(45);
			Row isSameVersionRow = sheet6.createRow(46);
			Row isSamePercentageRow = sheet6.createRow(47);
			cell = isGherkinNotProductionVersionRow.createCell((short) 0);
			cell.setCellValue("isGherkinAndNotProduction");
			cell = isGherkinNotProductionVersionPercentageRow.createCell((short) 0);
			cell.setCellValue("isGherkinAndNotProductionPercentage");
			cell = isGherkinAndProductionVersionRow.createCell((short) 0);
			cell.setCellValue("isGherkinAndProduction");
			cell = isGherkinAndProductionVersionPercentageRow.createCell((short) 0);
			cell.setCellValue("isGherkinAndProductionPercentage");
			cell = isProductionNotGherkinVersionRow.createCell((short) 0);
			cell.setCellValue("isProductionNotGherkin");
			cell = isProductionNotGherkinVersionPercentageRow.createCell((short) 0);
			cell.setCellValue("isProductionNotGherkinPercentage");
			cell = isSameVersionRow.createCell((short) 0);
			cell.setCellValue("isSame");
			cell = isSamePercentageRow.createCell((short) 0);
			cell.setCellValue("isSamePercentage");
			for (int versionColumnIndex = 0; versionColumnIndex < columnCount - 1; versionColumnIndex++) {
				cell = isGherkinNotProductionVersionRow.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isGherkinNotProduction")));
				cell = isGherkinNotProductionVersionPercentageRow.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isGherkinNotProductionPercentage")).length() > 5
						? String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isGherkinNotProductionPercentage")).substring(0, 5)
						: String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isGherkinNotProductionPercentage")));

				cell = isGherkinAndProductionVersionRow.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isGherkinAndProduction")));
				cell = isGherkinAndProductionVersionPercentageRow.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isGherkinAndProductionPercentage")).length() > 5
						? String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isGherkinAndProductionPercentage")).substring(0, 5)
						: String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isGherkinAndProductionPercentage")));

				cell = isProductionNotGherkinVersionRow.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isProductionNotGherkin")));
				cell = isProductionNotGherkinVersionPercentageRow.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isProductionNotGherkinPercentage")).length() > 5
						? String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isProductionNotGherkinPercentage")).substring(0, 5)
						: String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isProductionNotGherkinPercentage")));
				cell = isSameVersionRow.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isSame")));
				cell = isSamePercentageRow.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isSamePercentage")).length() > 5 ? String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isSamePercentage")).substring(0, 5)
						: String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("isSamePercentage")));
			}

			Row sheet7SecondVersionRow = sheet7.createRow(35);
			cell = sheet7SecondVersionRow.createCell((short) 0);
			cell.setCellValue("Version comparison");
			Row totalELOC = sheet7.createRow(36);
			cell = totalELOC.createCell((short) 0);
			cell.setCellValue("Total SLOC");
			Row totalTLOC = sheet7.createRow(37);
			cell = totalTLOC.createCell((short) 0);
			cell.setCellValue("Total TLOC");
			Row totalChangedFileCount = sheet7.createRow(38);
			cell = totalChangedFileCount.createCell((short) 0);
			cell.setCellValue("Total Changed File Count");
			Row majorCount = sheet7.createRow(40);
			cell = majorCount.createCell((short) 0);
			cell.setCellValue("Total Major Version Count");
			Row minorCount = sheet7.createRow(41);
			cell = minorCount.createCell((short) 0);
			cell.setCellValue("Total Minor Version Count");
			Row patchCount = sheet7.createRow(42);
			cell = patchCount.createCell((short) 0);
			cell.setCellValue("Total Patch Version Count");

			for (int versionColumnIndex = 0; versionColumnIndex < columnCount - 1; versionColumnIndex++) {
				cell = sheet7SecondVersionRow.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("formattedVersionDifference")));
				cell = totalELOC.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("totalELOC"))));
				cell = totalTLOC.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("totalTLOC"))));
				cell = totalChangedFileCount.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("totalChangedFileCount"))));
				cell = majorCount.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("majorVersionCount"))));
				cell = minorCount.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("minorVersionCount"))));
				cell = patchCount.createCell((short) versionColumnIndex + 1);
				cell.setCellValue(Double.parseDouble(String.valueOf(versionLineChangeMapList.get(versionColumnIndex).get("patchVersionCount"))));
			}
			cell = sheet7SecondVersionRow.createCell(columnCount - 1);
			cell.setCellValue(String.valueOf(versionLineChangeMapList.get(columnCount - 2).get("endVersion")));

			XSSFDrawing drawingTotalTLOCELOC = sheet7.createDrawingPatriarch();
			XSSFClientAnchor anchorTotalTLOCELOC = drawingTotalTLOCELOC.createAnchor(0, 0, 0, 0, 1, 1, 12, 15);

			XSSFChart chartTotalTLOCELOC = drawingTotalTLOCELOC.createChart(anchorTotalTLOCELOC);
			chartTotalTLOCELOC.setTitleText(fileChangeList.get(0).getProjectName());
			chartTotalTLOCELOC.setTitleOverlay(false);

			XDDFChartLegend legendTotalTLOCELOC = chartTotalTLOCELOC.getOrAddLegend();
			legendTotalTLOCELOC.setPosition(LegendPosition.TOP_RIGHT);

			XDDFCategoryAxis bottomAxisTotalTLOCELOC = chartTotalTLOCELOC.createCategoryAxis(AxisPosition.BOTTOM);
			bottomAxisTotalTLOCELOC.setTitle("Versions");
			XDDFValueAxis leftAxisTotalTLOCELOC = chartTotalTLOCELOC.createValueAxis(AxisPosition.LEFT);
			leftAxisTotalTLOCELOC.setTitle("Total TLOC - ELOC");

			XDDFDataSource<String> versionsTotalTLOCELOC = XDDFDataSourcesFactory.fromStringCellRange(sheet7, new CellRangeAddress(35, 35, 1, versionList.size() - 1));

			XDDFNumericalDataSource<Double> totalTLOCCount = XDDFDataSourcesFactory.fromNumericCellRange(sheet7, new CellRangeAddress(37, 37, 1, versionList.size() - 1));
			XDDFNumericalDataSource<Double> totalELOCCount = XDDFDataSourcesFactory.fromNumericCellRange(sheet7, new CellRangeAddress(36, 36, 1, versionList.size() - 1));

			XDDFLineChartData dataTotalTLOCELOC = (XDDFLineChartData) chartTotalTLOCELOC.createData(ChartTypes.LINE, bottomAxisTotalTLOCELOC, leftAxisTotalTLOCELOC);
			dataTotalTLOCELOC.setVaryColors(false);
			XDDFLineChartData.Series totalELOCSeries = (XDDFLineChartData.Series) dataTotalTLOCELOC.addSeries(versionsTotalTLOCELOC, totalELOCCount);
			totalELOCSeries.setSmooth(false);
			totalELOCSeries.setTitle("Total ELOC", null);
			totalELOCSeries.setMarkerStyle(MarkerStyle.NONE);

			XDDFLineChartData.Series totalTLOCSeries = (XDDFLineChartData.Series) dataTotalTLOCELOC.addSeries(versionsTotalTLOCELOC, totalTLOCCount);
			totalTLOCSeries.setSmooth(false);
			totalTLOCSeries.setTitle("Total TLOC", null);
			totalTLOCSeries.setMarkerStyle(MarkerStyle.NONE);

			chartTotalTLOCELOC.plot(dataTotalTLOCELOC);

			for (int j = 0; j < columnCount + 1; j++) {
				sheet.autoSizeColumn(j);
				sheet2.autoSizeColumn(j);
				sheet3.autoSizeColumn(j);
				sheet4.autoSizeColumn(j);
				sheet5.autoSizeColumn(j);
				sheet6.autoSizeColumn(j);
				sheet7.autoSizeColumn(j);
				sheet8.autoSizeColumn(j);
			}

			wb.setForceFormulaRecalculation(true);

			try (FileOutputStream fileOut = new FileOutputStream(!fileChangeList.isEmpty() ? fileChangeList.get(0).getProjectName() + ".xlsx" : "redacted" + Math.random() + ".xlsx")) {
				wb.write(fileOut);
			}
		}
	}

}
