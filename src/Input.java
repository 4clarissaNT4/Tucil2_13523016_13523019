import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Input {
    public String inputPath;
    public int errorMethod;
    public float threshold;
    public int minBlockSize;
    public float targetCompression;
    public String outputPath;
    public String gifPath;

    private final Scanner scanner = new Scanner(System.in);

    public void inputAll() {
        boolean valid;
        do {
            valid = true;
            List<String> errors = new ArrayList<>();

            System.out.print("Masukkan path gambar input: "); // pathnya tanpa tanda petik dua
            inputPath = scanner.nextLine();

            System.out.println("Pilihan Metode Error:");
            System.out.println("1. Variance");
            System.out.println("2. Mean Absolute Deviation (MAD)");
            System.out.println("3. Max Pixel Difference");
            System.out.println("4. Entropy");
            System.out.println("5. Structural Similarity Index Measure (SSIM)");
            System.out.println("Masukkan pilihan (1/2/3/4/5(opt)): ");
            try {
                errorMethod = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                errorMethod = -1;
            }

            System.out.print("Masukkan threshold: ");
            try {
                threshold = Float.parseFloat(scanner.nextLine());
            } catch (NumberFormatException e) {
                threshold = -1;
            }

            System.out.print("Masukkan ukuran blok minimum: ");
            try {
                minBlockSize = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                minBlockSize = -1;
            }

            System.out.print("Masukkan target kompresi (0-1): ");
            try {
                targetCompression = Float.parseFloat(scanner.nextLine());
            } catch (NumberFormatException e) {
                targetCompression = -1;
            }

            System.out.print("Masukkan path gambar output: "); // pathnya tanpa tanda petik dua
            outputPath = scanner.nextLine();

            System.out.print("Masukkan path GIF output: "); // pathnya tanpa tanda petik dua
            gifPath = scanner.nextLine();

            if (inputPath.isEmpty()) {
                valid = false;
                errors.add("Path input tidak boleh kosong.");
            } else if (!inputPath.matches("(?i)^.+\\.(jpg|jpeg|png)$")) {
                valid = false;
                errors.add("Format file input harus .jpg, .jpeg, atau .png.");
            } else {
                File inputFile = new File(inputPath);
                if (!inputFile.exists() || !inputFile.isFile()) {
                    valid = false;
                    errors.add("File input tidak ditemukan atau bukan file.");
                }
            }            

            if (errorMethod < 1 || errorMethod > 5) {
                valid = false;
                errors.add("Metode error harus antara 1 dan 5.");
            }

            if ((errorMethod == 1 || errorMethod == 4 || errorMethod == 5) && (threshold < 0 || threshold > 1)) {
                valid = false;
                errors.add("Threshold untuk metode tersebut harus antara 0 dan 1.");
            }

            if ((errorMethod == 2 || errorMethod == 3) && (threshold < 0 || threshold > 255)) {
                valid = false;
                errors.add("Threshold untuk metode tersebut harus antara 0 dan 255.");
            }

            if (minBlockSize < 1) {
                valid = false;
                errors.add("Ukuran blok minimum harus >= 1.");
            }

            if (targetCompression < 0 || targetCompression > 1) {
                valid = false;
                errors.add("Target kompresi harus antara 0 dan 1.");
            }

            if (outputPath.isEmpty()) {
                valid = false;
                errors.add("Path output gambar tidak boleh kosong.");
            } else if (!outputPath.matches("(?i)^.+\\.(jpg|jpeg|png)$")) {
                valid = false;
                errors.add("Format file output harus .jpg, .jpeg, atau .png.");
            } else {
                File outputFile = new File(outputPath);
                File parent = outputFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    valid = false;
                    errors.add("Folder tujuan untuk output gambar tidak ditemukan.");
                }
            }            

            if (gifPath.isEmpty()) {
                valid = false;
                errors.add("Path output GIF tidak boleh kosong.");
            } else if (!gifPath.matches("(?i)^.+\\.gif$")) {
                valid = false;
                errors.add("Format file output GIF harus .gif.");
            } else {
                File gifFile = new File(gifPath);
                File parent = gifFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    valid = false;
                    errors.add("Folder tujuan untuk output GIF tidak ditemukan.");
                }
            }            

            if (!valid) {
                System.out.println("\nTerdapat kesalahan pada input:");
                for (String error : errors) {
                    System.out.println("- " + error);
                }
                System.out.println("\nSilakan masukkan ulang semua input.\n");
            }

        } while (!valid);
    }

    public String getOutputExtension() {
        int dotIndex = outputPath.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < outputPath.length() - 1) {
            return outputPath.substring(dotIndex + 1).toLowerCase();
        } else {
            return "png";
        }
    }
}
