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
                    return new Position(
                            SS_X + x + 38,
                            SS_Y + y + 30);
                }
            }
        }
        return null;
    }

    public int SS_X = 900;
    public int SS_Y = 150;

    public double square(double x) {
        return x * x;
    }

    public double calculateTargetAngle(Position hoopPosition) {
        Position origin = new Position(BALL_X, BALL_Y);
        final double x = hoopPosition.getX() - origin.getX();
        final double y = origin.getY() - hoopPosition.getY();
        final double h = 350.0;
        // final double g = 0.0;
        // final double vy = 0.0;
        // final double c = 0.0;

        // return Math.atan2(y, x);
        // return Math.atan2(y + 0.5 * , x);
        return Math.atan2((4 * h * y), (x * x + 4 * h * x));
        // return Math.atan2(
        //         (y + square(Math.sqrt(h)+Math.sqrt(h-y))),
        //         x);
    }

    public void debugFrame() {
        BufferedImage screenShot = robot.createScreenCapture(
                new Rectangle(SS_X, SS_Y, 360, 600));
        Position hoopPosition = findTarget(screenShot);
        screenShot.getGraphics().drawOval(
                hoopPosition.getX() - SS_X - (25),
                hoopPosition.getY() - SS_Y - (25),
                50,
                50);
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(screenShot)));
        frame.pack();
        frame.setVisible(true);
    }

    public boolean isDynamicMode = false;

    public void run() throws InterruptedException, AWTException {
        robot.mouseMove(START_X, START_Y);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        Thread.sleep(300);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        Thread.sleep(300);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        Thread.sleep(1500);

        // All in ms
        final long SCREENSHOT_SLEEP = 500;
        final long THROW_DRAG_DELAY = 1000;

        for (int i = 0 ;  ; i++) {

            Position hoopPosition = null;

            while (true) {
                System.out.println("=============");
                BufferedImage screenShotOne = robot.createScreenCapture(
                        new Rectangle(SS_X, SS_Y, 360, 600));
                long t1 = System.currentTimeMillis();
                Thread.sleep(SCREENSHOT_SLEEP);
                BufferedImage screenShotTwo = robot.createScreenCapture(
                        new Rectangle(SS_X, SS_Y, 360, 600));
                long t2 = System.currentTimeMillis();
                double dt = ((double) (t2 - t1)) * 0.001;

                Position oldHoopPosition = findTarget(
                        screenShotOne);
                Position newHoopPosition = findTarget(
                        screenShotTwo);
                int oldX = oldHoopPosition.getX();
                int newX = newHoopPosition.getX();
                int diff = newX - oldX;

                if (diff == 0) {
                    if (isDynamicMode) {
                        System.out.println(
                                "Cannot backtrack to static mode!");
                        continue;
                    }
                    hoopPosition = newHoopPosition;
                    System.out.println("Static case");
                    break;

                }
                isDynamicMode = true;

                if (diff > 0) {  // moving to the right
                    if (newX > BALL_X) {
                        continue;
                    }

                } else if (diff < 0) {  // moving to the left
                    if (newX < BALL_X) {
                        continue;
                    }
                }
                System.out.println("Moving case");

                final double h = 350.0;
                final double a_y = -1840;
                final double u_y = Math.sqrt(-2 * a_y * h);

                double y = newHoopPosition.getY() - BALL_Y;
                double timeRequired =
                        (-u_y + Math.sqrt(u_y * u_y - 2 * a_y * y))
                        / a_y;
                double v_x = ((double) (newX - oldX)) / dt; // pixels / seconds

                double distanceFromCenter = Math.abs(newX - 1080);
                double timeAvailable =
                        distanceFromCenter / Math.abs(v_x);
                long sleepMs =
                        ((long) ((timeAvailable - timeRequired) * 1000))
                        - THROW_DRAG_DELAY;

                System.out.println("y = " + y);
                System.out.println("New x = " + newX);
                System.out.println("Old x = " + oldX);
                System.out.println("v_x = " + v_x);
                System.out.println("Time required = " + timeRequired);
                System.out.println("Time available = " + timeAvailable);
                System.out.println("Sleep time = " + sleepMs);
                if (sleepMs < 0) {
                    System.out.println("Not feasible!");
                    continue;
                } else if (sleepMs > 1000) {
                    System.out.println("Not sleeping for > 1s");
                    continue;
                }
                System.out.println(
                        "Sleeping now for " + sleepMs + " seconds");
                Thread.sleep(sleepMs);

                hoopPosition = new Position(1080, 427);
                break;
            }

            double angle = calculateTargetAngle(hoopPosition);
            double angleInDegrees = angle * 180.0 / Math.PI;
            Position origin = new Position(BALL_X, BALL_Y);
            System.out.println("origin = " + origin);
            System.out.println("hoop Position = " + hoopPosition);
            System.out.println("projection angle = " + angleInDegrees);
            robot.mouseMove(
                    hoopPosition.getX(),
                    hoopPosition.getY());
            robot.mouseMove(START_X, START_Y);
            throwTarget(angle);
            Thread.sleep(3000);
        }

    }


    public int START_X = 1083;
    public int START_Y = 700;
    public int BALL_X = 1080;
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

        //    System.out.println(
        //            x.calculateTargetAngle(
        //                new Position(
        //                    x.BALL_X + 50,
        //                    x.BALL_Y - 1))
        //            * 180.0 / Math.PI);
        try {
            // x.debugFrame();
            x.run();
        } catch (AWTException e) {

        } catch (InterruptedException e) {

        }
    }
}
