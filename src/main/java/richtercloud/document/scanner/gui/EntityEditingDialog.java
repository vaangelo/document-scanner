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
package richtercloud.document.scanner.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import static richtercloud.document.scanner.gui.EntityPanel.sortEntityClasses;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.reflection.form.builder.jpa.JPAReflectionFormBuilder;
import richtercloud.reflection.form.builder.jpa.panels.QueryPanel;
import richtercloud.reflection.form.builder.message.MessageHandler;

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- There's no sense in providing a reflection form panel for editing since both
the GUI and the code logic only makes sense if there's a OCRSelectPanelPanel
present -> only provide components to query and select to be edited entities and
open them in MainPanel.
*/
public class EntityEditingDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;
    private ListCellRenderer<Object> entityEditingClassComboBoxRenderer = new DefaultListCellRenderer() {
        private static final long serialVersionUID = 1L;
        @Override
        public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            String value0;
            if(value == null) {
                value0 = null;
            }else {
                //might be null during initialization
                Class<?> valueCast = (Class<?>) value;
                ClassInfo classInfo = valueCast.getAnnotation(ClassInfo.class);
                if(classInfo != null) {
                    value0 = classInfo.name();
                }else {
                    value0 = valueCast.getSimpleName();
                }
            }
            return super.getListCellRendererComponent(list, value0, index, isSelected, cellHasFocus);
        }
    };
    private DefaultComboBoxModel<Class<?>> entityEditingClassComboBoxModel = new DefaultComboBoxModel<>();
    private EntityManager entityManager;
    private JPAReflectionFormBuilder reflectionFormBuilder;
    private QueryPanel<Object> entityEditingQueryPanel;
    /**
     * A cache to keep custom queries when changing the query class (would be
     * overwritten at recreation. This also avoid extranous recreations.
     */
    /*
    internal implementation notes:
    - for necessity for instance creation see class comment
    */
    private Map<Class<?>, QueryPanel<Object>> entityEditingQueryPanelCache = new HashMap<>();
    private final MessageHandler messageHandler;

    /**
     * Creates new form EntityEditingDialog
     * @param parent
     * @param entityClasses
     * @param primaryClassSelection
     * @param entityManager
     * @param messageHandler
     */
    public EntityEditingDialog(java.awt.Frame parent,
            Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            EntityManager entityManager,
            MessageHandler messageHandler) {
        super(parent,
                true //modal
        );
        initComponents();
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.messageHandler = messageHandler;
        reflectionFormBuilder = new JPAReflectionFormBuilder(entityManager,
                DocumentScanner.generateApplicationWindowTitle("Field description", DocumentScanner.APP_NAME, DocumentScanner.APP_VERSION),
                messageHandler,
                new JPACachedFieldRetriever());
        if(entityManager == null) {
            throw new IllegalArgumentException("entityManager mustn't be null");
        }
        this.entityManager = entityManager;
        if(entityClasses == null) {
            throw new IllegalArgumentException("entityClasses mustn't be null");
        }
        if(entityClasses.isEmpty()) {
            throw new IllegalArgumentException("entityClass mustn't be empty");
        }
        if(!entityClasses.contains(primaryClassSelection)) {
            throw new IllegalArgumentException(String.format("primaryClassSelection '%s' has to be contained in entityClasses", primaryClassSelection));
        }

        this.entityEditingClassComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                //don't recreate QueryPanel instances, but cache them in order
                //to keep custom queries managed in QueryPanel
                if(EntityEditingDialog.this.entityEditingQueryPanel != null) {
                    Class<?> selectedEntityClass = (Class<?>) e.getItem();
                    handleEntityEditingQueryPanelUpdate(selectedEntityClass);
                }
            }
        });
        List<Class<?>> entityClassesSort = sortEntityClasses(entityClasses);
        for(Class<?> entityClass : entityClassesSort) {
            this.entityEditingClassComboBoxModel.addElement(entityClass);
        }
        this.entityEditingClassComboBox.setSelectedItem(primaryClassSelection);
    }

    private void handleEntityEditingQueryPanelUpdate(Class<?> selectedEntityClass) {
        entityEditingQueryPanel = this.entityEditingQueryPanelCache.get(selectedEntityClass);
        if(entityEditingQueryPanel == null) {
            try {
                this.entityEditingQueryPanel = new QueryPanel<>(entityManager,
                        selectedEntityClass,
                        messageHandler,
                        reflectionFormBuilder,
                        null, //initialValue
                        null //bidirectionalControlPanel (doesn't make sense)
                );
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            entityEditingQueryPanelCache.put(selectedEntityClass, entityEditingQueryPanel);
        }
        this.entityEditingQueryPanelScrollPane.setViewportView(entityEditingQueryPanel);
        this.entityEditingQueryPanelScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.DEFAULT_SCROLL_INTERVAL);
        this.entityEditingQueryPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(Constants.DEFAULT_SCROLL_INTERVAL);
        this.entityEditingQueryPanel.clearSelection();
    }

    public Object getSelectedEntity() {
        Object retValue = this.entityEditingQueryPanel.getSelectedObject();
        return retValue;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        entityEditingClassComboBox = new JComboBox<>();
        ;
        entityEditingClassComboBoxLabel = new javax.swing.JLabel();
        entityEditingClassSeparator = new javax.swing.JSeparator();
        entityEditingQueryPanelScrollPane = new javax.swing.JScrollPane();
        editButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        entityEditingClassComboBox.setModel(entityEditingClassComboBoxModel);
        entityEditingClassComboBox.setRenderer(entityEditingClassComboBoxRenderer);
        entityEditingClassComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entityEditingClassComboBoxActionPerformed(evt);
            }
        });

        entityEditingClassComboBoxLabel.setText("Query class:");

        editButton.setText("Edit");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(entityEditingClassComboBoxLabel)
                        .addGap(18, 18, 18)
                        .addComponent(entityEditingClassComboBox, 0, 606, Short.MAX_VALUE))
                    .addComponent(entityEditingClassSeparator)
                    .addComponent(entityEditingQueryPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 714, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(entityEditingClassComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(entityEditingClassComboBoxLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(entityEditingClassSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(entityEditingQueryPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void entityEditingClassComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entityEditingClassComboBoxActionPerformed
        Class<?> selectedEntityClass = (Class<?>) entityEditingClassComboBox.getSelectedItem();
        handleEntityEditingQueryPanelUpdate(selectedEntityClass);
    }//GEN-LAST:event_entityEditingClassComboBoxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.entityEditingQueryPanel.clearSelection(); //causes
            //getSelectedEntity to return null
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_editButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton editButton;
    private javax.swing.JComboBox<Class<?>> entityEditingClassComboBox;
    private javax.swing.JLabel entityEditingClassComboBoxLabel;
    private javax.swing.JSeparator entityEditingClassSeparator;
    private javax.swing.JScrollPane entityEditingQueryPanelScrollPane;
    // End of variables declaration//GEN-END:variables
}