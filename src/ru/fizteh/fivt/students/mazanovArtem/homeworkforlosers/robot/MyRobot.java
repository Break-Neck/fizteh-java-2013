package ru.fizteh.fivt.students.mazanovArtem.homeworkforlosers.robot;

import ru.fizteh.fivt.robot.Robot;
import ru.fizteh.fivt.robot.RobotLeg;
import ru.fizteh.fivt.robot.RobotLegType;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyRobot extends Robot {
    Lock lock;
    RobotLegType currentLeg;

    MyRobot(RobotLeg left, RobotLeg right, RobotLegType currentLeg) {
        super(left, right);
        this.currentLeg = currentLeg;
        lock = new ReentrantReadWriteLock().writeLock();
    }
}
