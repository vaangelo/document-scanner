/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.gui.conf;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 *
 * @author richter
 */
public class FieldOrderPanel extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    private final DefaultListModel<Field> fieldListModel = new DefaultListModel<>();

    /**
     * Creates new form FieldOrderPanel
     */
    public FieldOrderPanel(List<Field> fields) {
        initComponents();
        for(Field field : fields) {
            fieldListModel.addElement(field);
        }
        this.fieldList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                assert value instanceof Field;
                Field valueCast = (Field) value;
                String fieldName;
                FieldInfo fieldInfo = valueCast.getAnnotation(FieldInfo.class);
                if(fieldInfo != null) {
                    fieldName = fieldInfo.name();
                }else {
                    fieldName = valueCast.getName();
                }
                return super.getListCellRendererComponent(list,
                        fieldName,
                        index,
                        isSelected,
                        cellHasFocus);
            }
        });
        this.fieldList.addListSelectionListener((ListSelectionEvent e) -> {
            int fieldListSelectedIndex = fieldList.getSelectedIndex();
            upButton.setEnabled(fieldListSelectedIndex != -1
                    && fieldListSelectedIndex > 0);
            downButton.setEnabled(fieldListSelectedIndex != -1
                    && fieldListSelectedIndex < fieldListModel.getSize()-1);
        });
        upButton.addActionListener(event -> {
            int fieldListSelectedIndex = fieldList.getSelectedIndex();
            assert fieldListSelectedIndex != -1 && (fieldListSelectedIndex > 0 || fieldListSelectedIndex < fieldListModel.getSize()-1);
                //avoided through disabling
            Field removedField = fieldListModel.remove(fieldListSelectedIndex);
            fields.remove(fieldListSelectedIndex);
            int newIndex = fieldListSelectedIndex-1;
            fieldListModel.add(newIndex, removedField);
            fields.add(newIndex, removedField);
            fieldList.setSelectedIndex(newIndex);
        });
        downButton.addActionListener(event -> {
            int fieldListSelectedIndex = fieldList.getSelectedIndex();
            assert fieldListSelectedIndex != -1 && (fieldListSelectedIndex > 0 || fieldListSelectedIndex < fieldListModel.getSize()-1);
                //avoided through disabling
            Field removedField = fieldListModel.remove(fieldListSelectedIndex);
            fields.remove(fieldListSelectedIndex);
            int newIndex = fieldListSelectedIndex+1;
            fieldListModel.add(newIndex, removedField);
            fields.add(newIndex, removedField);
            fieldList.setSelectedIndex(newIndex);
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

        fieldListScrollPane = new javax.swing.JScrollPane();
        fieldList = new javax.swing.JList<>();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();

        fieldList.setModel(fieldListModel);
        fieldListScrollPane.setViewportView(fieldList);

        upButton.setText("Up");
        upButton.setEnabled(false);

        downButton.setText("Down");
        downButton.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fieldListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(downButton)
                    .addComponent(upButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fieldListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(upButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton downButton;
    private javax.swing.JList<Field> fieldList;
    private javax.swing.JScrollPane fieldListScrollPane;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables
}
