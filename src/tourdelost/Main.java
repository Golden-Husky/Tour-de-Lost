package tourdelost;

import java.awt.Font;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import tourdelost.ui.GameWindow;

/** จุดเริ่มโปรแกรมและการตั้งค่าหน้าตาของ Swing */
public final class Main {
    private static final String THAI_SAMPLE = "ภาษาไทย";

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configureFonts();
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }

    private static void configureFonts() {
        Font messageFont = findThaiFont(Font.PLAIN, 16);
        Font buttonFont = findThaiFont(Font.BOLD, 15);
        UIManager.put("OptionPane.messageFont", messageFont);
        UIManager.put("OptionPane.buttonFont", buttonFont);
        UIManager.put("ToolTip.font", messageFont);
    }

    private static Font findThaiFont(int style, int size) {
        String[] candidates = {"Tahoma", "Leelawadee UI", "Noto Sans Thai", Font.SANS_SERIF};
        for (String candidate : candidates) {
            Font font = new Font(candidate, style, size);
            if (font.canDisplayUpTo(THAI_SAMPLE) == -1) {
                return font;
            }
        }
        throw new IllegalStateException("No installed font can display Thai text.");
    }
}
