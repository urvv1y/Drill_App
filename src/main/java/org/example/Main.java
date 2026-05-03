package org.example;

import data.*;
import input_output.OpenBookQuestionParser;
import input_output.QuestionParser;

import java.io.*;
import java.util.*;

public class Main {
    private static final String CONFIG_FILE = "config.dat";
    private static AppConfig config;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("       STARTING TESTING SYSTEM          ");
        System.out.println("========================================");

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(CONFIG_FILE))) {
            config = (AppConfig) in.readObject();
            System.out.println("Settings successfully loaded. Subjects count: " + config.getSubjects().size());
        } catch (FileNotFoundException e) {
            System.out.println("Welcome for the first time! Creating new clean settings...");
            config = new AppConfig();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            config = new AppConfig();
        }

        boolean running = true;
        while (running) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Start quiz");
            System.out.println("2. Add new subject (file path, type & scoring)");
            System.out.println("3. Show available subjects");
            System.out.println("4. Save and exit");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> startQuiz();
                case "2" -> addSubject();
                case "3" -> showSubjects();
                case "4" -> running = false;
                default -> System.out.println("Invalid choice, please try again.");
            }
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE))) {
            out.writeObject(config);
            System.out.println("Settings successfully saved. Goodbye!");
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
        scanner.close();
    }

    private static void addSubject() {
        System.out.print("Enter subject name (e.g., PB112): ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter path to the text file with questions (e.g., pb112.txt): ");
        String filePath = scanner.nextLine().trim();

        System.out.println("Select question type:");
        System.out.println("A) Multiple Choice (with ABCD options)");
        System.out.println("B) Open Book (free text typing)");
        System.out.print("Choice: ");

        String typeChoice = scanner.nextLine().trim().toUpperCase();
        String quizType = typeChoice.equals("B") ? "OPEN_BOOK" : "MULTIPLE_CHOICE";

        System.out.println("Select difficulty:");
        System.out.println("1) EASY");
        System.out.println("2) INTERMEDIATE");
        System.out.println("3) HARD");
        System.out.print("Choice: ");

        String diffChoice = scanner.nextLine().trim();
        Difficulty difficulty = switch (diffChoice) {
            case "2" -> Difficulty.INTERMEDIATE;
            case "3" -> Difficulty.HARD;
            default -> Difficulty.EASY;
        };

        SubjectSettings newSettings = new SubjectSettings(filePath, quizType, difficulty);

        if ("MULTIPLE_CHOICE".equals(quizType)) {
            System.out.print("Do you want to use DEFAULT scoring (4, 0, -2, 1, -1)? (Y/N): ");
            String useDefault = scanner.nextLine().trim().toUpperCase();

            if ("N".equals(useDefault)) {
                try {
                    System.out.print("Points for 2 correct out of 2 (e.g., 4): ");
                    int p2 = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Points for 1 correct out of 2 (e.g., 0): ");
                    int p1in2 = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Points for 0 correct out of 2 (e.g., -2): ");
                    int p0in2 = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Points for 1 correct out of 1 (e.g., 1): ");
                    int p1 = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Points for 1 wrong out of 1 (e.g., -1): ");
                    int p1w = Integer.parseInt(scanner.nextLine().trim());

                    newSettings.setCustomScoring(p2, p1in2, p0in2, p1, p1w);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format. Default scoring will be used.");
                }
            }
        }

        config.addSubject(name, newSettings);
        System.out.println("Subject " + name + " was successfully added to the configuration.");
    }

    private static void showSubjects() {
        Map<String, SubjectSettings> subjects = config.getSubjects();
        if (subjects.isEmpty()) {
            System.out.println("You have no subjects configured yet.");
            return;
        }

        System.out.println("\n--- AVAILABLE SUBJECTS ---");
        for (Map.Entry<String, SubjectSettings> entry : subjects.entrySet()) {
            SubjectSettings s = entry.getValue();
            System.out.println("- " + entry.getKey() + " (File: " + s.getFilePath()
                    + ", Type: " + s.getQuizType() + ", Difficulty: " + s.getDifficulty() + ")");
        }
    }

    private static void startQuiz() {
        if (config.getSubjects().isEmpty()) {
            System.out.println("You must add a subject first (option 2 in the menu).");
            return;
        }

        showSubjects();
        System.out.print("\nType the name of the subject you want to start: ");
        String subjectName = scanner.nextLine().trim();

        SubjectSettings settings = config.getSubjects().get(subjectName);
        if (settings == null) {
            System.out.println("Subject with this name was not found.");
            return;
        }

        System.out.println("Loading questions from file " + settings.getFilePath() + "...");
        List<Question> allQuestions;

        if ("OPEN_BOOK".equals(settings.getQuizType())) {
            allQuestions = OpenBookQuestionParser.loadQuestions(settings.getFilePath(), subjectName, settings.getDifficulty());
        } else {
            // Sestavení strategie z uživatelského nastavení předmětu[cite: 2]
            CustomScoringStrategy userStrategy = new CustomScoringStrategy(
                    settings.getPtsTwoOk(), settings.getPtsOneOkInTwo(),
                    settings.getPtsZeroOkInTwo(), settings.getPtsOneOk(), settings.getPtsOneWrong()
            );
            allQuestions = QuestionParser.loadQuestions(settings.getFilePath(), subjectName, settings.getDifficulty(), userStrategy);
        }

        if (allQuestions.isEmpty()) {
            System.out.println("No questions could be loaded.");
            return;
        }

        Collections.shuffle(allQuestions);

        System.out.print("How many questions do you want? (MAX is " + allQuestions.size() + "): ");
        int limit = allQuestions.size();
        try {
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                limit = Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            System.out.println("INVALID input, MAXIMUM will be used.");
        }

        List<Question> testQuestions = allQuestions.stream()
                .limit(limit)
                .toList();

        double totalScore = 0.0;
        record History(Question question, Object userAnswer, double points) {}
        List<History> histories = new ArrayList<>();

        for (int i = 0; i < testQuestions.size(); i++) {
            Question q = testQuestions.get(i);
            System.out.println("\nQuestion " + (i + 1) + "/" + testQuestions.size());
            System.out.println(q.getText());
            System.out.println("----------------------------------------");

            if (q instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
                List<String> originalOptions = mcq.getOptions();

                record OptionDisplay(int originalIndex, String text) {}
                List<OptionDisplay> shuffledOptions = new ArrayList<>();

                for (int j = 0; j < originalOptions.size(); j++) {
                    shuffledOptions.add(new OptionDisplay(j, originalOptions.get(j)));
                }
                Collections.shuffle(shuffledOptions);

                for (int j = 0; j < shuffledOptions.size(); j++) {
                    char letter = (char) ('A' + j);
                    System.out.println("[" + letter + "] " + shuffledOptions.get(j).text());
                }

                System.out.print("Your answer (e.g., AC): ");
                String input = scanner.nextLine().trim().toUpperCase();

                Set<Integer> userAnswers = new HashSet<>();
                for (char c : input.toCharArray()) {
                    int displayIndex = c - 'A';
                    if (displayIndex >= 0 && displayIndex < shuffledOptions.size()) {
                        int originalIndex = shuffledOptions.get(displayIndex).originalIndex();
                        userAnswers.add(originalIndex);
                    }
                }

                double points = mcq.evaluateAnswer(userAnswers);
                totalScore += points;
                System.out.println("Result: " + points + " points.");
                histories.add(new History(q, userAnswers, points));

            } else if (q instanceof OpenBookAnswers obq) {
                System.out.print("Your answer: ");
                String input = scanner.nextLine().trim();

                boolean isCorrect = obq.isCorrect(input);
                double points = isCorrect ? 1.0 : 0.0;
                totalScore += points;

                System.out.println(isCorrect ? "Correct! You earned 1.0 points." : "Wrong. You earned 0.0 points.");
                histories.add(new History(q, input, points));
            }
        }


        double maxPoints = 0.0;
        for (Question q : testQuestions) {
            if (q instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
                int correctCount = mcq.getCorrectOptionIndexes().size();

                if (correctCount == 2) maxPoints += settings.getPtsTwoOk();
                else if (correctCount == 1) maxPoints += settings.getPtsOneOk();
                else maxPoints += correctCount;
            } else if (q instanceof OpenBookAnswers) {
                maxPoints += 1.0;
            }
        }

        System.out.println("\n========================================");
        System.out.println("FINAL SCORE: " + totalScore + " / " + maxPoints);

        if (maxPoints > 0) {
            double displayScore = Math.max(totalScore, 0);
            double percentage = (displayScore / maxPoints) * 100;
            System.out.printf("SUCCESS RATE: %.1f %%\n", percentage);

            if (percentage >= 90) System.out.println("GRADE: A");
            else if (percentage >= 80) System.out.println("GRADE: B");
            else if (percentage >= 70) System.out.println("GRADE: C");
            else if (percentage >= 60) System.out.println("GRADE: D");
            else if (percentage >= 50) System.out.println("GRADE: E");
            else System.out.println("GRADE: F");
        }

        System.out.println("\n========================================");
        System.out.println("              MISTAKES                  ");
        System.out.println("========================================");

        boolean perfect = true;
        for (int i = 0; i < histories.size(); i++) {
            History h = histories.get(i);


            double maxForThisQ = 1.0;
            if (h.question() instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
                int correctCount = mcq.getCorrectOptionIndexes().size();
                if (correctCount == 2) maxForThisQ = settings.getPtsTwoOk();
                else if (correctCount == 1) maxForThisQ = settings.getPtsOneOk();
                else maxForThisQ = correctCount;
            }

            if (h.points() < maxForThisQ) {
                perfect = false;
                System.out.println("\nQ: " + h.question().getText());
                System.out.println("Points earned: " + h.points() + " / " + maxForThisQ);

                if (h.question() instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
                    List<String> options = mcq.getOptions();
                    @SuppressWarnings("unchecked")
                    Set<Integer> uAnsSet = (Set<Integer>) h.userAnswer();

                    System.out.println("You selected:");
                    if (uAnsSet.isEmpty()) {
                        System.out.println(" - (skipped)");
                    } else {
                        for (Integer ansIndex : uAnsSet) {
                            System.out.println(" - " + options.get(ansIndex));
                        }
                    }

                    System.out.println("Correct answers:");
                    for (Integer correctIndex : mcq.getCorrectOptionIndexes()) {
                        System.out.println(" * " + options.get(correctIndex));
                    }
                } else if (h.question() instanceof OpenBookAnswers obq) {
                    System.out.println("You wrote: " + h.userAnswer());
                    System.out.println("Correct answer: " + obq.getCorrectAnswer());
                }
            }
        }

        if (perfect) {
            System.out.println("Perfect score! No mistakes made.");
        }
        System.out.println("========================================\n");
    }
}