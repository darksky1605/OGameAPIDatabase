package darksky.ogameapidatabasefx.application.main;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import darksky.ogameapidatabasefx.database.databasemanagement.DatabaseUpdater;
import darksky.ogameapidatabasefx.database.databasemanagement.DatabaseSettings;
import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.application.gui.Util;
import darksky.ogameapidatabasefx.application.gui.Settings;

public class ConsoleApplication {

	public static void run(String[] args) {

		String dbpathString = Main.defaultDatabaseLocation;
		Path dbpath = Paths.get(dbpathString);

		Util.getLogger().setLevel(Level.SEVERE);

		boolean showServerPrefixes = false;
		boolean showMyServerPrefixes = false;
		String availServerPrefix = "";
		ArrayList<OGameAPIDatabase> databasesToCreate = new ArrayList<>();
		ArrayList<OGameAPIDatabase> databasesToUpdate = new ArrayList<>();

		for (int i = 0; i < args.length; ++i) {

			if (args[i].equals("--location") && i < args.length - 1) {
                dbpathString = args[i + 1];
                dbpath = Paths.get(dbpathString);
                
				try{	
                    Files.createDirectories(dbpath);
                    System.out.println("database location : " + dbpathString);

				} catch( IOException e) {
                    dbpathString = Main.defaultDatabaseLocation;
                    dbpath = Paths.get(dbpathString);				
					System.out.println("cannot create database location. using default location " + dbpathString);
				}
			}

			if (args[i].equals("--create") && i < args.length - 1) {
                try{
                    List<String> dbnames = getMyExistingServerPrefixes(dbpath);
                            
                    //dbnames.forEach(System.out::println);
                    
                    for (; i < args.length - 1; i++) {
                        //System.out.println(args[i+1]);
                        if(args[i+1].matches("^s[0-9]+-[a-zA-Z]+")){
                            if(!dbnames.contains(args[i+1])){
                                databasesToCreate.add(new OGameAPIDatabase(args[i+1], dbpath.toAbsolutePath().toString(), "", ""));
                            }
                        }else{
                            break;
                        }
                    }
                } catch (IOException e) {
                    Util.getLogger().log(Level.SEVERE, "error getting existing databases", e);
                    System.out.println("error getting existing databases.");
                }
			}
			
			if (args[i].equals("--createall") && i < args.length - 1) {
                try{
                    List<String> dbnames = getMyExistingServerPrefixes(dbpath);
                    String param = args[i+1];
                    
                    if(!param.matches("^s[0-9]+-[a-zA-Z]+")){
                        System.out.println("--createall, param " + param + " is invalid");
                    }

                    try {
                        OGameAPIDatabase ogdb = new OGameAPIDatabase(param, dbpath.toAbsolutePath().toString(), "", "");
                        
                        List<String> list = ogdb.getServerPrefixList(param);
                        for(String s : list){
                            databasesToCreate.add(new OGameAPIDatabase(s, dbpath.toAbsolutePath().toString(), "", ""));
                        }
                        

                    } catch (Exception e) {
                        Util.getLogger().log(Level.SEVERE, "error getting available databases", e);
                        System.out.println("error getting available databases.");
                    }
                } catch (IOException e) {
                    Util.getLogger().log(Level.SEVERE, "error getting existing databases", e);
                    System.out.println("error getting existing databases.");
                }
			}			
			
			if (args[i].equals("--update") && i < args.length - 1) {
                try{
                    List<String> dbnames = getMyExistingServerPrefixes(dbpath);

                    for (; i < args.length - 1; i++) {
                        //System.out.println(args[i+1]);
                        if(args[i+1].matches("^s[0-9]+-[a-zA-Z]+")){
                            if(dbnames.contains(args[i+1])){
                                databasesToUpdate.add(new OGameAPIDatabase(args[i+1], dbpath.toAbsolutePath().toString(), "", ""));
                            }
                        }else{
                            break;
                        }
                    }
                } catch (IOException e) {
                    Util.getLogger().log(Level.SEVERE, "error getting existing databases", e);
                    System.out.println("error getting existing databases.");
                }
			}			

			if (args[i].equals("--updateall")) {
                try{
                    databasesToUpdate.clear();
                    
                    List<String> dbnames = getMyExistingServerPrefixes(dbpath);

                    for (String s : dbnames) {
                        databasesToUpdate.add(new OGameAPIDatabase(s, dbpath.toAbsolutePath().toString(), "", ""));
                    }
                } catch (IOException e) {
                    Util.getLogger().log(Level.SEVERE, "error getting existing databases", e);
                    System.out.println("error getting existing databases.");
                }
			}
			
			if (args[i].equals("--list")) {
				showMyServerPrefixes = true;
			}	
			
			if (args[i].equals("--avail") && i < args.length - 1) {
				showServerPrefixes = true;
				availServerPrefix = args[i + 1];
			}			
		}
		
		if(showServerPrefixes){
            try {
                OGameAPIDatabase ogdb = new OGameAPIDatabase(availServerPrefix, dbpath.toAbsolutePath().toString(), "", "");
                
                ogdb.getServerPrefixList(availServerPrefix).forEach(System.out::println);

            } catch (Exception e) {
				Util.getLogger().log(Level.SEVERE, "error getting existing databases", e);
				System.out.println("error getting existing databases.");
			}
		}
		
		if(showMyServerPrefixes){
            try {
                getMyExistingServerPrefixes(dbpath).forEach(System.out::println);

            } catch (Exception e) {
				Util.getLogger().log(Level.SEVERE, "error getting existing databases", e);
				System.out.println("error getting existing databases.");
			}
		}		
		
		if(databasesToCreate.size() > 0){
            create(databasesToCreate);
		}

		if (databasesToUpdate.size() > 0) {
            updateExisting(databasesToUpdate);

            databasesToUpdate.forEach(ogdb -> ogdb.closeDatabaseConnection());
		}
	}

