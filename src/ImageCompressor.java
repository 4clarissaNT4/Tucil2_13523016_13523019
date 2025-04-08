import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageCompressor {
    public static class QuadTreeNode {
        int x, y, width, height;
        Color color;
        boolean isLeaf;
        QuadTreeNode[] children;

        public QuadTreeNode(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isLeaf = false;
            this.children = new QuadTreeNode[4];
        }
    }

    private static int nodeCount = 0;
    private static int maxDepth = 0;
    private static final List<BufferedImage> gifFrames = new ArrayList<>();
    private static final int FRAME_INTERVAL = 100;
    private static int frameCounter = 0;

    public static QuadTreeNode compress(BufferedImage image, int errorMethod, double threshold, int minBlockSize) {
        nodeCount = 0;
        maxDepth = 0;
        gifFrames.clear();
        return buildQuadTree(image, 0, 0, image.getWidth(), image.getHeight(), threshold, errorMethod, minBlockSize, 0);
    }

    public static BufferedImage render(QuadTreeNode root, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        renderNode(g, root);
        g.dispose();
        return img;
    }

    public static List<BufferedImage> getGifFrames() {
        return gifFrames;
    }

    public static int getNodeCount() {
        return nodeCount;
    }

    public static int getMaxDepth() {
        return maxDepth;
    }

    private static QuadTreeNode buildQuadTree(BufferedImage img, int x, int y, int width, int height,
                                              double threshold, int errorMethod, int minSize, int depth) {
        QuadTreeNode node = new QuadTreeNode(x, y, width, height);
        nodeCount++;
        maxDepth = Math.max(maxDepth, depth);

        if ((width <= minSize && height <= minSize) || getError(img, x, y, width, height, errorMethod) <= threshold) {
            node.isLeaf = true;
            node.color = getAverageColor(img, x, y, width, height);
            drawBlockFrame(img.getWidth(), img.getHeight(), node);
            return node;
        }

        int midW = width / 2;
        int midH = height / 2;

        node.children[0] = buildQuadTree(img, x, y, midW, midH, threshold, errorMethod, minSize, depth + 1);
        node.children[1] = buildQuadTree(img, x + midW, y, width - midW, midH, threshold, errorMethod, minSize, depth + 1);
        node.children[2] = buildQuadTree(img, x, y + midH, midW, height - midH, threshold, errorMethod, minSize, depth + 1);
        node.children[3] = buildQuadTree(img, x + midW, y + midH, width - midW, height - midH, threshold, errorMethod, minSize, depth + 1);

        return node;
    }

    private static double getError(BufferedImage img, int x, int y, int width, int height, int method) {
        int pixelWidth = Math.min(width, img.getWidth() - x);
        int pixelHeight = Math.min(height, img.getHeight() - y);
        int pixelCount = pixelWidth * pixelHeight;

        double sumR = 0, sumG = 0, sumB = 0;
        double sumSqR = 0, sumSqG = 0, sumSqB = 0;
        List<Integer> diffR = new ArrayList<>(), diffG = new ArrayList<>(), diffB = new ArrayList<>();

        for (int i = y; i < y + pixelHeight; i++) {
            for (int j = x; j < x + pixelWidth; j++) {
                Color c = new Color(img.getRGB(j, i));
                int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
                sumR += r; sumG += g; sumB += b;
                sumSqR += r * r; sumSqG += g * g; sumSqB += b * b;
                diffR.add(r); diffG.add(g); diffB.add(b);
            }
        }

        double meanR = sumR / pixelCount;
        double meanG = sumG / pixelCount;
        double meanB = sumB / pixelCount;

        switch (method) {
            case 1: // Variance
                double varR = (sumSqR / pixelCount) - (meanR * meanR);
                double varG = (sumSqG / pixelCount) - (meanG * meanG);
                double varB = (sumSqB / pixelCount) - (meanB * meanB);
                return (varR + varG + varB) / 3.0;

            case 2: // Mean Absolute Deviation (MAD)
                double mad = 0.0;
                for (int i = 0; i < pixelCount; i++) {
                    mad += Math.abs(diffR.get(i) - meanR) + Math.abs(diffG.get(i) - meanG) + Math.abs(diffB.get(i) - meanB);
                }
                return mad / (3.0 * pixelCount);

            case 3: // Max Pixel Difference
                int maxDiff = 0;
                for (int i = y; i < y + pixelHeight; i++) {
                    for (int j = x; j < x + pixelWidth; j++) {
                        Color c = new Color(img.getRGB(j, i));
                        int dr = Math.abs(c.getRed() - (int) meanR);
                        int dg = Math.abs(c.getGreen() - (int) meanG);
                        int db = Math.abs(c.getBlue() - (int) meanB);
                        maxDiff = Math.max(maxDiff, Math.max(dr, Math.max(dg, db)));
                    }
                }
                return maxDiff;

            case 4: // Entropy
                int[] freqR = new int[256];
                int[] freqG = new int[256];
                int[] freqB = new int[256];
                for (int i = y; i < y + pixelHeight; i++) {
                    for (int j = x; j < x + pixelWidth; j++) {
                        Color c = new Color(img.getRGB(j, i));
                        freqR[c.getRed()]++;
                        freqG[c.getGreen()]++;
                        freqB[c.getBlue()]++;
                    }
                }
                double entropyR = 0.0, entropyG = 0.0, entropyB = 0.0;
                for (int i = 0; i < 256; i++) {
                    double pR = freqR[i] / (double) pixelCount;
                    double pG = freqG[i] / (double) pixelCount;
                    double pB = freqB[i] / (double) pixelCount;
                    if (pR > 0) entropyR -= pR * (Math.log(pR) / Math.log(2));
                    if (pG > 0) entropyG -= pG * (Math.log(pG) / Math.log(2));
                    if (pB > 0) entropyB -= pB * (Math.log(pB) / Math.log(2));
                }
                return (entropyR + entropyG + entropyB) / 3.0;

            case 5: // SSIM
                //SSIM constants
                final double K1 = 0.01; 
                final double K2 = 0.03;
                final int L = 255; 
                final double C1 = Math.pow(K1 * L, 2);
                final double C2 = Math.pow(K2 * L, 2);

                double muX = 0, muY = 0;
                List<Double> orig = new ArrayList<>();
                for (int i = y; i < y + pixelHeight; i++) {
                    for (int j = x; j < x + pixelWidth; j++) {
                        Color c = new Color(img.getRGB(j, i));
                        double gray = c.getRed() * 0.299 + c.getGreen() * 0.587 + c.getBlue() * 0.114; 
                        orig.add(gray);
                        muX += gray;
                    }
                }
                muX /= pixelCount;
                Color avg = getAverageColor(img, x, y, pixelWidth, pixelHeight);
                double avgGray = avg.getRed() * 0.299 + avg.getGreen() * 0.587 + avg.getBlue() * 0.114;
                muY = avgGray; 
                double sigmaX2 = 0, sigmaY2 = 0, sigmaXY = 0;
                for (double xValue : orig) {
                    sigmaX2 += Math.pow(xValue - muX, 2);
                    sigmaY2 += Math.pow(avgGray - muY, 2);
                    sigmaXY += (xValue - muX) * (avgGray - muY);
                }
                sigmaX2 /= pixelCount;
                sigmaY2 /= pixelCount;
                sigmaXY /= pixelCount;
                double ssim = ((2 * muX * muY + C1) * (2 * sigmaXY + C2)) / ((muX * muX + muY * muY + C1) * (sigmaX2 + sigmaY2 + C2));
                return 1 - ssim; 

            default:
                return 0;
        }
    }

    private static Color getAverageColor(BufferedImage img, int x, int y, int width, int height) {
        long r = 0, g = 0, b = 0;
        int count = 0;
        for (int i = y; i < y + height && i < img.getHeight(); i++) {
            for (int j = x; j < x + width && j < img.getWidth(); j++) {
                Color c = new Color(img.getRGB(j, i));
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
                count++;
            }
        }
        return new Color((int) (r / count), (int) (g / count), (int) (b / count));
    }

    private static void renderNode(Graphics2D g, QuadTreeNode node) {
        if (node.isLeaf) {
            g.setColor(node.color);
            g.fillRect(node.x, node.y, node.width, node.height);
        } else {
            for (QuadTreeNode child : node.children) {
                if (child != null) renderNode(g, child);
            }
        }
    }

    private static void drawBlockFrame(int width, int height, QuadTreeNode node) {
        frameCounter++;
        if (frameCounter % FRAME_INTERVAL != 0) return;

        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = frame.createGraphics();
        g.setColor(node.color);
        g.fillRect(node.x, node.y, node.width, node.height);
        g.setColor(Color.BLACK);
        g.drawRect(node.x, node.y, node.width, node.height);
        g.dispose();
        gifFrames.add(frame);
    }
}
