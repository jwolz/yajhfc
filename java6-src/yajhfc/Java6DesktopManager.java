package yajhfc;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class Java6DesktopManager extends DesktopManager {

    @Override
    public void browse(URI uri) throws IOException {
        if (Desktop.isDesktopSupported()) {
            final Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(uri);
            } else {
                super.browse(uri);
            }
        } else {
            super.browse(uri);
        }
    }

    @Override
    public void open(File file) throws IOException {
        if (Desktop.isDesktopSupported()) {
            final Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(file);
            } else {
                super.open(file);
            }
        } else {
            super.open(file);
        }
    }
}
