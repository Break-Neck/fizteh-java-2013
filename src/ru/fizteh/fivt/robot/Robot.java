package ru.fizteh.fivt.robot;

/**
 * Шагающий робот.
 */
public class Robot {

    private final RobotLeg left;
    private final RobotLeg right;

    public Robot(RobotLeg left, RobotLeg right) {
        if (left.getType() != RobotLegType.LEFT) {
            throw new IllegalArgumentException("Expected left leg");
        }
        if (right.getType() != RobotLegType.RIGHT) {
            throw new IllegalArgumentException("Expected right leg");
        }

        this.left = left;
        this.right = right;
    }

    public RobotLeg getLeft() {
        return left;
    }

    public RobotLeg getRight() {
        return right;
    }
}
