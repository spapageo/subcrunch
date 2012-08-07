package net.spapageo.subcrunch;

import java.io.IOException;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.*;
import java.util.regex.*;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @author Spyros Papageorgiou
 *
 */
public final class GreekSubtitlesDotInfo {

	private static final String SITE_URL = "http://www.greeksubtitles.info/search.php?name=";
	
	/**
	 * Constructor take the searh string.
	 * @param searchQuery
	 */
	private GreekSubtitlesDotInfo(String searchQuery){
		
	}
	
	
	/**
	 * Requests the appropriate page from greeksubtitles.info
	 * parses the results and downloads the first subtitle.
	 * @param searchQuery
	 * @return The subtitle file
	 * @throws IOException
	 */
	public static Path request(String searchQuery) throws IOException{
		
		// Download the page and 
		String searchQ = SITE_URL.concat(searchQuery.replaceAll(" ", "+"));
		Document doc = Jsoup.connect(searchQ).get();
		Elements subs = doc.select("#content tbody > tr > td > img ~ a");
		String href = subs.eq(0).attr("href");
		
		Path sub = GreekSubtitlesDotInfo.downloadSub(href);
		
		return sub;
	}
	
	
	/**
	 * Downloads the sub title based on the link provided y extracting the id
	 * @param href
	 * @return The Path of the subtitle file.
	 * @throws IOException
	 */
	private static Path downloadSub(String href) throws IOException{
		String url = null;
		final String DOWN_URL = "http://www.findsubtitles.eu/getp.php?id=";
		
		if(href.isEmpty()){
			return Paths.get(href);
		}
		
		//Find the download id and compose the download url.
		Matcher m = Pattern.compile("[0-9]++").matcher(href);
		if(m.find()){
			url = DOWN_URL.concat(m.group());
		}
		
		return GreekSubtitlesDotInfo.downSubToFile(url, 5000);
	}
	
	
	/**
	 * Downloads the supplied resource,to a file in the computers temporary directory 
	 *
	 *@param resource The resource you want to download.
	 *@param timeOut Read timeOut in milliseconds.
	 *@return A File object of the file that was download.
	 *@throws IOException
	 *
	 */
	private static Path downSubToFile(String resource, int timeOut) throws IOException{
		Path tmp = null;
		URL res = null;
		ReadableByteChannel rbc = null;
		FileChannel wbc = null;
		ByteBuffer bf = ByteBuffer.allocate(4096);
		HttpURLConnection con = null;

		Path tempDir = Files.createTempDirectory("subcrunch");
		tmp = Files.createTempFile(tempDir, "subcrunch", ".zip");
		
		//Perform the first request and then manually follow the redirect (automatic redirect)
		//fails miserably
		res = new URL(resource);
		con = (HttpURLConnection) res.openConnection();
		con.setInstanceFollowRedirects(false);
		con.setConnectTimeout(5000);
		con.setReadTimeout(timeOut);
		con.connect();
		
		while(con.getHeaderField("Location") != null){
			String newLoc = con.getHeaderField("Location");
			con.disconnect();
			con = (HttpURLConnection) new URL(newLoc.replaceAll(" ", "%20")).openConnection();
			con.setConnectTimeout(5000);
			con.setReadTimeout(timeOut);
			con.connect();
		}
		
		rbc = Channels.newChannel(con.getInputStream());
		wbc = FileChannel.open(tmp,StandardOpenOption.WRITE);

		while(rbc.read(bf) != -1){
			bf.flip();
			wbc.write(bf);
			if(bf.hasRemaining()){
				bf.compact();
			} else {
				bf.clear();
			}
		}

		rbc.close();
		wbc.close();

		return tmp;
	}
}