	private static List<String> getMyExistingServerPrefixes(Path pathToDatabaseFiles) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(pathToDatabaseFiles)) {
			List<Path> list = new ArrayList<Path>();
			stream.forEach(p -> {
				String s = p.getFileName().toString();
				if (s.matches("^s[0-9]+-[a-zA-Z]+")) {
					list.add(p);
				}
			});
			
			List<String> returnlist = list.stream()
                            .map(p -> p.getFileName().toString().split("\\.")[0]).sorted((a, b) -> {
                                String[] tmpa = a.substring(1).split("-");
                                String[] tmpb = b.substring(1).split("-");
                                assert tmpa.length == 2;
                                assert tmpb.length == 2;
                                if (tmpa[1].equals(tmpb[1])) {
                                    return Integer.compare(Integer.parseInt(tmpa[0]), Integer.parseInt(tmpb[0]));
                                } else {
                                    return tmpa[1].compareTo(tmpb[1]);
                                }
                            }).collect(Collectors.toList());
			return returnlist;
		} catch (NoSuchFileException e) {
			Util.getLogger().log(Level.WARNING, "invalid path", e);
			return Collections.emptyList();
		}
	}
	
	private static void create(List<OGameAPIDatabase> databases){
	
            boolean saveActivityStates = "true".equals(Settings.getIni().get("databasecreationdefaultsettings", "saveActivity"));
			boolean savePlanetDistribution = "true".equals(Settings.getIni().get("databasecreationdefaultsettings", "savePlanetDistribution"));
			boolean saveHighscoreDistribution = "true".equals(Settings.getIni().get("databasecreationdefaultsettings", "saveHighscoreDistribution"));
			int maxHighscoreEntriesPerEntity = 1;
			try{
				maxHighscoreEntriesPerEntity = Integer.parseInt(Settings.getIni().get("databasecreationdefaultsettings", "maxHighscoreEntriesPerId"));
			}catch(Exception e){}
			
 			DatabaseSettings settings = new DatabaseSettings(saveActivityStates, savePlanetDistribution, saveHighscoreDistribution, maxHighscoreEntriesPerEntity);
			
			for (OGameAPIDatabase ogdb : databases) {
                try {
					ogdb.createDatabase(settings);
				} catch (Exception e) {
					Util.getLogger().log(Level.SEVERE, "", e);
					System.out.println("Could not create " + ogdb.getServerPrefix());
					continue;
				}
			}
	}

	private static void updateExisting(List<OGameAPIDatabase> existingdatabases) {
		List<String> domainList = Collections.emptyList();
		List<OGameAPIDatabase> controllersToUpdate = new ArrayList<OGameAPIDatabase>();

		// check if update of server is possible
		String currentDomain = "";
		for (OGameAPIDatabase c : existingdatabases) {
			String prefix = c.getServerPrefix();
			String dom = prefix.split("-")[1];
			if (!currentDomain.equals(dom)) {
				try {
					domainList = OGameAPIDatabase.getServerPrefixList(prefix);
					currentDomain = dom;
				} catch (Exception e) {
					Util.getLogger().log(Level.SEVERE, "", e);
					continue;
				}
			}
			if (domainList.contains(prefix)) {
				controllersToUpdate.add(c);
			}
		}

		String[] updateStrings = { "server data", "players", "alliances", "galaxy", "highscore", };

		System.out.println("updating existing databases");
		for (OGameAPIDatabase controller : controllersToUpdate) {
			if (controller != null) {
				
				DatabaseUpdater du = controller.getDatabaseUpdater();
				String prefix = controller.getServerPrefix();
				du.addUpdateListener(i -> System.out.println(prefix + " : updating " + updateStrings[i]));
				try {
					du.updateDatabase();
				} catch (Exception e) {
					Util.getLogger().log(Level.SEVERE, "", e);
					System.out.println("error updating " + controller.getServerPrefix());
				}

			} else {
				System.out.println("error, database is null");
			}

		}
		System.out.println("done");
	}

}
