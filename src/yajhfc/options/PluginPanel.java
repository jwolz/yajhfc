/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package yajhfc.options;

import static yajhfc.Utils._;
import static yajhfc.options.OptionsWin.border;
import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.model.IconMap;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginTableModel;
import yajhfc.plugin.PluginType;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.JTableTABAction;

/**
 * @author jonas
 *
 */
public class PluginPanel extends AbstractOptionsPanel {
    JTable tablePlugins;
    PluginTableModel pluginTableModel;
    JButton buttonAddJDBC;
    JButton buttonAddPlugin;
    JButton buttonRemovePlugin;


    public PluginPanel() {
        super(false);
    }


    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        pluginTableModel.addAllItems(PluginManager.getKnownPlugins());
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        if (PluginManager.updatePluginList(pluginTableModel.getEntries())) {
            JOptionPane.showMessageDialog(this, Utils._("You will need to restart the program for the changes to the list of plugins and JDBC drivers to take full effect."), Utils._("Plugins & JDBC"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.options.AbstractOptionsPanel#createOptionsUI()
     */
    @Override
    protected void createOptionsUI() {
            double[][] dLay = {
                    {border, TableLayout.FILL, border, TableLayout.PREFERRED, border},
                    {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL, border}
            };
            setLayout(new TableLayout(dLay));
            
            pluginTableModel = new PluginTableModel();
            tablePlugins = new JTable(pluginTableModel); /* {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer,
                        int row, int column) {
                    Component comp = super.prepareRenderer(renderer, row, column);
                    PluginTableModel.Entry entry = ((PluginTableModel)this.dataModel).getEntry(row);
                    if (getSelectedRow() != row) {
                        if (!entry.persistent) {
                            comp.setBackground(UIManager.getColor("TextField.inactiveBackground"));
                            //comp.setForeground(UIManager.getColor("TextField.inactiveForeground"));
                        } else {
                            comp.setBackground(getBackground());
                            //comp.setForeground(getForeground());
                        }
                    }
                    return comp;
                }  
            };*/
            tablePlugins.setDefaultRenderer(IconMap.class, new IconMap.TableCellRenderer());
            tablePlugins.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        int selRow = tablePlugins.getSelectedRow();
                        buttonRemovePlugin.setEnabled(selRow >= 0) ; // && pluginTableModel.getEntry(selRow).persistent);
                    }
                }
                
            });
            tablePlugins.getColumnModel().getColumn(0).setPreferredWidth(300);
            JTableTABAction.replaceTABWithNextRow(tablePlugins);
            
            JScrollPane scrollTable = new JScrollPane(tablePlugins);
            
            ActionListener actionListener = new ActionListener() {
                JFileChooser fileChooser;
                
                private File chooseFile(String title) {
                    if (fileChooser == null) {
                        fileChooser = new yajhfc.util.SafeJFileChooser();
                        fileChooser.setAcceptAllFileFilterUsed(false);
                        fileChooser.addChoosableFileFilter(new ExampleFileFilter("jar", Utils._("JAR files")));
                    }
                    fileChooser.setDialogTitle(title);
                    if (fileChooser.showOpenDialog(PluginPanel.this) == JFileChooser.APPROVE_OPTION) {
                        return fileChooser.getSelectedFile();
                    } else {
                        return null;
                    }
                }
                
                public void actionPerformed(ActionEvent e) {
                    String actCmd = e.getActionCommand();
                    if (actCmd.equals("addJDBC")) {
                        File jar = chooseFile(Utils._("Add JDBC driver"));
                        if (jar == null)
                            return;
                        
                        pluginTableModel.addItem(jar, PluginType.JDBCDRIVER);
                    } else if (actCmd.equals("addPlugin")) {
                        File jar = chooseFile(Utils._("Add plugin"));
                        if (jar == null)
                            return;
                        
                        if (!PluginManager.isValidPlugin(jar)) {
                            JOptionPane.showMessageDialog(PluginPanel.this, MessageFormat.format(Utils._("The file {0} is not a valid YajHFC plugin!"), jar), Utils._("Add plugin"), JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        pluginTableModel.addItem(jar, PluginType.PLUGIN);
                    } else if (actCmd.equals("remove")) {
                        int idx = tablePlugins.getSelectedRow();
                        if (idx >= 0) {
                            pluginTableModel.removeItemAt(idx);
                        }
                    } else 
                        assert(false);
                }
            };
            buttonAddJDBC = new JButton(_("Add JDBC driver") + "...", Utils.loadIcon("development/JarAdd"));
            buttonAddJDBC.addActionListener(actionListener);
            buttonAddJDBC.setActionCommand("addJDBC");
            buttonAddPlugin = new JButton(_("Add plugin") +  "...", Utils.loadIcon("development/J2EEApplicationClientAdd"));
            buttonAddPlugin.addActionListener(actionListener);
            buttonAddPlugin.setActionCommand("addPlugin");
            buttonRemovePlugin = new JButton(_("Remove item"), Utils.loadIcon("general/Remove"));
            buttonRemovePlugin.addActionListener(actionListener);
            buttonRemovePlugin.setActionCommand("remove");
            buttonRemovePlugin.setEnabled(false);
            
            this.add(scrollTable, "1,1,1,7,f,f");
            this.add(buttonAddPlugin, "3,1");
            this.add(buttonAddJDBC, "3,3");
            this.add(buttonRemovePlugin, "3,5");
    }

}
