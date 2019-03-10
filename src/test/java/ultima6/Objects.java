/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ultima6;

import com.google.common.io.LittleEndianDataInputStream;
import java.io.FileInputStream;

public class Objects {

    public static void main(String[] args) throws Exception {
        
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
        
        for (Constants.Objects obj : Constants.Objects.values()) {
            int weight = weights[obj.getId()] & 0xff;
            System.out.printf("%s [%d]\n", obj, weight);
        }
        
        
        
    }

}