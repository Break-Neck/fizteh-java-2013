package src.ru.fizteh.fivt.students.krivchansky.multifilemap;

import java.io.File;
import java.io.IOException;

import src.ru.fizteh.fivt.students.krivchansky.filemap.*;


public class MultiFileMapReadingUtils {
	public static void load(TableBuilder build) throws IOException {
		File tableDir = build.getTableDirectory();
		if (tableDir.listFiles() == null) {
			return;
		}
		
		for (File dir : tableDir.listFiles()) {
			if(dir.isFile()) {
				continue;
			}
			
			if(dir.listFiles().length == 0) {
				throw new IllegalArgumentException("empty bucket");
			}
			
			for(File file : dir.listFiles()) {
				build.setCurrentFile(file);
				FileMapReadingUtils.scanFromDisk(file.getAbsolutePath(), build);
			}
		}
		
	}

}
