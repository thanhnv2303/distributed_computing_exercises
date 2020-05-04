import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors
import java.util.*;

public class SortWordCount {
    public static class Count implements Comparable {
        public String word;
        public int count;

        public Count(String word, int count) {
            this.word = word;
            this.count = count;
        }

        @Override
        public int compareTo(Object other) {
            if (other instanceof Count) {
                int otherCount = ((Count) other).count;
                return otherCount - this.count;
            }
            return 0;

        }


        @Override
        public String toString() {
            return word + "\t" + count;
        }


    }

    public static void main(String[] args) {
        List<Count> listWordCounts = new ArrayList<>();
        try {
            File myObj = new File(args[0]);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] temp = data.split("\t");
                listWordCounts.add(new Count(temp[0], Integer.parseInt(temp[1])));
            }
            myReader.close();
            Collections.sort(listWordCounts);


            FileWriter myWriter = new FileWriter(args[1]);
            for (int i = 0; i < listWordCounts.size(); i++) {
                myWriter.write(String.valueOf(listWordCounts.get(i) + "\n"));

            }

            myWriter.close();
            System.out.println("Successfully wrote to the file.");


        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


    }
}
