package net.xmeter.ethereum.common;

import java.io.File;

public class FileUtil {
	public static String handleUIFileName(String jmxPath, String file) {
		//File jmxPath = new File(FileServer.getFileServer().getBaseDir());
		File jmxFile = new File(jmxPath);
		
		File theFile = new File(file);
		File theFileParent = theFile.getParentFile().getAbsoluteFile();
		
		if(jmxFile.toString().equals(theFileParent.toString())) {
			return theFile.getName();
		} else {
			return file;
		}
	}
	
	public static String handleSamplerFileName(String jmxPath, String file) {
		File theFile = new File(file);
		if(!theFile.isAbsolute()) {
			File ret = new File(jmxPath, file);
			return ret.getAbsolutePath();
		}
		return file;
	}
	
}
