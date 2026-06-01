import controller.SpreadsheetController;
import view.MainView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            SpreadsheetController ctrl = new SpreadsheetController();
            new MainView(ctrl);
        });
    }
}
