package ru.fizteh.fivt.students.mazanovArtem.homeworkforlosers.robot;

import ru.fizteh.fivt.robot.Robot;
import ru.fizteh.fivt.robot.RobotFactory;
import ru.fizteh.fivt.robot.RobotLegType;

import java.io.OutputStream;

public class MyRobotFactory implements RobotFactory {
    public Robot createRobot(OutputStream output, int steps, RobotLegType firstStepLeg) {
        if (output == null) {
            throw new IllegalArgumentException("output is null");
        }
        if (firstStepLeg == null) {
            throw new IllegalArgumentException("firstStepLeg is null");
        }
        if (!(firstStepLeg.equals(RobotLegType.LEFT) || firstStepLeg.equals(RobotLegType.RIGHT))) {
            throw new IllegalArgumentException("firstStepLeg is invalid");
        }
        MyRobotLeg firstLeg = new MyRobotLeg(RobotLegType.LEFT, output, steps, firstStepLeg);
        MyRobotLeg secondLeg = new MyRobotLeg(RobotLegType.RIGHT, output , steps, firstStepLeg);
        MyRobot robot = new MyRobot(firstLeg, secondLeg, firstStepLeg);
        firstLeg.setRobot(robot);
        secondLeg.setRobot(robot);
        return robot;
    }
}
