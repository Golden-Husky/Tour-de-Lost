package tourdelost.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** หน้าเริ่มเกมแบบ Java2D ที่แยกฉาก ตัวละคร และเมนูออกจากกัน */
public final class GamePanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int TARGET_FRAMES_PER_SECOND = 60;
    private static final long FRAME_DURATION_NANOS =
        1_000_000_000L / TARGET_FRAMES_PER_SECOND;
    private static final double SOURCE_TO_DESIGN_SCALE = 1.25;
    private static final double BUSH_MOTION_SCALE = 1.006;

    private final transient TitleAssets assets;
    private final transient List<MenuImageButton> menuButtons;
    private final transient Consumer<MenuAction> actionHandler;
    private final transient Timer animationTimer;
    private long animationStartedNanos;
    private long nextFrameNanos;

    public GamePanel(Consumer<MenuAction> actionHandler) {
        this.actionHandler = Objects.requireNonNull(actionHandler, "actionHandler");
        assets = TitleAssets.load();
        setLayout(null);
        setPreferredSize(new Dimension(1280, 720));
        setMinimumSize(new Dimension(960, 540));
        setBackground(new Color(24, 16, 34));
        setFocusable(true);
        setDoubleBuffered(true);

        menuButtons = List.of(
            createButton(assets.startButton(), "เริ่มเกม", MenuAction.START_GAME),
            createButton(assets.levelsButton(), "เลือกด่าน", MenuAction.SELECT_LEVEL),
            createButton(assets.howToButton(), "วิธีเล่น", MenuAction.HOW_TO_PLAY),
            createButton(assets.settingsButton(), "ตั้งค่า", MenuAction.SETTINGS)
        );
        for (MenuImageButton button : menuButtons) {
            button.setVisible(false);
            add(button);
        }

        installKeyBindings();
        animationStartedNanos = System.nanoTime();
        animationTimer = new Timer(0, this::advanceAnimation);
        animationTimer.setCoalesce(true);
        animationTimer.setRepeats(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        nextFrameNanos = System.nanoTime();
        scheduleNextFrame();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    @Override
    public void removeNotify() {
        animationTimer.stop();
        super.removeNotify();
    }

    @Override
    public void doLayout() {
        layoutMenuButtons(elapsedSeconds());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D copy = (Graphics2D) graphics.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
            copy.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            Viewport viewport = viewport();
            copy.translate(viewport.x(), viewport.y());
            copy.scale(viewport.scale(), viewport.scale());
            paintDesign(copy, elapsedSeconds());
        } finally {
            copy.dispose();
        }
    }

    private MenuImageButton createButton(
        BufferedImage image,
        String accessibleName,
        MenuAction action
    ) {
        MenuImageButton button = new MenuImageButton(image, accessibleName);
        button.addActionListener(event -> actionHandler.accept(action));
        return button;
    }

    private void installKeyBindings() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke("SPACE"),
            "skipIntro"
        );
        getActionMap().put("skipIntro", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                skipIntro();
            }
        });
    }

    private void skipIntro() {
        if (elapsedSeconds() >= TitleAnimation.MENU_START_SECONDS) {
            return;
        }
        long skippedNanos = (long) (TitleAnimation.MENU_START_SECONDS * 1_000_000_000L);
        animationStartedNanos = System.nanoTime() - skippedNanos;
        layoutMenuButtons(elapsedSeconds());
        repaint();
    }

    private void advanceAnimation(ActionEvent event) {
        Objects.requireNonNull(event, "event");
        layoutMenuButtons(elapsedSeconds());
        repaint();
        nextFrameNanos += FRAME_DURATION_NANOS;
        long now = System.nanoTime();
        if (nextFrameNanos <= now) {
            long skippedFrames = ((now - nextFrameNanos) / FRAME_DURATION_NANOS) + 1L;
            nextFrameNanos += skippedFrames * FRAME_DURATION_NANOS;
        }
        scheduleNextFrame();
    }

    private void paintDesign(Graphics2D graphics, double elapsedSeconds) {
        double cameraSourceY = TitleAnimation.cameraSourceY(elapsedSeconds);
        double worldGap = (960.0 - cameraSourceY) * SOURCE_TO_DESIGN_SCALE;

        drawWorld(graphics, cameraSourceY);
        drawMainSceneClouds(graphics, elapsedSeconds, worldGap);
        drawSceneSkyOccluder(graphics, worldGap);
        drawWindmill(graphics, elapsedSeconds, worldGap);
        drawSakuraFoliage(graphics, cameraSourceY);
        drawCharacter(graphics, elapsedSeconds);
        drawForeground(graphics, worldGap);
        drawBushMotions(graphics, elapsedSeconds, worldGap);
        drawSakuraPetals(graphics, elapsedSeconds, cameraSourceY);
        drawOpeningLogo(graphics, elapsedSeconds, cameraSourceY);
        drawFinalLogo(graphics, elapsedSeconds);
    }

    private void drawWorld(Graphics2D graphics, double cameraSourceY) {
        if (cameraSourceY >= 959.999) {
            graphics.drawImage(assets.finalScene(), 0, 0, null);
            return;
        }
        drawScrollingLayer(graphics, assets.world(), cameraSourceY);
    }

    private void drawMainSceneClouds(
        Graphics2D graphics,
        double elapsedSeconds,
        double worldGap
    ) {
        for (int cloudIndex = 0; cloudIndex < assets.clouds().size(); cloudIndex++) {
            double cloudX = TitleAnimation.cloudX(elapsedSeconds, cloudIndex);
            double cloudY = worldGap + TitleAnimation.cloudY(cloudIndex);
            AffineTransform cloudTransform = AffineTransform.getTranslateInstance(
                cloudX,
                cloudY
            );
            graphics.drawImage(assets.clouds().get(cloudIndex), cloudTransform, null);
        }
    }

    private void drawWindmill(Graphics2D graphics, double elapsedSeconds, double worldGap) {
        AffineTransform bodyTransform = AffineTransform.getTranslateInstance(
            1076.0,
            worldGap + 58.0
        );
        graphics.drawImage(assets.windmillBody(), bodyTransform, null);

        AffineTransform previousTransform = graphics.getTransform();
        try {
            double centerX = 1170.0;
            double centerY = worldGap + 92.0;
            graphics.rotate(Math.toRadians(TitleAnimation.windmillAngle(elapsedSeconds)),
                centerX, centerY);
            AffineTransform bladeTransform = AffineTransform.getTranslateInstance(
                1092.0,
                centerY - 78.0
            );
            graphics.drawImage(assets.windmillBlades(), bladeTransform, null);
        } finally {
            graphics.setTransform(previousTransform);
        }
    }

    private void drawSceneSkyOccluder(Graphics2D graphics, double worldGap) {
        AffineTransform transform = AffineTransform.getTranslateInstance(0.0, worldGap);
        graphics.drawImage(assets.sceneSkyOccluder(), transform, null);
    }

    private void drawSakuraFoliage(Graphics2D graphics, double cameraSourceY) {
        drawScrollingLayer(graphics, assets.sakuraFoliage(), cameraSourceY);
    }

    private void drawCharacter(Graphics2D graphics, double elapsedSeconds) {
        TitleAnimation.CharacterFrame frame = TitleAnimation.characterFrame(elapsedSeconds);
        if (!frame.visible()) {
            return;
        }

        BufferedImage characterImage = assets.travelerFrames().get(frame.poseIndex());
        int destinationX = (int) Math.round(frame.x() - (frame.size() / 2.0));
        int destinationY = (int) Math.round(frame.y() - frame.size());
        int destinationWidth = (int) Math.round(frame.size());
        int destinationHeight = (int) Math.round(frame.size() * 1.08);

        graphics.drawImage(characterImage, destinationX, destinationY,
            destinationWidth, destinationHeight, null);
    }

    private void drawForeground(Graphics2D graphics, double worldGap) {
        AffineTransform transform = AffineTransform.getTranslateInstance(0.0, worldGap);
        graphics.drawImage(assets.foreground(), transform, null);
    }

    private void drawBushMotions(
        Graphics2D graphics,
        double elapsedSeconds,
        double worldGap
    ) {
        drawBushMotion(
            graphics,
            assets.leftBushMotion(),
            0.0,
            worldGap,
            TitleAnimation.leftBushAngle(elapsedSeconds)
        );
        drawBushMotion(
            graphics,
            assets.rightBushMotion(),
            TitleAnimation.DESIGN_WIDTH,
            worldGap,
            TitleAnimation.rightBushAngle(elapsedSeconds)
        );
    }

    private void drawSakuraPetals(
        Graphics2D graphics,
        double elapsedSeconds,
        double cameraSourceY
    ) {
        graphics.setColor(new Color(255, 157, 206, 190));
        for (int petalIndex = 0; petalIndex < 10; petalIndex++) {
            double fallingDistance = (elapsedSeconds * 20.0) + (petalIndex * 109.0);
            double worldY = 420.0 + (fallingDistance % 650.0);
            double screenY = (worldY - cameraSourceY) * SOURCE_TO_DESIGN_SCALE;
            if (screenY < -12.0 || screenY > TitleAnimation.DESIGN_HEIGHT + 12.0) {
                continue;
            }
            double drift = 18.0 * Math.sin((elapsedSeconds * 0.9) + petalIndex);
            double x = petalIndex % 2 == 0
                ? 38.0 + ((petalIndex * 51.0) % 185.0) + drift
                : 1242.0 - ((petalIndex * 47.0) % 185.0) + drift;
            graphics.fill(new Ellipse2D.Double(x, screenY, 8.0, 4.5));
        }
    }

    private void drawOpeningLogo(
        Graphics2D graphics,
        double elapsedSeconds,
        double cameraSourceY
    ) {
        double opacity = TitleAnimation.openingLogoOpacity(elapsedSeconds);
        if (opacity <= 0.0) {
            return;
        }
        Graphics2D logoGraphics = (Graphics2D) graphics.create();
        try {
            logoGraphics.setComposite(AlphaComposite.SrcOver.derive((float) opacity));
            logoGraphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );
            logoGraphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
            );
            double y =
                177.0
                    - (cameraSourceY * SOURCE_TO_DESIGN_SCALE)
                    + TitleAnimation.logoOffsetY(elapsedSeconds);
            logoGraphics.rotate(TitleAnimation.logoAngle(elapsedSeconds), 640.0, y + 183.0);
            AffineTransform logoTransform = AffineTransform.getTranslateInstance(315.0, y);
            logoGraphics.drawImage(assets.openingLogo(), logoTransform, null);
        } finally {
            logoGraphics.dispose();
        }
    }

    private void drawFinalLogo(Graphics2D graphics, double elapsedSeconds) {
        double progress = TitleAnimation.finalLogoProgress(elapsedSeconds);
        if (progress <= 0.0) {
            return;
        }
        float opacity = (float) Math.min(1.0, progress);
        double sizeScale = 0.78 + (0.22 * progress);
        double width = 390.0 * sizeScale;
        double height = 219.0 * sizeScale;
        double x = 35.0 + ((390.0 - width) / 2.0);
        double y = 22.0
            + ((219.0 - height) / 2.0)
            + TitleAnimation.logoOffsetY(elapsedSeconds);
        Graphics2D logoGraphics = (Graphics2D) graphics.create();
        try {
            logoGraphics.setComposite(AlphaComposite.SrcOver.derive(opacity));
            logoGraphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );
            logoGraphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
            );
            logoGraphics.rotate(
                TitleAnimation.logoAngle(elapsedSeconds),
                x + (width / 2.0),
                y + (height / 2.0)
            );
            AffineTransform logoTransform = new AffineTransform();
            logoTransform.translate(x, y);
            logoTransform.scale(
                width / assets.menuLogo().getWidth(),
                height / assets.menuLogo().getHeight()
            );
            logoGraphics.drawImage(assets.menuLogo(), logoTransform, null);
        } finally {
            logoGraphics.dispose();
        }
    }

    private void layoutMenuButtons(double elapsedSeconds) {
        Viewport viewport = viewport();
        for (int index = 0; index < menuButtons.size(); index++) {
            MenuImageButton button = menuButtons.get(index);
            MenuButtonLayout layout = menuButtonLayout(index);
            double progress = TitleAnimation.menuProgress(elapsedSeconds, index);
            boolean visible = progress > 0.01;
            if (button.isVisible() != visible) {
                button.setVisible(visible);
            }
            if (!visible) {
                continue;
            }
            double designX = 1320.0 + ((layout.x() - 1320.0) * progress);
            int x = viewport.x() + (int) Math.round(designX * viewport.scale());
            int y = viewport.y() + (int) Math.round(layout.y() * viewport.scale());
            int width = (int) Math.round(layout.width() * viewport.scale());
            int height = (int) Math.round(layout.height() * viewport.scale());
            button.setBounds(x, y, width, height);
        }
    }

    private static MenuButtonLayout menuButtonLayout(int buttonIndex) {
        return switch (buttonIndex) {
            case 0 -> new MenuButtonLayout(810.0, 108.0, 440.0, 145.0);
            case 1 -> new MenuButtonLayout(830.0, 238.0, 420.0, 136.0);
            case 2 -> new MenuButtonLayout(845.0, 358.0, 405.0, 132.0);
            case 3 -> new MenuButtonLayout(865.0, 475.0, 385.0, 126.0);
            default -> throw new IllegalArgumentException(
                "Menu button index must be between 0 and 3."
            );
        };
    }

    private Viewport viewport() {
        double horizontalScale = getWidth() / (double) TitleAnimation.DESIGN_WIDTH;
        double verticalScale = getHeight() / (double) TitleAnimation.DESIGN_HEIGHT;
        double scale = Math.min(horizontalScale, verticalScale);
        int width = (int) Math.round(TitleAnimation.DESIGN_WIDTH * scale);
        int height = (int) Math.round(TitleAnimation.DESIGN_HEIGHT * scale);
        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;
        return new Viewport(x, y, scale);
    }

    private double elapsedSeconds() {
        return (System.nanoTime() - animationStartedNanos) / 1_000_000_000.0;
    }

    private void scheduleNextFrame() {
        long remainingNanos = Math.max(0L, nextFrameNanos - System.nanoTime());
        int delayMillis = (int) Math.max(
            1L,
            (remainingNanos + 999_999L) / 1_000_000L
        );
        animationTimer.setInitialDelay(delayMillis);
        animationTimer.restart();
    }

    private void drawScrollingLayer(
        Graphics2D graphics,
        BufferedImage image,
        double cameraSourceY
    ) {
        double designOffset = cameraSourceY * SOURCE_TO_DESIGN_SCALE;
        AffineTransform transform = AffineTransform.getTranslateInstance(0.0, -designOffset);
        graphics.drawImage(image, transform, null);
    }

    private void drawBushMotion(
        Graphics2D graphics,
        TitleAssets.MotionLayer layer,
        double pivotX,
        double worldGap,
        double angle
    ) {
        AffineTransform transform = new AffineTransform();
        transform.translate(pivotX, worldGap + TitleAnimation.DESIGN_HEIGHT);
        transform.rotate(angle);
        transform.scale(BUSH_MOTION_SCALE, BUSH_MOTION_SCALE);
        transform.translate(
            layer.x() - pivotX,
            layer.y() - TitleAnimation.DESIGN_HEIGHT
        );
        graphics.drawImage(layer.image(), transform, null);
    }

    private record Viewport(int x, int y, double scale) {
    }

    private record MenuButtonLayout(double x, double y, double width, double height) {
    }
}
