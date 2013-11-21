package ru.fizteh.fivt.students.dubovpavel.multifilehashmap;

import ru.fizteh.fivt.students.dubovpavel.filemap.Serial;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DataBaseMultiFileHashMap<V> extends FileRepresentativeDataBase<V> {
    private static final int dirsCount = 16;
    private static final int chunksCount = 16;
    protected File root;

    public DataBaseMultiFileHashMap(File path, Serial<V> builder) {
        super(builder);
        root = path;
    }

    public File getPath() {
        return root;
    }

    private void setPath(File path) {
        savingEndPoint = path;
    }

    private File generateChunksDir(int dir) {
        return new File(root, String.format("%d.dir", dir));
    }

    private File generateChunk(int dir, int chunk) {
        return new File(generateChunksDir(dir), String.format("%d.dat", chunk));
    }

    private Distribution<Integer, Integer> getDistribution(String key) {
        int key_hash = key.hashCode(), hashcode;
        if(key_hash == Integer.MIN_VALUE) {
            hashcode = 0;
        } else {
            hashcode = Math.abs(key_hash);
        }
        int ndirectory = hashcode % 16;
        int nfile = hashcode / 16 % 16;
        return new Distribution<Integer, Integer>(ndirectory, nfile);
    }

    @Override
    public void open() throws DataBaseException {
        boolean allReadSuccessfully = true;
        HashMap<String, V> chunksCollector = new HashMap<>();
        StringBuilder exceptionMessage = new StringBuilder();
        for(int i = 0; i < dirsCount; i++) {
            File sub = generateChunksDir(i);
            if(sub.isDirectory()) {
                boolean dirIsEmpty = true;
                for(int j = 0; j < chunksCount; j++) {
                    File data = generateChunk(i, j);
                    if(data.isFile()) {
                        dirIsEmpty = false;
                        try {
                            dict = new HashMap<>();
                            setPath(data);
                            super.open();
                            if(dict.size() == 0) {
                                throw new DataBaseException(String.format("Chunk can not be empty"), false);
                            }
                            for(String key: dict.keySet()) {
                                Distribution<Integer, Integer> distr = getDistribution(key);
                                if(i != distr.getDir() || j != distr.getChunk()) {
                                    throw new DataBaseException(
                                            String.format("Key '%s' must not belong to this chunk. The whole chunk denied", key), false);
                                }
                            }
                            chunksCollector.putAll(dict);
                        } catch(DataBaseException e) {
                            String errorMessage = String.format("%nChunk (%d, %d): %s", i, j, e.getMessage());
                            if(e.acceptable) {
                                allReadSuccessfully = false;
                                exceptionMessage.append(errorMessage);
                            } else {
                                throw new DataBaseException(errorMessage, false);
                            }
                        }
                    }
                }
                if(dirIsEmpty) {
                    throw new DataBaseException(String.format("Dir %d: Dir can not be empty", i), false);
                }
            }
        }
        dict = chunksCollector;
        if(!allReadSuccessfully) {
            throw new DataBaseException(exceptionMessage.toString());
        }
    }
    @Override
    public void save() throws DataBaseException {
        HashMap<String, V> backUp = dict;
        HashMap<String, V>[][] distribution = new HashMap[dirsCount][chunksCount];
        for(int i = 0; i < dirsCount; i++) {
            File sub = generateChunksDir(i);
            for(int j = 0; j < chunksCount; j++) {
                File data = generateChunk(i, j);
                distribution[i][j] = new HashMap<>();
                if(data.isFile()) {
                    if(!data.delete()) {
                        dict = backUp;
                        throw new DataBaseException(String.format("Can not delete chunk '%s'", data.getPath()));
                    }
                }
            }
            if(sub.isDirectory() && sub.listFiles().length == 0 && !sub.delete()) {
                dict = backUp;
                throw new DataBaseException(String.format("Can not delete directory '%s'", sub.getPath()));
            }
        }
        for(Map.Entry<String, V> entry: dict.entrySet()) {
            Distribution<Integer, Integer> distr = getDistribution(entry.getKey());
            distribution[distr.getDir()][distr.getChunk()].put(entry.getKey(), entry.getValue());
        }
        for(int i = 0; i < dirsCount; i++) {
            File sub = generateChunksDir(i);
            for(int j = 0; j < chunksCount; j++) {
                if(!distribution[i][j].isEmpty()) {
                    if(!sub.isDirectory() && !sub.mkdir()) {
                        dict = backUp;
                        throw new DataBaseException(String.format("Chunk (%d, *): Can not create directory", i));
                    }
                    dict = distribution[i][j];
                    setPath(generateChunk(i, j));
                    try {
                        super.save();
                    } catch(DataBaseException e) {
                        dict = backUp;
                        throw new DataBaseException(String.format("Chunk (%d, %d): %s", i, j, e.getMessage()));
                    }
                }
            }
        }
        dict = backUp;
    }
}
