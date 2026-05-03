package org.example;

import data.Difficulty;
import data.Question;
import input_output.OpenBookQuestionParser;
import input_output.QuestionParser;

import javax.swing.*;
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

    public MainGUI() {
        loadConfig();

        setTitle("IS MUNI- Tester");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveConfig();
            }
        });
    }

    private void initUI() {
        listModel = new DefaultListModel<>();
        refreshList();
        subjectList = new JList<>(listModel);
        subjectList.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(subjectList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Dostupné předměty"));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnAdd = new JButton("Přidat");
        JButton btnEdit = new JButton("Upravit");
        JButton btnDelete = new JButton("Smazat");
        JButton btnStart = new JButton("Spustit kvíz");

        btnAdd.addActionListener(e -> showSubjectDialog(null));

        btnEdit.addActionListener(e -> {
            String selected = subjectList.getSelectedValue();
            if (selected != null) {
                String subjectName = selected.split(" - ")[0];
                showSubjectDialog(subjectName);
            } else {
                JOptionPane.showMessageDialog(this, "Vyber předmět k úpravě!", "Upozornění", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            String selected = subjectList.getSelectedValue();
            if (selected != null) {
                String subjectName = selected.split(" - ")[0];
                config.getSubjects().remove(subjectName);
                refreshList();
            }
        });

        btnStart.addActionListener(e -> {
            String selected = subjectList.getSelectedValue();
            if (selected != null) {
                String subjectName = selected.split(" - ")[0];
                startQuizMode(subjectName);
            } else {
                JOptionPane.showMessageDialog(this, "Nejprve vyber předmět!", "Upozornění", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnStart);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showSubjectDialog(String existingName) {
        boolean isEdit = existingName != null;
        SubjectSettings existingSettings = isEdit ? config.getSubjects().get(existingName) : null;

        JDialog dialog = new JDialog(this, isEdit ? "Upravit předmět" : "Přidat nový předmět", true);
        dialog.setSize(500, 650);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));
        dialog.setLocationRelativeTo(this);

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

        dialog.add(new JLabel(" Název předmětu:")); dialog.add(txtName);
        dialog.add(new JLabel(" Soubor s otázkami:")); dialog.add(txtFile);
        dialog.add(new JLabel("")); dialog.add(btnBrowse);
        dialog.add(new JLabel(" Typ otázek:")); dialog.add(cbType);
        dialog.add(new JLabel(" Obtížnost:")); dialog.add(cbDiff);

        dialog.add(new JLabel(" --- Běžné bodování ---")); dialog.add(new JLabel(""));
        dialog.add(new JLabel(" 2 správně ze 2:")); dialog.add(txtP2);
        dialog.add(new JLabel(" 1 správně ze 2:")); dialog.add(txtP1in2);
        dialog.add(new JLabel(" 0 správně ze 2:")); dialog.add(txtP0in2);
        dialog.add(new JLabel(" 1 správně z 1:")); dialog.add(txtP1);
        dialog.add(new JLabel(" 1 chybně z 1:")); dialog.add(txtP1W);

        dialog.add(new JLabel(" --- Penaltové bodování (Platí pro PV080) ---")); dialog.add(new JLabel(""));
        dialog.add(new JLabel(" Správná odpověď (+):")); dialog.add(txtPenOk);
        dialog.add(new JLabel(" Hloupá chyba (-):")); dialog.add(txtPenStupid);
        dialog.add(new JLabel(" Velmi hloupá chyba (-):")); dialog.add(txtPenVeryStupid);

        JButton btnSave = new JButton("Uložit");
        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                if (name.isEmpty() || txtFile.getText().isEmpty()) throw new IllegalArgumentException("Název ani soubor nesmí být prázdné!");

                SubjectSettings settings = new SubjectSettings(
                        txtFile.getText().trim(),
                        cbType.getSelectedItem().toString(),
                        (Difficulty) cbDiff.getSelectedItem()
                );
                settings.setCustomScoring(
                        Integer.parseInt(txtP2.getText()), Integer.parseInt(txtP1in2.getText()),
                        Integer.parseInt(txtP0in2.getText()), Integer.parseInt(txtP1.getText()),
                        Integer.parseInt(txtP1W.getText())
                );
                settings.setPenaltyScoring(
                        Integer.parseInt(txtPenOk.getText()), Integer.parseInt(txtPenStupid.getText()),
                        Integer.parseInt(txtPenVeryStupid.getText())
                );

                if (isEdit && !name.equals(existingName)) config.getSubjects().remove(existingName);
                config.addSubject(name, settings);

                refreshList();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Chyba zadání: " + ex.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel(""));
        dialog.add(btnSave);
        dialog.setVisible(true);
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
            JOptionPane.showMessageDialog(this, "Nepodařilo se načíst žádné otázky ze souboru.\nZkontroluj, zda soubor existuje a má správný formát.", "Chyba", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Collections.shuffle(allQuestions);

        String input = JOptionPane.showInputDialog(this, "Kolik otázek chceš? (Maximum: " + allQuestions.size() + ")", "Počet otázek", JOptionPane.QUESTION_MESSAGE);
        int limit = allQuestions.size();
        if (input != null && !input.trim().isEmpty()) {
            try {
                limit = Math.min(Integer.parseInt(input.trim()), allQuestions.size());
                if (limit < 1) limit = 1;
            } catch (NumberFormatException ignored) {}
        } else if (input == null) {
            return; // Zrušeno uživatelem - opravena chyba komentáře
        }

        List<Question> testQuestions = allQuestions.stream().limit(limit).toList();

        this.setVisible(false);
        new QuizGUI(this, subjectName, settings, testQuestions).setVisible(true);
    }

    private void refreshList() {
        listModel.clear();
        for (Map.Entry<String, SubjectSettings> entry : config.getSubjects().entrySet()) {
            listModel.addElement(entry.getKey() + " - " + entry.getValue().getQuizType());
        }
    }

    private void loadConfig() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(CONFIG_FILE))) {
            config = (AppConfig) in.readObject();
        } catch (Exception e) {
            config = new AppConfig();
        }
    }

    private void saveConfig() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE))) {
            out.writeObject(config);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}