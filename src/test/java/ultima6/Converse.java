package ultima6;

import com.badlogic.gdx.graphics.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import ultima6.Conversations.Conversation;

public class Converse {

    public static void main(String[] args) throws Exception {

        GZIPInputStream is = new GZIPInputStream(new FileInputStream("src\\main\\resources\\data\\conversations"));
        byte[] tmp = IOUtils.toByteArray(is);
        is.close();

        Conversations convs = new Conversations();

        ByteBuffer bba = ByteBuffer.wrap(tmp);
        while (bba.position() < bba.limit()) {
            short len = bba.getShort();
            byte[] data = new byte[len];
            bba.get(data);
            StringBuilder sb = new StringBuilder();
            byte b = 0;
            for (int i = 2; i < 20; i++) {
                b = data[i];
                if (b == (byte) 0xf1) {
                    break;
                }
                sb.append((char) b);
            }
            convs.put(data[1] & 0xff, sb.toString(), data);
        }

//        Iterator<Conversation> iter = convs.iter();
//        while (iter.hasNext()) {
//            System.out.println(iter.next());
//        }
        Conversation conv = convs.get(9);
        conv.debugOutput();

        final List<String> last = new ArrayList<>();
        Conversations.OutputStream output = new Conversations.OutputStream() {
            @Override
            public void print(String text, Color color) {
                last.clear();
                last.add(text);
                System.out.println(text);
            }

            @Override
            public void close() {
            }
        };

        Party party = new Party();
        Player player = new Player();
        player.setName("Paul");
        player.setParty(party);
        party.add(player);

        conv.process(player, "1", output);
        conv.process(player, "2", output);
        conv.process(player, "3", output);

        int i = 0;
        do {

            if (last.get(0).contains("trolls")) {
                conv.process(player, "end", output);
            } else if (last.get(0).contains("tangle")) {
                conv.process(player, "cent", output);
            } else if (last.get(0).contains("headlesses")) {
                conv.process(player, "wiza", output);
            } else if (last.get(0).contains("Hydras")) {
                conv.process(player, "nigh", output);
            } else if (last.get(0).contains("rotworms")) {
                conv.process(player, "torc", output);
            } else if (last.get(0).contains("serpents")) {
                conv.process(player, "fire", output);
            } else if (last.get(0).contains("squids")) {
                conv.process(player, "beak", output);
            } else if (last.get(0).contains("wisps")) {
                conv.process(player, "fire", output);
            } else if (last.get(0).contains("silver")) {
                conv.process(player, "tomb", output);
            } else if (last.get(0).contains("reapers")) {
                conv.process(player, "anci", output);
            } else {
                conv.process(player, "" + i, output);
            }

            i++;
        } while (i < 20);

    }

    public static void main2(String[] args) throws Exception {
        File dir = new File("D:\\Nuvie\\tools\\conv");
        File[] files = dir.listFiles();

        int size = 0;
        int count = 0;
        for (File f : files) {
            FileInputStream is = new FileInputStream(f);
            byte[] conv = IOUtils.toByteArray(is);
            size += conv.length;
            count++;
        }
        ByteBuffer bb = ByteBuffer.allocate(size + count * 2);
        for (File f : files) {
            FileInputStream is = new FileInputStream(f);
            byte[] conv = IOUtils.toByteArray(is);
            bb.putShort((short) conv.length);
            bb.put(conv);
        }
        GZIPOutputStream gz = new GZIPOutputStream(new FileOutputStream(new File("src\\main\\resources\\data\\conversations")));
        IOUtils.write(bb.array(), gz);
        gz.close();
    }

}
