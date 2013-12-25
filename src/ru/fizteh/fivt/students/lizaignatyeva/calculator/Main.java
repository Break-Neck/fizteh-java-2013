package ru.fizteh.fivt.students.lizaignatyeva.calculator;

public class Main {
    public static void main(String[] args) {
        new Calculator().run(args, 17);
    }
}

class Calculator {
    private char nextChar;
    private String currentToken;
    private String expression;
    private int currentIndexInExpression = 0;
    private int base;

    private void getNextChar() {
        if (currentIndexInExpression < expression.length()) {
            nextChar = expression.charAt(currentIndexInExpression);
        }
        currentIndexInExpression++;
    }

    private boolean isDigit(char character) {
        if (base != 17) {
            throw new IllegalArgumentException("base is not valid");
        }
        return Character.isDigit(character) 
               || ('A' <= character && character <= 'G') 
               || ('a' <= character && character <= 'g');
    }

    private boolean isSyntaxSymbol(char character) {
        return (character == '(') || (character == ')') 
               || (character == '*') || (character == '-') 
               || (character == '+') || (character == '/');
    }

    private boolean canBeAnInteger(long value) {
        return (Integer.MIN_VALUE <= value) && (value <= Integer.MAX_VALUE);
    }

    private int safeAddition(int x, int y) {
        long result = (long) x + (long) y;
        if (canBeAnInteger(result)) {
            return (int) result;
        } else {
            throw new ArithmeticException("integer overflow");
        }
    }

    private int safeMultiplication(int x, int y) {
        long result = (long) x * (long) y;
        if (canBeAnInteger(result)) {
            return (int) result;
        } else {
            throw new ArithmeticException("integer overflow");
        }
    }

    private int safeDivision(int x, int y) {
        if (y == 0) {
            throw new ArithmeticException("division by zero");
        }
        if ((x == -Integer.MIN_VALUE) && (y == -1)) {
            throw new ArithmeticException("integer overflow");
        }
        return x / y;
    }

    private void getNextToken() {
        if (isDigit(nextChar)) {
            StringBuilder number = new StringBuilder();
            while (isDigit(nextChar)) {
                number.append(nextChar);
                getNextChar();
            }
            currentToken = number.toString();
        } else {
            currentToken = Character.toString(nextChar);
            getNextChar();
        }
    }

    private int readExpr() {
        int res = readAdd();
        while (currentToken.equals("+") || currentToken.equals("-")) {
            char buf = currentToken.charAt(0);
            getNextToken();
            if ((!isDigit(currentToken.charAt(0)))
                    && (!currentToken.equals("(") && (!currentToken.equals("-")))) {
                String mistake = "symbol " + Character.toString(currentToken.charAt(0));
                if (currentToken.charAt(0) == '.') {
                    mistake = "EOLN";
                }
                throw new IllegalArgumentException("a digit expected; " + mistake + " found");
            }
                
            int add = readAdd();
            if (buf == '+') {
                res = safeAddition(res, add);
            }
            if (buf == '-') {
                res = safeAddition(res, -1 * add);
            }
        }
        return res;
    }

    private int readAdd() {
        int res = readMul();
        while (currentToken.equals("*") || currentToken.equals("/")) {
            char buf = currentToken.charAt(0);
            getNextToken();
            int mul = readMul();
            if (buf == '*') {
                res = safeMultiplication(res, mul);
            } else {
                res = safeDivision(res, mul);
            }
        }
        return res;
    }

    private int readMul() {
        int res;
        if (currentToken.equals("(")) {
            getNextToken();
            res = readExpr();
            if (!currentToken.equals(")")) {
                throw new IllegalArgumentException("a closing bracket expected");
            }
            getNextToken();
        } else {
            int sign = 1;
            if (currentToken.charAt(0) == '-') {
                sign = -1;
                getNextToken();
            }
            if (!isDigit(currentToken.charAt(0))) {
                throw new IllegalArgumentException("a valid digit expected");
            }
            res = sign * Integer.parseInt(currentToken, base);
            getNextToken();
        }
        return res;
    }


    private String cleanFromSpaces(String s) {
        s = s.replaceAll("\\s\\s", "\\s");
        //System.out.println(s); //debug output
        for (int i = 1; i < s.length() - 1; ++i) {
            if (isDigit(s.charAt(i - 1)) && isDigit(s.charAt(i + 1)) && (s.charAt(i) == ' ')) {
                throw new IllegalArgumentException("there are two consequent numbers");
            }
        }
        s = s.replaceAll("\\s", "");
        return s;
    }

    private String concatenateStrings(String[] args) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            s.append(args[i]);
            s.append(" ");
        }
        return cleanFromSpaces(s.toString());
    }

    private void checkString() {
        for (int i = 0; i < expression.length(); ++i) {
            char currChar = expression.charAt(i);
            if (!(isDigit(currChar) || isSyntaxSymbol(currChar))) {
                throw new IllegalArgumentException("invalid symbol found");
            }
        }
    }

    private void printUsage() {
        System.out.println("It is a java-based calculator with base 17 numeral system");
        System.out.println("A valid number: a sequence of [0-9a-gA-G]");
        System.out.println("Valid operations: + - * /");
        System.out.println("You can also use brackets to write longer expressions");
    }


    public void run(String[] args, int ourBase) {
        try {
            if (args.length == 0) {
                printUsage();
                throw new IllegalArgumentException("nothing to count :(");
            }
            base = ourBase;
            expression = concatenateStrings(args);
            
            //System.out.println(expression); //debug output
            checkString();
            expression += ".";
            getNextChar();
            getNextToken();
            int ans = readExpr();
            if (!currentToken.equals(".")) {
                throw new IllegalArgumentException("symbols after expected end");
            }
            System.out.println(Integer.toString(ans, base));
        } catch (ArithmeticException e) {
            System.err.println("Invalid operation: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
            System.exit(1);
        }
    }
}


