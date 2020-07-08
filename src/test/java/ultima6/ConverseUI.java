/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ultima6;

import com.badlogic.gdx.graphics.Color;
import java.awt.Component;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharUtils;
import ultima6.Conversations.OutputStream;
import static ultima6.Ultima6.CLOCK;

/**
 *
 * @author Paul
 */
public class ConverseUI extends javax.swing.JFrame {

    /**
     * Creates new form ConverseUI
     */
    public ConverseUI() {

        try {
            InputStream is = new FileInputStream("src\\main\\resources\\data\\SCHEDULE");
            Ultima6.initSchedules(is);
            CLOCK.incMinute(1 * 60 * 11 + 59);
            CLOCK.setDayMonth(1, 1, 1);
        } catch (Exception e) {

        }

        initComponents();

        party = new Party();

        Map<Integer, String> players = new HashMap<>();
        players.put(0, "Avatar");
        players.put(2, "Dupre");
        players.put(3, "Shamino");
        players.put(4, "Iolo");
        players.put(62, "Jaana");
        players.put(66, "Gwenno");
        players.put(186, "Sentri");
        players.put(67, "Julia");

        for (Integer id : players.keySet()) {
            Player p = new Player(id, players.get(id));
            party.add(p);
            p.addItem(Objects.Object.GOLD_COIN, 50, 0);
            p.setStrength(12);
            p.setHp(32);
            p.setIntelligence(10);
            p.setDex(12);
        }

        avatar = party.get(0);

        GZIPInputStream is;
        ByteBuffer bba = null;
        try {
            is = new GZIPInputStream(new FileInputStream("src\\main\\resources\\data\\conversations"));

            byte[] tmp = IOUtils.toByteArray(is);
            is.close();

            bba = ByteBuffer.wrap(tmp);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Conversations convs = new Conversations();

        Conversations.DEBUG = true;

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

        DefaultListModel<Conversations.Conversation> model = new DefaultListModel<>();
        ListCellRenderer cellRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Conversations.Conversation conv = (Conversations.Conversation) value;
                return super.getListCellRendererComponent(list, conv.getName() + " - " + conv.getId(), index, isSelected, cellHasFocus);
            }
        };

        ListSelectionListener selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    JList source = (JList) e.getSource();

                    DefaultListModel<String> cmodel = (DefaultListModel) conversation.getModel();
                    cmodel.clear();

                    selected = (Conversations.Conversation) source.getSelectedValue();

                    debugTA.setText(debugOutput(selected.data()));

                    selected.init(avatar, party, output);
                }
            }

        };

        names.setModel(model);
        names.setCellRenderer(cellRenderer);
        names.addListSelectionListener(selectionListener);

        Iterator<Conversations.Conversation> iter = convs.iter();
        while (iter.hasNext()) {
            Conversations.Conversation conv = iter.next();
            model.addElement(conv);
        }

        DefaultListModel<String> cmodel = new DefaultListModel<>();
        conversation.setModel(cmodel);

        convSP.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
            }
        });

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        namesSP = new javax.swing.JScrollPane();
        names = new javax.swing.JList<>();
        convSP = new javax.swing.JScrollPane();
        conversation = new javax.swing.JList<>();
        input = new javax.swing.JTextField();
        debugSP = new javax.swing.JScrollPane();
        debugTA = new javax.swing.JTextArea();
        partySP = new javax.swing.JScrollPane();
        partyTA = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        namesSP.setViewportView(names);

        convSP.setViewportView(conversation);

        input.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputActionPerformed(evt);
            }
        });

        debugTA.setColumns(20);
        debugTA.setRows(5);
        debugSP.setViewportView(debugTA);

        partyTA.setColumns(20);
        partyTA.setRows(5);
        partySP.setViewportView(partyTA);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(debugSP)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(namesSP, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(partySP)
                            .addComponent(convSP, javax.swing.GroupLayout.DEFAULT_SIZE, 1043, Short.MAX_VALUE)
                            .addComponent(input))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(partySP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(convSP, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(namesSP, javax.swing.GroupLayout.PREFERRED_SIZE, 519, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(input, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(debugSP, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void inputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputActionPerformed
        String in = input.getText();
        input.setText("");
        selected.process(avatar, party, in, output);

        partyTA.setText("");
        for (Player p : party.getPlayers()) {
            partyTA.append(p.toString());
            partyTA.append("\n");
        }


    }//GEN-LAST:event_inputActionPerformed

    private static String debugOutput(ByteBuffer bb) {
        bb.rewind();
        StringBuffer sb = new StringBuffer();
        while (bb.position() < bb.limit()) {
            U6OP op = U6OP.get(bb);
            if (op != null) {
                
                bb.get();
                
                if (op == U6OP.IF || op == U6OP.ASK || op == U6OP.DECL || op == U6OP.KEYWORDS || op == U6OP.ASKC || op == U6OP.ENDANSWERS || op == U6OP.EVAL) {
                    sb.append("").append(bb.position()).append("\n");
                }

                if (op == U6OP.JUMP) {
                    sb.append(String.format("[%s to %d]", op, bb.getInt()));
                } else {
                    sb.append(String.format("[%s]", op));
                }

                if (op == U6OP.ONE_BYTE) {
                    sb.append(String.format("[%02x]", bb.get()));
                }

                if (op == U6OP.ENDANSWERS || op == U6OP.ENDIF || op == U6OP.ASK) {
                    sb.append("\n");
                }
            } else if (bb.get(bb.position() - 1) == U6OP.IF.code()) {
                sb.append(String.format("[%02x]", bb.get()));
            } else {
                boolean ascii = CharUtils.isAsciiPrintable((char) bb.get(bb.position()));
                sb.append(ascii ? (char) bb.get() : String.format("[%02x]", bb.get()));
            }
            
        }

        return sb.toString();

    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {

        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                ConverseUI ui = new ConverseUI();
                ui.setLocationRelativeTo(null);
                ui.setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane convSP;
    private javax.swing.JList<String> conversation;
    private javax.swing.JScrollPane debugSP;
    private javax.swing.JTextArea debugTA;
    private javax.swing.JTextField input;
    private javax.swing.JList<Conversations.Conversation> names;
    private javax.swing.JScrollPane namesSP;
    private javax.swing.JScrollPane partySP;
    private javax.swing.JTextArea partyTA;
    // End of variables declaration//GEN-END:variables

    private Party party;
    private Player avatar;
    Conversations.Conversation selected;

    private final OutputStream output = new OutputStream() {
        @Override
        public void print(String text, Color color) {
            DefaultListModel<String> cmodel = (DefaultListModel) conversation.getModel();
            cmodel.addElement(text);
        }

        @Override
        public void close() {
        }

        @Override
        public void setPortrait(int npc) {
        }
    };

}
