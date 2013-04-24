package yajhfc;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import yajhfc.file.FileFormat;
import yajhfc.util.CancelAction;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.SafeJFileChooser;

public class TextViewPanel extends JPanel {
    private static final int border = 8;

    /**
     * Displays a frame showing the specified texts with the TextViewPanel as its contentPane
     * @param title the window title
     * @param toDisplay the text to display
     * @param wordWrap whether to wrap words or not
     * @return the displayed window
     * @throws HeadlessException
     */
    public static JFrame displayFrame(String title, List<Text> toDisplay, boolean wordWrap) throws HeadlessException {
        JFrame frame = new JFrame(title);
        initFrame(frame, toDisplay, wordWrap);
        
        frame.setVisible(true);
        return frame;
    }
    
    /**
     * Displays a dialog showing the specified texts with the TextViewPanel as its contentPane
     * @param title the window title
     * @param toDisplay the text to display
     * @param wordWrap whether to wrap words or not
     * @return the displayed window
     * @throws HeadlessException
     */
    public static JDialog displayDialog(Frame owner, String title, List<Text> toDisplay, boolean wordWrap) throws HeadlessException {
        JDialog dialog = new JDialog(owner, title, false);
        initDialog(dialog, toDisplay, wordWrap);
        
        dialog.setVisible(true);
        return dialog;
    }
    
    /**
     * Displays a dialog showing the specified texts with the TextViewPanel as its contentPane
     * @param title the window title
     * @param toDisplay the text to display
     * @param wordWrap whether to wrap words or not
     * @return the displayed window
     * @throws HeadlessException
     */
    public static JDialog displayDialog(Dialog owner, String title, List<Text> toDisplay, boolean wordWrap) throws HeadlessException {
        JDialog dialog = new JDialog(owner, title, false);
        initDialog(dialog, toDisplay, wordWrap);
        
        dialog.setVisible(true);
        return dialog;
    }
    
    private static void initFrame(final JFrame frame, List<Text> toDisplay, boolean wordWrap) {
        TextViewPanel contentPane = new TextViewPanel(toDisplay, wordWrap, frame);
        frame.setContentPane(contentPane);
        frame.setIconImage(Utils.loadIcon("general/History").getImage());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        commonWindowInit(frame);
    }
    
    private static void initDialog(final JDialog dialog, List<Text> toDisplay, boolean wordWrap) {
        TextViewPanel contentPane = new TextViewPanel(toDisplay, wordWrap, dialog);
        dialog.setContentPane(contentPane);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        commonWindowInit(dialog);
    }
    
    private static void commonWindowInit(final Window win) {        
        if (Utils.getFaxOptions().logViewerBounds != null) {
            win.setBounds(Utils.getFaxOptions().logViewerBounds);
        } else {
            win.pack();
            Utils.setDefWinPos(win);
        }
        win.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Utils.getFaxOptions().logViewerBounds = win.getBounds();
            }
        });
    }
    
    /////////////////////////////////////////////////////////////////////////////
    
    Action actSave, actCopy;
    JTabbedPane tabs;
    boolean wordWrap;
    
    public TextViewPanel(List<Text> toDisplay, boolean wordWrap, Window parent) {
        super(new BorderLayout());
        this.wordWrap = wordWrap;
        initialize(toDisplay, parent);
    }
    
    private void initialize(List<Text> toDisplay, Window parent) {
        createActions();
        CancelAction cancelAct = new CancelAction(parent, Utils._("Close"));
        
        //setLayout(new BorderLayout());
        tabs = new JTabbedPane(JTabbedPane.BOTTOM);
        
        for (Text log : toDisplay) {
            addLog(log);
        }
        
        JPanel buttonsPane = new JPanel(new GridLayout(1, 3, border, border));
        buttonsPane.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        buttonsPane.add(new JButton(actCopy));
        buttonsPane.add(new JButton(actSave));
        buttonsPane.add(cancelAct.createCancelButton());
        
        add(tabs, BorderLayout.CENTER);
        add(buttonsPane, BorderLayout.SOUTH);
    }
    
    private void addLog(Text log) {
        JTextArea textDisplay = new JTextArea(log.log);
        textDisplay.setEditable(false);
        textDisplay.setFont(new Font("Monospaced", Font.PLAIN, 12));
        if (wordWrap) {
            textDisplay.setWrapStyleWord(true);
            textDisplay.setLineWrap(true);
        }
        ClipboardPopup.DEFAULT_POPUP.addToComponent(textDisplay);

        tabs.addTab(log.caption, 
                new JScrollPane(textDisplay, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    }
    
    private void createActions() {
        actSave = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new SafeJFileChooser();
                if (Utils.getFaxOptions().lastSavePath.length() > 0) {
                    fileChooser.setCurrentDirectory(new File(Utils.getFaxOptions().lastSavePath));
                }
                fileChooser.resetChoosableFileFilters();
                FileFilter txtFilter = new ExampleFileFilter(FileFormat.PlainText.getPossibleExtensions(), FileFormat.PlainText.getDescription());
                FileFilter logFilter = new ExampleFileFilter("log", Utils._("Log files"));
                fileChooser.addChoosableFileFilter(txtFilter);
                fileChooser.addChoosableFileFilter(logFilter);
                fileChooser.setFileFilter(txtFilter);
                
                if (fileChooser.showSaveDialog(TextViewPanel.this) == JFileChooser.APPROVE_OPTION) {
                    Utils.getFaxOptions().lastSavePath = fileChooser.getCurrentDirectory().getAbsolutePath();
                    
                    File selectedFile = Utils.getSelectedFileFromSaveChooser(fileChooser);
                    JTextArea selectedText = (JTextArea)((JScrollPane)tabs.getSelectedComponent()).getViewport().getView();
                    
                    try {
                        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(selectedFile));
                        writer.write(selectedText.getText());
                        writer.close();
                    } catch (Exception e1) {
                        ExceptionDialog.showExceptionDialog(TextViewPanel.this, Utils._("Error saving the file"), e1);
                    }
                }
            }
        };
        actSave.putValue(Action.NAME, Utils._("Save") + "...");
        actSave.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Save"));
        
        actCopy = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                JTextArea selectedText = (JTextArea)((JScrollPane)tabs.getSelectedComponent()).getViewport().getView();
                StringSelection contents = new StringSelection(selectedText.getText());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, contents);
            }
        };
        actCopy.putValue(Action.NAME, Utils._("Copy") );
        actCopy.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Copy"));
    }
    
    public static class Text {
        public String caption;
        public String log;
        
        public Text(String caption, String log) {
            super();
            this.caption = caption;
            this.log = log;
        }        
    }
}