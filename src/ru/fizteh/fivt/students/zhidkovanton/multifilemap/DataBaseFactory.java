package ru.fizteh.fivt.students.zhidkovanton.multifilemap;

public class DataBaseFactory {
    public DataBase dataBase = null;
    public DataFactory dataFactory;

    public DataBaseFactory(String directory) {
        dataFactory = new DataFactory(directory);
    }
}
