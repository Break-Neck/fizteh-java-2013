package ru.fizteh.fivt.students.krivchansky.calculator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;


public class Calculator {
    private final static int RADIX = 19;
    private String token;
    private char currentChar;
    private InputStream numericExpression;
    private boolean prevSpace;
    private enum OperationType {
        PLUS("+"),
        MINUS("-"),
        MULT("*"),
        DIV("/"),
        OPEN("("),
        CLOSE(")"),
        END(".");
        private String opType;
        
        private OperationType(String type) {
            opType = type;
        }
        
        public static OperationType getByType(String type) throws NoSuchElementException {

            for (OperationType operation: OperationType.values()) {
                if (operation.opType.equals(type)) {
                    return operation;
                }
            }
            throw new NoSuchElementException("\"" + type + "\"" + " this operation is not supported");
        }

        public String getType() {
            return opType;
        }
    }
    
    Calculator() {
        token = "";
        prevSpace = false;
    }
    
    public boolean isOperation(char symbol)
    {
        for (OperationType operation: OperationType.values()) {
            if (operation.opType.equals(symbol) && symbol != '(' && symbol != ')') {
                return true;
            }
        }
        return false;
    }
    
    private void fail(String error) throws UnknownExpressionExceptions {
        throw new UnknownExpressionExceptions(error); 
    }
    
    private void getChar() throws UnknownExpressionExceptions {
        try {
            if(numericExpression.available() > 0){
            currentChar = (char) numericExpression.read();
        }
        else currentChar = '.';
        } catch (IOException except) { 
            currentChar = '.';
        }
        if (currentChar == ' ') {
            if (!prevSpace) {
                prevSpace = true;
                getChar();
            } else {
                fail("Two spaces in a row. Not supported numeric expression");
            }
        }
    }
    
    private void getToken() throws UnknownExpressionExceptions {
        prevSpace = false;
        if (currentChar == '(' || currentChar == ')' || currentChar == '-' || 
            currentChar == '+' || currentChar == '*' || currentChar == '/' || currentChar == '.') {
            char[] tempArr = {currentChar};
            token = new String(tempArr);
            getChar();
        } else {
            if( !valid() ){
                fail(currentChar + " is unknown.");
            }
            StringBuilder tokenBuild = new StringBuilder();
            while( (valid()) && (!prevSpace) ){
                tokenBuild.append(currentChar);
                getChar();
            }
            prevSpace = false;
            token = tokenBuild.toString();
        }    
        
    }
    
    public boolean valid() {
        if (currentChar >= '0' && currentChar <= '9' || 'A' <= currentChar && 'A' + RADIX - 10 >= currentChar) {
            return true;
        }
        return false;
    }
    
    public int doEverything() throws UnknownExpressionExceptions, ArithmeticException, NoSuchElementException {
        int result;
        getChar();
        getToken();
        result = parseNoPriority();
        if ((token.charAt(token.length() - 1) != '.')) {
            fail("Too long expression. Unable to work it out.");
        }
        return result;
    }    
    
    private boolean Numeral(String smth) {
        try {
            int tmp = Integer.parseInt(smth, RADIX);
        } catch (NumberFormatException op) {
            return false;
        }
        return true;
    }
     
    private int parseNoPriority() throws UnknownExpressionExceptions, ArithmeticException, NoSuchElementException {
        int result = parseHighPriority();
        int partialResult;
        while (token.equals("-") || token.equals("+")) {
            OperationType last = OperationType.getByType(token);
            if(last.opType == "-" || last.opType == "+") {
                if (last.opType == "+") {
                    getToken();
                    partialResult = parseHighPriority();
                    if (Integer.MAX_VALUE - Math.abs(partialResult) < Math.abs(result)) {
                        fail("Overflow");
                    }
                    result += partialResult;
                } else {
                    getToken();
                    partialResult = parseHighPriority();
                    if (Integer.MAX_VALUE - Math.abs(partialResult) < Math.abs(result)) {
                        fail("Overflow");
                    }
                    result -= partialResult;
                }
            }
            if (last.opType == "*" || last.opType == "/" || last.opType == ")" || last.opType == ".") {
                return result;
            }
        }
        return result;
    }
    
    private int parseHighPriority() throws UnknownExpressionExceptions, ArithmeticException, NoSuchElementException {
        int result = parseHighestPriority();
        int partialResult;
        while (token.equals("*") || token.equals("/")) {
            OperationType last = OperationType.getByType(token);
            if (last.opType == "*" || last.opType == "/") {
                if (last.opType == "*") {
                    getToken();
                    partialResult = parseHighestPriority();
                    if (partialResult != 0 && Integer.MAX_VALUE / Math.abs(partialResult) < Math.abs(result)) {
                        fail("Overflow");
                    }
                    result *= partialResult;
                } else {
                    getToken();
                    partialResult = parseHighestPriority();
                    if (0 != partialResult){
                        result /= partialResult;
                    } else throw new ArithmeticException("Division by zero.");
                }
            }
            if (last.opType == "-" || last.opType == "+" || last.opType == ")" || last.opType == ".") {
                return result;
            }
        }
        return result;
    }
    
    private int parseHighestPriority() throws UnknownExpressionExceptions, ArithmeticException {
        int result = 0;
        if (token.equals("(")) {
            getToken();
            result = parseNoPriority();
            if (!token.equals(")")) {
                fail("Broken bracket balance.");
            }
            getToken();
        } else if(Numeral(token)) {
            result = Integer.parseInt(token, RADIX);
            getToken();
        } else if(token.equals("-")) {
            getToken();
            result = -parseHighestPriority();        
        } else {
            if(token.equals(".")) {
                fail("Unexpected end of expression.");
            } else {
                fail(token + " bad placement."); 
            }
        }
        return result;
    }
    
    public static void main(String[] args) {
        String input;
        Integer Result;
        Calculator worker = new Calculator();
        StringBuilder inputBuild = new StringBuilder();
        for (String t : args) {
            inputBuild.append(t + " ");
        }
        input = inputBuild.toString();
        if (input.equals("")) {
            System.out.println("No way! Something must be entered!");
            System.exit(1);
        }
        worker.numericExpression =     new ByteArrayInputStream(input.getBytes());
        try {
            Result = worker.doEverything();
            System.out.println(Integer.toString(Result, RADIX).toUpperCase());
        } catch (UnknownExpressionExceptions except) {
            System.out.println(except.getMessage() + " Me not understand you.");
            System.exit(2);
        }
        catch (ArithmeticException except) {
            System.out.println(except.getMessage() + " I can't divide by zero.");
            System.exit(3);
        }
        catch (NoSuchElementException except) {
            System.out.println(except.getMessage() + " Me not understand you.");
            System.exit(4);
        }
        System.exit(0);
    }
}


