package dk.easv.belman.GUI;

import dk.easv.belman.BE.Cut;
import dk.easv.belman.BE.CutList;
import dk.easv.belman.BE.Item;
import dk.easv.belman.BE.ItemList;
import dk.easv.belman.BE.SalesOrderList;
import dk.easv.belman.BLL.ListManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.table.AbstractTableModel;

public class CutTableModel extends AbstractTableModel {

    private CutList cutList; // A list of cuts 
    private ListManager listManager;
    // The names of columns
    private String[] colNames = {"PO Description", "Date", "Operator", "Time"};
    // The type of columns
    private Class[] classes = {String.class, Long.class, String.class, Long.class};

    /**
     * Constructor for the SleeveTableModel.
     *
     * @param sList Constructs a table model based on a SalesOrdeList.
     */
    public CutTableModel(CutList cutList) {
        this.cutList = cutList;
        listManager = new ListManager();

        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return cutList.size();
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

        Cut cut = cutList.get(row);

        switch (col) {
            case 0: listManager.getProductOrderList(Main.allOrderData).getById(cut.getSleeve().getProductOrderId()).getDescription();
            case 1: cut.getDate();
            //@TODO case 2: cut.getOperator();
            case 3: cut.getTimeSpent();
        }
        return null;
    }

    /**
     * Removes an cut from the list
     *
     * @param cut
     */
    public void removeCut(Cut cut) {
        cutList.remove(cut);
    }

    /**
     * Adds an cut to the list
     *
     * @param cut
     */
    public void addCut(Cut cut) {
        cutList.add(cut);
    }

    /**
     * Sets the Cut list to new cutList
     *
     * @param cutList the list of cuts to set
     */
    public void setCutList(CutList cutList) {
        this.cutList = cutList;
    }

    /**
     * Gets a Cut by row.
     *
     * @param row the number of row from where we want to get the Cut.
     * @return a Cut from a selected row.
     */
    public Cut getCutByRow(int row) {
        return cutList.get(row);
    }

    @Override
    public String getColumnName(int col) {
        return colNames[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return classes[col];
    }
}