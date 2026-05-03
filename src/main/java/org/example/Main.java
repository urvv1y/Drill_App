package org.example;

import data.Difficulty;
import data.Question;
import data.MultipleChoiceQuestionWithOrWithoutPenalization;
import input_output.QuestionParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Loading...");


        List<Question> allQuestions = QuestionParser.loadQuestions("C:\\Users\\Lukáš\\Desktop\\MUNI\\PB152\\exam_script\\otazky.txt", "Subject", Difficulty.EASY);

        if (allQuestions.isEmpty()) {
            System.out.println("Questions cannot be loaded.");
            return;
        }


        Collections.shuffle(allQuestions);

        System.out.print("How many questions? (MAX is " + allQuestions.size() + "): ");
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


        record History(Question question, Set<Integer> userAnswers, double points) {}
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
            }
        }


        // PB152 (+4 body): testQuestions.size() * 4
        double maxPoints = testQuestions.size();

        System.out.println("\n========================================");
        System.out.println("FINAL SCORE: " + totalScore + " / " + maxPoints);

        if (maxPoints > 0) {
            double percentage = (totalScore / maxPoints) * 100;
            System.out.printf("SUCCESS RATE: %.1f %%\n", percentage);


            if (percentage >= 90) System.out.println("GRADE: A");
            else if (percentage >= 80) System.out.println("GRADE: B");
            else if (percentage >= 70) System.out.println("GRADE: C");
            else if (percentage >= 60) System.out.println("GRADE: D");
            else if (percentage >= 50) System.out.println("GRADE: E");
            else System.out.println("GRADE: F");
        }

        System.out.println("\n========================================");
        System.out.println("                MISTAKES                ");
        System.out.println("========================================");

        boolean perfect = true;

        for (int i = 0; i < histories.size(); i++) {
            History h = histories.get(i);


            if (h.points() < 1.0) {
                perfect = false;
                System.out.println("\nQ: " + h.question().getText());
                System.out.println("Points earned: " + h.points());

                if (h.question() instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
                    List<String> options = mcq.getOptions();
                    System.out.println("You selected:");

                    if (h.userAnswers().isEmpty()) {
                        System.out.println(" - (skipped)");
                    } else {
                        for (Integer ansIndex : h.userAnswers()) {
                            System.out.println(" - " + options.get(ansIndex));
                        }
                    }
                }
            }
        }
        if (perfect) {
            System.out.println("No mistakes made.");
        }
        System.out.println("========================================");

        scanner.close();
    }
}