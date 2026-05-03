package input_output;

import data.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QuestionParser {

    public static List<Question> loadQuestions(String filePath, String subject, Difficulty difficulty, CustomScoringStrategy defaultStrategy, int pOk, int pStupid, int pVeryStupid) {
        List<Question> questions = new ArrayList<>();


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            String questionText = "";
            List<String> options = new ArrayList<>();
            Set<Integer> correctAnswers = new HashSet<>();
            Map<Integer, HowStupidAnswerPenalize> penalties = new HashMap<>();
            boolean isFormatWithPenalty = false;

            int lastAddedOptionIndex = -1;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    if (line.startsWith("\uFEFF")) line = line.substring(1); // Odstranění BOM
                    isFirstLine = false;
                }

                String trimmed = line.trim();

                if (trimmed.isEmpty()) continue;

                if (trimmed.equals("---")) {
                    if (!questionText.isEmpty()) {
                        saveQuestion(questions, subject, questionText, options, correctAnswers, penalties, isFormatWithPenalty, difficulty, defaultStrategy, pOk, pStupid, pVeryStupid);
                        questionText = "";
                    }
                    continue;
                }

                if (trimmed.startsWith("Q:")) {
                    if (!questionText.isEmpty()) {
                        saveQuestion(questions, subject, questionText, options, correctAnswers, penalties, isFormatWithPenalty, difficulty, defaultStrategy, pOk, pStupid, pVeryStupid);
                    }
                    questionText = trimmed.substring(trimmed.indexOf(':') + 1).trim();
                    options.clear(); correctAnswers.clear(); penalties.clear();
                    isFormatWithPenalty = false;
                    lastAddedOptionIndex = -1;
                }
                else if (trimmed.startsWith("OK:")) {
                    correctAnswers.add(options.size());
                    options.add(trimmed.substring(trimmed.indexOf(':') + 1).trim());
                    lastAddedOptionIndex = options.size() - 1;
                } else if (trimmed.startsWith("NOK:")) {
                    options.add(trimmed.substring(trimmed.indexOf(':') + 1).trim());
                    lastAddedOptionIndex = options.size() - 1;
                }
                else if (trimmed.matches("^[A-Z]:.*")) {
                    isFormatWithPenalty = true;
                    options.add(trimmed.substring(trimmed.indexOf(':') + 1).trim());
                    lastAddedOptionIndex = options.size() - 1;
                }
                else if (trimmed.startsWith("ANS:")) {
                    String ansLetters = trimmed.substring(trimmed.indexOf(':') + 1).trim();
                    for (char c : ansLetters.toCharArray()) {
                        if (c >= 'A' && c <= 'Z') correctAnswers.add(c - 'A');
                    }
                    lastAddedOptionIndex = -2;
                }
                else if (trimmed.startsWith("MINUS1:")) {
                    String ansLetters = trimmed.substring(trimmed.indexOf(':') + 1).trim();
                    for (char c : ansLetters.toCharArray()) {
                        if (c >= 'A' && c <= 'Z') penalties.put(c - 'A', HowStupidAnswerPenalize.STUPID);
                    }
                    lastAddedOptionIndex = -2;
                } else if (trimmed.startsWith("MINUS2:")) {
                    String ansLetters = trimmed.substring(trimmed.indexOf(':') + 1).trim();
                    for (char c : ansLetters.toCharArray()) {
                        if (c >= 'A' && c <= 'Z') penalties.put(c - 'A', HowStupidAnswerPenalize.VERY_STUPID);
                    }
                    lastAddedOptionIndex = -2;
                }
                else {

                    if (lastAddedOptionIndex == -1 && !questionText.isEmpty()) {
                        questionText += "\n" + trimmed;
                    } else if (lastAddedOptionIndex >= 0 && lastAddedOptionIndex < options.size()) {
                        String updated = options.get(lastAddedOptionIndex) + "\n" + trimmed;
                        options.set(lastAddedOptionIndex, updated);
                    }
                }
            }

            if (!questionText.isEmpty()) {
                saveQuestion(questions, subject, questionText, options, correctAnswers, penalties, isFormatWithPenalty, difficulty, defaultStrategy, pOk, pStupid, pVeryStupid);
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return questions;
    }

    private static void saveQuestion(List<Question> questions, String subject, String questionText,
                                     List<String> options, Set<Integer> correctAnswers,
                                     Map<Integer, HowStupidAnswerPenalize> penalties, boolean isFormatWithPenalty,
                                     Difficulty difficulty, CustomScoringStrategy defaultStrategy, int pOk, int pStupid, int pVeryStupid) {

        if (options.isEmpty()) return;

        ScoringStrategy strategy = isFormatWithPenalty ?
                new PenaltyScoringStrategy(new HashMap<>(penalties), pOk, pStupid, pVeryStupid) : defaultStrategy;

        questions.add(new MultipleChoiceQuestionWithOrWithoutPenalization(
                subject, difficulty, questionText, new ArrayList<>(options), new HashSet<>(correctAnswers), strategy));
    }
}