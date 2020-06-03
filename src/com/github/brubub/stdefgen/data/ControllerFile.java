package com.github.brubub.stdefgen.data;

import java.io.File;
import java.util.ArrayList;

public class ControllerFile extends FileData {

	public ControllerFile() {
		super();
	}

	public ControllerFile(ArrayList<String> file, File from, File to) {
		super(file, from, to);
	}

	public ControllerFile(ArrayList<String> file, File from) {
		super(file, from);
	}

	public ControllerFile(ArrayList<String> file) {
		super(file);
	}

}
