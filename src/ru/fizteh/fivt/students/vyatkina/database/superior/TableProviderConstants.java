package ru.fizteh.fivt.students.vyatkina.database.superior;

public interface TableProviderConstants {

    String UNSUPPORTED_TABLE_NAME = "Unsupported table name";
    String IS_NOT_A_DIRECTORY = " is not a directory";
    String IS_NOT_A_FILE = " is not a file";
    String FILE_OR_DIRECTORY_DOES_NOT_EXIST = " file or directory does not exist";
    String TABLE_NOT_EXIST = "Table not exist";
    String UNEXPECTED_CLASS_IN_STORABLE = "Unexpected class in Storable. ";
    String EMPTY_DIRECTORY = "Empty table directory.";
    String BAD_FILE_NAME = "Bad file name";
    String EXPECTED = "Expected : ";
    String BUT_HAVE = " but have: ";
    int MAX_SUPPORTED_NAME_LENGTH = 1024;
    int NUMBER_OF_FILES = 16;
    int NUMBER_OF_DIRECTORIES = 16;
    String DOT_DIR = ".dir";
    String DOT_DAT = ".dat";
    String SIGNATURE_FILE = "signature.tsv";
    String PROPERTY_DIRECTORY = "fizteh.db.dir";
    String MAC_DS_FILE = ".DS_Store";
}
