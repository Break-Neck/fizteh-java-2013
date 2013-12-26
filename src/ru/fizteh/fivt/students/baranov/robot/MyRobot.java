package ru.fizteh.fivt.students.baranov.robot;

import ru.fizteh.fivt.robot.Robot;

public class MyRobot extends Robot {
    public Integer maxNumberOfSteps;
    public int numberOfMadeSteps;

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
