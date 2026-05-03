package input_output;

import data.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class QuestionParser {

    public static List<Question> loadQuestions(String filePath, String subject, Difficulty difficulty, CustomScoringStrategy defaultStrategy) {
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

                if (line.isEmpty() || line.equals("---")) {
                    if (!questionText.isEmpty()) {
                        saveQuestion(questions, subject, questionText, options, correctAnswers, penalties, isFormatWithPenalty, difficulty, defaultStrategy);
                        questionText = "";
                    }
                    continue;
                }

                if (line.startsWith("Q:")) {
                    if (!questionText.isEmpty()) {
                        saveQuestion(questions, subject, questionText, options, correctAnswers, penalties, isFormatWithPenalty, difficulty, defaultStrategy);
                    }
                    questionText = line.substring(line.indexOf(':') + 1).trim();
                    options.clear();
                    correctAnswers.clear();
                    penalties.clear();
                    isFormatWithPenalty = false;
                } else if (line.startsWith("OK:")) {
                    correctAnswers.add(options.size());
                    options.add(line.substring(line.indexOf(':') + 1).trim());
                } else if (line.startsWith("NOK:")) {
                    options.add(line.substring(line.indexOf(':') + 1).trim());
                } else if (line.matches("^[A-Z]:.*")) {
                    isFormatWithPenalty = true;
                    options.add(line.substring(line.indexOf(':') + 1).trim());
                } else if (line.startsWith("ANS:")) {
                    String ansLetters = line.substring(line.indexOf(':') + 1).trim();
                    for (char c : ansLetters.toCharArray()) {
                        if (c >= 'A' && c <= 'Z') correctAnswers.add(c - 'A');
                    }
                } else if (line.startsWith("MINUS1:")) {
                    String ansLetters = line.substring(line.indexOf(':') + 1).trim();
                    for (char c : ansLetters.toCharArray()) {
                        if (c >= 'A' && c <= 'Z') penalties.put(c - 'A', HowStupidAnswerPenalize.STUPID);
                    }
                } else if (line.startsWith("MINUS2:")) {
                    String ansLetters = line.substring(line.indexOf(':') + 1).trim();
                    for (char c : ansLetters.toCharArray()) {
                        if (c >= 'A' && c <= 'Z') penalties.put(c - 'A', HowStupidAnswerPenalize.VERY_STUPID);
                    }
                }
            }

            if (!questionText.isEmpty()) {
                saveQuestion(questions, subject, questionText, options, correctAnswers, penalties, isFormatWithPenalty, difficulty, defaultStrategy);
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return questions;
    }

    private static void saveQuestion(List<Question> questions, String subject, String questionText,
                                     List<String> options, Set<Integer> correctAnswers,
                                     Map<Integer, HowStupidAnswerPenalize> penalties, boolean isFormatWithPenalty,
                                     Difficulty difficulty, CustomScoringStrategy defaultStrategy) {

        if (options.isEmpty()) return;

        ScoringStrategy strategy;
        if (isFormatWithPenalty) {
            strategy = new PenaltyScoringStrategy(new HashMap<>(penalties));
        } else {
            strategy = defaultStrategy;
        }

        Question q = new MultipleChoiceQuestionWithOrWithoutPenalization(
                subject, difficulty, questionText, new ArrayList<>(options), new HashSet<>(correctAnswers), strategy);

        questions.add(q);
    }
}