package ru.fizteh.fivt.students.dzvonarev.calc;

import java.util.Stack;
import java.math.BigInteger;

class Calc {

    public static int priority(Character str) {
        if (str.equals('+') || str.equals('-')) {
            return 1;
        }
        if (str.equals('*') || str.equals('/')) {
            return 2;
        }
        if (str.equals('(')) {
            return 0;
        }
        if (!str.equals('*') && !str.equals('-') && !str.equals('/')
                && !str.equals('+') && !str.equals('(')) {
            System.err.println("Wrong operation");
            System.exit(1);
        }
        return 0;
    }

    public static BigInteger doOp(BigInteger num1, Character op, BigInteger num2) {
        if (op.equals('+')) {
            return num1.add(num2);
        }
        if (op.equals('*')) {
            return num1.multiply(num2);
        }
        if (op.equals('-')) {
            return num1.subtract(num2);
        }
        if (op.equals('/') && !num2.equals(BigInteger.ZERO)) {
            return num1.divide(num2);
        } else {
            System.err.println("Dividing by zero");
            System.exit(1);
        }
        return BigInteger.ZERO;
    }

    private static Stack<BigInteger> number = new Stack<BigInteger>();
    private static Stack<Character> operation = new Stack<Character>();

    public static void currentOperation() {
        BigInteger num1 = number.pop();
        BigInteger num2 = number.pop();
        Character op = operation.pop();
        number.push(doOp(num2, op, num1));
    }

    public static void main(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            int j = 0;
            while (j < args[i].length()) {
                String str = "";
                boolean num = true;
                while (num && j < args[i].length()) {
                    String strNew = str + args[i].charAt(j);
                    if (strNew.matches("[0-9A-Ia-i]+")) {
                        str = strNew;
                        num = true;
                        ++j;
                    } else {
                        num = false;
                    }
                }
                str = str.substring(0, str.length()); /*
                                                       * deleting last - not
                                                       * number symbol
                                                       */
                if (!str.isEmpty()) { /* we've found number */
                    BigInteger n = new BigInteger(str, 19);
                    number.push(n);
                    continue;
                }
                Character ch = args[i].charAt(j);
                if (ch.equals(' ')) {
                    ++j;
                    continue;
                }

                if (!ch.equals('+') && !ch.equals('-') && !ch.equals('*')
                        && !ch.equals('/') && !ch.equals('(')
                            && !ch.equals(')')) {
                    System.err.println("Unknown symbol =  " + ch);
                    System.exit(1);
                }

                if (ch.equals('+') || ch.equals('-') || ch.equals('*')
                        || ch.equals('/')) { /* we've found operation */
                    if (operation.isEmpty()) {
                        operation.push(ch);
                    } else {
                        if (priority(operation.peek()) < priority(ch)) {
                            operation.push(ch);
                        } else { /* now we do if op's bigger priority then now */
                            while (priority(operation.peek()) >= priority(ch)) {
                                if (number.size() < 2) {
                                    System.err.println("I can't do " + ch
                                            + " operation");
                                    System.exit(1);
                                }
                                currentOperation();
                                if (operation.isEmpty()) {
                                    break;
                                }
                            }
                            operation.push(ch);
                        }
                    }
                    ++j;
                    continue;
                }

                if (ch.equals('(')) {
                    operation.push(ch);
                    ++j;
                    continue;
                }

                if (ch.equals(')')) {
                    if (!operation.isEmpty()) {
                        while (!operation.peek().equals('(')) {
                            currentOperation();
                            if (operation.isEmpty()) {
                                System.err.println("Wrong bracket sequence");
                                System.exit(1);
                            }
                        }
                        operation.pop(); // we pop open clause
                    } else {
                        System.err.println("Wrong bracket sequence");
                        System.exit(1);
                    }
                    ++j;
                    continue;
                }
            }
        }

        while (!operation.isEmpty()) {
            if (number.size() < 2) {
                System.err.println("I can't do " + operation.peek()
                        + " operation");
                System.exit(1);
            }
            if (operation.peek().equals('(')) {
                System.err.println("Wrong bracket sequence");
                System.exit(1);
            }
            currentOperation();
        }
        if (number.size() != 1) {
            System.err.println("Wrong expression");
            System.exit(1);
        }
        System.out.println(number.pop().toString(19));
    }
}
