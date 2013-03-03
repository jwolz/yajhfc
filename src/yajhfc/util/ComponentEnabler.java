package yajhfc.util;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

public class ComponentEnabler implements ItemListener {
    protected final JComponent[] toEnable;
    protected final JCheckBox checkBox;
    protected final boolean enabledState;
    protected boolean globalEnable = true;
    protected ComponentEnabler[] childs = null;
    
    public void itemStateChanged(ItemEvent e) {
        checkEnabled();
    }

    public void checkEnabled() {
        boolean enable = globalEnable && (checkBox.isSelected() == enabledState);
        for (JComponent comp : toEnable) {
            comp.setEnabled(enable);
        }
        if (childs != null) {
            for (ComponentEnabler comp : childs) {
                comp.setEnabled(enable);
            }
        }
    }
    
    public void setEnabled(boolean enable) {
        this.globalEnable = enable;
        checkEnabled();
    }
    
    public void setChilds(ComponentEnabler... childs) {
        this.childs = childs;
    }

    /**
     * Creates the component enabler 
     * @param checkBox the checkBox to check for the isSelected state
     * @param enabledState the isSelected state when the components should be enabled
     * @param toEnable the components to enable
     */
    public ComponentEnabler(JCheckBox checkBox, boolean enabledState,
            JComponent... toEnable) {
        super();
        this.checkBox = checkBox;
        this.enabledState = enabledState;
        this.toEnable = toEnable;
    }
    
    /**
     * Installs a component enabler on the specified check box 
     * @param checkBox the checkBox to check for the isSelected state
     * @param enabledState the isSelected state when the components should be enabled
     * @param toEnable the components to enable
     */
    public static ComponentEnabler installOn(JCheckBox checkBox, boolean enabledState, JComponent... toEnable) {
        for (int i=0; i<toEnable.length; i++) {
            if (toEnable[i] == null)
                throw new IllegalArgumentException("toEnable[" + i + "] == null");
        }
        
        final ComponentEnabler enabler = new ComponentEnabler(checkBox, enabledState, toEnable);
        checkBox.addItemListener(enabler);
        enabler.checkEnabled();
        return enabler;
    }
}