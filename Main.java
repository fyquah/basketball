import java.awt.Robot;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.FlowLayout;


public class Main {

    private Robot robot;
    private JFrame frame;

    public static class Position {
        int x;
        int y;

        Position(int a, int b) {
            x = a;
            y = b;
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        @Override
        public String toString() {
            return "x = " + x + ", y = " + y;
        }
    };


    public void scoreStageOne() throws InterruptedException {
        robot.mouseMove(1100, 700);
        for (int i = 1 ; i < 10 ; i++) {
            robot.mouseMove(1100, 700 - i * 10);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            Thread.sleep(50);
        }
        Thread.sleep(2000);
    }

    public Main() {
        try {
            robot = new Robot();
            frame = new JFrame();
        } catch (AWTException e) {
        }
    }

    public int getR(int x) {
        return (x >> 16) & 0xFF;
    }

    public int getG(int x) {
        return (x >> 8) & 0xFF;
    }

    public int getB(int x) {
        return (x >> 0) & 0xFF;
    }

    public boolean isClose(int a, int b) {
        return Math.abs(a - b) < 2;
    }

    public Position findTarget(BufferedImage screenShot) {
        for (int y = 0 ; y < screenShot.getHeight(); y++) {
            for (int x = 0 ; x < screenShot.getWidth() ; x++) {
                int color = screenShot.getRGB(x, y);
                if (isClose(getR(color), 245)
                        && isClose(getG(color), 223)
                        && isClose(getB(color), 78)) {
                    return new Position(SS_X + x + 41, SS_Y + y + 20);
                }
            }
        }
        return null;
    }

    public int SS_X = 900;
    public int SS_Y = 150;

    public double calculateTargetAngle(Position hoopPosition) {
        Position origin = new Position(BALL_X, BALL_Y);
        final double x = hoopPosition.getX() - origin.getX();
        final double y = origin.getY() - hoopPosition.getY();
        final double h = 500.0;
        // final double g = 0.0;
        // final double vy = 0.0;
        // final double c = 0.0;

        // return Math.atan2(y + 0.5 * , x);
        return Math.atan2((4 * h * y), (x * x + 4 * h * x));
    }

    public void debugFrame() {
        BufferedImage screenShot = robot.createScreenCapture(
                new Rectangle(SS_X, SS_Y, 360, 600));
        Position hoopPosition = findTarget(screenShot);
        screenShot.getGraphics().drawOval(
                hoopPosition.getX() - SS_X,
                hoopPosition.getY() - SS_Y,
                50,
                50);
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(screenShot)));
        frame.pack();
        frame.setVisible(true);
    }

    public void run() throws InterruptedException, AWTException {
        robot.mouseMove(BALL_X, BALL_Y);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        Thread.sleep(300);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        Thread.sleep(300);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        for (int i = 0 ; i < 10 ; i++) {
            BufferedImage screenShot = robot.createScreenCapture(
                    new Rectangle(SS_X, SS_Y, 360, 600));
            Position hoopPosition = findTarget(screenShot);
            double angle = calculateTargetAngle(hoopPosition);
            double angleInDegrees = angle * 180.0 / Math.PI;
            Position origin = new Position(BALL_X, BALL_Y);
            System.out.println("origin = " + origin);
            System.out.println("hoop Position = " + hoopPosition);
            System.out.println("projection angle = " + angleInDegrees);
            throwTarget(angle);
            Thread.sleep(3000);
        }

    }


    public int BALL_X = 1083;
    public int BALL_Y = 675;

    public void throwTarget(double angle) throws InterruptedException {
        robot.mouseMove(BALL_X, BALL_Y);
        robot.mousePress(InputEvent.BUTTON1_MASK);

        double x = Math.cos(angle);
        double y = Math.sin(angle);

        for (int i = 1 ; i < 10 ; i++) {
            robot.mouseMove(
                    BALL_X + (int) (x * i * 10),
                    BALL_Y - (int) (y * i * 10));
            Thread.sleep(50);
        }
    }


    public static void main(String[] args) {
        Main x = new Main();
        try {
            x.run();
        } catch (AWTException e) {

        } catch (InterruptedException e) {

        }
    }
}
