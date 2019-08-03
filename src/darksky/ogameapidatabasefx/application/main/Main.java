package darksky.ogameapidatabasefx.application.main;

import javafx.application.Application;

public class Main {
	
	public static final String defaultDatabaseLocation = "./databases";
	
	
	public static void help(){
        System.out.println("--help , Show this help");
        System.out.println("--gui , Show GUI");
        System.out.println("--location , Folder to store database files. default: --location ./databases ");
        System.out.println(" ");
        System.out.println("Parameters when not using GUI:");
        System.out.println("--list , Print list of databases in database folder");
        System.out.println("--avail x , Print list of available servers with same domain as server x. Ignores other settings");
        System.out.println("--create x [y, ...], Create database x (and y and ...) in database folder. Example: --create s1-de s79-de");
        System.out.println("--update x [y, ...], Update database x (and y and ...) in database folder. Example: --update s1-de s79-de");
        System.out.println("--createall x , Create databases of all servers with same domain as server x. Example: --createall s1-de");
        System.out.println("--updateall , Update all databases in database folder, if possible.");

        System.out.println("If --location is used, it must be the first argument.");
        System.out.println("Cannot create and update the same database in one command.");
	}

	public static void main(String[] args) {
		boolean gui = false;
		
		if(args.length == 0){
            help();
            return;
        }
            

		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("--gui")) {
				gui = true;
			}
			if (args[i].equals("--help")) {
				help();
				return;
			}			
		}
		if (gui) {
			//System.out.println("running gui version");
			Application.launch(GUIApplication.class, args);
		} else {
			//System.out.println("running console version");
			ConsoleApplication.run(args);
		}
	}
}
