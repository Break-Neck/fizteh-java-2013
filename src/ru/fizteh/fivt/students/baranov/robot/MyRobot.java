package ru.fizteh.fivt.students.baranov.robot;

import ru.fizteh.fivt.robot.Robot;
import ru.fizteh.fivt.robot.RobotLegType;


public class MyRobot extends Robot {
    private int maxNumberOfSteps;
    public int numberOfMadeSteps;
    public Object lock;

    public MyRobot(MyRobotLeg leftLeg, MyRobotLeg rightLeg, int steps) {
        super(leftLeg, rightLeg);
        this.maxNumberOfSteps = steps;
        this.numberOfMadeSteps = 0;
        leftLeg.master = this;
        rightLeg.master = this;
    }

    public int getMaxNumberOfSteps() {
        return maxNumberOfSteps;
    }

}
