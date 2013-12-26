package ru.fizteh.fivt.students.dobrinevski.robot.tests;

import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.robot.Robot;
import ru.fizteh.fivt.robot.RobotFactory;
import ru.fizteh.fivt.robot.RobotLegType;
import ru.fizteh.fivt.robot.RobotWalker;
import ru.fizteh.fivt.students.dobrinevski.robot.MyRobotFactory;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class RobotFactoryTester {
    public static RobotFactory robotFac;
    public static Robot robot;
    public static ByteArrayOutputStream ostream;

    @Before
    public void init() throws InterruptedException {
        robotFac = new MyRobotFactory();
        ostream = new ByteArrayOutputStream();
    }

    @Test (expected = IllegalArgumentException.class)
    public void nullStepsGiven() {
        robot = robotFac.createRobot(ostream, 0, RobotLegType.RIGHT);
    }

    @Test (expected = IllegalArgumentException.class)
    public void nullStreamGiven() {
        robot = robotFac.createRobot(null, 100, RobotLegType.RIGHT);
    }

    @Test (expected = IllegalArgumentException.class)
    public void nullFirstStepGiven() {
        robot = robotFac.createRobot(ostream, 100, null);
    }

    @Test
    public void uberManyWalks() throws InterruptedException {
        Random rand = new Random();
        int tests = 5;
        while (tests != 0) {
            int moves = Math.abs(rand.nextInt()) % 100 + 1;
            boolean firstTurn = rand.nextBoolean();
            robot = robotFac.createRobot(ostream, moves, firstTurn ? RobotLegType.LEFT : RobotLegType.RIGHT);
            RobotWalker.walk(robot);
            Scanner scanner = new Scanner(ostream.toString());
            try {
                int i = 0;
                while (scanner.hasNextLine()) {
                    String command = scanner.nextLine();
                    if ((i % 2 == 0 && firstTurn) || (i % 2 == 1 && !firstTurn)) {
                        assertEquals(RobotLegType.LEFT.toString(), command);
                    } else {
                        assertEquals(RobotLegType.RIGHT.toString(), command);
                    }
                    i++;
                }
                assertEquals(moves, i);
            } finally {
                scanner.close();
            }
            tests--;
            ostream.reset();
        }
    }


}
