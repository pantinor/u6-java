package ultima6;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import ultima6.Conversations.Conversation;

public class Converse {

    public static void main(String[] args) throws Exception {

        GZIPInputStream is = new GZIPInputStream(new FileInputStream("src\\main\\resources\\data\\conversations"));
        byte[] conv = IOUtils.toByteArray(is);
        is.close();

        Conversations convs = new Conversations();

        ByteBuffer bba = ByteBuffer.wrap(conv);
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

        Iterator<Conversation> iter = convs.iter();
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
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
