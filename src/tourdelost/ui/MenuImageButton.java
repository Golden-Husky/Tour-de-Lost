package tourdelost.ui;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.Objects;
import javax.swing.JButton;

/** ปุ่มเมนูที่ใช้ภาพจริงและยังคงรับคีย์บอร์ดได้ */
final class MenuImageButton extends JButton {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final double MAXIMUM_HOVER_SCALE = 1.045;

    private final transient BufferedImage image;
    private transient BufferedImage normalImage;
    private transient BufferedImage hoverImage;
    private int cachedPixelWidth;
    private int cachedPixelHeight;

    MenuImageButton(BufferedImage image, String accessibleName) {
        this.image = Objects.requireNonNull(image, "image");
        Objects.requireNonNull(accessibleName, "accessibleName");
        getAccessibleContext().setAccessibleName(accessibleName);
        setToolTipText(accessibleName);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        Graphics2D copy = (Graphics2D) graphics.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            copy.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
            AffineTransform transform = copy.getTransform();
            double deviceScaleX = Math.hypot(transform.getScaleX(), transform.getShearY());
            double deviceScaleY = Math.hypot(transform.getShearX(), transform.getScaleY());
            int pixelWidth = Math.max(1, (int) Math.ceil(width * deviceScaleX));
            int pixelHeight = Math.max(1, (int) Math.ceil(height * deviceScaleY));
            prepareCachedImages(pixelWidth, pixelHeight);
            int pressOffset = getModel().isPressed() ? 2 : 0;
            boolean hovered = getModel().isRollover();
            BufferedImage displayImage = hovered ? hoverImage : normalImage;
            double displayScale = hovered ? MAXIMUM_HOVER_SCALE : 1.0;
            drawCachedImage(copy, displayImage, displayScale, pressOffset);
        } finally {
            copy.dispose();
        }
    }

    private void drawCachedImage(
        Graphics2D graphics,
        BufferedImage displayImage,
        double scale,
        int pressOffset
    ) {
        double drawWidth = getWidth() * scale;
        double drawHeight = getHeight() * scale;
        double drawX = (getWidth() - drawWidth) / 2.0;
        double drawY = ((getHeight() - drawHeight) / 2.0) + pressOffset;
        AffineTransform imageTransform = new AffineTransform();
        imageTransform.translate(drawX, drawY);
        imageTransform.scale(
            drawWidth / displayImage.getWidth(),
            drawHeight / displayImage.getHeight()
        );
        graphics.drawImage(displayImage, imageTransform, null);
    }

    private void prepareCachedImages(int pixelWidth, int pixelHeight) {
        if (normalImage != null
            && cachedPixelWidth == pixelWidth
            && cachedPixelHeight == pixelHeight) {
            return;
        }
        normalImage = createScaledImage(pixelWidth, pixelHeight);
        int hoverWidth = Math.max(1, (int) Math.ceil(pixelWidth * MAXIMUM_HOVER_SCALE));
        int hoverHeight = Math.max(1, (int) Math.ceil(pixelHeight * MAXIMUM_HOVER_SCALE));
        hoverImage = createScaledImage(hoverWidth, hoverHeight);
        cachedPixelWidth = pixelWidth;
        cachedPixelHeight = pixelHeight;
    }

    private BufferedImage createScaledImage(int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(image, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return resized;
    }
}
