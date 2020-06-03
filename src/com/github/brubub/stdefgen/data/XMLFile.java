package com.github.brubub.stdefgen.data;

import java.io.File;
import java.util.ArrayList;

public class XMLFile extends FileData {
	
	public XMLFile() {
		super();
	}

	public XMLFile(ArrayList<String> file) {
		super(file);
	}
	
	public XMLFile(ArrayList<String> file, File from, File to) {
		super(file, from, to);
	}
	
	public XMLFile(ArrayList<String> file, File from) {
		super(file, from);
	}

}
