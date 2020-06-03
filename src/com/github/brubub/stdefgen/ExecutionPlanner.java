package com.github.brubub.stdefgen;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import com.github.brubub.stdefgen.core.*;
import com.github.brubub.stdefgen.data.ControllerFile;
import com.github.brubub.stdefgen.data.XMLFile;
import com.github.brubub.stdefgen.exceptions.ImpossibleToContinueExecutionException;
import com.github.brubub.stdefgen.exceptions.MethodNonAcceptableReturnException;
import com.github.brubub.stdefgen.io.*;

import java.io.File;

public class ExecutionPlanner {

	private DirectoriesManager dirMan;
	
	public ExecutionPlanner(String srcDir){
		dirMan = new DirectoriesManager(srcDir);
	}
	
	public ExecutionPlanner(String srcDir, String destDir){
		dirMan = new DirectoriesManager(srcDir,destDir);
	}
	
	public boolean run() throws IOException, ImpossibleToContinueExecutionException {
		int presenza = controllersManager();
		if(presenza == -1) {
			throw new ImpossibleToContinueExecutionException("Non e' stato possibile generare le classi controller");
		}
		
		if(!dirMan.createFakeDir()) {
			throw new ImpossibleToContinueExecutionException("Non e' stato possibile creare la fakeDir al path: "+dirMan.getFakeDir());
		}
		
		//copiamo soltanto il codice java dalla cartella con il codice sorgente nella nuova cartella
		Utils.copyDir(dirMan.getCoreServDir()+"/src/main/java", dirMan.getFakeDir()+"/src/main/java");
		
		if(presenza == 2) { // i controllori sono nella cartella target/generated-sources	
			Utils.copyDir(dirMan.getCoreServDir()+dirMan.getGeneratedControllersSubpath(), dirMan.getFakeDir()+dirMan.getControllersSubpath()); 
		}
		
		if(!Utils.isDirectory(dirMan.getFakeDir()+dirMan.getControllersSubpath())) {
			throw new ImpossibleToContinueExecutionException("I controller non sono stati copiati in fakeDir");
		}
		
		createPom(dirMan.getSrcDir()+"/pom.xml",dirMan.getCoreServDir()+"/pom.xml", dirMan.getFakeDir()+"/pom.xml");
		annotateControllers(dirMan.getFakeDir()+dirMan.getControllersSubpath(),dirMan.getFakeDir()+dirMan.getControllersSubpath());
		boolean risultato = compileFakeProject();
		return risultato;
	}
	
	private boolean compileFakeProject() throws IOException, ImpossibleToContinueExecutionException{
		String projectPath = dirMan.getFakeDir();
		String commands = "mvn clean compile -f "+projectPath;
		System.out.println("\n Compilazione progetto fasullo \n");
        return executeShellCommands(commands);  
	}
	
	private int controllersManager() throws IOException, ImpossibleToContinueExecutionException {
		int presenza = controllersPresenceChecker();
		
		if(presenza == -1) {// i controllori non ci sono
			if(!compileStCoreServices()){// generiamo i controllori
				return -1;	//non e' stato possibile generarli
			}
			presenza = controllersPresenceChecker();
		}
		return presenza;
	}
	
	private int controllersPresenceChecker() throws IOException {
		String path = dirMan.getCoreServDir()+dirMan.getControllersSubpath();
		String path2 = dirMan.getCoreServDir()+dirMan.getGeneratedControllersSubpath();
		if(Utils.isDirectory(path) && !Utils.dirIsEmpty(path)) {//la cartella esiste e non e' vuota
			return 1;
		}
		else if(Utils.isDirectory(path2) && !Utils.dirIsEmpty(path2)) {//abbiamo la cartella e i controllori generati ma dobbiamo spostarli
			return 2;
		}
		return -1; //terzo caso: i controllori non ci sono
	}
	
	private void createPom(String parent, String child, String dest) throws ImpossibleToContinueExecutionException, 
																			FileNotFoundException, IOException{
		FileReaderWriter reader = new FileReaderWriter();
		XMLFile parentPom = new XMLFile( reader.readFile(parent) );
		XMLFile childPom = new XMLFile( reader.readFile(child) );
		PomFilesMerger merger = new PomFilesMerger();
		XMLFile generatedPom = merger.generate(parentPom,childPom);
		reader.writeToFile(generatedPom,dest);
	}
		
	private void annotateControllers(String src, String dest) throws FileNotFoundException, IOException {
		FileReaderWriter reader = new FileReaderWriter();
		ControllersCodeElaborator elaborator = new ControllersCodeElaborator();
		
		File dir = new File(src);
		File[] files = dir.listFiles();
		
		for(File element: files) {
			if(element.isDirectory() || !element.getAbsolutePath().endsWith(".java")) {
				continue;
			}
			ControllerFile controller = new ControllerFile( reader.readFile(element.getAbsolutePath()), element );
			try {
				controller = elaborator.annotate(controller);
				reader.writeToFile(controller,dest+"/"+element.getName());
			} catch (MethodNonAcceptableReturnException e) {
				System.err.println(e);
				continue;
			}
		}
	}
	
	private boolean compileStCoreServices() throws IOException, ImpossibleToContinueExecutionException {
		String coreserv = dirMan.getCoreServDir();
		String operation = "mvn clean compile -f "+coreserv;
		System.out.println("\n Compilazione modulo st-core-services \n");
		return executeShellCommands(operation);	
	}
	
	private boolean executeShellCommands(String commands) throws ImpossibleToContinueExecutionException {
		boolean stato = false;
		ProcessBuilder builder = new ProcessBuilder();
		
		String[] operation = null;
		if(isWindows()) {
			operation = new String[] {"cmd.exe", "/c", commands};
		}
		else if(isLinux()) {
			operation = new String[] { "bash", "-c", commands };
		}
		if(operation == null) {
			throw new ImpossibleToContinueExecutionException("stdefgen.executeShellCommands(): rilevato il sistema operativo "+Utils.getOS()+" ,sono supportati windows e linux");
		}
		builder.command(operation);
		try {
			Process process = builder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				if(line.contains("BUILD SUCCESS")) {
	            	stato = true;
	            }
			}
			
		}
		catch (IOException e) {
            e.printStackTrace();
        }
        return stato;
	}
	
	private boolean isWindows() {
		return (Utils.getOS().contains("windows"));
	}
	
	private boolean isLinux() {
		return (Utils.getOS().contains("linux"));
	}
	
}

