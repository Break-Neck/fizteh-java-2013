package ru.fizteh.fivt.students.dobrinevski.robot;

import ru.fizteh.fivt.robot.Robot;
import ru.fizteh.fivt.robot.RobotFactory;
import ru.fizteh.fivt.robot.RobotLegType;

import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyRobotFactory implements RobotFactory {

    public Robot createRobot(OutputStream output, int steps, RobotLegType firstStepLeg) {

        if (steps < 1) {
            throw new IllegalArgumentException("Irregular count of steps");
        }
        if (output == null) {
            throw new IllegalArgumentException("output is null");
        }
        if (firstStepLeg == null) {
            throw new IllegalArgumentException("third arg is null");
        }
        MyRobotLeg left;
        MyRobotLeg right;
        Lock lock = new ReentrantLock();
        if (firstStepLeg == RobotLegType.LEFT) {
            left = new MyRobotLeg(RobotLegType.LEFT, output, true, (steps + 1) / 2, lock);
            right = new MyRobotLeg(RobotLegType.RIGHT, output, true, steps / 2, lock);
        } else {
            left = new MyRobotLeg(RobotLegType.LEFT, output, false, steps / 2, lock);
            right = new MyRobotLeg(RobotLegType.RIGHT, output, false, (steps + 1) / 2, lock);
        }

        return new Robot(left, right);
    }
}
