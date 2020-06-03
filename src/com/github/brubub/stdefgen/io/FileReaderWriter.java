package com.github.brubub.stdefgen.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.github.brubub.stdefgen.data.FileData;

import java.io.FileNotFoundException;
import java.io.FileWriter;

public class FileReaderWriter {
	
	public void writeToFile(FileData file, String path) throws IOException {
		try ( FileWriter writer = new FileWriter(path);	)
		{
			for(String line: file) {
				writer.write(line+System.lineSeparator());
			}
		}
		catch(IOException e) {
			throw e;
		}
	}
	
	public ArrayList<String> readFile(String path) throws FileNotFoundException, IOException{
		ArrayList<String> fileLines = new ArrayList<String>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(path)); )
		{	
			for(String line; (line = br.readLine()) !=null ;){
				fileLines.add(line);
			}
			return fileLines;
		}
		catch(IOException e) {
			throw e;
		}
	}
}
