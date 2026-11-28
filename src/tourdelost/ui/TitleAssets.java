package tourdelost.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;

/** ชุดภาพที่เตรียมขนาดและรวมชั้นนิ่งไว้ก่อนเริ่มวาดเฟรม */
record TitleAssets(
    BufferedImage world,
    BufferedImage finalScene,
    BufferedImage sceneSkyOccluder,
    BufferedImage sakuraFoliage,
    List<BufferedImage> clouds,
    BufferedImage windmillBody,
    BufferedImage windmillBlades,
    List<BufferedImage> travelerFrames,
    BufferedImage foreground,
    MotionLayer leftBushMotion,
    MotionLayer rightBushMotion,
    BufferedImage openingLogo,
    BufferedImage menuLogo,
    BufferedImage startButton,
    BufferedImage levelsButton,
    BufferedImage howToButton,
    BufferedImage settingsButton
) {
    TitleAssets {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(finalScene, "finalScene");
        Objects.requireNonNull(sceneSkyOccluder, "sceneSkyOccluder");
        Objects.requireNonNull(sakuraFoliage, "sakuraFoliage");
        clouds = List.copyOf(clouds);
        if (clouds.size() != 3 || clouds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Title animation requires three clouds.");
        }
        Objects.requireNonNull(windmillBody, "windmillBody");
        Objects.requireNonNull(windmillBlades, "windmillBlades");
        travelerFrames = List.copyOf(travelerFrames);
        if (travelerFrames.size() != 8 || travelerFrames.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Traveler animation requires eight frames.");
        }
        Objects.requireNonNull(foreground, "foreground");
        Objects.requireNonNull(leftBushMotion, "leftBushMotion");
        Objects.requireNonNull(rightBushMotion, "rightBushMotion");
        Objects.requireNonNull(openingLogo, "openingLogo");
        Objects.requireNonNull(menuLogo, "menuLogo");
        Objects.requireNonNull(startButton, "startButton");
        Objects.requireNonNull(levelsButton, "levelsButton");
        Objects.requireNonNull(howToButton, "howToButton");
        Objects.requireNonNull(settingsButton, "settingsButton");
    }

    static TitleAssets load() {
        BufferedImage world = readScaled("world.png", 1280, 1920, BufferedImage.TYPE_INT_RGB);
        BufferedImage foliage = readScaled(
            "sakura-foliage.png",
            1280,
            1920,
            BufferedImage.TYPE_INT_ARGB_PRE
        );
        BufferedImage leftTree = readScaled(
            "tree-left.png",
            1280,
            720,
            BufferedImage.TYPE_INT_ARGB_PRE
        );
        BufferedImage rightTree = readScaled(
            "tree-right.png",
            1280,
            720,
            BufferedImage.TYPE_INT_ARGB_PRE
        );
        BufferedImage leftBush = readScaled(
            "bush-left.png",
            1280,
            720,
            BufferedImage.TYPE_INT_ARGB_PRE
        );
        BufferedImage rightBush = readScaled(
            "bush-right.png",
            1280,
            720,
            BufferedImage.TYPE_INT_ARGB_PRE
        );
        BufferedImage logo = read("logo.png");

        BufferedImage finalScene = copyRegion(
            world,
            new Rectangle(0, 1200, 1280, 720),
            BufferedImage.TYPE_INT_RGB
        );
        BufferedImage foreground = composeForeground(leftTree, rightTree, leftBush, rightBush);
        return new TitleAssets(
            world,
            finalScene,
            createSceneSkyOccluder(finalScene),
            foliage,
            createClouds(read("clouds.png")),
            readScaled("windmill-body.png", 180, 165, BufferedImage.TYPE_INT_ARGB_PRE),
            readScaled("windmill-blades.png", 156, 156, BufferedImage.TYPE_INT_ARGB_PRE),
            createTravelerFrames(read("traveler-sprites.png")),
            foreground,
            createBushMotionLayer(leftBush),
            createBushMotionLayer(rightBush),
            scaleImage(logo, 650, 366, BufferedImage.TYPE_INT_ARGB_PRE),
            scaleImage(logo, 390, 219, BufferedImage.TYPE_INT_ARGB_PRE),
            readScaled("button-start.png", 880, 290, BufferedImage.TYPE_INT_ARGB_PRE),
            readScaled("button-levels.png", 840, 272, BufferedImage.TYPE_INT_ARGB_PRE),
            readScaled("button-how-to.png", 810, 264, BufferedImage.TYPE_INT_ARGB_PRE),
            readScaled("button-settings.png", 770, 252, BufferedImage.TYPE_INT_ARGB_PRE)
        );
    }

    private static BufferedImage composeForeground(BufferedImage... layers) {
        BufferedImage foreground = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D graphics = foreground.createGraphics();
        try {
            for (BufferedImage layer : layers) {
                graphics.drawImage(layer, 0, 0, null);
            }
        } finally {
            graphics.dispose();
        }
        return foreground;
    }

    private static List<BufferedImage> createTravelerFrames(BufferedImage spriteSheet) {
        int cellWidth = spriteSheet.getWidth() / 4;
        int cellHeight = spriteSheet.getHeight() / 2;
        List<BufferedImage> frames = new ArrayList<>(8);
        for (int frameIndex = 0; frameIndex < 8; frameIndex++) {
            int column = frameIndex % 4;
            int row = frameIndex / 4;
            BufferedImage frame = new BufferedImage(320, 360, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D graphics = frame.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.drawImage(
                    spriteSheet,
                    0,
                    0,
                    frame.getWidth(),
                    frame.getHeight(),
                    column * cellWidth,
                    row * cellHeight,
                    (column + 1) * cellWidth,
                    (row + 1) * cellHeight,
                    null
                );
            } finally {
                graphics.dispose();
            }
            frames.add(frame);
        }
        return frames;
    }

    private static List<BufferedImage> createClouds(BufferedImage cloudSheet) {
        BufferedImage firstCloud = copyRegion(
            cloudSheet,
            new Rectangle(640, 90, 340, 100),
            BufferedImage.TYPE_INT_ARGB_PRE
        );
        BufferedImage secondCloud = copyRegion(
            cloudSheet,
            new Rectangle(1100, 90, 300, 100),
            BufferedImage.TYPE_INT_ARGB_PRE
        );
        BufferedImage thirdCloud = copyRegion(
            cloudSheet,
            new Rectangle(280, 180, 210, 80),
            BufferedImage.TYPE_INT_ARGB_PRE
        );
        return List.of(
            scaleImage(firstCloud, 260, 76, BufferedImage.TYPE_INT_ARGB_PRE),
            scaleImage(secondCloud, 230, 76, BufferedImage.TYPE_INT_ARGB_PRE),
            scaleImage(thirdCloud, 190, 68, BufferedImage.TYPE_INT_ARGB_PRE)
        );
    }

    private static BufferedImage createSceneSkyOccluder(BufferedImage scene) {
        int width = scene.getWidth();
        int height = 210;
        int[] pixels = scene.getRGB(0, 0, width, height, null, 0, width);
        boolean[] examined = new boolean[pixels.length];
        boolean[] skyPixels = new boolean[pixels.length];
        int[] queue = new int[pixels.length];
        int queueStart = 0;
        int queueEnd = 0;
        int seed = width / 2;
        if (!isSkyColor(pixels[seed])) {
            throw new IllegalStateException("Cannot locate the main-scene sky.");
        }
        queue[queueEnd++] = seed;
        examined[seed] = true;

        while (queueStart < queueEnd) {
            int index = queue[queueStart++];
            skyPixels[index] = true;
            int x = index % width;
            int y = index / width;
            queueEnd = enqueueSkyPixel(pixels, examined, queue, queueEnd, width, height,
                x - 1, y);
            queueEnd = enqueueSkyPixel(pixels, examined, queue, queueEnd, width, height,
                x + 1, y);
            queueEnd = enqueueSkyPixel(pixels, examined, queue, queueEnd, width, height,
                x, y - 1);
            queueEnd = enqueueSkyPixel(pixels, examined, queue, queueEnd, width, height,
                x, y + 1);
        }

        for (int index = 0; index < pixels.length; index++) {
            int x = index % width;
            int y = index / width;
            boolean sunPixel = x >= 690
                && x <= 850
                && y >= 70
                && y <= 205
                && isSunColor(pixels[index]);
            pixels[index] = skyPixels[index] || sunPixel
                ? 0
                : 0xff000000 | (pixels[index] & 0x00ffffff);
        }
        BufferedImage occluder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        occluder.setRGB(0, 0, width, height, pixels, 0, width);
        return occluder;
    }

    private static int enqueueSkyPixel(
        int[] pixels,
        boolean[] examined,
        int[] queue,
        int queueEnd,
        int width,
        int height,
        int x,
        int y
    ) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return queueEnd;
        }
        int index = (y * width) + x;
        if (examined[index]) {
            return queueEnd;
        }
        examined[index] = true;
        if (!isSkyColor(pixels[index])) {
            return queueEnd;
        }
        queue[queueEnd] = index;
        return queueEnd + 1;
    }

    private static boolean isSkyColor(int pixel) {
        int red = (pixel >>> 16) & 0xff;
        int green = (pixel >>> 8) & 0xff;
        int blue = pixel & 0xff;
        return red >= 210
            && green >= 80
            && green <= 220
            && blue >= 65
            && blue <= 200
            && red >= green + 20;
    }

    private static boolean isSunColor(int pixel) {
        int red = (pixel >>> 16) & 0xff;
        int green = (pixel >>> 8) & 0xff;
        int blue = pixel & 0xff;
        return red >= 235
            && green >= 205
            && blue >= 100
            && blue <= 220
            && red - green <= 55
            && green - blue >= 20;
    }

    private static BufferedImage readScaled(
        String fileName,
        int width,
        int height,
        int imageType
    ) {
        return scaleImage(read(fileName), width, height, imageType);
    }

    private static BufferedImage scaleImage(
        BufferedImage source,
        int width,
        int height,
        int imageType
    ) {
        BufferedImage scaled = new BufferedImage(width, height, imageType);
        Graphics2D graphics = scaled.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return scaled;
    }

    private static BufferedImage copyRegion(
        BufferedImage source,
        Rectangle bounds,
        int imageType
    ) {
        BufferedImage copy = new BufferedImage(bounds.width, bounds.height, imageType);
        Graphics2D graphics = copy.createGraphics();
        try {
            graphics.drawImage(source, -bounds.x, -bounds.y, null);
        } finally {
            graphics.dispose();
        }
        return copy;
    }

    private static MotionLayer createBushMotionLayer(BufferedImage source) {
        BufferedImage motion = new BufferedImage(
            source.getWidth(),
            source.getHeight(),
            BufferedImage.TYPE_INT_ARGB_PRE
        );
        Graphics2D graphics = motion.createGraphics();
        try {
            graphics.drawImage(source, 0, 0, null);
            graphics.setComposite(AlphaComposite.DstIn);
            graphics.setPaint(new GradientPaint(
                0.0f,
                455.0f,
                new Color(255, 255, 255, 220),
                0.0f,
                650.0f,
                new Color(255, 255, 255, 0),
                false
            ));
            graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
        } finally {
            graphics.dispose();
        }

        Rectangle bounds = alphaBounds(motion);
        BufferedImage cropped = new BufferedImage(
            bounds.width,
            bounds.height,
            BufferedImage.TYPE_INT_ARGB_PRE
        );
        Graphics2D cropGraphics = cropped.createGraphics();
        try {
            cropGraphics.drawImage(motion, -bounds.x, -bounds.y, null);
        } finally {
            cropGraphics.dispose();
        }
        return new MotionLayer(cropped, bounds.x, bounds.y);
    }

    private static Rectangle alphaBounds(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < height; y++) {
            int rowOffset = y * width;
            for (int x = 0; x < width; x++) {
                int alpha = pixels[rowOffset + x] >>> 24;
                if (alpha <= 2) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        if (maxX < minX || maxY < minY) {
            throw new IllegalStateException("Bush motion layer is empty.");
        }
        int margin = 4;
        int x = Math.max(0, minX - margin);
        int y = Math.max(0, minY - margin);
        int right = Math.min(width, maxX + margin + 1);
        int bottom = Math.min(height, maxY + margin + 1);
        return new Rectangle(x, y, right - x, bottom - y);
    }

    private static BufferedImage read(String fileName) {
        String resourcePath = "/images/title/" + fileName;
        URL resource = TitleAssets.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Missing title asset: " + resourcePath);
        }
        try {
            BufferedImage image = ImageIO.read(resource);
            if (image == null) {
                throw new IllegalStateException("Unsupported title asset: " + resourcePath);
            }
            return image;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read title asset: " + resourcePath, exception);
        }
    }

    record MotionLayer(BufferedImage image, int x, int y) {
        MotionLayer {
            Objects.requireNonNull(image, "image");
        }
    }
}
