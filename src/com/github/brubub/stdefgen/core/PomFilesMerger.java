package com.github.brubub.stdefgen.core;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.brubub.stdefgen.Utils;
import com.github.brubub.stdefgen.data.XMLFile;
import com.github.brubub.stdefgen.exceptions.ImpossibleToContinueExecutionException;
import com.github.brubub.stdefgen.exceptions.MethodNonAcceptableReturnException;

public class PomFilesMerger {
	
	public PomFilesMerger() {
	
	}
	
	public XMLFile generate(XMLFile parentPom, XMLFile childPom) throws ImpossibleToContinueExecutionException {
		XMLFile pomValuedContent = null;
		try {
			pomValuedContent = mergePoms(parentPom, childPom);
		} catch (MethodNonAcceptableReturnException e) {
			throw new ImpossibleToContinueExecutionException("Errore non riparabile",e);
		}
		try {
			pomValuedContent = deleteDependencies(pomValuedContent);
		} catch (MethodNonAcceptableReturnException e) {
			System.err.println(e);
		}
		pomValuedContent = addDependencies(pomValuedContent);
		pomValuedContent = addBuildBlock(pomValuedContent);
		pomValuedContent = addMvnCoordinates(pomValuedContent);
		pomValuedContent = addProjectBlock(pomValuedContent);
		return pomValuedContent;
	}
	
	public int[] dependencySearcher(XMLFile blocksLines, String groupId, String artifactId) {
		int startingPosition;
		int endingPosition = -1;
		String line = null;
		String concatenated = "";
		String[] substr = {groupId, artifactId};
		extFor:
		for(int i=0; i<blocksLines.size(); i++) {
			line = blocksLines.get(i);
			startingPosition = 0;
			while(line.indexOf("<dependency>",startingPosition) > -1) { // trovata una dipendenza
				startingPosition = line.indexOf("<dependency>",startingPosition);
				endingPosition = line.indexOf("</dependency>", startingPosition);
				int tempstart = line.indexOf("<dependency>",startingPosition+1);
				if(endingPosition > -1) { //sulla stessa riga trovato un tag </dependency>
					if(tempstart > -1 && tempstart< endingPosition) { // </dependency> chiude un tag <dependency> successivo a quello in startingPosition
						return null;
					}
					concatenated = line.substring(startingPosition,endingPosition);
					if(Utils.containsSubStr(concatenated,substr)){
						return new int[] {i,i, startingPosition, endingPosition+"</dependency>".length()};
					}
					else {
						startingPosition = endingPosition;
						continue;
					}
				}
				
				if(tempstart > -1) {
					return null; //c'e' un altro tag <dependency> sulla stessa riga ma senza un </dependency> che chiudesse quello in startingPosition
				}
				
				// il tag </dependency> non c'e' sulla riga 'i' e quindi va cercato nelle righe successive
				concatenated = line.substring(startingPosition);
				String line2 = null;
				for(int k=i+1; k<blocksLines.size(); k++) {
					line2 = blocksLines.get(k);
					endingPosition = line2.indexOf("</dependency>");
					tempstart = line2.indexOf("<dependency>");
					if(endingPosition > -1) {
						if(tempstart > -1) {
							if (tempstart < endingPosition) { //il tag in endingPosition chiude un <dependency> diverso da quello in startingPosition
								return null;
							}
						}
						concatenated += line2.substring(0,endingPosition); //trovato il tag </dependecy> che chiude il tag in startingPosition
						if(Utils.containsSubStr(concatenated,substr)){ //trovata la dipendenza cercata
							return new int[] {i,k, startingPosition, endingPosition+"</dependency>".length()};
						}
						continue extFor;
					}
					else { //la riga e' il continuo del contenuto della dipendenza
						if(tempstart > -1) {
							return null;
						}
						concatenated += line2;
					}
				}
			}
		}
		return new int[] {-1}; //non trovata la riga desiderata
	}
		
