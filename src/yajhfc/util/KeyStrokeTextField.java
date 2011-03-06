package yajhfc.util;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

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
                }
            }
        }
            
    }
}