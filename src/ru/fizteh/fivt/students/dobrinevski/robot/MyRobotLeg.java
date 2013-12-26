package ru.fizteh.fivt.students.dobrinevski.robot;

import ru.fizteh.fivt.robot.RobotLeg;
import ru.fizteh.fivt.robot.RobotLegType;

import java.io.OutputStream;

public class MyRobotLeg extends RobotLeg {
    private static boolean turn;
    private Integer step;
    private static final Object LOCK = new Object();

    public MyRobotLeg(RobotLegType type, OutputStream output, boolean myTurn, int stepCount) {
        super(type, output);
        turn = myTurn;
        step = stepCount;
    }

    public boolean step() {
        if (step == 0) {
            return false;
        }
        while (true) {
            if (getType() == RobotLegType.RIGHT && !turn) {
                makeStep();
                step--;
                turn = !turn;
                return true;
            }
            if (getType() == RobotLegType.LEFT && turn) {
                makeStep();
                step--;
                turn = !turn;
                return true;
            }
        }
    }
}
