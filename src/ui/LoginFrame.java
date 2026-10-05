package ui;

import dao.AdminDAO;
import model.Admin;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private final AdminDAO adminDAO = new AdminDAO();

    public LoginFrame() {
        setTitle("Blood Bank Management System - Login");
        setSize(420, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND);

        JPanel header = new JPanel();
        header.setBackground(UIConstants.PRIMARY);
        header.setPreferredSize(new Dimension(0, 90));
        JLabel title = new JLabel("Blood Bank Management System");
        title.setForeground(Color.WHITE);
        title.setFont(UIConstants.FONT_TITLE);
        header.add(title);
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BACKGROUND);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(UIConstants.FONT_LABEL);
        gc.gridx = 0; gc.gridy = 0;
        form.add(userLabel, gc);

        usernameField = new JTextField(16);
        gc.gridx = 1; gc.gridy = 0;
        form.add(usernameField, gc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(UIConstants.FONT_LABEL);
        gc.gridx = 0; gc.gridy = 1;
        form.add(passLabel, gc);

        passwordField = new JPasswordField(16);
        gc.gridx = 1; gc.gridy = 1;
        form.add(passwordField, gc);

        JLabel hint = new JLabel("Demo login: admin / admin123");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        gc.gridx = 1; gc.gridy = 2;
        form.add(hint, gc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        buttonPanel.setBackground(UIConstants.BACKGROUND);

        JButton loginBtn = new JButton("Login");
        styleButton(loginBtn, UIConstants.PRIMARY);
        loginBtn.addActionListener(e -> doLogin());

        JButton clearBtn = new JButton("Clear");
        styleButton(clearBtn, Color.GRAY);
        clearBtn.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
        });

        JButton exitBtn = new JButton("Exit");
        styleButton(exitBtn, UIConstants.DANGER);
        exitBtn.addActionListener(e -> System.exit(0));

        buttonPanel.add(loginBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(exitBtn);

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        form.add(buttonPanel, gc);

        root.add(form, BorderLayout.CENTER);
        setContentPane(root);

        getRootPane().setDefaultButton(loginBtn);
    }

    private void styleButton(JButton button, Color bg) {
        button.setFont(UIConstants.FONT_BUTTON);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(90, 32));
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Admin admin = adminDAO.login(username, password);
            if (admin != null) {
                JOptionPane.showMessageDialog(this, "Login successful! Welcome " + admin.getUsername() + ".",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                new DashboardFrame(admin).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database connection error:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
