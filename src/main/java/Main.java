import java.io.*;
import java.nio.file.Files;
import java.util.zip.InflaterInputStream;

public class Main {
  public static void main(String[] args) throws IOException {
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
         if(plumbing.equals("-w")) {
            String fileName = args[2];
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));

            String line = bufferedReader.readLine();

           System.out.println(line);
         }
       }
       default -> System.out.println("Unknown command: " + command);
     }
  }
}
