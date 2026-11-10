package tourdelost.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Starter screen; gameplay and rendering belong to Dream's UI work. */
public final class GamePanel extends JPanel {
    public GamePanel() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(1100, 720));
        setBackground(new Color(20, 29, 42));
        setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        JLabel title = new JLabel("TOUR DE LOST", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        title.setForeground(new Color(245, 197, 92));
        add(title, BorderLayout.CENTER);

        JLabel tagline = new JLabel("Explore. Solve. Learn.", SwingConstants.CENTER);
        tagline.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
        tagline.setForeground(new Color(215, 225, 235));
        add(tagline, BorderLayout.SOUTH);
    }
}
