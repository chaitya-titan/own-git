import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;


public class Main {
  public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    // Uncomment this block to pass the first stage
    //
     final String command = args[0];

     switch (command) {
       case "init" -> {
         final File root = new File(".git");
         new File(root, "objects").mkdirs();
         new File(root, "refs").mkdirs();
         final File head = new File(root, "HEAD");

         try {
           head.createNewFile();
           Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
           System.out.println("Initialized git directory");
         } catch (IOException e) {
           throw new RuntimeException(e);
         }
       }
       case "cat-file" -> {
         String plumbing = args[1];
         if(plumbing.equals("-p")) {
           String bob_sha = args[2];

           String hash1 = bob_sha.substring(0, 2);
           String hash2 = bob_sha.substring(2);

           String filePath = ".git/objects/" + hash1 + "/" + hash2;

           BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new InflaterInputStream(new FileInputStream(filePath))));

           String line = bufferedReader.readLine();

           System.out.print(line.substring(line.indexOf("\0") + 1));

           while ((line = bufferedReader.readLine()) != null) {
             System.out.print(line);
           }
         }
       }
       case "hash-object" -> {
         String plumbing = args[1];
             if (plumbing.equals("-w")) {
             String fileName = args[2];
             Path filePath = Paths.get(fileName);

             if (!Files.exists(filePath)) {
                 System.err.println("Error: File does not exist.");
                 return;
             }

             byte[] fileContent = Files.readAllBytes(filePath);
             long fileSize = fileContent.length;

             String blobHeader = "blob " + fileSize + "\0";
             byte[] blobHeaderBytes = blobHeader.getBytes();
             byte[] combinedData = concatenate(blobHeaderBytes, fileContent);

             String hash = DigestUtils.sha1Hex(combinedData);
             System.out.println(hash);

             String hash1 = hash.substring(0, 2);
             String hash2 = hash.substring(2);
             Path objectDir = Paths.get(".git/objects/" + hash1);
             if (!Files.exists(objectDir)) {
                 Files.createDirectories(objectDir);
             }

             Path objectFilePath = objectDir.resolve(hash2);
             if (!Files.exists(objectFilePath)) {
                 byte[] compressedData = compressData(combinedData);
                 Files.write(objectFilePath, compressedData);
             }
         }
       }
       default -> System.out.println("Unknown command: " + command);
     }
  }

  public static byte[] compressData(byte[] data){
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      try (DeflaterOutputStream deflater = new DeflaterOutputStream(bos)) {
          deflater.write(data);
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
      return bos.toByteArray();
  }

    private static byte[] concatenate(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
