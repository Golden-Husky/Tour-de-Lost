package tourdelost.ui;

import java.awt.Dimension;
import java.io.Serial;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/** หน้าต่างหลักและจุดเชื่อมคำสั่งจากหน้าเริ่มเกม */
public final class GameWindow extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    public GameWindow() {
        super("Tour de Lost");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new GamePanel(this::handleMenuAction));
        setMinimumSize(new Dimension(960, 540));
        pack();
        setLocationRelativeTo(null);
    }

    private void handleMenuAction(MenuAction action) {
        switch (action) {
            case START_GAME -> showPendingConnection(
                "ระบบเริ่มด่านกำลังรอเชื่อม GameSession จากทีมเกม"
            );
            case SELECT_LEVEL -> showPendingConnection(
                "หน้ารายการด่านจะเปิดเมื่อข้อมูล Level พร้อมใช้งาน"
            );
            case HOW_TO_PLAY -> JOptionPane.showMessageDialog(
                this,
                "คลิกโหนดที่เชื่อมกับตำแหน่งปัจจุบันโดยตรง\n"
                    + "วางแผนเส้นทางและใช้ทรัพยากรให้พอดี\n"
                    + "ผ่านภารกิจเพื่อเปิดบทเรียนอัลกอริทึม",
                "วิธีเล่น",
                JOptionPane.INFORMATION_MESSAGE
            );
            case SETTINGS -> showPendingConnection(
                "เมนูเสียงและการตั้งค่าจะพร้อมเมื่อมีระบบเสียงของเกม"
            );
        }
    }

    private void showPendingConnection(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Tour de Lost",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
