package tourdelost;

import javax.swing.SwingUtilities;
import tourdelost.ui.GameWindow;

/** Application entry point. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}
