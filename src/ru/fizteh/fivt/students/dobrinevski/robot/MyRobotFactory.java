package ru.fizteh.fivt.students.dobrinevski.robot;

import ru.fizteh.fivt.robot.*;

import java.io.OutputStream;

public class MyRobotFactory implements RobotFactory {

    public Robot createRobot(OutputStream output, int steps, RobotLegType firstStepLeg) {
        if(steps < 1) {
            throw new IllegalArgumentException("Irregular count of steps");
        }
        if(output == null) {
            throw new IllegalArgumentException("output is null");
        }
        MyRobotLeg left;
        MyRobotLeg right;
        if(firstStepLeg == RobotLegType.LEFT) {
            //left = new MyRobotLeg(RobotLegType.LEFT, output, true, steps/2)
        }
        return new Robot(new MyRobotLeg(RobotLegType.LEFT, output), new MyRobotLeg(RobotLegType.RIGHT, output));
    }
}
