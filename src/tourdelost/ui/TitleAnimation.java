package tourdelost.ui;

/** คำนวณสถานะแอนิเมชันโดยไม่แก้ข้อมูลภายนอก */
final class TitleAnimation {
    static final int DESIGN_WIDTH = 1280;
    static final int DESIGN_HEIGHT = 720;
    static final double MENU_START_SECONDS = 7.4;

    private static final double CAMERA_START_SECONDS = 2.1;
    private static final double CAMERA_END_SECONDS = 6.9;
    private static final double CHARACTER_LOOP_SECONDS = 16.5;

    private TitleAnimation() {
    }

    static double cameraSourceY(double elapsedSeconds) {
        double progress = normalized(
            elapsedSeconds,
            CAMERA_START_SECONDS,
            CAMERA_END_SECONDS
        );
        return 960.0 * smootherStep(progress);
    }

    static double openingLogoOpacity(double elapsedSeconds) {
        return 1.0 - smoothStep(normalized(elapsedSeconds, 3.1, 5.3));
    }

    static double finalLogoProgress(double elapsedSeconds) {
        return backOut(normalized(elapsedSeconds, 6.9, 7.7));
    }

    static double menuProgress(double elapsedSeconds, int buttonIndex) {
        double startSeconds = MENU_START_SECONDS + (buttonIndex * 0.16);
        return backOut(normalized(elapsedSeconds, startSeconds, startSeconds + 0.62));
    }

    static CharacterFrame characterFrame(double elapsedSeconds) {
        double characterSeconds = elapsedSeconds - MENU_START_SECONDS;
        if (characterSeconds < 0.0) {
            return CharacterFrame.hidden();
        }

        double loopSeconds = characterSeconds % CHARACTER_LOOP_SECONDS;
        if (loopSeconds < 3.0) {
            double progress = smootherStep(loopSeconds / 3.0);
            return walkingFrame(progress, -220.0, 290.0, 675.0, 675.0, 310.0, 310.0,
                loopSeconds);
        }
        if (loopSeconds < 4.2) {
            return new CharacterFrame(true, 0, 290.0, 675.0, 310.0);
        }
        if (loopSeconds < 5.6) {
            int lookPose = Math.min(3, 1 + (int) ((loopSeconds - 4.2) * 2.2));
            return new CharacterFrame(true, lookPose, 290.0, 675.0, 310.0);
        }
        if (loopSeconds < 6.5) {
            return new CharacterFrame(true, 0, 290.0, 675.0, 310.0);
        }
        if (loopSeconds < 15.5) {
            double progress = smootherStep((loopSeconds - 6.5) / 9.0);
            double x = cubicBezier(290.0, 510.0, 950.0, 1190.0, progress);
            double y = cubicBezier(675.0, 730.0, 655.0, 701.0, progress);
            double size = interpolate(310.0, 245.0, progress);
            int poseIndex = 4 + ((int) Math.floor(loopSeconds * 7.5) % 4);
            return new CharacterFrame(true, poseIndex, x, y, size);
        }
        return CharacterFrame.hidden();
    }

    static double windmillAngle(double elapsedSeconds) {
        return (elapsedSeconds * 42.0) % 360.0;
    }

    static double leftBushAngle(double elapsedSeconds) {
        return Math.toRadians(0.34 * Math.sin(elapsedSeconds * 1.1));
    }

    static double rightBushAngle(double elapsedSeconds) {
        return Math.toRadians(0.31 * Math.sin((elapsedSeconds * 1.05) + 1.7));
    }

    static double cloudX(double elapsedSeconds, int cloudIndex) {
        double initialX = switch (cloudIndex) {
            case 0 -> 130.0;
            case 1 -> 560.0;
            case 2 -> 990.0;
            default -> throw new IllegalArgumentException("Cloud index must be between 0 and 2.");
        };
        double loopDistance = 1_560.0;
        double travel = (elapsedSeconds * 30.0) % loopDistance;
        double x = initialX - travel;
        return x < -280.0 ? x + loopDistance : x;
    }

    static double cloudY(int cloudIndex) {
        return switch (cloudIndex) {
            case 0 -> 38.0;
            case 1 -> 72.0;
            case 2 -> 28.0;
            default -> throw new IllegalArgumentException("Cloud index must be between 0 and 2.");
        };
    }

    static double logoOffsetY(double elapsedSeconds) {
        return 2.5 * Math.sin(elapsedSeconds * 0.9);
    }

    static double logoAngle(double elapsedSeconds) {
        return Math.toRadians(0.45 * Math.sin((elapsedSeconds * 0.8) + 0.4));
    }

    private static CharacterFrame walkingFrame(
        double progress,
        double startX,
        double endX,
        double startY,
        double endY,
        double startSize,
        double endSize,
        double elapsedSeconds
    ) {
        int poseIndex = 4 + ((int) Math.floor(elapsedSeconds * 7.5) % 4);
        return new CharacterFrame(
            true,
            poseIndex,
            interpolate(startX, endX, progress),
            interpolate(startY, endY, progress),
            interpolate(startSize, endSize, progress)
        );
    }

    private static double normalized(double value, double start, double end) {
        if (end <= start) {
            throw new IllegalArgumentException("Animation range must increase.");
        }
        return clamp((value - start) / (end - start));
    }

    private static double interpolate(double start, double end, double progress) {
        return start + ((end - start) * progress);
    }

    private static double cubicBezier(
        double start,
        double firstControl,
        double secondControl,
        double end,
        double progress
    ) {
        double inverse = 1.0 - progress;
        return (inverse * inverse * inverse * start)
            + (3.0 * inverse * inverse * progress * firstControl)
            + (3.0 * inverse * progress * progress * secondControl)
            + (progress * progress * progress * end);
    }

    private static double smoothStep(double progress) {
        double clamped = clamp(progress);
        return clamped * clamped * (3.0 - (2.0 * clamped));
    }

    private static double smootherStep(double progress) {
        double clamped = clamp(progress);
        return clamped * clamped * clamped
            * ((clamped * ((clamped * 6.0) - 15.0)) + 10.0);
    }

    private static double backOut(double progress) {
        double clamped = clamp(progress);
        double shifted = clamped - 1.0;
        double overshoot = 1.70158;
        return 1.0 + ((overshoot + 1.0) * shifted * shifted * shifted)
            + (overshoot * shifted * shifted);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    record CharacterFrame(boolean visible, int poseIndex, double x, double y, double size) {
        static CharacterFrame hidden() {
            return new CharacterFrame(false, 0, 0.0, 0.0, 0.0);
        }
    }
}
