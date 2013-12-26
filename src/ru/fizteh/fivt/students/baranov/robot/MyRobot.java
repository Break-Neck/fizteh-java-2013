package ru.fizteh.fivt.students.baranov.robot;

import ru.fizteh.fivt.robot.Robot;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class MyRobot extends Robot {
    private int maxNumberOfSteps;
    public Integer numberOfMadeSteps;
    //public Lock lock;

    public MyRobot(MyRobotLeg leftLeg, MyRobotLeg rightLeg, int steps) {
        super(leftLeg, rightLeg);
        this.maxNumberOfSteps = steps;
        this.numberOfMadeSteps = 0;
        leftLeg.master = this;
        rightLeg.master = this;
        //ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
        //lock = readWriteLock.writeLock();
    }

    public int getMaxNumberOfSteps() {
        return maxNumberOfSteps;
    }

}
