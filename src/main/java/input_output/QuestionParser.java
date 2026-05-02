package input_output;

import data.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class QuestionParser {

    public static List<Question> loadQuestions(String filePath, String subject, Difficulty difficulty) {
        List<Question> questions = new ArrayList<>();


        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            String questionText = "";
            List<String> options = new ArrayList<>();
            Set<Integer> correctAnswers = new HashSet<>();
            Map<Integer, HowStupidAnswerPenalize> penalties = new HashMap<>();
            boolean isFormatWithPenalty = false;


            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("Q: ")) {
                    questionText = line.substring(3).trim();
                    options.clear();
                    correctAnswers.clear();
                    penalties.clear();
                    isFormatWithPenalty = false;

                } else if (line.startsWith("OK: ")) {
                    correctAnswers.add(options.size());
                    options.add(line.substring(4).trim());

                } else if (line.startsWith("NOK: ")) {
                    options.add(line.substring(5).trim());

                } else if (line.matches("^[A-Z]: .*")) {
                    isFormatWithPenalty = true;
                    options.add(line.substring(3).trim());

                } else if (line.startsWith("ANS: ")) {
                    String ansLetters = line.substring(5).trim();
                    for (char c : ansLetters.toCharArray()) {
                        correctAnswers.add(c - 'A');
                    }

                } else if (line.startsWith("MINUS1: ")) {
                    String ansLetters = line.substring(8).trim();
                    for (char c : ansLetters.toCharArray()) {
                        penalties.put(c - 'A', HowStupidAnswerPenalize.STUPID);
                    }

                } else if (line.startsWith("MINUS2: ")) {
                    String ansLetters = line.substring(8).trim();
                    for (char c : ansLetters.toCharArray()) {
                        penalties.put(c - 'A', HowStupidAnswerPenalize.VERY_STUPID);
                    }

                } else if (line.equals("---")) {

                    saveQuestion(questions, subject, questionText, options, correctAnswers, penalties, isFormatWithPenalty, difficulty);

                    questionText = "";
                }
            }


            if (!questionText.isEmpty()) {
                saveQuestion(questions, subject, questionText, options, correctAnswers, penalties, isFormatWithPenalty, difficulty);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return questions;
    }


    private static void saveQuestion(List<Question> questions, String subject, String questionText,
                                     List<String> options, Set<Integer> correctAnswers,
                                     Map<Integer, HowStupidAnswerPenalize> penalties, boolean isFormatWithPenalty, Difficulty difficulty) {

        ScoringStrategy strategy;

        if (isFormatWithPenalty) {
            strategy = new PenaltyScoringStrategy(new HashMap<>(penalties));
        } else {
            strategy = new CustomScoringStrategy(2, 1, 0, 1, 0);
        }

        Question q = new MultipleChoiceQuestionWithOrWithoutPenalization(
                subject,
                difficulty,
                questionText,
                new ArrayList<>(options),
                new HashSet<>(correctAnswers),
                strategy
        );

        questions.add(q);
    }
}