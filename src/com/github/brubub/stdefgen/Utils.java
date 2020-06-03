package com.github.brubub.stdefgen;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

public class Utils {
	
	private static final String operatingSystem = System.getProperty("os.name").toLowerCase();

	public static String getOS() {
		return operatingSystem;
	}
	
	public static String substractString(String minuendo, String sottraendo) {
		String risultato = null;
		String preSottraendo = "";
		String postSottraendo = "";
		int startsAt = minuendo.indexOf(sottraendo);
		if(startsAt!=-1) {
			if(startsAt>0) {
				preSottraendo = minuendo.substring(0,startsAt-1);
			}
			postSottraendo = minuendo.substring(startsAt+sottraendo.length());
			risultato = preSottraendo + postSottraendo;
		}
		return risultato;
	}
	
	public static boolean containsSubStr(String str, String[] substrings) {
		for(String sub: substrings) {
			if(!str.contains(sub)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isValidPath(String path) {
	    try {
	        Paths.get(path);
	    } catch (InvalidPathException | NullPointerException ex) {
	        return false;
	    }
	    return true;
	}
	
	public static boolean fileExists(String path) {
		File f = new File(path);
		if(f.exists()) {
			return true;
		}
		return false;
	}
	
	public static boolean isDirectory(String path) {
		File f = new File(path);
		return f.isDirectory();
	}

	public static boolean dirIsEmpty(String path) {
		File directory = new File(path);
		File[] dirfiles = directory.listFiles();
		if(dirfiles.length>0) {
			return false;
		}
		return true;
	}

	public static void copyDir(String from, String to) {
		File srcDir = new File(from);
		File destDir = new File(to);
		try {
		    FileUtils.copyDirectory(srcDir, destDir);
		} 
		catch (IOException e) {
		    e.printStackTrace();
		}
	}
}
