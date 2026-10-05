import ui.LoginFrame;

import javax.swing.*;

/**
 * Entry point for the Blood Bank Management System.
 * Launches the login screen on the Swing Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to default look and feel if system L&F is unavailable
            }
            new LoginFrame().setVisible(true);
        });
    }
}