	public XMLFile deleteDependency(XMLFile file, String groupId, String artifactId) throws MethodNonAcceptableReturnException {
		int[] position = dependencySearcher(file, groupId, artifactId);
		if( position!= null && position[0]>-1) {
			int startline = position[0];
			int finishline= position[1];
			int startindex = position[2];
			int finishindex = position[3];
			
			if(startline == finishline) {
				String temp = file.get(startline).substring(0,startindex);
				if(finishindex < file.get(startline).length()-1) {
					temp +=  file.get(startline).substring(finishindex);
				}
				file.set(startline,temp);
				return file;
			}
			for(int i=startline; i<=finishline;) {
				if(i == startline) {
					file.set(i, file.get(i).substring(0,startindex));
					++i;
				}
				else if ( i == finishline){
					if(finishindex == file.get(i).length()) {
						file.remove(i);
					}
					else{
						file.set(i, file.get(i).substring(finishindex+1));
					}
					++i;
				}
				else {
					file.remove(i);
					--finishline;
				}
			}
		}
		else {
			throw new MethodNonAcceptableReturnException("stdefgen.core.deleteDependency(): La dipendenza da cancellare <"+groupId+"><"+artifactId+"> non e' stata trovata nel file");
		}
		return file;
	}
	
	public ArrayList<String> copyPomBlocks(String[] keys, XMLFile file){
		ArrayList<String> merged = new ArrayList<String>();
		
		for(String tag: keys) {
			String openingTag = "<"+tag+">";
			String endingTag = "</"+tag+">";
			
			String row = null;
			secondFor:
			for(int i=0; i<file.size(); i++) {
				row = file.get(i);
				if(!row.contains(openingTag)) {
					continue;
				}
				else { //trovato il tag di apertura
					int start = row.indexOf(openingTag);
					int finish = row.indexOf(endingTag, start); //potrebbe restituire -1 se non si trova endingTag
					if(start < finish) { //il tag di apertura e quello di chiusura si trovano sulla stessa riga
						merged.add( row.substring(start, finish + endingTag.length()) ); 
						break secondFor;
					}
					else { //il tag di chiusura va cercato nelle righe successive
						merged.add( row.substring(start) );
						String row2 = null;
						for(int k=i+1; k<file.size(); k++) {
							row2 = file.get(k);
							if(!row2.contains(endingTag)) {
								merged.add(row2);
							}
							else {
								merged.add( row2.substring(0, row2.indexOf(endingTag) + endingTag.length()) );
								break secondFor;
							}
						}
					}
				}
			}
		}
		return merged;
	}
	
	private XMLFile mergePoms(XMLFile parentPom, XMLFile childPom) throws MethodNonAcceptableReturnException{
		String[] blocchipadre = {"version","properties"};
		String[] blocchifiglio = {"dependencies"};
		
		ArrayList<String> blocchicopiatipadre = copyPomBlocks(blocchipadre, parentPom);
		
		if(blocchicopiatipadre==null || blocchicopiatipadre.isEmpty()) {
			throw new MethodNonAcceptableReturnException("stdefgen.core.mergePoms(): Non e' stato possibile copiare i blocchi "+Arrays.toString(blocchipadre)+" dal file pom padre "+parentPom);
		}
		
		ArrayList<String> blocchicopiatifiglio = copyPomBlocks(blocchifiglio, childPom);
		
		if(blocchicopiatifiglio==null || blocchicopiatifiglio.isEmpty()) {
			throw new MethodNonAcceptableReturnException("stdefgen.core.mergePoms(): Non e' stato possibile copiare i blocchi "+Arrays.toString(blocchifiglio)+" dal file pom figlio "+childPom);
		}
		
		XMLFile nuovopom = new XMLFile();
		nuovopom.addAll(blocchicopiatipadre);
		nuovopom.addAll(blocchicopiatifiglio);
		
		return nuovopom;
	}
	
	private XMLFile deleteDependencies(XMLFile blocksLines) throws MethodNonAcceptableReturnException {
		String[][] dependencies = {{"it.uniroma2.art.semanticturkey","st-codegen-processor"}}; 
		for(String[] dependency: dependencies) {
			blocksLines = deleteDependency(blocksLines, dependency[0], dependency[1]);
		}
		return blocksLines;
	}
	
