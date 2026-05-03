package org.example;

import data.Difficulty;
import data.Question;
import input_output.OpenBookQuestionParser;
import input_output.QuestionParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainGUI extends JFrame {
    private static final String CONFIG_FILE = "config.dat";
    private AppConfig config;
    private DefaultListModel<String> listModel;
    private JList<String> subjectList;

    // Barvy ve stylu IS MUNI
    private final Color IS_BLUE = new Color(0, 92, 165);
    private final Color IS_LIGHT_BG = new Color(245, 246, 248);

    public MainGUI() {
        loadConfig();

        // Nastavení moderního vzhledu oken
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        setTitle("IS MUNI - Odpovědníky");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Color.WHITE);

        initUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveConfig();
            }
        });
    }

    private void initUI() {
        // Hlavička
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(IS_BLUE);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel("Výběr odpovědníku");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Seznam předmětů
        listModel = new DefaultListModel<>();
        refreshList();
        subjectList = new JList<>(listModel);
        subjectList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subjectList.setSelectionBackground(new Color(220, 235, 250));
        subjectList.setSelectionForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(subjectList);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));
        scrollPane.setBackground(Color.WHITE);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        centerPanel.setBackground(IS_LIGHT_BG);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Spodní panel s tlačítky
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(IS_LIGHT_BG);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        JButton btnAdd = createStyledButton("Přidat konfiguraci", false);
        JButton btnEdit = createStyledButton("Upravit", false);
        JButton btnDelete = createStyledButton("Odstranit", false);
        JButton btnStart = createStyledButton("Složit test", true);

        btnAdd.addActionListener(e -> showSubjectDialog(null));
        btnEdit.addActionListener(e -> {
            String selected = subjectList.getSelectedValue();
            if (selected != null) showSubjectDialog(selected.split(" - ")[0]);
            else JOptionPane.showMessageDialog(this, "Vyberte předmět k úpravě.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        btnDelete.addActionListener(e -> {
            String selected = subjectList.getSelectedValue();
            if (selected != null) {
                config.getSubjects().remove(selected.split(" - ")[0]);
                refreshList();
            }
        });

        btnStart.addActionListener(e -> {
            String selected = subjectList.getSelectedValue();
            if (selected != null) startQuizMode(selected.split(" - ")[0]);
            else JOptionPane.showMessageDialog(this, "Nejprve vyberte test ze seznamu.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnStart);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", isPrimary ? Font.BOLD : Font.PLAIN, 14));
        btn.setFocusPainted(false);
        if (isPrimary) {
            btn.setBackground(IS_BLUE);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.DARK_GRAY);
        }
        return btn;
    }

    private void showSubjectDialog(String existingName) {
        boolean isEdit = existingName != null;
        SubjectSettings existingSettings = isEdit ? config.getSubjects().get(existingName) : null;

        JDialog dialog = new JDialog(this, isEdit ? "Úprava nastavení testu" : "Nový test", true);
        dialog.setSize(500, 680);
        dialog.setLayout(new GridLayout(0, 2, 8, 8));
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Color.WHITE);
        ((JComponent)dialog.getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextField txtName = new JTextField(isEdit ? existingName : "");
        JTextField txtFile = new JTextField(isEdit ? existingSettings.getFilePath() : "");
        txtFile.setEditable(false);

        JButton btnBrowse = new JButton("Procházet...");
        btnBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Textové soubory (*.txt)", "txt"));
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                txtFile.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JComboBox<String> cbType = new JComboBox<>(new String[]{"MULTIPLE_CHOICE", "OPEN_BOOK"});
        JComboBox<Difficulty> cbDiff = new JComboBox<>(Difficulty.values());

        JTextField txtP2 = new JTextField("4");
        JTextField txtP1in2 = new JTextField("0");
        JTextField txtP0in2 = new JTextField("-2");
        JTextField txtP1 = new JTextField("1");
        JTextField txtP1W = new JTextField("-1");

        JTextField txtPenOk = new JTextField("1");
        JTextField txtPenStupid = new JTextField("-1");
        JTextField txtPenVeryStupid = new JTextField("-2");

        if (isEdit) {
            cbType.setSelectedItem(existingSettings.getQuizType());
            cbDiff.setSelectedItem(existingSettings.getDifficulty());
            txtP2.setText(String.valueOf(existingSettings.getPtsTwoOk()));
            txtP1in2.setText(String.valueOf(existingSettings.getPtsOneOkInTwo()));
            txtP0in2.setText(String.valueOf(existingSettings.getPtsZeroOkInTwo()));
            txtP1.setText(String.valueOf(existingSettings.getPtsOneOk()));
            txtP1W.setText(String.valueOf(existingSettings.getPtsOneWrong()));
            txtPenOk.setText(String.valueOf(existingSettings.getPenaltyOk()));
            txtPenStupid.setText(String.valueOf(existingSettings.getPenaltyStupid()));
            txtPenVeryStupid.setText(String.valueOf(existingSettings.getPenaltyVeryStupid()));
        }

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font headerFont = new Font("Segoe UI", Font.BOLD, 15);

        dialog.add(createLabel("Název předmětu (např. PB152):", labelFont)); dialog.add(txtName);
        dialog.add(createLabel("Zdrojový textový soubor:", labelFont)); dialog.add(txtFile);
        dialog.add(new JLabel("")); dialog.add(btnBrowse);
        dialog.add(createLabel("Typ otázek:", labelFont)); dialog.add(cbType);
        dialog.add(createLabel("Úroveň obtížnosti:", labelFont)); dialog.add(cbDiff);

        dialog.add(createLabel("--- Bodování (Klasické) ---", headerFont)); dialog.add(new JLabel(""));
        dialog.add(createLabel("2 správně ze 2 možných:", labelFont)); dialog.add(txtP2);
        dialog.add(createLabel("1 správně ze 2 možných:", labelFont)); dialog.add(txtP1in2);
        dialog.add(createLabel("0 správně ze 2 možných:", labelFont)); dialog.add(txtP0in2);
        dialog.add(createLabel("1 správně z 1 možné:", labelFont)); dialog.add(txtP1);
        dialog.add(createLabel("Špatná odpověď:", labelFont)); dialog.add(txtP1W);

        dialog.add(createLabel("--- Bodování (Penaltové) ---", headerFont)); dialog.add(new JLabel(""));
        dialog.add(createLabel("Zisk za správnou odpověď:", labelFont)); dialog.add(txtPenOk);
        dialog.add(createLabel("Ztráta za chybu (MINUS1):", labelFont)); dialog.add(txtPenStupid);
        dialog.add(createLabel("Ztráta za hrubku (MINUS2):", labelFont)); dialog.add(txtPenVeryStupid);

        JButton btnSave = createStyledButton("Uložit do systému", true);
        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                if (name.isEmpty() || txtFile.getText().isEmpty()) throw new IllegalArgumentException("Vyplňte název a vyberte soubor.");

                SubjectSettings settings = new SubjectSettings(
                        txtFile.getText().trim(), cbType.getSelectedItem().toString(), (Difficulty) cbDiff.getSelectedItem());

                settings.setCustomScoring(
                        Integer.parseInt(txtP2.getText()), Integer.parseInt(txtP1in2.getText()),
                        Integer.parseInt(txtP0in2.getText()), Integer.parseInt(txtP1.getText()),
                        Integer.parseInt(txtP1W.getText()));

                settings.setPenaltyScoring(
                        Integer.parseInt(txtPenOk.getText()), Integer.parseInt(txtPenStupid.getText()),
                        Integer.parseInt(txtPenVeryStupid.getText()));

                if (isEdit && !name.equals(existingName)) config.getSubjects().remove(existingName);
                config.addSubject(name, settings);

                refreshList();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Chybný formát čísel nebo dat.", "Chyba", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel(""));
        dialog.add(btnSave);
        dialog.setVisible(true);
    }

    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(new Color(50, 50, 50));
        return label;
    }

    private void startQuizMode(String subjectName) {
        SubjectSettings settings = config.getSubjects().get(subjectName);
        List<Question> allQuestions;

        if ("OPEN_BOOK".equals(settings.getQuizType())) {
            allQuestions = OpenBookQuestionParser.loadQuestions(settings.getFilePath(), subjectName, settings.getDifficulty());
        } else {
            data.CustomScoringStrategy userStrategy = new data.CustomScoringStrategy(
                    settings.getPtsTwoOk(), settings.getPtsOneOkInTwo(), settings.getPtsZeroOkInTwo(),
                    settings.getPtsOneOk(), settings.getPtsOneWrong()
            );
            allQuestions = QuestionParser.loadQuestions(
                    settings.getFilePath(), subjectName, settings.getDifficulty(), userStrategy,
                    settings.getPenaltyOk(), settings.getPenaltyStupid(), settings.getPenaltyVeryStupid()
            );
        }

        if (allQuestions == null || allQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nelze načíst soubor s otázkami. Je poškozený nebo neexistuje.", "Chyba", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Collections.shuffle(allQuestions);
        String input = JOptionPane.showInputDialog(this, "Zadejte počet otázek do testu (Max: " + allQuestions.size() + ")", "Délka testu", JOptionPane.QUESTION_MESSAGE);
        int limit = allQuestions.size();
        if (input != null && !input.trim().isEmpty()) {
            try { limit = Math.max(1, Math.min(Integer.parseInt(input.trim()), allQuestions.size())); } catch (NumberFormatException ignored) {}
        } else if (input == null) { return; }

        List<Question> testQuestions = allQuestions.stream().limit(limit).toList();
        this.setVisible(false);
        new QuizGUI(this, subjectName, settings, testQuestions).setVisible(true);
    }

    private void refreshList() {
        listModel.clear();
        for (Map.Entry<String, SubjectSettings> entry : config.getSubjects().entrySet()) {
            listModel.addElement(entry.getKey() + " - " + entry.getValue().getQuizType() + " (" + entry.getValue().getDifficulty() + ")");
        }
    }

    private void loadConfig() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(CONFIG_FILE))) { config = (AppConfig) in.readObject(); }
        catch (Exception e) { config = new AppConfig(); }
    }

    private void saveConfig() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE))) { out.writeObject(config); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}