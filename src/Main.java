import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        Input input = new Input();
        input.inputAll();

        long start = System.nanoTime();

        try {
            BufferedImage inputImage = ImageIO.read(new File(input.inputPath));
            ImageCompressor.QuadTreeNode root = ImageCompressor.compress(
                inputImage,
                input.errorMethod,
                input.threshold,
                input.minBlockSize
            );

            BufferedImage compressedImage = ImageCompressor.render(root, inputImage.getWidth(), inputImage.getHeight());

            String outputFormat = input.getOutputExtension();
            ImageIO.write(compressedImage, outputFormat, new File(input.outputPath));
        } catch (IOException e) {
            System.err.println("Terjadi kesalahan: " + e.getMessage());
        }

        long end = System.nanoTime();

        Output.printStats(
            start,
            end,
            input,
            ImageCompressor.getMaxDepth(),
            ImageCompressor.getNodeCount()
        );
    }
}
