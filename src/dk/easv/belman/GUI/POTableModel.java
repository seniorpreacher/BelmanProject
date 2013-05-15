package dk.easv.belman.GUI;

import dk.easv.belman.BE.Cut;
import dk.easv.belman.BE.CutList;
import dk.easv.belman.BE.ProductionOrder;
import dk.easv.belman.BE.ProductionOrderList;
import dk.easv.belman.BLL.ListManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.table.AbstractTableModel;

public class POTableModel extends AbstractTableModel {

    private ProductionOrderList poList; // A list of cuts 
    private ListManager listManager;
    // The names of columns
    private String[] colNames = {"PO Description", "Date"};
    // The type of columns
    private Class[] classes = {String.class, Long.class};

    /**
     * Constructor for the SleeveTableModel.
     *
     * @param sList Constructs a table model based on a SalesOrdeList.
     */
    public POTableModel(ProductionOrderList poList) {
        this.poList = poList;
        listManager = new ListManager();

        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return poList.size();
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

        ProductionOrder po = poList.get(row);

        switch (col) {
            case 0:
                return po.getDescription();           
            case 1:
                return "READY";           
        }
        return null;
    }


    /**
     * Gets a PO by row.
     *
     * @param row the number of row from where we want to get the PO.
     * @return a PO from a selected row.
     */
    public ProductionOrder getPOByRow(int row) {
        return poList.get(row);
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