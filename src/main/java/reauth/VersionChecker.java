package reauth;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import net.minecraftforge.fml.common.versioning.ComparableVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;

class VersionChecker implements Runnable {

	private static final String url = "https://raw.githubusercontent.com/TechnicianLP/ReAuth/master/version";

	private static boolean isLatestVersion = true;
	private static String[] versionInfo = new String[] { "", "" };
	private static boolean isVersionAllowed = true;
	private static long run = 0;

	@Override
	public void run() {
		Main.log.info("Looking for Updates");
		InputStream in = null;
		try {
			in = new URL(url).openStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			versionInfo = IOUtils.readLines(in).get(0).split(";");

			ComparableVersion local = new ComparableVersion(Main.meta.version);
			ComparableVersion recommended = new ComparableVersion(versionInfo[0]);
			VersionRange allowed = VersionParser.parseRange(versionInfo[1]);

			isLatestVersion = local.compareTo(recommended) >= 0;
			isVersionAllowed = allowed.containsVersion(new DefaultArtifactVersion(Main.meta.version));
			run = System.currentTimeMillis();

			Main.log.info("Looking for Updates - Finished");
			return;
		} catch (IOException e) {
			Main.log.error("Looking for Updates - Failed");
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
		}
		run = System.currentTimeMillis() - (long) ((1000 * 60 * 60) * 0.75);
	}

	public static boolean isLatestVersion() {
		return isLatestVersion;
	}

	public static String getLatestVersion() {
		return versionInfo[0];
	}

	public static boolean isVersionAllowed() {
		return isVersionAllowed;
	}

	public static boolean shouldRun() {
		return System.currentTimeMillis() - run > (1000 * 60 * 60);
	}

	public static void update() {
		Thread t = new Thread(new VersionChecker(), "ReAuth-VersionChecker");
		t.setDaemon(true);
		t.start();
	}

}
