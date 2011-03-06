package yajhfc.util;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz
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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EventListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import yajhfc.Utils;

public class KeyStrokeTextField extends JTextField {
    public KeyStrokeTextField() {
        super(20);
        setKeymap(new AcceleratorKeymap());
    }
    
    protected KeyStroke keyStroke;
    
    public KeyStroke getKeyStroke() {
        return keyStroke;
    }
    
    public void setKeyStroke(KeyStroke keyStroke) {
        this.keyStroke = keyStroke;
        setText(keyStrokeToUserString(keyStroke));
    }
    
    public void addKeyStrokeTextFieldListener(KeyStrokeTextFieldListener l) {
        this.listenerList.add(KeyStrokeTextFieldListener.class, l);
    }
    
    public void removeKeyStrokeTextFieldListener(KeyStrokeTextFieldListener l) {
        this.listenerList.remove(KeyStrokeTextFieldListener.class, l);
    }
    
    protected void fireUserTypedShortCut(KeyStroke ks) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==KeyStrokeTextFieldListener.class) {
                ((KeyStrokeTextFieldListener)listeners[i+1]).userTypedShortcut(ks);
            }          
        }
    }
    
    public static String keyStrokeToUserString(KeyStroke keyStroke) {
    	if (keyStroke == null)
    		return Utils._("<none>");
        if (keyStroke.getModifiers() != 0) {
            return KeyEvent.getModifiersExText(keyStroke.getModifiers()) + "+" + KeyEvent.getKeyText(keyStroke.getKeyCode());   
        } else {
            return KeyEvent.getKeyText(keyStroke.getKeyCode());
        }
    }
    
    class AcceleratorKeymap extends AbstractAction implements Keymap  {
        private Keymap resolveParent;  
        
        private KeyStroke lastKeystroke;
        
        public String getName() {
            return "Accelerator key map";
        }

        public Action getDefaultAction() {
            return this;
        }

        public void setDefaultAction(Action a) {
            // NOP
        }

        public Action getAction(KeyStroke key) {
            lastKeystroke = key;
            return this;
        }

        public KeyStroke[] getBoundKeyStrokes() {
            return new KeyStroke[0];
        }

        public Action[] getBoundActions() {
            return new Action[0];
        }

        public KeyStroke[] getKeyStrokesForAction(Action a) {
            return new KeyStroke[0];
        }

        public boolean isLocallyDefined(KeyStroke key) {
            return true;
        }

        public void addActionForKeyStroke(KeyStroke key, Action a) {
            // NOP
        }

        public void removeKeyStrokeBinding(KeyStroke keys) {
            // NOP 
        }

        public void removeBindings() {
            // NOP
        }

        public Keymap getResolveParent() {
            return resolveParent;
        }

        public void setResolveParent(Keymap parent) {
            resolveParent = parent;
        }

        public void actionPerformed(ActionEvent e) {
            if (lastKeystroke.getKeyEventType() == KeyEvent.KEY_PRESSED) {
                switch (lastKeystroke.getKeyCode()) {
                case KeyEvent.VK_CONTROL:
                case KeyEvent.VK_ALT:
                case KeyEvent.VK_SHIFT:
                case KeyEvent.VK_ALT_GRAPH:
                case KeyEvent.VK_META:
                    // Do nothing for modifier keys
                    break;
                default:
                    setKeyStroke(lastKeystroke);
                    fireUserTypedShortCut(lastKeystroke);
                }
            }
        }
    }
    
    public interface KeyStrokeTextFieldListener extends EventListener {
        public void userTypedShortcut(KeyStroke newShortcut);
    }
}