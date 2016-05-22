import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Kemal on 19.04.2016.
 */
public class Main {
    public static HashMap<String, ArrayList<String>> categoryImageMap = new HashMap();
    public static List<String> stroopImageList = new ArrayList<String>();
    public static int maxNumberOfStroopImages = 20;

    public static void main(String[] args) {

        createImageSequenceScript();

        createStroopImageSequenceScript();
    }

    private static void createImageSequenceScript() {
        String strFolderPath = "src/main/resources/images/categories/";

        try {
            Object[] objectArray = Files.walk(Paths.get(strFolderPath)).toArray();
            Path[] pathArray = Arrays.copyOf(objectArray, objectArray.length, Path[].class);

            // Get folders
            ArrayList<Path> folders = getFolderPaths(pathArray);

            // Create imageMap
            for (int i = 0; i < folders.size(); i++) {
                addToCategoryMap(folders.get(i));
            }

            // Generate image sequence
            generateImageSequenceScript();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createStroopImageSequenceScript() {
        String strFolderPath = "src/main/resources/images/stroopTest/";

        try {
            Files.walk(Paths.get(strFolderPath)).forEach(childPath ->
            {
                if(!strFolderPath.equals(childPath.toString()) && Files.isRegularFile(childPath) && childPath.toString().endsWith("png"))
                {
                    String childPathForJs = childPath.toString().replace("src/main/", "");
                    System.out.println("Image File :" + childPathForJs);
                    stroopImageList.add(childPathForJs);
                }
            });

            // Add Randomly Copy of Images
            Random rand = new Random();
            while(stroopImageList.size() < maxNumberOfStroopImages){
                int randomNum = rand.nextInt(stroopImageList.size() - 1 );

                // Add if random image is not the same with the last image
                if(!stroopImageList.get(stroopImageList.size() - 1).equalsIgnoreCase(stroopImageList.get(randomNum))) {
                    stroopImageList.add(stroopImageList.get(randomNum));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        generateStroopImageSequenceScript();
    }

    private static void generateStroopImageSequenceScript() {
        StringBuilder strBuilder  = new StringBuilder("var stroopImageSequence = [");

        for (int i = 0; i < stroopImageList.size(); i++) {
            strBuilder.append("\n'" + stroopImageList.get(i) + "',");
        }

        // Remove last ','
        strBuilder.deleteCharAt(strBuilder.length() - 1);

        strBuilder.append("];\n");

        // Generate Color Answers
        strBuilder.append("var colorAnswers = [");

        for (int i = 0; i < stroopImageList.size(); i++) {
            String strImage = stroopImageList.get(i);
            String strColor = strImage.substring(strImage.lastIndexOf("/") + 1, strImage.lastIndexOf("-"));
            strBuilder.append("'" + strColor + "',");
        }

        // Remove last ','
        strBuilder.deleteCharAt(strBuilder.length() - 1);

        strBuilder.append("];\n");

        // Generate Word Answers
        strBuilder.append("var wordAnswers = [");

        for (int i = 0; i < stroopImageList.size(); i++) {
            String strImage = stroopImageList.get(i);
            String strColor = strImage.substring(strImage.lastIndexOf("-") + 1, strImage.lastIndexOf("."));
            strBuilder.append("'" + strColor + "',");
        }

        // Remove last ','
        strBuilder.deleteCharAt(strBuilder.length() - 1);

        strBuilder.append("];\n");

        System.out.println(strBuilder.toString());

        try {
            Files.write(Paths.get("stroopImageSequence.js"), strBuilder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void generateImageSequenceScript() {
        StringBuilder strBuilder  = new StringBuilder("var imageSequence = [");

        categoryImageMap.keySet().forEach(category -> {
            categoryImageMap.get(category).forEach(image -> {
                strBuilder.append("'" + image + "',");
            });
        });

        // Remove last ','
        strBuilder.deleteCharAt(strBuilder.length() - 1);

        strBuilder.append("];");

        System.out.println(strBuilder.toString());

        try {
            Files.write(Paths.get("imageSequence.js"), strBuilder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<Path> getFolderPaths(Path[] pathArray) {
        ArrayList<Path> folders = new ArrayList<Path>();
        // i = 0 => root, discard it
        for (int i = 1; i < pathArray.length; i++) {
            if(Files.isDirectory(pathArray[i], LinkOption.NOFOLLOW_LINKS)) {
                folders.add(pathArray[i]);
            }
        }
        return folders;
    }

    private static void addToCategoryMap(Path filePath) {
        String strFilePath = filePath.toString();
        System.out.println("Dir : " + strFilePath);
        categoryImageMap.put(strFilePath, new ArrayList<>());

        try {
            Files.walk(filePath).forEach(childPath ->
            {
                if(!filePath.equals(childPath) && Files.isRegularFile(childPath) && childPath.toString().endsWith("jpg"))
                {
                    String childPathForJs = childPath.toString().replace("src/main/", "");
                    System.out.println("Image File :" + childPathForJs);
                    ((ArrayList)categoryImageMap.get(strFilePath)).add(childPathForJs);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
