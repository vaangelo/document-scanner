/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.components.tag;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.Constants;
import richtercloud.document.scanner.gui.DocumentScanner;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.IssueHandler;
import richtercloud.reflection.form.builder.ResetException;

/**
 *
 * @author richter
 */
public class TagComponent extends JPanel {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(TagComponent.class);
    private final TagStorage tagStorage;
    private final DefaultListModel availableListModel = new DefaultListModel();
    private final DefaultListModel selectedListModel = new DefaultListModel();
    private final Set<TagComponentUpdateListener> updateListeners = new HashSet<>();
    private final Set<String> initialValues;
    private final IssueHandler issueHandler;

    /**
     * Creates new form TagComponent
     */
    public TagComponent(TagStorage tagStorage,
            Set<String> initialValues,
            IssueHandler issueHandler) throws ResetException {
        this.tagStorage = tagStorage;
        this.initialValues = initialValues;
        if(issueHandler == null) {
            throw new IllegalArgumentException("issueHandler mustn't be null");
        }
        this.issueHandler = issueHandler;
        initComponents();
        reset0();
    }

    public void addUpdateListener(TagComponentUpdateListener updateListener) {
        this.updateListeners.add(updateListener);
    }

    public void removeUpdateListener(TagComponentUpdateListener updateListener) {
        this.updateListeners.remove(updateListener);
    }

    public void reset() throws ResetException {
        reset0();
    }

    private void reset0() throws ResetException {
        Set<String> availableTags;
        try {
            availableTags = tagStorage.getAvailableTags();
        } catch (TagRetrievalException ex) {
            throw new ResetException(ex);
        }
        for(String tag : availableTags) {
            if(!initialValues.contains(tag)) {
                availableListModel.addElement(tag);
            }
        }
        if(initialValues != null) {
            for(String initialValue : initialValues) {
                selectedListModel.addElement(initialValue);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        selectedListPanel = new javax.swing.JPanel();
        selectedListScrollPane = new javax.swing.JScrollPane();
        selectedList = new javax.swing.JList<>();
        availablePanel = new javax.swing.JPanel();
        availableListFilterTextField = new javax.swing.JTextField();
        availableListScrollPane = new javax.swing.JScrollPane();
        availableList = new javax.swing.JList<>();
        selectButton = new javax.swing.JButton();
        deselectButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(400, 200));

        splitPane.setDividerLocation(200);

        selectedList.setModel(selectedListModel);
        selectedListScrollPane.setViewportView(selectedList);

        javax.swing.GroupLayout selectedListPanelLayout = new javax.swing.GroupLayout(selectedListPanel);
        selectedListPanel.setLayout(selectedListPanelLayout);
        selectedListPanelLayout.setHorizontalGroup(
            selectedListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectedListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectedListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                .addContainerGap())
        );
        selectedListPanelLayout.setVerticalGroup(
            selectedListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectedListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectedListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                .addContainerGap())
        );

        splitPane.setRightComponent(selectedListPanel);

        availableListFilterTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                availableListFilterTextFieldKeyPressed(evt);
            }
        });

        availableList.setModel(availableListModel);
        availableListScrollPane.setViewportView(availableList);

        selectButton.setText(">");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        deselectButton.setText("<");
        deselectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectButtonActionPerformed(evt);
            }
        });

        addButton.setText("+");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("-");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout availablePanelLayout = new javax.swing.GroupLayout(availablePanel);
        availablePanel.setLayout(availablePanelLayout);
        availablePanelLayout.setHorizontalGroup(
            availablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(availablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(availablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(availableListFilterTextField)
                    .addGroup(availablePanelLayout.createSequentialGroup()
                        .addComponent(availableListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(availablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(selectButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deselectButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        availablePanelLayout.setVerticalGroup(
            availablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(availablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(availablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(availablePanelLayout.createSequentialGroup()
                        .addComponent(selectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deselectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(availableListScrollPane))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(availableListFilterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        splitPane.setLeftComponent(availablePanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );
    }// </editor-fold>//GEN-END:initComponents

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void availableListFilterTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_availableListFilterTextFieldKeyPressed
        try {
            Set<String> tags = tagStorage.getAvailableTags();
            if(availableListFilterTextField.getText().isEmpty()) {
                this.availableListModel.clear();
                for(String tag : tags) {
                    this.availableListModel.addElement(tag);
                }
                return;
            }
            for(String tag : tags) {
                if(tag.contains(this.availableListFilterTextField.getText())) {
                    this.availableListModel.addElement(tag);
                }
            }
        } catch (TagRetrievalException ex) {
            LOGGER.error("unexpected exception during retrieval of tags",
                    ex);
            issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
        }
    }//GEN-LAST:event_availableListFilterTextFieldKeyPressed

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        if(this.availableList.getSelectedIndex() == -1) {
            return;
        }
        String tag = this.availableList.getSelectedValue();
        this.availableListModel.remove(this.availableList.getSelectedIndex());
        this.selectedListModel.addElement(tag);
        for(TagComponentUpdateListener updateListener : updateListeners) {
            updateListener.onUpdate(new TagComponentUpdateEvent(new HashSet<>(Collections.list(this.selectedListModel.elements()))));
        }
    }//GEN-LAST:event_selectButtonActionPerformed

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void deselectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectButtonActionPerformed
        if(this.selectedList.getSelectedIndex() == -1) {
            return;
        }
        String tag = this.selectedList.getSelectedValue();
        this.selectedListModel.remove(this.selectedList.getSelectedIndex());
        this.availableListModel.addElement(tag);
        for(TagComponentUpdateListener updateListener : updateListeners) {
            updateListener.onUpdate(new TagComponentUpdateEvent(new HashSet<>(Collections.list(this.selectedListModel.elements()))));
        }
    }//GEN-LAST:event_deselectButtonActionPerformed

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        TagComponentCreateDialog dialog = new TagComponentCreateDialog(SwingUtilities.getWindowAncestor(this),
                tagStorage,
                issueHandler);
        dialog.setVisible(true);
        if(dialog.getNewTag() != null) {
            this.availableListModel.addElement(dialog.getNewTag());
        }
    }//GEN-LAST:event_addButtonActionPerformed

    @SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.AvoidCatchingThrowable"})
    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        try {
            if(availableList.getSelectedIndex() == -1) {
                return;
            }
            String selectedTag = availableList.getSelectedValue();
            int answer = JOptionPane.showConfirmDialog(this,
                    String.format("Do you want to delete the tag '%s'?", selectedTag),
                    DocumentScanner.generateApplicationWindowTitle("Delete tag",
                            Constants.APP_NAME,
                            Constants.APP_VERSION),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if(answer == JOptionPane.YES_OPTION) {
                this.tagStorage.removeTag(selectedTag);
                this.availableListModel.remove(availableList.getSelectedIndex());
            }
        }catch(Throwable ex) {
            LOGGER.error("unexpected exception during deletion of tag",
                    ex);
            issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
        }
    }//GEN-LAST:event_deleteButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JList<String> availableList;
    private javax.swing.JTextField availableListFilterTextField;
    private javax.swing.JScrollPane availableListScrollPane;
    private javax.swing.JPanel availablePanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton deselectButton;
    private javax.swing.JButton selectButton;
    private javax.swing.JList<String> selectedList;
    private javax.swing.JPanel selectedListPanel;
    private javax.swing.JScrollPane selectedListScrollPane;
    private javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables
}
