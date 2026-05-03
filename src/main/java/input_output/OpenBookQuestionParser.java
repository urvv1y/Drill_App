package input_output;

import data.Difficulty;
import data.OpenBookAnswers;
import data.Question;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for the open style questions.
 * @author urvy.
 */
public class OpenBookQuestionParser {

    public static List<Question> loadQuestion(String filePath, String subject, Difficulty difficulty) {
        List<Question> questions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            String questionText = "";
            String answerText = "";

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("Q:")) {
                    questionText = line.substring(2).trim();
                } else if (line.startsWith("A:")) {
                    answerText = line.substring(2).trim();
                } else if (line.equals("---")) {
                    if (!questionText.isEmpty() && !answerText.isEmpty()) {
                        saveQuestion(questions, subject, questionText, answerText, difficulty);
                        questionText = "";
                        answerText = "";
                    }
                }

            }
            if (!questionText.isEmpty() && !answerText.isEmpty()) {
                saveQuestion(questions, subject, questionText, answerText, difficulty);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return questions;
    }

    public static void saveQuestion(List<Question> questions, String subject, String questionText, String answerText, Difficulty difficulty) {
        Question q = new OpenBookAnswers(subject, difficulty, questionText, answerText);
        questions.add(q);
    }
}
