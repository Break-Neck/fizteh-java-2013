package ru.fizteh.fivt.students.baranov.robot;

import ru.fizteh.fivt.robot.Robot;
import ru.fizteh.fivt.robot.RobotLeg;


/**
 * @author Dmitriy Komanov (spacelord)
 */
public class Walker {

    public Walker() {
    }

    public void walk(Robot robot) throws InterruptedException {
        Thread leftThread = getThread(robot.getLeft());
        Thread rightThread = getThread(robot.getRight());
        try {
            leftThread.start();
            rightThread.start();
            leftThread.join();
            rightThread.join();
        } finally {
            leftThread.interrupt();
            rightThread.interrupt();
        }
    }

    private static Thread getThread(RobotLeg leg) {
        Thread thread = new Thread(getRunnableForLeg(leg));
        thread.setDaemon(true);
        return thread;
    }

    private static Runnable getRunnableForLeg(final RobotLeg leg) {
        return new Runnable() {
            @Override
            public void run() {
                while (leg.step()) {
                    // do steps
                }
            }
        };
    }
}
