package org.example;

import data.MultipleChoiceQuestionWithOrWithoutPenalization;
import data.OpenBookAnswers;
import data.Question;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

public class QuizGUI extends JFrame {
    private MainGUI parentFrame;
    private String subjectName;
    private SubjectSettings settings;
    private List<Question> questions;

    private int currentIndex = 0;
    private double totalScore = 0.0;

    record History(Question question, Object userAnswer, double points) {}
    private List<History> histories = new ArrayList<>();

    private JLabel lblHeader;
    private JTextArea txtQuestion;
    private JPanel optionsPanel;
    private JButton btnNext;

    private List<JCheckBox> currentCheckBoxes;
    private JTextField currentOpenTextField;

    public QuizGUI(MainGUI parentFrame, String subjectName, SubjectSettings settings, List<Question> questions) {
        this.parentFrame = parentFrame;
        this.subjectName = subjectName;
        this.settings = settings;
        this.questions = questions;

        setTitle("Kvíz: " + subjectName);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parentFrame.setVisible(true);
            }
        });

        initUI();
        showQuestion(0);
    }

    private void initUI() {
        lblHeader = new JLabel("Otázka 1 / " + questions.size(), SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblHeader, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        txtQuestion = new JTextArea();
        txtQuestion.setFont(new Font("Arial", Font.PLAIN, 16));
        txtQuestion.setLineWrap(true);
        txtQuestion.setWrapStyleWord(true);
        txtQuestion.setEditable(false);
        txtQuestion.setBackground(new Color(240, 240, 240));
        centerPanel.add(new JScrollPane(txtQuestion), BorderLayout.NORTH);

        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        centerPanel.add(new JScrollPane(optionsPanel), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        btnNext = new JButton("Další otázka");
        btnNext.setFont(new Font("Arial", Font.BOLD, 14));
        btnNext.addActionListener(e -> processAnswerAndNext());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnNext);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void showQuestion(int index) {
        Question q = questions.get(index);
        lblHeader.setText("Otázka " + (index + 1) + " / " + questions.size());
        txtQuestion.setText(q.getText());

        optionsPanel.removeAll();
        currentCheckBoxes = new ArrayList<>();
        currentOpenTextField = null;

        if (q instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {

            if (mcq.getScoringStrategy() instanceof data.PenaltyScoringStrategy) {
                JLabel lblWarning = new JLabel("<html><b style='color:red;'>POZOR: Tato otázka je penaltová!</b> Za špatné odpovědi se strhávají body.</html>");
                lblWarning.setFont(new Font("Arial", Font.PLAIN, 14));
                optionsPanel.add(lblWarning);
                optionsPanel.add(Box.createVerticalStrut(10));
            }

            List<String> originalOptions = mcq.getOptions();
            record OptionDisplay(int originalIndex, String text) {}
            List<OptionDisplay> shuffledOptions = new ArrayList<>();
            for (int i = 0; i < originalOptions.size(); i++) {
                shuffledOptions.add(new OptionDisplay(i, originalOptions.get(i)));
            }
            Collections.shuffle(shuffledOptions);

            for (OptionDisplay od : shuffledOptions) {
                JCheckBox cb = new JCheckBox(od.text());
                cb.setFont(new Font("Arial", Font.PLAIN, 14));
                cb.putClientProperty("originalIndex", od.originalIndex());
                currentCheckBoxes.add(cb);
                optionsPanel.add(cb);
                optionsPanel.add(Box.createVerticalStrut(5));
            }
        } else if (q instanceof OpenBookAnswers) {
            optionsPanel.add(new JLabel("Zadej svou odpověď:"));
            currentOpenTextField = new JTextField();
            currentOpenTextField.setFont(new Font("Arial", Font.PLAIN, 16));
            currentOpenTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            optionsPanel.add(currentOpenTextField);
        }

        if (index == questions.size() - 1) {
            btnNext.setText("Vyhodnotit kvíz");
        }

        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    private void processAnswerAndNext() {
        Question q = questions.get(currentIndex);
        double points = 0.0;
        Object userAnswerObj = null;

        if (q instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
            Set<Integer> userAnswers = new HashSet<>();
            for (JCheckBox cb : currentCheckBoxes) {
                if (cb.isSelected()) {
                    int originalIndex = (int) cb.getClientProperty("originalIndex");
                    userAnswers.add(originalIndex);
                }
            }
            points = mcq.evaluateAnswer(userAnswers);
            userAnswerObj = userAnswers;
        } else if (q instanceof OpenBookAnswers obq) {
            String answer = currentOpenTextField.getText().trim();
            points = obq.isCorrect(answer) ? 1.0 : 0.0;
            userAnswerObj = answer;
        }

        totalScore += points;
        histories.add(new History(q, userAnswerObj, points));

        currentIndex++;
        if (currentIndex < questions.size()) {
            showQuestion(currentIndex);
        } else {
            showFinalReport();
        }
    }

    private void showFinalReport() {
        getContentPane().removeAll();
        setLayout(new BorderLayout(10, 10));

        double maxPoints = 0.0;
        for (Question q : questions) {
            if (q instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
                int correctCount = mcq.getCorrectOptionIndexes().size();
                if (mcq.getScoringStrategy() instanceof data.PenaltyScoringStrategy) {
                    maxPoints += correctCount * settings.getPenaltyOk();
                } else {
                    if (correctCount == 2) maxPoints += settings.getPtsTwoOk();
                    else if (correctCount == 1) maxPoints += settings.getPtsOneOk();
                    else maxPoints += correctCount;
                }
            } else if (q instanceof OpenBookAnswers) {
                maxPoints += 1.0;
            }
        }

        double displayScore = Math.max(totalScore, 0);
        double percentage = maxPoints > 0 ? (displayScore / maxPoints) * 100 : 0;


        String grade = "F";
        if (percentage >= 90) grade = "A (Výborně)";
        else if (percentage >= 80) grade = "B (Velmi dobře)";
        else if (percentage >= 70) grade = "C (Dobře)";
        else if (percentage >= 60) grade = "D (Uspokojivě)";
        else if (percentage >= 50) grade = "E (Dostatečně)";

        JPanel headerPanel = new JPanel(new GridLayout(3, 1));
        headerPanel.add(new JLabel("KONEČNÉ SKÓRE: " + totalScore + " / " + maxPoints, SwingConstants.CENTER));
        headerPanel.add(new JLabel(String.format("ÚSPĚŠNOST: %.1f %%", percentage), SwingConstants.CENTER));
        headerPanel.add(new JLabel("ZNÁMKA: " + grade, SwingConstants.CENTER));
        headerPanel.setFont(new Font("Arial", Font.BOLD, 20));
        add(headerPanel, BorderLayout.NORTH);

        JTextArea txtReport = new JTextArea();
        txtReport.setEditable(false);
        txtReport.setFont(new Font("Monospaced", Font.PLAIN, 14));

        StringBuilder report = new StringBuilder("=== PŘEHLED CHYB A SPRÁVNÝCH ODPOVĚDÍ ===\n\n");
        boolean perfect = true;

        for (History h : histories) {
            double maxForThisQ = 1.0;
            if (h.question() instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
                int cCount = mcq.getCorrectOptionIndexes().size();
                if (mcq.getScoringStrategy() instanceof data.PenaltyScoringStrategy) {
                    maxForThisQ = cCount * settings.getPenaltyOk();
                } else {
                    maxForThisQ = (cCount == 2) ? settings.getPtsTwoOk() : (cCount == 1 ? settings.getPtsOneOk() : cCount);
                }
            }

            if (h.points() < maxForThisQ) {
                perfect = false;
                report.append("Q: ").append(h.question().getText()).append("\n");
                report.append("Získáno: ").append(h.points()).append(" / ").append(maxForThisQ).append(" bodů\n");

                if (h.question() instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
                    List<String> options = mcq.getOptions();
                    @SuppressWarnings("unchecked")
                    Set<Integer> uAns = (Set<Integer>) h.userAnswer();

                    report.append("Tvá odpověď:\n");
                    if (uAns.isEmpty()) report.append(" - (přeskočeno)\n");
                    else for (int idx : uAns) report.append(" - ").append(options.get(idx)).append("\n");

                    report.append("Správná odpověď:\n");
                    for (int idx : mcq.getCorrectOptionIndexes()) report.append(" * ").append(options.get(idx)).append("\n");
                } else if (h.question() instanceof OpenBookAnswers obq) {
                    report.append("Tvá odpověď: ").append(h.userAnswer()).append("\n");
                    report.append("Správně mělo být: ").append(obq.getCorrectAnswer()).append("\n");
                }
                report.append("--------------------------------------------------\n");
            }
        }

        if (perfect) report.append("Bez chyby.");

        txtReport.setText(report.toString());
        txtReport.setCaretPosition(0);
        add(new JScrollPane(txtReport), BorderLayout.CENTER);

        JButton btnClose = new JButton("Zpět do menu");
        btnClose.setFont(new Font("Arial", Font.BOLD, 14));
        btnClose.addActionListener(e -> {
            this.dispose();
            parentFrame.setVisible(true);
        });
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }
}