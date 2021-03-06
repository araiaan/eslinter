package gui;

import static burp.BurpExtender.extensionConfig;
import static burp.BurpExtender.lintPool;
import static burp.BurpExtender.log;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractButton;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.io.FileUtils;
import burp.Config;
import linttable.LintTable;
import utils.FileChooser;
import utils.StringUtils;

/**
 * BurpTab
 */
public class BurpTab {

    public JSplitPane panel;
    public LintTable lintTable;

    public BurpTab() {
        initComponents();
    }

    private void initComponents() {

        // Panel that is returned.
        panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        topPanel = new JPanel();
        // configPanel.setBorder(BorderFactory.createBevelBorder(1));
        loadConfigButton = new JButton("Load Config");
        loadConfigButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loadConfigAction();
            }
        });

        saveConfigButton = new JButton("Save Config");
        saveConfigButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveConfigAction();
            }
        });

        processToggleButton = new JToggleButton("Process");
        processToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                processToggleAction(evt);
            }
        });

        searchTextField = new JTextField();
        // Every time the textfield changes, update the table.
        // https://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableFilterDemoProject/src/components/TableFilterDemo.java
        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                searchAction(searchTextField.getText());
            }

            public void insertUpdate(DocumentEvent e) {
                searchAction(searchTextField.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                searchAction(searchTextField.getText());
            }
        });

        // searchButton = new JButton("Search");
        // searchButton.addActionListener(new java.awt.event.ActionListener() {
        // public void actionPerformed(java.awt.event.ActionEvent evt) {
        // // Get the text from searchTextField.
        // String query = searchTextField.getText().toLowerCase();
        // log.debug("Searching for %s.", query);
        // searchAction(query);
        // }
        // });


        resetButton = new JButton("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Get the text from searchTextField.
                searchTextField.setText("");
            }
        });

        topSeparator = new JSeparator(SwingConstants.VERTICAL);
        topSeparator.setMaximumSize(new Dimension(2, 30));

        GroupLayout topPanelLayout = new GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);

        /**
         * Start GUI generated code. Do not modify.
         */
        topPanelLayout.setHorizontalGroup(topPanelLayout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(topPanelLayout.createSequentialGroup().addContainerGap()
                        .addGroup(topPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(topPanelLayout.createSequentialGroup()
                                        .addComponent(processToggleButton,
                                                GroupLayout.PREFERRED_SIZE, 200,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(topSeparator)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(loadConfigButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(saveConfigButton))
                                .addGroup(topPanelLayout.createSequentialGroup()
                                        .addComponent(searchTextField, GroupLayout.PREFERRED_SIZE,
                                                400, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        // .addComponent(searchButton)
                                        // .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(resetButton)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        topPanelLayout.setVerticalGroup(topPanelLayout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(topPanelLayout.createSequentialGroup().addContainerGap().addGroup(
                        topPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                                topPanelLayout.createSequentialGroup().addGroup(topPanelLayout
                                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(topPanelLayout
                                                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(loadConfigButton)
                                                .addComponent(saveConfigButton))
                                        .addComponent(topSeparator)))
                                .addGroup(topPanelLayout.createSequentialGroup()
                                        .addComponent(processToggleButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(topPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(searchTextField, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                // .addComponent(searchButton)
                                .addComponent(resetButton))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        // Link size of buttons.
        topPanelLayout.linkSize(SwingConstants.HORIZONTAL, loadConfigButton, saveConfigButton);
        // topPanelLayout.linkSize(SwingConstants.HORIZONTAL, searchButton, resetButton);

        /**
         * End GUI generated code.
         */

        lintTable = new LintTable();
        tableScrollPane = new JScrollPane(lintTable);

        panel.setLeftComponent(topPanel);
        panel.setRightComponent(tableScrollPane);
    }

    private void loadConfigAction() {
        File sf = FileChooser.openFile(panel, FileChooser.getLastWorkingDirectory(),
                "Save config file", "json");

        if (sf != null) {
            // Set the last working directory.
            FileChooser.setLastWorkingDirectory(sf.getParent());

            String configFromFile = "";
            // Read the file and load it into extensionConfig.
            try {
                configFromFile = FileUtils.readFileToString(sf, StringUtils.UTF8);
                extensionConfig = Config.loadConfig(configFromFile);
            } catch (IOException e) {
                log.alert("Could not open config file %s.", sf.getAbsolutePath());
                log.error("Could not open config file %s.", sf.getAbsolutePath());
                log.error("%s", StringUtils.getStackTrace(e));
            }

            log.debug("Loaded extension config from %s and saved it to extension settings",
                    sf.getAbsolutePath());
            log.debug("Loaded config: %s", configFromFile);
        }
    }

    private void saveConfigAction() {
        File sf = FileChooser.saveFile(panel, FileChooser.getLastWorkingDirectory(),
                "Save config file", "json");

        if (sf != null) {
            // Set the last working directory.
            FileChooser.setLastWorkingDirectory(sf.getParent());
            // Save the file.
            try {
                extensionConfig.writeToFile(sf);
            } catch (Exception e) {
                String errMsg =
                        String.format("Could not write to file: %s", StringUtils.getStackTrace(e));
                log.alert(errMsg);
                log.error(errMsg);
            }
        }
    }

    // Called when the toggle button state changes.
    private void processToggleAction(ActionEvent evt) {
        // http://www.java2s.com/Tutorials/Java/Java_Swing/0880__Java_Swing_JToggleButton.htm
        AbstractButton abstractButton = (AbstractButton) evt.getSource();
        boolean selected = abstractButton.getModel().isSelected();
        if (selected) {
            lintPool.resume();
        } else {
            lintPool.pause();
        }
    }

    // Filter the tablemodel.
    private void searchAction(String query) {
        // Change 1 to 0 if you want to search by host instead of URL.
        lintTable.filter(query, 1);
    }

    // GUI Variables
    private JButton loadConfigButton;
    private JButton saveConfigButton;
    private JTextField searchTextField;
    private JButton resetButton;
    private JPanel topPanel;
    private JToggleButton processToggleButton;
    private JSeparator topSeparator;
    private JScrollPane tableScrollPane;
}