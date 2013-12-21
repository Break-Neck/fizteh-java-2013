package src.ru.fizteh.fivt.students.krivchansky.multifilemap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import src.ru.fizteh.fivt.students.krivchansky.filemap.FileMapWritingUtils;
import src.ru.fizteh.fivt.students.krivchansky.filemap.GlobalUtils;
import src.ru.fizteh.fivt.students.krivchansky.filemap.TableBuilder;

public class MultiFileMapWritingUtils {

	public static void save(TableBuilder build) throws IOException {
		File tableDir = build.getTableDirectory();
		if (tableDir.listFiles() == null) {
			return;
		}
		ArrayList<Set<String>> toSave = new ArrayList<Set<String>>();
		boolean dirIsEmpty;

		for (int dirNumber = 0; dirNumber < GlobalUtils.DIR_QUANTITY; ++dirNumber) {
			toSave.clear();
			for (int fileNumber = 0; fileNumber < GlobalUtils.FILES_PER_DIR; ++fileNumber) {
				toSave.add(new HashSet<String>());
			}
			dirIsEmpty = true;

			for (String key : build.getKeys()) {
				if (GlobalUtils.getDirNumber(key) == dirNumber) {
					int fileNumber = GlobalUtils.getFileNumber(key);
					toSave.get(fileNumber).add(key);
					dirIsEmpty = false;
				}
			}
			String dirName = dirNumber + ".dir";
			File dir = new File(tableDir, dirName);
			if (dirIsEmpty) {
				GlobalUtils.deleteFile(dir);
			}
			for (int fileNumber = 0; fileNumber < GlobalUtils.FILES_PER_DIR; ++fileNumber) {
				String fileName = fileNumber + ".dat";
				File file = new File(dir, fileName);
				if (toSave.get(fileNumber).isEmpty()) {
					GlobalUtils.deleteFile(file);
					continue;
				}
				if (!dir.exists()) {
					dir.mkdir();
				}
				FileMapWritingUtils.writeOnDisk(toSave.get(fileNumber),
						file.getAbsolutePath(), build);
			}
		}
	}
}
