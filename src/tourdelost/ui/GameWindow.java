package tourdelost.ui;

import java.awt.Dimension;
import javax.swing.JFrame;

/** Main window. Owner: Dream. */
public final class GameWindow extends JFrame {
    public GameWindow() {
        super("Tour de Lost");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new GamePanel());
        setMinimumSize(new Dimension(800, 560));
        pack();
        setLocationRelativeTo(null);
    }
}
