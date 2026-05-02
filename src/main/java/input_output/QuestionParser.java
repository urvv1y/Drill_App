package input_output;

import data.Question;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuestionParser {

    public static List<Question> parseFile(String filePath) throws FileNotFoundException {
        List<Question> questions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();

            while (line != null) {
                line = line.trim();

                if (line.isEmpty() || line.equals("---")) {
                    continue;
                }

                if (line.startsWith("Q: ")) {
                    String questionText = line.substring(3).trim();

                    reader.mark(1000);
                    String nextLine = reader.readLine();

                    if (nextLine != null) {
                        if (nextLine.startsWith("OK:") || nextLine.startsWith("NOK:")) {
                            reader.reset();
                        } else if (nextLine.startsWith("A:")) {
                            reader.reset();
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return questions;
    }
}
