package darksky.ogameapidatabasefx.application.gui;
//package darksky.ogameapidatabase.application.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * 
 * @author darksky
 *
 */
public class ServerNameConstants {
	

	
	/*
	 * tables to convert newer servers to their names and vice versa, e.g.
	 * nameToServer : andromeda -> s101, serverToName : s101 -> andromeda
	 */	
	public static Map<String, String> nameToServer = new HashMap<String, String>();
	public static Map<String, String> serverToName = new HashMap<String, String>();
	
	static{

		try {
			List<String> lines = Files.readAllLines(Paths.get("servernames.txt"));
            lines.forEach(line -> {
                String[] tokens = line.split(" ");
                assert(tokens.length == 2);
                nameToServer.put(tokens[1], tokens[0]);
                serverToName.put(tokens[0], tokens[1]);
            });
            
            lines = null;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Cannot open servernames file " + "servernames.txt");
			throw new RuntimeException();
		}
	}
	
	private ServerNameConstants(){}

}
