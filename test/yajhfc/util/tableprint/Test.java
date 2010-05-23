package yajhfc.util.tableprint;

import java.awt.HeadlessException;
import java.awt.print.PrinterException;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import yajhfc.FaxNotification;
import yajhfc.IconMap;
import yajhfc.util.StatusDialogPrintable;

public class Test extends AbstractTableModel {

    private static final int COL_COUNT = 8;
    protected int rowCount;
    protected int colCount;
    
    public Test(int rowCount, int colCount) {
        this.rowCount = rowCount;
        this.colCount = colCount;
    }
    
    public Test() {
        this(200, COL_COUNT);
    }
    
    public int getColumnCount() {
        return colCount;
    }
    
    @Override
    public String getColumnName(int column) {
        switch (column % COL_COUNT) {
        case 0:
            return "Integer";
        case 1:
            return "String";
        case 2:
            return "Boolean";
        case 3:
            return "Date";
        case 4:
            return "IconMap";
        case 5:
            return "Multi line text";
        case 6:
            return "Long text";
        case 7:
            return "Long text w/ line break";
        default:
            return null;
        }
    }
    
    public int getRowCount() {
        return rowCount;
    }
    
    IconMap[] iconMapVals = FaxNotification.values();
    
    @SuppressWarnings("deprecation")
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex % COL_COUNT) {
        case 0:
            return rowIndex;
        case 1:
            return "Line " + rowIndex;
        case 2:
            return (rowIndex % 2) == 0;
        case 3:
            return new Date(2000 + rowIndex/(28*12), (rowIndex / 28)%12, rowIndex%28+1);
        case 4:
            return iconMapVals[rowIndex % iconMapVals.length];
        case 5:
            return "In line\nnumber " + rowIndex + "\nthere is\ntext with\nmultiple lines!";
        case 6:
            return "This is quite a long text which probably needs a word break to be rendered completely. By the way, this text is displayed for you in line " + rowIndex + ".";
        case 7:
            return "This is quite a long text\nwith line breaks which needs a word break to be renderedlongwordlywordly completely.\nBy the way, this text is displayed for you in line " + rowIndex + ".";
        default:
            return null;
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex % COL_COUNT) {
        case 0:
            return Integer.class;
        case 1:
            return String.class;
        case 2:
            return Boolean.class;
        case 3:
            return Date.class;
        case 4:
            return IconMap.class;
        case 5:
        case 6:
        case 7:
            return String.class;
        default:
            return Object.class;
        }
    }
    
    /**
     * @param args
     * @throws PrinterException 
     * @throws HeadlessException 
     */
    public static void main(String[] args) throws Exception {
        TableModel model = new Test();
        TablePrintable tp = new TablePrintable(model);
        tp.getPageHeader().put(Alignment.CENTER, new MessageFormat("'Testausgabe'"));
        tp.getRendererMap().put(IconMap.class, new IconMapCellRenderer());
        
        tp.getColumnLayout().getHeaderLayout()[2].setWidth(30);
        
        tp.getColumnLayout().getHeaderLayout()[6].setWordWrap(true);
        tp.getColumnLayout().getHeaderLayout()[6].setWidth(0.25f);
        tp.getColumnLayout().getHeaderLayout()[7].setWordWrap(true);
        tp.getColumnLayout().getHeaderLayout()[7].setWidth(TablePrintColumn.WIDTH_FILL);
        
        //tp.setLineWidth(-1);
        
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(OrientationRequested.LANDSCAPE);
        pras.add(new Destination(new File("/tmp/out-test.ps").toURI()));
        if (StatusDialogPrintable.printWithDialog(null, tp, pras)) {
            Runtime.getRuntime().exec(new String[] { "gv", "/tmp/out-test.ps" });
        }
    }

}
