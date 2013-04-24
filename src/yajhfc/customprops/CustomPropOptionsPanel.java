/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
 */
package yajhfc.customprops;

import info.clearthought.layout.TableLayout;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.options.AbstractOptionsPanel;
import yajhfc.options.OptionsWin;
import yajhfc.util.ComponentEnabler;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.IntVerifier;
import yajhfc.util.MapEditorDialog;

/**
 * @author jonas
 *
 */
public class CustomPropOptionsPanel extends AbstractOptionsPanel<FaxOptions> {
    JTextField textHttpProxyHost;
    JTextField textHttpProxyPort;
    JTextField textHttpNonProxyHosts;
    JCheckBox checkUseSystemProxies;
    
    Action actEditProps;
    
    Map<String,String> customProps;
    
    public CustomPropOptionsPanel() {
        super(null);
    }

    public void loadSettings(FaxOptions foEdit) {
    	customProps = new HashMap<String,String>(EntryPoint.getCustomJavaProperties());
        
    	loadUIFromProperties(customProps, true);
    }

    private String getProperty(Map<String,String> p, String propertyName, String defaultValue, boolean initialLoad) {
    	String res = p.get(propertyName);
    	if (initialLoad && res == null)
    	    res = System.getProperty(propertyName);
    	
    	return (res == null) ? defaultValue : res;
    }
    
    void loadUIFromProperties(Map<String,String> p, boolean initialLoad) {
        checkUseSystemProxies.setSelected(Boolean.parseBoolean(getProperty(p, EntryPoint.PROP_USE_SYSTEM_PROXIES, "false", initialLoad)));
        
        textHttpProxyHost.setText(getProperty(p, EntryPoint.PROP_HTTP_HOST, "", initialLoad));
        textHttpProxyPort.setText(getProperty(p, EntryPoint.PROP_HTTP_PORT, "", initialLoad));
        textHttpNonProxyHosts.setText(getProperty(p, EntryPoint.PROP_HTTP_NON_PROXY_HOSTS, "", initialLoad));
    }
    
    void saveUIToProperties(Map<String,String> p) {
    	p.put(EntryPoint.PROP_USE_SYSTEM_PROXIES, Boolean.toString(checkUseSystemProxies.isSelected()));
        
    	putTextFieldValue(p, textHttpProxyHost, EntryPoint.PROP_HTTP_HOST);
    	putTextFieldValue(p, textHttpProxyPort, EntryPoint.PROP_HTTP_PORT);
    	putTextFieldValue(p, textHttpNonProxyHosts, EntryPoint.PROP_HTTP_NON_PROXY_HOSTS);
    }
    
    private void putTextFieldValue(Map<String,String> p, JTextComponent textField, String propertyName) {
    	String s = textField.getText();
    	if (s==null || s.length() == 0) {
    		p.remove(propertyName);
    	} else {
    		p.put(propertyName, s);
    	}
    }
    
    public void saveSettings(FaxOptions foEdit) {
    	saveUIToProperties(customProps);
    	
    	Map<String,String> target = EntryPoint.getCustomJavaProperties();
    	Properties sysProps = System.getProperties();

    	sysProps.keySet().removeAll(target.keySet());
    	target.clear();
    	
    	target.putAll(customProps);
    	sysProps.putAll(customProps);
    }

    @Override
    protected void createOptionsUI() {
        actEditProps = new ExcDialogAbstractAction(Utils._("Define system properties") + "...") {
			
			@Override
			protected void actualActionPerformed(ActionEvent e) {
				saveUIToProperties(customProps);
				
				MapEditorDialog med = new MapEditorDialog((Dialog)SwingUtilities.getWindowAncestor(CustomPropOptionsPanel.this),
						Utils._("Define system properties"), customProps) {

					@Override
					protected String getCaption() {
						return Utils._("Here you can define Java system properties. Only do this if you know what you are doing.");
					}

					@Override
					protected String[] getAvailableProperties() {
						return new String[] {
								"java.net.useSystemProxies",
								"http.proxyHost",
								"http.proxyPort",
								"http.nonProxyHosts",
								"https.proxyHost",
								"https.proxyPort",
								"ftp.proxyHost",
								"ftp.proxyPort",
								"ftp.nonProxyHosts",
								"socksProxyHost",
								"socksProxyPort"
						};
					}
				};
				
				med.setVisible(true);
				
				loadUIFromProperties(customProps, false);
			}
		};
        
		textHttpNonProxyHosts = new JTextField();
		textHttpProxyHost = new JTextField();
		textHttpProxyPort = new JTextField();
		textHttpProxyPort.setInputVerifier(new IntVerifier(1, 65536, true));
		
		checkUseSystemProxies = new JCheckBox(Utils._("Use system proxy settings"));
		ComponentEnabler.installOn(checkUseSystemProxies, false, textHttpNonProxyHosts, textHttpProxyHost, textHttpProxyPort);
		
		double[][] dLay = {
				{OptionsWin.border, 0.666, OptionsWin.border, TableLayout.FILL, OptionsWin.border},
				{OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.FILL, TableLayout.PREFERRED, OptionsWin.border}
		};
		this.setLayout(new TableLayout(dLay));
		
		this.add(checkUseSystemProxies, "1,1,3,1,l,c");
		Utils.addWithLabel(this, textHttpProxyHost, Utils._("HTTP proxy server") + ":", "1,4");
		Utils.addWithLabel(this, textHttpProxyPort, Utils._("HTTP proxy port") + ":", "3,4");
		Utils.addWithLabel(this, textHttpNonProxyHosts, Utils._("HTTP non proxy hosts") + ":", "1,7,3,7");
		this.add(new JButton(actEditProps), "1,10,3,10,f,f");
    }
}