	private XMLFile addDependencies(XMLFile file) {
		String[] dependencies = dependenciesToAdd();
		for(int i=0; i<file.size(); i++) {
			if(file.get(i).contains("<dependencies>")) {
				int index = file.get(i).indexOf("</dependencies>");
				if(index != -1) { //il blocco delle dipendenze e' scritto su un'unica riga
					String temp = file.get(i).substring(index); //copiamo il tag di chiusura ed il contenuto dopo di esso
					file.set(i,file.get(i).substring(0,index)); //sovrascriviamo l'intera riga con il suo contenuto fino al tag di chiusura
					file.add(i+1,temp); //aggiungiamo la riga con il solo tag di chiusura ed il contenuto dopo di esso 
					//abbiamo spezzato la riga "i" in due: prima di </dependencies> e dopo
				}
				for(String dependency: dependencies) {
					file.add(i+1, dependency);
				}
			}
		}
		return file;
	}

	private XMLFile addBuildBlock(XMLFile blocksLines){
		String[] plugins = pluginsToAdd();
		blocksLines.add(0,"");
		for(int i=0; i<plugins.length ;i++) {
			blocksLines.add(i+1,plugins[i]);
		}
		return blocksLines;
	}
	
	private XMLFile addProjectBlock(XMLFile blocksLines){
		blocksLines.add(0,headerToAdd());
		blocksLines.add("</project>");
		return blocksLines;
	}
	
	private XMLFile addMvnCoordinates(XMLFile blocksLines){
		blocksLines.add(0,"");
		blocksLines.add(0,mvnCoordinatesToAdd());
		return blocksLines;
	}
	
	private String[] dependenciesToAdd(){
		String[] dependencies = {
				"	<dependency><groupId>org.springframework</groupId><artifactId>spring-webmvc</artifactId><version>3.2.8.RELEASE</version></dependency>",
				"	<dependency><groupId>io.swagger</groupId><artifactId>swagger-core</artifactId><scope>compile</scope><version>1.5.24</version></dependency>",
				"	<dependency><groupId>javax.xml.bind</groupId><artifactId>jaxb-api</artifactId><version>2.3.1</version></dependency>"
				};
		return dependencies;
	}
	
	private String[] pluginsToAdd() {
		String[] plugins = {
				"<build>",
				"	<plugins>",
				"		<plugin>",
				"			<groupId>com.github.kongchen</groupId>",
				"				<artifactId>swagger-maven-plugin</artifactId>",
				"				<version>3.1.8</version>",
				"				<configuration>",
				"					<apiSources>",
				"						<apiSource>",
				"							<springmvc>true</springmvc>",
				"							<locations>it.uniroma2.art.semanticturkey.services.core.controllers</locations>",
				"							<basePath>/it.uniroma2.art.semanticturkey/st-core-services</basePath>",
				"							<schemes>http</schemes>",
				"							<info><title>Semantic Turkey API</title>",
				"								<version>${project.version}</version>",
				"								<description>Swagger Specification 2.0 definition of Semantic Turkey created by st-def-gen using swagger-maven-plugin</description>",
				"								<contact>",
				"									<email>bruno.budris@students.uniroma2.eu</email>",
				"									<name>Bruno Budris</name>",
				"								</contact>",
				"							</info>",
				"							<swaggerDirectory>api definition</swaggerDirectory>",
				"							<outputFormats>json</outputFormats>",
				"						</apiSource>",
				"					</apiSources>",
				"				</configuration>",
				"			<executions>",
				"				<execution>",
				"					<phase>compile</phase>",
				"					<goals>",
				"						<goal>generate</goal>",
				"					</goals>",
				"				</execution>",
				"			</executions>",
				"		</plugin>",
				"<plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId><version>3.8.1</version>",
				"<configuration><source>1.8</source><target>1.8</target></configuration></plugin>",
				"</plugins></build>",
		};
		return plugins;
	}
	
	private String headerToAdd() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\"> <modelVersion>4.0.0</modelVersion>";
	}

	private String mvnCoordinatesToAdd() {
		return "	<groupId>abc</groupId><artifactId>xyz</artifactId>";
	}
}
