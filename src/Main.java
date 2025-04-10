import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {

        
        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println(" _   __                                    _   _____                 _  ");
        System.out.println("| | / /                                   (_) |  __ \\               | | ");
        System.out.println("| |/ /  ___  _ __ ___  _ __  _ __ ___  ___ _  | |  \\/ __ _ _ __ ___ | |__   __ _ _ __ ");
        System.out.println("|    \\ / _ \\| '_ ` _ \\| '_ \\| '__/ _ \\/ __| | | | __ / _` | '_ ` _ \\| '_ \\ / _` | '__|");
        System.out.println("| |\\  \\ (_) | | | | | | |_) | | |  __/\\__ \\ | | |_\\ \\ (_| | | | | | | |_) | (_| | |   ");
        System.out.println("\\_| \\_/\\___/|_| |_| |_| .__/|_|  \\___||___/_|  \\____/\\__,_|_| |_| |_|_.__/ \\__,_|_|   ");
        System.out.println("                      | | ");
        System.out.println("                      |_| ");
        System.out.println("");
        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("                           Clarissa Nethania Tambunan / 13523016");
        System.out.println("                         Shannon Aurellius Anastasya Lie / 13523019");
        System.out.println("");
        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("Haiiiii, Selamat datang di program kompresi gambar menggunakan metode QuadTree!");
        System.out.println("");        
        
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

            if (!ImageCompressor.getGifFrames().isEmpty()) {
                BufferedImage lastFrame = ImageCompressor.getGifFrames().get(ImageCompressor.getGifFrames().size() - 1);
                if (!ImageCompressor.imagesEqual(lastFrame, inputImage)) {
                    ImageCompressor.getGifFrames().add(inputImage);
                }
            }

            GIFGenerator.saveGif(ImageCompressor.getGifFrames(), input.gifPath);

        } catch (IOException e) {
            // System.err.println("Error: " + e.getMessage());
        }

        long end = System.nanoTime();

        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("                               Kompresi gambar selesai!");
        System.out.println("----------------------------------------------------------------------------------------");

        Output.printStats(
            start,
            end,
            input,
            ImageCompressor.getMaxDepth(),
            ImageCompressor.getNodeCount()
        );
    }
}