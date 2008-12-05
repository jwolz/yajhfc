package yajhfc.tray;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This interface wraps the java.awt.TrayIcon methods from Java 6
 * @author jonas
 *
 */
public interface ITrayIcon {

    public static final int MSGTYPE_NONE = 0;
    public static final int MSGTYPE_INFO = 1;
    public static final int MSGTYPE_WARNING = 2;
    public static final int MSGTYPE_ERROR = 3;
    
    void addActionListener(ActionListener listener);

    void addMouseListener(MouseListener listener);

    void addMouseMotionListener(MouseMotionListener listener);

    void displayMessage(String caption, String text, int msgType);

    String getActionCommand();

    ActionListener[] getActionListeners();

    Image getImage();

    MouseListener[] getMouseListeners();

    MouseMotionListener[] getMouseMotionListeners();

    PopupMenu getPopupMenu();

    Dimension getSize();

    String getToolTip();

    boolean isImageAutoSize();

    void removeActionListener(ActionListener listener);

    void removeMouseListener(MouseListener listener);

    void removeMouseMotionListener(MouseMotionListener listener);

    void setActionCommand(String command);

    void setImage(Image image);

    void setImageAutoSize(boolean autosize);

    void setPopupMenu(PopupMenu popup);

    void setToolTip(String tooltip);

}