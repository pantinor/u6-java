/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ultima6;

import com.google.common.io.LittleEndianDataInputStream;
import java.io.FileInputStream;

public class ObjectsTest {

    public static void main(String[] args) throws Exception {
        
        int x = 611;
        int b0 = x & 0xff000000;
        int b1 = x & 0x00ff0000;
        int b2 = x & 0x0000ff00;
        int b3 = x & 0x000000ff;

        FileInputStream is = new FileInputStream("src/main/resources/data/TILEFLAG");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

        byte[] f1 = new byte[2048];
        byte[] f2 = new byte[2048];
        byte[] weights = new byte[1024];
        byte[] none = new byte[1024];
        byte[] f3 = new byte[2048];

        dis.read(f1);
        dis.read(f2);
        dis.read(weights);
        dis.read(none);
        dis.read(f3);

        for (Objects.Object obj : Objects.Object.values()) {
            int weight = weights[obj.getId()] & 0xff;
            //System.out.printf("%s [%d]\n", obj, weight);
        }

        Clock clock = new Clock();
        clock.setDayMonth(1, 1, 1);
        for (int i = 0; i < 24 * 8; i++) {
            clock.incMinute(60);
            System.out.println(clock.getTimeString());
        }

    }

}
