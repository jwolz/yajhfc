package yajhfc.tray;

import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.TrayIcon;

public class DefaultTrayIcon extends TrayIcon implements ITrayIcon {

    public DefaultTrayIcon(Image image, String tooltip, PopupMenu popup) {
        super(image, tooltip, popup);
    }

    public DefaultTrayIcon(Image image, String tooltip) {
        super(image, tooltip);
    }

    public DefaultTrayIcon(Image image) {
        super(image);
    }

    public void displayMessage(String caption, String text, int messageType) {
        MessageType msgType;
        switch (messageType) {
        case MSGTYPE_INFO:
            msgType = MessageType.INFO;
            break;
        case MSGTYPE_WARNING:
            msgType = MessageType.WARNING;
            break;
        case MSGTYPE_ERROR:
            msgType = MessageType.ERROR;
            break;
        case MSGTYPE_NONE:
        default:
            msgType = MessageType.NONE;
            break;
        }
        
        displayMessage(caption, text, msgType);
    }
}
