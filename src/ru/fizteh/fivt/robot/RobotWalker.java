package ru.fizteh.fivt.robot;

/**
 * @author Dmitriy Komanov (spacelord)
 */
public final class RobotWalker {

    private RobotWalker() {
    }

    public static void walk(Robot robot) throws InterruptedException {
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
