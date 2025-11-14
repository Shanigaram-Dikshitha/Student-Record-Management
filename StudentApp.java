package src;// src/StudentApp.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;


public class StudentApp extends JFrame {
    private final StudentService service;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public StudentApp() {
        super("Student Record Manager");
        service = new StudentService("students.csv");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 450);
        setLocationRelativeTo(null);

        String[] columns = {"ID", "Name", "Age", "Course"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        refreshTable();

        JScrollPane scrollPane = new JScrollPane(table);

        // Buttons
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        
        JButton refreshBtn = new JButton("Refresh");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(addBtn);
        topPanel.add(editBtn);
        topPanel.add(delBtn);
        
        topPanel.add(refreshBtn);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Button actions
        addBtn.addActionListener(e -> showAddDialog());
        editBtn.addActionListener(e -> showEditDialog());
        delBtn.addActionListener(e -> deleteSelected());
        
        refreshBtn.addActionListener(e -> refreshTable());

        // Double click to edit
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    showEditDialog();
                }
            }
        });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Student s : service.getAll()) {
            tableModel.addRow(new Object[]{s.getId(), s.getName(), s.getAge(), s.getCourse()});
        }
    }

    private void showAddDialog() {
        JTextField idF = new JTextField();
        JTextField nameF = new JTextField();
        JTextField ageF = new JTextField();
        JTextField courseF = new JTextField();

        Object[] msg = {
            "ID:", idF,
            "Name:", nameF,
            "Age:", ageF,
            "Course:", courseF
        };

        int option = JOptionPane.showConfirmDialog(this, msg, "Add Student", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idF.getText().trim());
                String name = nameF.getText().trim();
                int age = Integer.parseInt(ageF.getText().trim());
                String course = courseF.getText().trim();

                if (name.isEmpty() || course.isEmpty()) {
                    showError("Name and Course cannot be empty.");
                    return;
                }
                if (service.existsId(id)) {
                    showError("ID already exists. Choose a unique ID.");
                    return;
                }
                service.add(new Student(id, name, age, course));
                refreshTable();
            } catch (NumberFormatException ex) {
                showError("Please enter valid numeric ID and Age.");
            }
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showError("Select a student to edit.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String currentName = (String) tableModel.getValueAt(row, 1);
        int currentAge = (int) tableModel.getValueAt(row, 2);
        String currentCourse = (String) tableModel.getValueAt(row, 3);

        JTextField nameF = new JTextField(currentName);
        JTextField ageF = new JTextField(String.valueOf(currentAge));
        JTextField courseF = new JTextField(currentCourse);

        Object[] msg = {
            "ID: " + id,
            "Name:", nameF,
            "Age:", ageF,
            "Course:", courseF
        };

        int option = JOptionPane.showConfirmDialog(this, msg, "Edit Student", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameF.getText().trim();
                int age = Integer.parseInt(ageF.getText().trim());
                String course = courseF.getText().trim();

                if (name.isEmpty() || course.isEmpty()) {
                    showError("Name and Course cannot be empty.");
                    return;
                }
                service.update(new Student(id, name, age, course));
                refreshTable();
            } catch (NumberFormatException ex) {
                showError("Please enter a valid numeric Age.");
            }
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showError("Select a student to delete.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student ID " + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.delete(id);
            refreshTable();
        }
    }

     

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentApp app = new StudentApp();
            app.setVisible(true);
        });
    }
}
