import java.io.File;

public class Output {
    public static void printStats(long startTime, long endTime, Input input, int depth, int nodeCount) {
        double duration = (endTime - startTime) / 1e9;

        File inputFile = new File(input.inputPath);
        File outputFile = new File(input.outputPath);

        System.out.printf("Waktu eksekusi: %.3f detik\n", duration);
        System.out.println("Ukuran sebelum: " + inputFile.length() + " bytes");
        System.out.println("Ukuran setelah: " + outputFile.length() + " bytes");

        double compressionRatio = 100.0 * (1.0 - (double) outputFile.length() / inputFile.length());
        System.out.printf("Persentase kompresi: %.2f%%\n", compressionRatio);

        System.out.println("Kedalaman pohon: " + depth);
        System.out.println("Jumlah simpul: " + nodeCount);
        System.out.println("");
        System.out.println("Gambar hasil kompresi disimpan di: " + input.outputPath);
        System.out.println("GIF hasil kompresi disimpan di: " + input.gifPath);
    }
}
