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
    private static final List<List<QuadTreeNode>> depthLevels = new ArrayList<>();

    public static QuadTreeNode compress(BufferedImage image, int errorMethod, float threshold, int minBlockSize) {
        nodeCount = 0;
        maxDepth = 0;
        gifFrames.clear();
        depthLevels.clear();
    
        QuadTreeNode root = new QuadTreeNode(0, 0, image.getWidth(), image.getHeight());
        buildQuadTree(image, 0, 0, image.getWidth(), image.getHeight(), threshold, errorMethod, minBlockSize, 0, root, root);
    
        for (int depth = 0; depth < depthLevels.size(); depth++) {
            BufferedImage frame = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = frame.createGraphics();
            renderByDepth(g, root, depth);
            g.dispose();
            gifFrames.add(frame);
        }
    
        gifFrames.add(image);
        return root;
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
        float threshold, int errorMethod, int minSize, int depth,
        QuadTreeNode node, QuadTreeNode root) {
        nodeCount++;
        maxDepth = Math.max(maxDepth, depth);

        while (depthLevels.size() <= depth) depthLevels.add(new ArrayList<>());
        depthLevels.get(depth).add(node);

        if ((width <= minSize && height <= minSize) || getError(img, x, y, width, height, errorMethod) <= threshold) {
            node.isLeaf = true;
            node.color = getAverageColor(img, x, y, width, height);
            return node;
        }

        int midW = width / 2;
        int midH = height / 2;

        node.children[0] = buildQuadTree(img, x, y, midW, midH, threshold, errorMethod, minSize, depth + 1, new QuadTreeNode(x, y, midW, midH), root);
        node.children[1] = buildQuadTree(img, x + midW, y, width - midW, midH, threshold, errorMethod, minSize, depth + 1, new QuadTreeNode(x + midW, y, width - midW, midH), root);
        node.children[2] = buildQuadTree(img, x, y + midH, midW, height - midH, threshold, errorMethod, minSize, depth + 1, new QuadTreeNode(x, y + midH, midW, height - midH), root);
        node.children[3] = buildQuadTree(img, x + midW, y + midH, width - midW, height - midH, threshold, errorMethod, minSize, depth + 1, new QuadTreeNode(x + midW, y + midH, width - midW, height - midH), root);

        return node;
    }

    private static void renderByDepth(Graphics2D g, QuadTreeNode node, int maxRenderDepth) {
        renderByDepthHelper(g, node, 0, maxRenderDepth);
    }
    
    private static void renderByDepthHelper(Graphics2D g, QuadTreeNode node, int currentDepth, int maxDepth) {
        if (node == null) return;
        
        if (currentDepth == maxDepth || node.isLeaf) {
            g.setColor(node.color != null ? node.color : Color.BLACK);
            g.fillRect(node.x, node.y, node.width, node.height);
        } else {
            for (QuadTreeNode child : node.children) {
                renderByDepthHelper(g, child, currentDepth + 1, maxDepth);
            }
        }
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
            case 1:
                double varR = (sumSqR / pixelCount) - (meanR * meanR);
                double varG = (sumSqG / pixelCount) - (meanG * meanG);
                double varB = (sumSqB / pixelCount) - (meanB * meanB);
                return (varR + varG + varB) / 3.0;
            case 2:
                double mad = 0.0;
                for (int i = 0; i < pixelCount; i++) {
                    mad += Math.abs(diffR.get(i) - meanR) + Math.abs(diffG.get(i) - meanG) + Math.abs(diffB.get(i) - meanB);
                }
                return mad / (3.0 * pixelCount);
            case 3:
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
            case 4:
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
            case 5:
                double K1 = 0.01; 
                double K2 = 0.03;
                int L = 255; 
                double C1 = Math.pow(K1 * L, 2);
                double C2 = Math.pow(K2 * L, 2);

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

    public static boolean imagesEqual(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) return false;
        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) return false;
            }
        }
        return true;
    }
}