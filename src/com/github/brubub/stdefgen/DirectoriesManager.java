package com.github.brubub.stdefgen;

import java.io.IOException;
import java.io.File;

import org.apache.commons.io.FileUtils;

public class DirectoriesManager {
	
	private String srcDir;
	private String parentDir;
	private String fakeDir;
	private String coreServDir;

	public DirectoriesManager(String dir) {
		this.srcDir = dir;
		this.parentDir = getParentDir(this.srcDir);
		this.fakeDir = this.parentDir+"/st-def-gen_-_"+getDirName(this.srcDir);
		this.coreServDir = this.srcDir+"/st-core-services";
	}
	
	public DirectoriesManager(String srcDir, String destDir) {
		this(srcDir);
		this.fakeDir = destDir+"/st-def-gen_-_"+getDirName(this.srcDir);
	}
	
	public String getSrcDir() {
		return this.srcDir;
	}
	
	public String getCoreServDir() {
		return this.coreServDir;
	}
	
	public String getFakeDir() {
		return this.fakeDir;
	}
	
	public String getControllersSubpath() {
		return "/src/main/java/it/uniroma2/art/semanticturkey/services/core/controllers";
	}
	
	public String getGeneratedControllersSubpath() {
		return "/target/generated-sources/annotations/it/uniroma2/art/semanticturkey/services/core/controllers";
	}
	
	public boolean createFakeDir() throws IOException {
		if(Utils.isDirectory(this.fakeDir)) {
			FileUtils.deleteDirectory(new File(this.fakeDir));
		}
		return new File(this.fakeDir).mkdirs();
	}	
	
	private String getParentDir(String dir) {
		if(dir!=null && !dir.equals("")) {
			int index = dir.lastIndexOf("/");
			return dir.substring(0,index);
		}
		return null;
	}
	
	private String getDirName(String path) {
		if(path != null) {
			int index = path.lastIndexOf("/");
			if(index > -1) {
				return path.substring(index+1);
			}
		}
		return null;
	}
	
	
}
