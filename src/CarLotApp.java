import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the CarLot desktop application.
 */
public class CarLotApp {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default L&F
        }

        SwingUtilities.invokeLater(() -> {
            CarLotFrame frame = new CarLotFrame();
            frame.setVisible(true);
        });
    }
}
