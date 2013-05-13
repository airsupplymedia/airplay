package de.airsupply.airplay.core.model.test.misc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.commons.io.IOUtils;

import de.airsupply.airplay.core.model.Song;

public class CDDB {

	public static void main(String[] args) {
		new CDDB().searchForSongDescription(null);

		int FRAMES_PER_SECOND = 75;
		int[] frames = { 150, 14672, 27367, 45030, 60545, 76707, 103645, 116430, 137730, 156887, 171577, 185792, 208500 };

		int N = frames.length - 1; // 12
		int totalLength = (frames[N] - frames[0]) / FRAMES_PER_SECOND;
		int checkSum = 0;
		// int s = 2;

		for (int i = 0; i < N; i++)
			checkSum += sumOfDigits(frames[i] / FRAMES_PER_SECOND);

		int XX = checkSum % 255;
		int YYYY = totalLength;
		int ZZ = N;

		// XXYYYYZZ
		int discID = ((XX << 24) | (YYYY << 8) | ZZ);
		System.out.println(Integer.toHexString(discID));
	}

	// return sum of decimal digits in n
	static int sumOfDigits(int n) {
		int sum = 0;
		while (n > 0) {
			sum = sum + (n % 10);
			n = n / 10;
		}
		return sum;
	}

	public void searchForSongDescription(Song song) {
		System.setProperty("http.proxyHost", "pro-campus.noc.fiducia.de");
		System.setProperty("http.proxyPort", "8080");

		InputStream inputStream = null;
		try {
			// String command =
			// "cddb+query+680b8d0a+10+150+20475+44550+64050+89325+112950+132375+156600+178875+197850+2957";
			String command = "cddb+read+misc+8f0b9a0a";
			String clientLogin = "asm";
			String clientDomain = "asm.de";
			String clientName = "asm";
			String clientVersion = "1.0";
			String query = "cmd=" + command + "&hello=" + clientLogin + "+" + clientDomain + "+" + clientName + "+"
					+ clientVersion + "&proto=5";
			URL url = new URL("http://freedb.freedb.org:80/~cddb/cddb.cgi?" + query);
			// socket.connect(cddbAddress);
			URLConnection openConnection;
			openConnection = url.openConnection();
			inputStream = openConnection.getInputStream();
			List<String> lines = IOUtils.readLines(inputStream);
			for (String string : lines) {
				System.out.println(string);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(inputStream);
		}

		try {
			String apiKey = "f0fdcbc883d9cae91da5025de2c679f4";
			URL url = new URL(
					"http://ws.audioscrobbler.com/2.0/?method=track.getinfo&artist=JACKSON,+MICHAEL&track=THRILLER&autocorrect=1&api_key="
							+ apiKey);
			URLConnection openConnection;
			openConnection = url.openConnection();
			inputStream = openConnection.getInputStream();
			List<String> lines = IOUtils.readLines(inputStream);
			for (String string : lines) {
				System.out.println(string);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		try {
			URL url = new URL(
					"http://www.musicbrainz.org/ws/2/recording?query=%22THRILLER%22%20AND%20artist:%22JACKSON%22");
			URLConnection openConnection;
			openConnection = url.openConnection();
			inputStream = openConnection.getInputStream();
			List<String> lines = IOUtils.readLines(inputStream);
			for (String string : lines) {
				System.out.println(string);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

}
