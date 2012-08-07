/**
 * 
 */
package net.spapageo.subcrunch;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.*;

/**
 * @author Spyros Papageorgiou
 *
 */
public class P7zip {
	private final static Logger LOGGER = Logger.getLogger(P7zip.class.getName());
	
	private String command = null;
	
	P7zip(){
		String os = System.getProperty("os.name").toLowerCase();
		if(os != null){
			if(os.contains("win")){
				this.command = "./bin/7za.exe";
			}else{
				this.command = "./bin/7z";
			}
		}else{
			LOGGER.log(Level.SEVERE, "Could not retrieve OS information");
			throw new RuntimeException("Could not retrieve OS information");
		}
	}
	
	/**
	 * Tests the P7zip installation
	 * @return True if it is working fine, false otherwise
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean test() throws IOException, InterruptedException{
		Process p = Runtime.getRuntime().exec(this.command);
		BufferedInputStream stream = new BufferedInputStream(p.getInputStream());
		StringBuilder sb = new StringBuilder();
		int t;
		while((t = stream.read()) != -1){
			sb.append((char)t);
		}
		stream.close();
		
		if(sb.toString().indexOf("7-Zip") != -1){
			return true;
		} else {
			LOGGER.log(Level.SEVERE, "Command did not execute succefully");
			LOGGER.log(Level.SEVERE, sb.toString());
			return false;
		}
	}
	
	
	public List<Path> extract(Path file,Path dest) throws InterruptedException, IOException{
		String[] args = {this.command,"x",file.toString(),"-o".concat(dest.toString()),"-y"};
		
		Process p = Runtime.getRuntime().exec(args);
		
		// Check for successful termination
		if(p.waitFor() == 0){
			
			List<Path> l = new ArrayList<>();
			
			// Get the processes stdout
			BufferedInputStream stream = new BufferedInputStream(p.getInputStream());
			Scanner scan = new Scanner(stream);
						
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(line.indexOf("Extracting  ") != -1){
					LOGGER.log(Level.INFO, line);
					l.add(dest.resolve(line.replaceFirst("Extracting  ", "")));
				}
			}
			scan.close();
			
			return l;
			
		}else{
			BufferedInputStream stream = new BufferedInputStream(p.getInputStream());
			Scanner scan = new Scanner(stream);
			StringBuilder sb = new StringBuilder();
			while(scan.hasNextLine()){
				sb.append(scan.nextLine());
			}
			scan.close();
			
			LOGGER.log(Level.SEVERE,"P7zip extraction failed:\n {0}\n Aborting.",sb.toString());
			throw new RuntimeException("P7zip extraction failed. Aborting");
		}
	}
	
	public List<Path> recursiveExtract(List<Path> files) throws InterruptedException, IOException{
		
		List<Path> srtFiles = new ArrayList<>();
		
		for(Path p: files){
			LOGGER.log(Level.INFO, "Found {0}",p.toString());
			if(p.toString().endsWith(".zip") || p.toString().endsWith(".rar")){
				List<Path> extractedFiles = this.extract(p,p.getParent());
				srtFiles.addAll(recursiveExtract(extractedFiles));
			}else if(p.toString().endsWith(".srt")){
				srtFiles.add(p);
			}
		}
		return srtFiles;
	}
	

}
