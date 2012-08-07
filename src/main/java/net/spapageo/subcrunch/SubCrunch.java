package net.spapageo.subcrunch;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

import static net.spapageo.subcrunch.TorTitleUtils.*;

/**
 * Program entry point. Here we parse the command line options and
 *  then call the appropriate functions to process the input.
 * @author Spyros Papageorgiou
 *
 */
public class SubCrunch 
{
	
	private static final Logger LOGGER = Logger.getLogger(SubCrunch.class.getName());
	
	/**
	 * Program entry point.
	 * @param args The command line arguments
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main( String[] args ){
		try{
			Path logDir = Paths.get("log");
			if(!Files.isDirectory(logDir)){
				if(Files.exists(logDir)){
					Files.delete(logDir);
					Files.createDirectory(logDir);
				}else{
					Files.createDirectory(logDir);
				}
			}
			
			Handler handler = new FileHandler(logDir.resolve(Paths.get("subcrunch.log")).toString(),5 * 1024 * 1024 , 100);
			handler.setFormatter(new SimpleFormatter());
			Logger.getLogger("").addHandler(handler);

			String torTitle = null;
			String saveDir = null;
			String infoHash = null;

			if (args.length != 3) {
				abort();
			}

			infoHash = args[0].trim();
			torTitle = args[1].trim();
			saveDir = args[2].trim();



			LOGGER.log(Level.INFO, "Initializing..." );
			LOGGER.log(Level.INFO,"The torrent hash is: {0}",infoHash);
			LOGGER.log(Level.INFO,"The torrent title is: {0}",torTitle);
			LOGGER.log(Level.INFO,"The torrent directory is: {0}",saveDir);

			String rlYear = findReleaseYear(torTitle);
			String rlType = findReleaseType(torTitle);
			String rlName = findReleaseName(torTitle);
			String cleanTitle = removeSymbols(removeBrackets(torTitle));
			String trimmedTitle = trimTitle(cleanTitle);

			LOGGER.log(Level.INFO,"The release year is  : {0}",rlYear);
			LOGGER.log(Level.INFO,"The release type is  : {0}",rlType);
			LOGGER.log(Level.INFO,"The release name is  : {0}",rlName);
			LOGGER.log(Level.INFO,"The trimmed title is : {0}",trimmedTitle);

			P7zip p7 = new P7zip();
			if(!p7.test()){
				LOGGER.log(Level.SEVERE,"P7zip is not setup successfully in this environment");
				System.exit(1);
			}


			final int TRIES = 3;
			List<Path> archive =  new ArrayList<Path>();
			String[] searchQuaries = new String[TRIES];
			searchQuaries[0] = trimmedTitle + " " + rlYear + " " + rlType + " " + rlName;
			searchQuaries[1] = trimmedTitle + " " + rlYear + " " + rlType;
			searchQuaries[2] = trimmedTitle + " " + rlYear;

			List<Path> srtList = new ArrayList<>();

			boolean success = false;
			for(int i = 0;i < TRIES && !success;i++){
				archive.clear();
				Path subPath = GreekSubtitlesDotInfo.request(searchQuaries[i]);
				LOGGER.log(Level.INFO,"Tried search string: {0}",searchQuaries[i]);

				if(subPath.toString().isEmpty()){
					LOGGER.log(Level.WARNING,"Didn't get any results.");
					continue;
				}else{
					success = true;
				}
				archive.add(subPath);
				List<Path> l = p7.recursiveExtract(archive);
				for(Path p: l){
					LOGGER.log(Level.INFO,"Copying: {0} To: {1}",new String[]{p.toString(),Paths.get(saveDir).resolve(p.getFileName()).toString()});
					Files.copy(p, Paths.get(saveDir).resolve(p.getFileName()), StandardCopyOption.REPLACE_EXISTING);
					srtList.add(Paths.get(saveDir).resolve(p.getFileName()));
				}
			}
			if(!success){
				LOGGER.log(Level.INFO,"No subs found. Exiting");
				System.exit(1);
			}

			renameSrts(srtList, findAvis(Paths.get(saveDir)));

			System.exit(0);
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, "Exception thrown:", e);
		}
	}
	
	
	/**
	 * Renames the subtitles so that they are automatically opened by VLC
	 * @param srtList
	 * @param aviList
	 * @throws IOException
	 */
	public static void renameSrts(List<Path> srtList, List<Path> aviList) throws IOException{
		//Sort both lists
		Comparator<Path> pathComp = new Comparator<Path>() {

			@Override
			public int compare(Path o1, Path o2) {
				return o1.toString().compareTo(o2.toString());
			}
			
		};
		
		Collections.sort(srtList,pathComp);
		Collections.sort(aviList,pathComp);
		
		
		
		//Equalize the size of the 2 lists
		int aviIndex = 0;
		if(aviList.size() == 0){
			return;
		}else if(aviList.size() < srtList.size()){
			while(aviList.size() < srtList.size()){
				srtList.remove(srtList.size()-1);
			}
		}
		
		for(Path srt: srtList){
			Path newName = Paths.get(aviList.get(aviIndex).toString().replaceAll(".avi", ".srt"));
			Files.move(srt, newName, StandardCopyOption.REPLACE_EXISTING);
			LOGGER.log(Level.INFO, "Renaming: {0} to {1}", new String[]{ srt.toString() , aviList.get(aviIndex).toString().replaceAll(".avi", ".srt")});
			aviIndex++;
		}
	}
	
	/**
	 * Recursively finds the avi files in the specified folder.
	 * 
	 * @param dir
	 * @return the avi list
	 * @throws IOException
	 */
	public static List<Path> findAvis(Path dir) throws IOException{
		
		DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir);
		List<Path> aviList = new ArrayList<>();
		
		for(Path p: dirStream){
			if(Files.isDirectory(p)){
				aviList.addAll(findAvis(p));
				continue;
			}
			
			if(p.toString().toLowerCase().indexOf("sample") == -1 && p.toString().toLowerCase().endsWith(".avi")){
				LOGGER.log(Level.INFO, "Found avi file: {0}",p.toString());
				aviList.add(p);
			}
		}
		
		return aviList;
	}
	
	
	
	/*
	 * Prints the usage of the program and then exit the application.
	 */
	public static void abort(){
		System.out.print( "Invalid Command Line Arguments.\n" +
									"Usage:\n" + "'torrent_hash' 'torrent_title' 'torrent_directory'\n");
		System.exit(1);
	}
}







