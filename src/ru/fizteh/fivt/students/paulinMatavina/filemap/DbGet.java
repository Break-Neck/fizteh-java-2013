package ru.fizteh.fivt.students.paulinMatavina.filemap;

import ru.fizteh.fivt.students.paulinMatavina.utils.*;

public class DbGet implements Command {
    @Override
    public int execute(String[] args, State state) {
        String key = args[0];
        MyTableProvider multiState = (MyTableProvider) state;
        if (multiState.getCurrTable() == null) {
            System.out.println("no table");
            return 0;
        }
        
        String result = multiState.getCurrTable().get(key);  
        if (result != null) {
            System.out.println("found");
            System.out.println(result);
        } else {
            System.out.println("not found");
        }
        return 0;
    }
    
    @Override
    public String getName() {
        return "get";
    }
    
    @Override
    public int getArgNum() {
        return 1;
    }
    
    @Override
    public boolean spaceAllowed() {
        return false;
    }
}
