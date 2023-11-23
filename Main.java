import java.io.IOException;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        String directoryPath = "C:\\Users\\erick\\Downloads\\acoba";
        try {
            organizeAndDeleteFiles(directoryPath);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static void organizeAndDeleteFiles(String directoryPath) throws IOException, NoSuchAlgorithmException {
        Map<String, Path> fileChecksumMap = new HashMap<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
            for (Path filePath : directoryStream) {
                if (Files.isRegularFile(filePath)){
                    String checksum = getChecksum(filePath);
                    if (fileChecksumMap.containsKey(checksum)) {
                        System.out.println("Deleting duplicate file: " + filePath); 
                        Files.delete(filePath);
                    } else {
                        fileChecksumMap.put(checksum, filePath);
                        organizeFileByExtension(filePath, directoryPath);
                    }
                }
            }
        }
    }

    private static void organizeFileByExtension(Path filePath, String directoryPath) throws IOException {
        String fileExtension = getFileExtension(filePath);
        Path destinationDirectory = Paths.get(directoryPath, fileExtension);

        if (!Files.exists(destinationDirectory)) {
            Files.createDirectory(destinationDirectory);
        }

        Path destinationFile = Paths.get(destinationDirectory.toString(), filePath.getFileName().toString());
        Files.move(filePath, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Moved file to: " + destinationFile);
    }

    private static String getFileExtension(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "unknown";
    }

    private static String getChecksum(Path filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (DigestInputStream dis = new DigestInputStream(Files.newInputStream(filePath), md)) {
            while (dis.read() != -1) ;
        }

        byte[] digest = md.digest();
        StringBuilder checksum = new StringBuilder();
        for (byte b : digest) {
            checksum.append(String.format("%02x", b));
        }

        return checksum.toString();
    }
}
