package ru.fizteh.fivt.students.baranov.robot;

import ru.fizteh.fivt.robot.Robot;
import ru.fizteh.fivt.robot.RobotLegType;


public class Main {
    public static void main(String[] args) {
        MyRobotFactory factory = new MyRobotFactory();
        MyRobot robot = factory.createRobot(System.out, 10, RobotLegType.LEFT);
        Walker walker = new Walker();
        try {
            walker.walk(robot);
        } catch (InterruptedException e) {
            //
        }
    }
}
