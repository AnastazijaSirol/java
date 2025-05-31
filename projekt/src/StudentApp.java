import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

// Glavna aplikacija za upravljanje  zadacima
public class StudentApp {
    private static final String FILE_NAME = "tasks.txt"; // Datoteka u koju se spremaju zadaci

    // UI komponente
    private JFrame frame;
    private DefaultListModel<Task> taskModel; // Model za prikaz zadataka
    private JList<Task> taskList; // Vizualni prikaz liste zadataka

    // Polja za unos podataka
    private JTextField titleField;
    private JTextField subjectField;
    private JTextField dueDateField;
    private JComboBox<String> statusCombo; // Padajući izbornik za status zadatka
    private JComboBox<String> filterCombo; // Padajući izbornik za filtriranje

    // Gumbi
    private JButton editButton;
    private boolean isEditing = false;
    private int editingIndex = -1;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentApp::new);
    }

    public StudentApp() {
        frame = new JFrame("Student Task Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 450);
        frame.setLayout(new BorderLayout());

        // Inicijalizacija modela i liste
        taskModel = new DefaultListModel<>();
        taskList = new JList<>(taskModel);
        taskList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        frame.add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Panel za unos podataka
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Polja za unos
        titleField = new JTextField();
        subjectField = new JTextField();
        dueDateField = new JTextField("dd.MM.yyyy"); // Hint format datuma
        statusCombo = new JComboBox<>(new String[]{"Novi", "U tijeku", "Završen"});
        filterCombo = new JComboBox<>(new String[]{"Svi", "Novi", "U tijeku", "Završen"});

        // Dodavanje labela i polja na panel
        inputPanel.add(new JLabel("Zadatak:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Kolegij:"));
        inputPanel.add(subjectField);
        inputPanel.add(new JLabel("Rok:"));
        inputPanel.add(dueDateField);
        inputPanel.add(new JLabel("Status:"));
        inputPanel.add(statusCombo);

        // Gumbi za akcije
        JButton addButton = new JButton("Dodaj");
        JButton deleteButton = new JButton("Obriši");
        editButton = new JButton("Uredi");
        JButton sortButton = new JButton("Sortiraj po roku");
        JButton filterButton = new JButton("Filtriraj");

        inputPanel.add(addButton);
        inputPanel.add(deleteButton);

        // Dodavanje gornjeg panela
        frame.add(inputPanel, BorderLayout.NORTH);

        // Donji panel s dodatnim akcijama
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(sortButton);
        bottomPanel.add(editButton);
        bottomPanel.add(filterCombo);
        bottomPanel.add(filterButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Dodjeljivanje akcija gumbima
        addButton.addActionListener(e -> addTask());
        deleteButton.addActionListener(e -> deleteTask());
        sortButton.addActionListener(e -> sortTasks());
        filterButton.addActionListener(e -> filterTasks());

        // Akcija za uređivanje/spremanje zadatka
        editButton.addActionListener(e -> {
            if (!isEditing) {
                int index = taskList.getSelectedIndex();
                if (index == -1) {
                    JOptionPane.showMessageDialog(frame, "Odaberi zadatak za uređivanje.");
                    return;
                }

                // Postavljanje podataka u formu
                Task selected = taskModel.get(index);
                titleField.setText(selected.getTitle());
                subjectField.setText(selected.getSubject());
                dueDateField.setText(selected.getDueDate());
                statusCombo.setSelectedItem(selected.getStatus());

                isEditing = true;
                editingIndex = index;
                editButton.setText("Spremi");
            } else {
                // Spremanje izmjena
                Task task = taskModel.get(editingIndex);
                task.setTitle(titleField.getText().trim());
                task.setSubject(subjectField.getText().trim());
                task.setDueDate(dueDateField.getText().trim());
                task.setStatus((String) statusCombo.getSelectedItem());

                taskModel.set(editingIndex, task);
                saveTasksToFile();
                clearFormFields();
            }
        });

        // Učitavanje zadataka iz datoteke
        loadTasksFromFile();

        // Prikaz prozora
        frame.setVisible(true);
    }

    // Dodavanje novog zadatka
    private void addTask() {
        String title = titleField.getText().trim();
        String subject = subjectField.getText().trim();
        String dueDate = dueDateField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();

        // Provjera ispunjenosti polja
        if (title.isEmpty() || subject.isEmpty() || dueDate.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Sva polja su obavezna.");
            return;
        }

        // Kreiranje i dodavanje zadatka
        Task task = new Task(title, subject, dueDate, status);
        taskModel.addElement(task);
        saveTasksToFile();
        clearFormFields();
    }

    // Brisanje odabranog zadatka
    private void deleteTask() {
        int selected = taskList.getSelectedIndex();
        if (selected != -1) {
            taskModel.remove(selected);
            saveTasksToFile();
        }
    }

    // Sortiranje zadataka po datumu
    private void sortTasks() {
        List<Task> sorted = Collections.list(taskModel.elements());
        sorted.sort(Comparator.comparing(Task::getDueDateAsDate));
        taskModel.clear();
        for (Task t : sorted) {
            taskModel.addElement(t);
        }
    }

    // Filtriranje zadataka po statusu
    private void filterTasks() {
        String selectedStatus = (String) filterCombo.getSelectedItem();
        taskModel.clear();
        List<Task> allTasks = loadTasks();

        for (Task t : allTasks) {
            if (selectedStatus.equals("Svi") || t.getStatus().equals(selectedStatus)) {
                taskModel.addElement(t);
            }
        }
    }

    // Spremanje svih zadataka u datoteku
    private void saveTasksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < taskModel.size(); i++) {
                writer.write(taskModel.get(i).toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Greška pri spremanju zadataka.");
        }
    }

    // Učitavanje zadataka iz datoteke u model
    private void loadTasksFromFile() {
        List<Task> loadedTasks = loadTasks();
        for (Task task : loadedTasks) {
            taskModel.addElement(task);
        }
    }

    // Učitavanje zadataka iz datoteke kao lista
    private List<Task> loadTasks() {
        List<Task> taskList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Task task = Task.fromFileString(line);
                if (task != null) {
                    taskList.add(task);
                }
            }
        } catch (IOException ignored) {}
        return taskList;
    }

    // Čišćenje forme nakon dodavanja/uređivanja
    private void clearFormFields() {
        titleField.setText("");
        subjectField.setText("");
        dueDateField.setText("dd.MM.yyyy");
        isEditing = false;
        editingIndex = -1;
        editButton.setText("Uredi");
    }
}
