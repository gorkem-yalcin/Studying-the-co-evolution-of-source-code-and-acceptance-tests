package util;

import java.util.ArrayList;
import java.util.List;

import models.VersionType;

public class CrawlerUtil {

	public static String getUpdateType(String oldVersion, String newVersion) {
		String updateType = "unsupported versions";
		List<Integer> newVersionDotIndexList = new ArrayList<>();
		List<Integer> oldVersionDotIndexList = new ArrayList<>();
		VersionType oldVersionType = getVersionTypeFromVersionString(oldVersion, newVersionDotIndexList);
		VersionType newVersionType = getVersionTypeFromVersionString(newVersion, oldVersionDotIndexList);
		System.out.println(oldVersionType);
		System.out.println(newVersionType);
		if (oldVersionType.getVersionParseError().equals("") && newVersionType.getVersionParseError().equals("")) {
			if (newVersionType.getMajorVersion() > oldVersionType.getMajorVersion()) {
				updateType = "major release";
			} else if (newVersionType.getMinorVersion() > oldVersionType.getMinorVersion()) {
				updateType = "minor release";
			} else if (newVersionType.getMinorestVersion() > oldVersionType.getMinorestVersion()) {
				updateType = "minorest release";
			} else {
				updateType = "same version";
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
			if (dotIndexList.size() != 2) {
				versionParseError = "wrong amount of dots";
			} else {
				if (dotIndexList.get(0) == 0 || dotIndexList.get(0) == version.length() || dotIndexList.get(1) == 0 || dotIndexList.get(1) == version.length()) {
					versionParseError = "first or last character is a dot";
				} else {
					if (Math.abs(dotIndexList.get(0) - dotIndexList.get(1)) == 1) {
						versionParseError = "adjacent dots";
					} else {
						if (version.length() < 5) {
							versionParseError = "wrong length of version";
						} else {
							String[] splittedVersions = version.split("\\.");
							String majorVersion = splittedVersions[0];
							String minorVersion = splittedVersions[1];
							String minorestVersion = splittedVersions[2];
							versionType = new VersionType(Integer.parseInt(majorVersion), Integer.parseInt(minorVersion), Integer.parseInt(minorestVersion), versionParseError);
						}
					}
				}
			}
		} else {
			versionParseError = "unsupported version";
		}
		versionType.setVersionParseError(versionParseError);
		return versionType;
	}
}
