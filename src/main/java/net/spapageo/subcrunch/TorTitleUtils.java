package net.spapageo.subcrunch;

import java.util.regex.*;

/**
 * @author Spyros Papageorgiou
 *
 */
public final class TorTitleUtils {
	
	/**
	 * Private constructor to prevent the initialization of
	 * an object of this classs
	 */
	private TorTitleUtils(){}
	
	
	/**
	 * Removes the text between brackets,angle brackets, curly braces (commonly tracker web sites, or download locations)
	 * from the title
	 * @param title
	 * @return the new title
	 */
	public static String removeBrackets(String title){
		//replace the following () {} [] <> with a single space
		return title.replaceAll("(\\[([^]]*)\\])|(\\{([^}]*)\\})|(<([^>]*)>)|(\\(([^)]*)\\))"," ").trim();
	}
	
	
	/**
	 * Removes !@#$%^&*()+=.,-_ from the title
	 * @param title The title to process
	 * @return the processed title
	 */
	public static String removeSymbols(String title){
		return title.replaceAll("[^a-zA-Z0-9\\s]", " ").trim();
	}
	
	/**
	 * Finds if available the release year
	 * @param title The title to check
	 * @return the year or an empty string
	 */
	public static String findReleaseYear(String title){
		String year = new String();
		
		Pattern p = Pattern.compile("[^a-z0-9][12][0-9]{3}([^a-z0-9]|$)");
		Matcher m = p.matcher(TorTitleUtils.removeSymbols(title.toLowerCase()));
		// Return the last year found in the title
		while(m.find()){
			year = m.group();
		}
		return year.trim();
	}
	
	/**
	 * Finds if available the release type of the torrent
	 * @param title The title to check
	 * @return the release type or an empty string
	 */
	public static String findReleaseType(String title){
		String rtype = new String();
		
		Pattern p = Pattern.compile("\\s(dvdrip|ts|dvdscr|720p|1080p|bdrip|cam|screener|bluray|r5|r6|r7|r9)");
		Matcher m = p.matcher(TorTitleUtils.removeSymbols(title.toLowerCase()));
		
		// Return the first release type found in the title
		while(m.find()){
			rtype = m.group();
		}
		return rtype.trim();
	}
	
	/**
	 * Finds if available the release type of the torrent
	 * @param title The title to check
	 * @return the release type or an empty string
	 */
	public static String findReleaseName(String title){
		String rname = new String();

		String[] results = title.toLowerCase().split("(-|‒|–|—|―)");
		
		// Return the last part of the title separated be dashes
		if(results.length > 1){
			rname = results[results.length-1];
		}
		return TorTitleUtils.removeSymbols(rname);
	}
	
	/**
	 * Tries to trim the title of the torrent
	 * @param title The title to check
	 * @return the release type or an empty string
	 */
	public static String trimTitle(String title){
		
		final int MSIZE = 2;
		Matcher[] m = new Matcher[MSIZE];
		
		m[0] = Pattern.compile("(\\s[0-9]{4,4}\\s|$)").matcher(title.toLowerCase());
		m[1] = Pattern.compile("\\s(dvdrip|ts|dvdscr|720p|1080p|bdrip|cam|screener|bluray|r5|r6|r7|r9|xvid|divx|ac3|vod|rip)").matcher(title.toLowerCase());

		int minIndex = title.length();
		
		for(int i = 0;i < MSIZE;i++){
			if(m[i].find()){
				int temp = m[i].start();
				minIndex = (minIndex > m[i].start()) ? temp : minIndex;
			}
		}
		
		return title.substring(0, minIndex).toLowerCase().trim();
	}
	
}
