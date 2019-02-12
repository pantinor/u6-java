package ultima6;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class MakeFrame {

    public static void main(String[] argv) throws Exception {
        makeU6Frame();
    }

    private static void makeU6Frame() throws Exception {

        Color BASE_YELLOW = new Color(252, 244, 192);

        Color ORANGY = new Color(232, 144, 0);
        Color YELLOW_2 = new Color(252, 184, 0);
        Color YELLOW_3 = new Color(252, 228, 72);

        Color TAN_LIGHT = new Color(168, 60, 0);
        Color TAN_DARKER = new Color(104, 8, 0);

        BufferedImage output = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) output.getGraphics();
        Composite defComposite = g2d.getComposite();
        Composite clearComposite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f);

        g2d.setColor(BASE_YELLOW);
        g2d.fillRect(0, 0, 1024, 768);

        {
            g2d.setColor(ORANGY);
            g2d.fillRect(32 - 18, 64 - 18, 608 + 18 * 2, 608 + 18 * 2);

            g2d.setColor(YELLOW_2);
            g2d.fillRect(32 - 12, 64 - 12, 608 + 12 * 2, 608 + 12 * 2);

            g2d.setColor(YELLOW_3);
            g2d.fillRect(32 - 6, 64 - 6, 608 + 6 * 2, 608 + 6 * 2);

            g2d.setComposite(clearComposite);
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillRect(32, 64, 608, 608);
            g2d.setComposite(defComposite);
        }

        {
            g2d.setColor(ORANGY);
            g2d.fillRect(688 - 18, 64 - 18, 304 + 18 * 2, 672 + 18 * 2);

            g2d.setColor(YELLOW_2);
            g2d.fillRect(688 - 12, 64 - 12, 304 + 12 * 2, 672 + 12 * 2);

            g2d.setColor(YELLOW_3);
            g2d.fillRect(688 - 6, 64 - 6, 304 + 6 * 2, 672 + 6 * 2);

            g2d.setColor(BASE_YELLOW);
            g2d.fillRect(688 - 0, 64 - 0, 304 + 0 * 2, 672 + 0 * 2);

        }

        for (int i = 0; i < 6; i++) {
            g2d.setColor(YELLOW_2);
            g2d.fillRect(688 + 2, 64 + 2 + (i * (32 + 6)), 32 + 2 * 2, 32 + 2 * 2);

            g2d.setColor(YELLOW_3);
            g2d.fillRect(688 + 3, 64 + 3 + (i * (32 + 6)), 32 + 1 * 2, 32 + 1 * 2);

            g2d.setColor(BASE_YELLOW);
            g2d.fillRect(688 + 4, 64 + 4 + (i * (32 + 6)), 32 + 0 * 2, 32 + 0 * 2);
        }

        for (int i = 0; i < 6; i++) {
            g2d.setColor(YELLOW_2);
            g2d.fillRect(726 + 2, 64 + 2 + (i * (32 + 6)), 258 + 2 * 2, 32 + 2 * 2);

            g2d.setColor(YELLOW_3);
            g2d.fillRect(726 + 3, 64 + 3 + (i * (32 + 6)), 258 + 1 * 2, 32 + 1 * 2);

            g2d.setColor(BASE_YELLOW);
            g2d.fillRect(726 + 4, 64 + 4 + (i * (32 + 6)), 258 + 0 * 2, 32 + 0 * 2);
        }

        {
            g2d.setColor(YELLOW_2);
            g2d.fillRect(690, 294, 300, 440);

            g2d.setColor(YELLOW_3);
            g2d.fillRect(691, 295, 298, 438);

            g2d.setColor(BASE_YELLOW);
            g2d.fillRect(692, 296, 296, 436);

        }

        ImageIO.write(output, "PNG", new File("frame.png"));
    }

}
