package org.example;

import data.MultipleChoiceQuestionWithOrWithoutPenalization;
import data.OpenBookAnswers;
import data.Question;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    private JLabel lblProgress;
    private JTextPane txtQuestion;
    private JPanel optionsPanel;
    private JButton btnNext;
    private JScrollPane mainScrollPane;

    private List<JCheckBox> currentCheckBoxes;
    private JTextField currentOpenTextField;

    private final Color IS_GREEN = new Color(40, 167, 69);
    private final Color IS_BLUE = new Color(0, 92, 165);

    public QuizGUI(MainGUI parentFrame, String subjectName, SubjectSettings settings, List<Question> questions) {
        this.parentFrame = parentFrame;
        this.subjectName = subjectName;
        this.settings = settings;
        this.questions = questions;

        setTitle(subjectName + " - Odpovědník");
        setSize(950, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Color.WHITE);

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
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        lblProgress = new JLabel("Otázka 1 z " + questions.size(), SwingConstants.LEFT);
        lblProgress.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblProgress.setForeground(new Color(60, 60, 60));
        lblProgress.setBorder(new EmptyBorder(10, 25, 10, 25));
        topPanel.add(lblProgress, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        txtQuestion = new JTextPane();
        txtQuestion.setContentType("text/html");
        txtQuestion.setEditable(false);
        txtQuestion.setBackground(Color.WHITE);
        txtQuestion.setBorder(null);
        txtQuestion.setFocusable(false);
        txtQuestion.setAlignmentX(Component.LEFT_ALIGNMENT);

        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(Color.WHITE);
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(txtQuestion);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(optionsPanel);

        mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(5, 25, 15, 25));

        btnNext = new JButton("Uložit a další");
        btnNext.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnNext.setFocusPainted(false);
        btnNext.setBackground(IS_BLUE);
        btnNext.setForeground(Color.WHITE);
        btnNext.setPreferredSize(new Dimension(130, 35));
        btnNext.addActionListener(e -> processAnswerAndNext());

        bottomPanel.add(btnNext);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    private void showQuestion(int index) {
        Question q = questions.get(index);
        lblProgress.setText("Otázka " + (index + 1) + " z " + questions.size());

        txtQuestion.setText("<html><div style='font-family: \"Segoe UI\", sans-serif; font-size: 14px; color: #333; line-height: 1.4;'>"
                + escapeHtml(q.getText()) + "</div></html>");

        optionsPanel.removeAll();
        currentCheckBoxes = new ArrayList<>();
        currentOpenTextField = null;

        if (q instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
            if (mcq.getScoringStrategy() instanceof data.PenaltyScoringStrategy) {
                JPanel warnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                warnWrap.setBackground(Color.WHITE);
                warnWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel lblWarning = new JLabel("<html><div style='background-color: #fff3cd; color: #856404; padding: 5px 10px; border: 1px solid #ffeeba; font-size: 12px; border-radius: 3px;'><b>Upozornění:</b> Penaltové bodování.</div></html>");
                warnWrap.add(lblWarning);
                optionsPanel.add(warnWrap);
                optionsPanel.add(Box.createVerticalStrut(15));
            }

            List<String> originalOptions = mcq.getOptions();
            record OptionDisplay(int originalIndex, String text) {}
            List<OptionDisplay> shuffledOptions = new ArrayList<>();
            for (int i = 0; i < originalOptions.size(); i++) {
                shuffledOptions.add(new OptionDisplay(i, originalOptions.get(i)));
            }
            Collections.shuffle(shuffledOptions);

            for (OptionDisplay od : shuffledOptions) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                row.setBackground(Color.WHITE);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                JCheckBox cb = new JCheckBox("<html><span style='font-family: \"Segoe UI\"; font-size: 13px;'>" + escapeHtml(od.text()) + "</span></html>");
                cb.setBackground(Color.WHITE);
                cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
                cb.putClientProperty("originalIndex", od.originalIndex());

                currentCheckBoxes.add(cb);
                row.add(cb);
                optionsPanel.add(row);
                optionsPanel.add(Box.createVerticalStrut(5));
            }
        } else if (q instanceof OpenBookAnswers) {
            JLabel lblInstruction = new JLabel("Vaše odpověď:");
            lblInstruction.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblInstruction.setAlignmentX(Component.LEFT_ALIGNMENT);
            optionsPanel.add(lblInstruction);

            currentOpenTextField = new JTextField();
            currentOpenTextField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            currentOpenTextField.setMaximumSize(new Dimension(600, 30));
            currentOpenTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
            optionsPanel.add(currentOpenTextField);
        }

        if (index == questions.size() - 1) {
            btnNext.setText("Odevzdat");
            btnNext.setBackground(IS_GREEN);
        }

        optionsPanel.revalidate();
        optionsPanel.repaint();
        SwingUtilities.invokeLater(() -> mainScrollPane.getVerticalScrollBar().setValue(0));
    }

    private void processAnswerAndNext() {
        Question q = questions.get(currentIndex);
        double points = 0.0;
        Object userAnswerObj = null;

        if (q instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
            Set<Integer> userAnswers = new HashSet<>();
            for (JCheckBox cb : currentCheckBoxes) {
                if (cb.isSelected()) userAnswers.add((int) cb.getClientProperty("originalIndex"));
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
        if (currentIndex < questions.size()) showQuestion(currentIndex);
        else showFinalReport();
    }

    private void showFinalReport() {
        getContentPane().removeAll();
        setLayout(new BorderLayout(0, 0));

        double maxPoints = 0.0;
        for (Question q : questions) {
            if (q instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
                int correctCount = mcq.getCorrectOptionIndexes().size();
                if (mcq.getScoringStrategy() instanceof data.PenaltyScoringStrategy) {
                    maxPoints += correctCount * settings.getPenaltyOk();
                } else {
                    maxPoints += (correctCount >= 2) ? settings.getPtsTwoOk() : (correctCount == 1 ? settings.getPtsOneOk() : correctCount);
                }
            } else if (q instanceof OpenBookAnswers) { maxPoints += 1.0; }
        }

        double displayScore = Math.max(totalScore, 0);
        double percentage = maxPoints > 0 ? (displayScore / maxPoints) * 100 : 0;

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(new Color(235, 240, 245));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("Vyhodnocení testu: " + subjectName);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerPanel.add(lblTitle);

        JLabel lblScore = new JLabel(String.format("Získáno %.2f z %.2f bodů ( %.1f %% )", totalScore, maxPoints, percentage));
        lblScore.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerPanel.add(lblScore);
        add(headerPanel, BorderLayout.NORTH);

        JEditorPane htmlPane = new JEditorPane();
        htmlPane.setContentType("text/html");
        htmlPane.setEditable(false);

        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
                .append("body { font-family: 'Segoe UI', sans-serif; font-size: 12px; padding: 10px; margin: 0; }")
                .append(".scale-box { background-color: #f8f9fa; padding: 10px; margin-bottom: 20px; border-radius: 4px; border: 1px solid #dee2e6; text-align: center; color: #495057; font-size: 13px; }")
                .append(".q-block { padding: 10px; margin-bottom: 15px; border-radius: 4px; border: 1px solid #ddd; }")
                .append(".q-perfect { border-left: 5px solid #28a745; background-color: #f9fff9; }")
                .append(".q-wrong { border-left: 5px solid #dc3545; background-color: #fff9f9; }")
                .append(".q-text { font-size: 13px; font-weight: bold; margin-bottom: 8px; color: #222; }")
                .append(".pts { float: right; font-weight: bold; color: #444; background: #eee; padding: 2px 6px; }")
                .append(".opt { margin-bottom: 3px; margin-left: 0; padding-left: 0; font-size: 12px; text-align: left; }")
                .append(".correct-text { color: #1e7e34; font-weight: bold; }")
                .append(".wrong-text { color: #bd2130; text-decoration: line-through; }")
                .append(".missed-text { color: #856404; font-style: italic; }")
                .append("</style></head><body>");

      
        html.append("<div class='scale-box'>")
                .append("<b>Hodnoticí stupnice:</b> &nbsp;&nbsp;")
                .append("<span style='color:#28a745'><b>A:</b> 90–100%</span> &nbsp;|&nbsp; ")
                .append("<span style='color:#28a745'><b>B:</b> 80–89%</span> &nbsp;|&nbsp; ")
                .append("<span style='color:#17a2b8'><b>C:</b> 70–79%</span> &nbsp;|&nbsp; ")
                .append("<span style='color:#ffc107'><b>D:</b> 60–69%</span> &nbsp;|&nbsp; ")
                .append("<span style='color:#fd7e14'><b>E:</b> 50–59%</span> &nbsp;|&nbsp; ")
                .append("<span style='color:#dc3545'><b>F:</b> &lt; 50%</span>")
                .append("</div>");

        for (int i = 0; i < histories.size(); i++) {
            History h = histories.get(i);
            double maxForThisQ = 1.0;
            if (h.question() instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
                int cCount = mcq.getCorrectOptionIndexes().size();
                maxForThisQ = (mcq.getScoringStrategy() instanceof data.PenaltyScoringStrategy) ?
                        cCount * settings.getPenaltyOk() : ((cCount >= 2) ? settings.getPtsTwoOk() : (cCount == 1 ? settings.getPtsOneOk() : cCount));
            }

            String stateClass = (h.points() >= maxForThisQ) ? "q-perfect" : "q-wrong";
            html.append("<div class='q-block ").append(stateClass).append("'>");
            html.append("<div class='pts'>").append(h.points()).append(" / ").append(maxForThisQ).append(" b</div>");
            html.append("<div class='q-text'>").append(i + 1).append(". ").append(escapeHtml(h.question().getText())).append("</div>");

            if (h.question() instanceof MultipleChoiceQuestionWithOrWithoutPenalization mcq) {
                List<String> options = mcq.getOptions();
                @SuppressWarnings("unchecked")
                Set<Integer> uAns = (Set<Integer>) h.userAnswer();
                Set<Integer> cAns = mcq.getCorrectOptionIndexes();

                for (int j = 0; j < options.size(); j++) {
                    boolean checked = uAns.contains(j);
                    boolean correct = cAns.contains(j);

                    String box = checked ? "[x]" : "[ ]";
                    String labelClass = "";
                    String status = "";

                    if (checked && correct) { status = "<span style='color:green'>[ANO]</span>"; labelClass = "correct-text"; }
                    else if (checked && !correct) { status = "<span style='color:red'>[NE!]</span>"; labelClass = "wrong-text"; }
                    else if (!checked && correct) { status = "<span style='color:orange'>[CHYBÍ]</span>"; labelClass = "missed-text"; }

                    html.append("<div class='opt ").append(labelClass).append("'>")
                            .append(box).append(" ").append(status).append(" ").append(escapeHtml(options.get(j)))
                            .append("</div>");
                }
            } else if (h.question() instanceof OpenBookAnswers obq) {
                boolean ok = h.points() > 0;
                html.append("<div class='opt'><b>Zadáno:</b> ").append(escapeHtml((String)h.userAnswer()))
                        .append(ok ? " <span style='color:green'>[OK]</span>" : " <span style='color:red'>[CHYBA]</span>").append("</div>");
                if (!ok) html.append("<div class='opt missed-text'><b>Správně:</b> ").append(escapeHtml(obq.getCorrectAnswer())).append("</div>");
            }
            html.append("</div>");
        }

        html.append("</body></html>");
        htmlPane.setText(html.toString());
        htmlPane.setCaretPosition(0);
        add(new JScrollPane(htmlPane), BorderLayout.CENTER);

        JPanel bPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bPanel.setBackground(Color.WHITE);
        JButton btn = new JButton("Ukončit");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.addActionListener(e -> { this.dispose(); parentFrame.setVisible(true); });
        bPanel.add(btn);
        add(bPanel, BorderLayout.SOUTH);
        revalidate(); repaint();
    }
}