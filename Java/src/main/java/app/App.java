package app;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import crawler.Crawler;

public class App {

	public static void main(String[] args) throws IOException, InterruptedException {
		Crawler crawler = new Crawler();
		List<String> projectUrlList = Arrays.asList(
			"https://github.com/sdkman/sdkman-cli"
			,"https://github.com/thoughtbot/factory_bot_rails"
			,"https://github.com/inukshuk/jekyll-scholar"
			,"https://github.com/Behatch/contexts"
			,"https://github.com/trema/trema"
			,"https://github.com/hoppinger/advanced-custom-fields-wpcli"
			,"https://github.com/iphoting/ovpnmcgen.rb"
			,"https://github.com/bbc/bbc-a11y"
			,"https://github.com/awendt/poet"
			,"https://github.com/psalm/psalm-plugin-doctrine"
			,"https://github.com/middleman/middleman-sprockets"
			,"https://github.com/wp-cli/scaffold-package-command"
			,"https://github.com/psalm/psalm-plugin-phpunit"
			,"https://github.com/DigitalState/Platform"
			,"https://github.com/alphagov/smokey"
			,"https://github.com/splattael/minitest-around"
			,"https://github.com/feduxorg-attic/proxy_rb"
			,"https://github.com/cucumber/cucumber-ruby-wire"
			,"https://github.com/BillyRuffian/chutney"
			,"https://github.com/continuousphp/sdk"
			,"https://github.com/moodlehq/moodle-local_moodlemobileapp"
			,"https://github.com/DigitalState/Forms"
			,"https://github.com/dimagi/commcare-ui-tests"
		);
		
		List<String> projectNameList = Arrays.asList(
				"The SDKMAN! Command Line Interface"
				,"Factory Bot Rails"
				,"jekyll extensions for the blogging scholar"
				,"Behat extension with most custom helper steps"
				,"Full-Stack OpenFlow Framework in Ruby"
				,"Manage Advanced Custom Fields groups in WP-CLI"
				,"An OpenVPN iOS Configuration Profile (.mobileconfig) Utility—Configures OpenVPN for use with VPN-on-Demand that are n…"
				,"BBC Accessibility Guidelines Checker"
				,"Lets you split your ssh_config into separate files"
				,"Stubs to let Psalm understand Doctrine better"
				,"Sprockets support for Middleman"
				,"Scaffolds WP-CLI commands with functional tests, full README.md, and more."
				,"A PHPUnit plugin for Psalm"
				,"The DigitalState Platform"
				,"Smoke tests for GOV.UK"
				,"Around block for minitest."
				,"Helps to test your proxy infrastructure"
				,"Wire protocol plugin for Cucumber"
				,"Best practice for Cucumber"
				,"PHP SDK to consume the continuousphp API"
				,"Moodle Mobile plugin including the app language strings. This plugin is for translating the app strings in AMOS"
				,"The DigitalState Forms Microservice"
				,"UI and integration tests for CommCare Android app"
			);	
			crawler.crawl(projectUrlList, projectNameList);
			// List<ProjectCommitData> projectCommitData =
			// crawler.crawlProjectGherkinData(projectUrlList, projectNameList);
			/*
			 * for (ProjectCommitData pcd : projectCommitData) {
			 * ExcelUtil.createFileChangeExcel(pcd.getFileChangeList()); }
			 */
			// ExcelUtil.createGherkinDataExcel(null, null);
		}

	}
