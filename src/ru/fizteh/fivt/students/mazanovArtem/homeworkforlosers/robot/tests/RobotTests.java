package ru.fizteh.fivt.students.mazanovArtem.homeworkforlosers.robot.tests;

import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.robot.Robot;
import ru.fizteh.fivt.robot.RobotFactory;
import ru.fizteh.fivt.robot.RobotLegType;
import ru.fizteh.fivt.robot.RobotWalker;
import ru.fizteh.fivt.students.mazanovArtem.homeworkforlosers.robot.MyRobotFactory;

import java.io.ByteArrayOutputStream;


public class RobotTests {
    RobotFactory robotFactory;
    Robot robot;
    public static ByteArrayOutputStream output;

    @Before
    public void init() {
        robotFactory = new MyRobotFactory();
        output = new ByteArrayOutputStream();
    }

    @Test
    public void ololo() throws InterruptedException {
        robot = robotFactory.createRobot(output, 123 , RobotLegType.RIGHT);
        RobotWalker.walk(robot);
    }
}
