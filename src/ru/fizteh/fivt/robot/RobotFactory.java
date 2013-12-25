package ru.fizteh.fivt.robot;

import java.io.OutputStream;

/**
 * Представляет фабрику для создания шагающих роботов.
 */
public interface RobotFactory {

    /**
     * Создаёт экземпляр шагающего робота.
     *
     * @param output       Поток, в который необходимо вести запись шагов робота.
     * @param steps        Количество шагов, которые должен сделать робот.
     * @param firstStepLeg С какой ноги надо начинать ходьбу.
     * @return Робот.
     *
     * @throws IllegalArgumentException Если любой из параметров имеет некорректное значение.
     */
    Robot createRobot(OutputStream output, int steps, RobotLegType firstStepLeg);
}
