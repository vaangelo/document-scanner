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
package richtercloud.document.scanner.valuedetectionservice;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import richtercloud.document.scanner.valuedetectionservice.annotations.ConfPanel;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;

/**
 * Allows scanning a JAR for implementations of
 * {@link ValueDetectionService} which can be selected and configured
 * using an instance of {@link ValueDetectionServiceConfPanel} referenced
 * in the {@link ConfPanel} annotation of the service implementation created
 * through reflection.
 *
 * Currently service implementations can only be added one-by-one which makes
 * sense since they have to be configured and adding a feature to add multiple
 * services at once is difficult to implement.
 *
 * @author richter
 */
public class ValueDetectionServiceAddDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final MessageHandler messageHandler;
    private final DefaultComboBoxModel<Pair<String, ValueDetectionServiceConfPanel>> servicesComboBoxModel = new DefaultComboBoxModel<>();
    private final ListCellRenderer servicesComboBoxCellRenderer = new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value == null) {
                //can occur during initialization
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
            assert value instanceof Pair;
            Pair<String, ValueDetectionServiceConfPanel> valueCast = (Pair<String, ValueDetectionServiceConfPanel>) value;
            String serviceName = valueCast.getKey();
            return super.getListCellRendererComponent(list,
                    serviceName,
                    index,
                    isSelected,
                    cellHasFocus);
        }
    };
    /**
     * A pair consisting of the JAR path used for retrieval of implementations
     * (have to be stored outside {@link ValueDetectionService} implementation
     * specific configuration classes because they cannot be loaded without
     * knowing the path of the JAR and loading it first) and the created
     * configuration which can be accessed after the dialog has been
     * closed. {@code null} indicates that the dialog has been canceled.
     */
    private Pair<String, ValueDetectionServiceConf> createdConf = null;
    /**
     * Stores the path of the last successful loading of
     * {@link ValueDetectionService} implementations because the user might
     * change it after loading successfully - and in this case it's fine to
     * assume that he_she wanted to point to the last successful load.
     */
    private String lastSuccessfulPath;
    private ValueDetectionServiceConfPanel serviceConfPanel;

    /**
     * Creates new form AutoOCRValueDetectionServiceAddDialog
     */
    public ValueDetectionServiceAddDialog(Window parent,
            MessageHandler messageHandler) {
        super(parent,
                ModalityType.APPLICATION_MODAL);
        this.messageHandler = messageHandler;
        initComponents();
        servicesComboBox.setRenderer(servicesComboBoxCellRenderer);
        servicesComboBox.addItemListener((ItemEvent e) -> {
            confPanelPanel.removeAll();
            Pair<String, ValueDetectionServiceConfPanel> confPanelPair = (Pair<String, ValueDetectionServiceConfPanel>) servicesComboBoxModel.getSelectedItem();
            ValueDetectionServiceConfPanel confPanel = confPanelPair.getValue();
            confPanelPanel.add(confPanel);
            ValueDetectionServiceAddDialog.this.pack();
            this.serviceConfPanel = confPanelPair.getValue();
        });
    }

    public Pair<String, ValueDetectionServiceConf> getCreatedConf() {
        return createdConf;
    }

    public static Set<Class<? extends ValueDetectionService>> loadValueDetectionServices(File valueDetectionServiceJARPath,
            ClassLoader classLoader) throws IOException {
        //Classes might be already be available on class path, so they
        //should be sorted out
        Set<Class<? extends ValueDetectionService>> alreadyLoadedClasses;
        ConfigurationBuilder configuration = new ConfigurationBuilder()
                .addClassLoader(classLoader)
                .useParallelExecutor(Runtime.getRuntime().availableProcessors())
                    //this idiom is necessary for the detection with
                    //Reflections.getSubTypesOf to work
                .filterInputsBy(FilterBuilder.parse("+.*"));
        configuration.setUrls(ClasspathHelper.forClassLoader()
            //works here, but not for files in JAR
        );
        Reflections reflections = new Reflections(configuration);
        alreadyLoadedClasses = reflections.getSubTypesOf(ValueDetectionService.class);

        //- unclear how to configure Reflections for packages in JAR
        //programmatically (without package string) -> get all package names
        //first
        //- adding JAR entries which are not root directories causes
        //Reflections to not work
        JarFile jarFile = new JarFile(valueDetectionServiceJARPath);
        Enumeration allEntries = jarFile.entries();
        Set<String> packages = new HashSet<>();
        while (allEntries.hasMoreElements()) {
            JarEntry entry = (JarEntry) allEntries.nextElement();
            if(entry.isDirectory()) {
                String package0 = entry.getName();
                //only add root packages because Reflections is confused
                //otherwise (still unclear how to use) (have exactly one,
                //trailing slash)
                if(StringUtils.countMatches(package0, "/") == 1) {
                    assert package0.endsWith("/");
                    package0 = StringUtils.removeEnd(package0, "/");
                    package0 = package0.replaceAll("/", ".");
                    packages.add(package0);
                }
            }
        }
        configuration = new ConfigurationBuilder()
                .addClassLoader(classLoader)
                .useParallelExecutor(Runtime.getRuntime().availableProcessors())
                    //this idiom is necessary for the detection with
                    //Reflections.getSubTypesOf to work
                .filterInputsBy(FilterBuilder.parse("+.*"))
                .addUrls(ClasspathHelper.forClassLoader());
        for(String package0 : packages) {
            configuration.setUrls(ClasspathHelper.forPackage(package0, classLoader));
        }
        reflections = new Reflections(configuration);
        Set<Class<? extends ValueDetectionService>> jarServices = reflections.getSubTypesOf(ValueDetectionService.class);
        Set<Class<? extends ValueDetectionService>> services = new HashSet<>();
        for(Class<? extends ValueDetectionService> jarService : jarServices) {
            if(!alreadyLoadedClasses.contains(jarService)) {
                services.add(jarService);
            }
        }
        return services;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameTextField = new javax.swing.JTextField();
        nameTextFieldLabel = new javax.swing.JLabel();
        pathTextField = new javax.swing.JTextField();
        pathBrowseButton = new javax.swing.JButton();
        pathTextFieldLabel = new javax.swing.JLabel();
        loadButton = new javax.swing.JButton();
        separator = new javax.swing.JSeparator();
        confPanelPanel = new javax.swing.JPanel();
        saveButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        servicesComboBox = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nameTextFieldLabel.setText("Service name:");

        pathBrowseButton.setText("Browse");
        pathBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pathBrowseButtonActionPerformed(evt);
            }
        });

        pathTextFieldLabel.setText("JAR path:");

        loadButton.setText("Load");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        confPanelPanel.setLayout(new java.awt.BorderLayout());

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        servicesComboBox.setModel(servicesComboBoxModel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameTextFieldLabel)
                            .addComponent(pathTextFieldLabel))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(nameTextField)
                                .addContainerGap())
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(pathTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(pathBrowseButton)
                                .addGap(10, 10, 10))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(confPanelPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(separator, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(loadButton, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(cancelButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(saveButton))))
                            .addComponent(servicesComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameTextFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pathBrowseButton)
                    .addComponent(pathTextFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(servicesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(confPanelPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pathBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pathBrowseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "JAR archives", "jar");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final File selectedFile = chooser.getSelectedFile();
        this.pathTextField.setText(selectedFile.getPath());
    }//GEN-LAST:event_pathBrowseButtonActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        File selectedFile = new File(this.pathTextField.getText());
        if(!selectedFile.exists()) {
            messageHandler.handle(new Message(String.format("Selected file "
                    + "'%s' doesn't exist",
                            selectedFile.getName()
                        //don't use absolute path here beacuse the message that
                        //an existing directory doesn't exist is confusing
                    ),
                    JOptionPane.ERROR_MESSAGE,
                    "File doesn't exist"));
            return;
        }
        String selectedFilePath = selectedFile.getAbsolutePath();
        servicesComboBoxModel.removeAllElements();
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[] { selectedFile.toURI().toURL()},
                    Thread.currentThread().getContextClassLoader()
                        //System.class.getClassLoader doesn't work
            );
            Set<Class<? extends ValueDetectionService>> services = loadValueDetectionServices(selectedFile,
                    classLoader);
            if(services.isEmpty()) {
                messageHandler.handle(new Message(String.format("Loaded JAR doesn't contain "
                        + "classes implementing %s",
                                ValueDetectionService.class),
                        JOptionPane.ERROR_MESSAGE,
                        "No implementations in JAR"));
                return;
            }
            for(Class<? extends ValueDetectionService> service : services) {
                ConfPanel serviceConfPanelAnnotation = service.getAnnotation(ConfPanel.class);
                if(serviceConfPanelAnnotation == null) {
                    messageHandler.handle(new Message(String.format("implementation of %s doesn't have a class "
                            + "annotation %s, implementation will be ignored",
                            service,
                            ConfPanel.class),
                            JOptionPane.WARNING_MESSAGE,
                            "Implementation without annotation"));
                    continue;
                }
                Class<? extends ValueDetectionServiceConfPanel> serviceConfPanelClass = serviceConfPanelAnnotation.confPanelClass();
                ValueDetectionServiceConfPanel serviceConfPanel;
                try {
                    Constructor<? extends ValueDetectionServiceConfPanel> serviceConfPanelClassConstructor;
                    try {
                        serviceConfPanelClassConstructor = serviceConfPanelClass.getDeclaredConstructor();
                    } catch (NoSuchMethodException | SecurityException ex) {
                        messageHandler.handle(new Message(String.format(
                                "configuration "
                                + "panel class %s of service implementation %s "
                                + "doesn't have an accessible constructor "
                                + "which allows to pass an instance of %s as "
                                + "only argument",
                                        serviceConfPanelClass,
                                        service,
                                        ValueDetectionServiceConf.class),
                                JOptionPane.ERROR_MESSAGE,
                                "Missing constructor"
                                ));
                        continue;
                    }
                    serviceConfPanel = serviceConfPanelClassConstructor.newInstance();
                    serviceConfPanel.init(messageHandler);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    messageHandler.handle(new Message(String.format(
                            "initialization of %s for service implementation "
                            + "%s failed due to the following exception: %s",
                                    serviceConfPanelClass,
                                    service,
                                    ExceptionUtils.getRootCauseMessage(ex)),
                            JOptionPane.ERROR_MESSAGE,
                            "Inialization of conf panel failed"));
                    continue;
                }
                Pair<String, ValueDetectionServiceConfPanel> newElement = new ImmutablePair<String, ValueDetectionServiceConfPanel>(service.getSimpleName(),
                        serviceConfPanel);
                servicesComboBoxModel.addElement(newElement);
                servicesComboBox.setSelectedItem(newElement);
                this.lastSuccessfulPath = selectedFilePath;
            }
        } catch (MalformedURLException ex) {
            messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
        } catch (IOException ex) {
            Logger.getLogger(ValueDetectionServiceAddDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_loadButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        ValueDetectionServiceConf createdConf = this.serviceConfPanel.getServiceConf();
        try {
            createdConf.validate();
        } catch (ValueDetectionServiceConfValidationException ex) {
            messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
            return;
        }
        this.createdConf = new ImmutablePair<>(this.lastSuccessfulPath,
                createdConf);
        this.setVisible(false);
    }//GEN-LAST:event_saveButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel confPanelPanel;
    private javax.swing.JButton loadButton;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel nameTextFieldLabel;
    private javax.swing.JButton pathBrowseButton;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JLabel pathTextFieldLabel;
    private javax.swing.JButton saveButton;
    private javax.swing.JSeparator separator;
    private javax.swing.JComboBox<Pair<String, ValueDetectionServiceConfPanel>> servicesComboBox;
    // End of variables declaration//GEN-END:variables
}