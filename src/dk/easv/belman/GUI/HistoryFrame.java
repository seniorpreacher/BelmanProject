package dk.easv.belman.GUI;

//<editor-fold defaultstate="collapsed" desc=" Imports ">
import dk.easv.belman.GUI.Models.CutTableModel;
import dk.easv.belman.BE.Cut;
import dk.easv.belman.BLL.XMLWriter;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import org.jdesktop.swingx.JXTable;
//</editor-fold>

public class HistoryFrame extends javax.swing.JFrame {

    //<editor-fold defaultstate="collapsed" desc=" Global variables ">
    private MainGui parent; // The parent frame.
    private JXTable table; // The table where we show the contents.
    private CutTableModel model; // The TableModel where we store the contents.
    private Cut selectedCut; // The currently selected cut.
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Constructor ">
    /**
     * Creates new form CutHistoryFrame
     */
    public HistoryFrame(MainGui parent) {
        this.parent = parent; // Set the parent.
        initComponents(); // Init components put in using the NetBeans designer.
        init(); // Init other components.
        this.setVisible(true); // Make the frame visible.
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Only dispose this window in case the 'Close' button is pressed.
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Initialize components ">
    /**
     * Initialize components which were added/created manually.
     */
    private void init() {
        //Initialize the table and sets the model
        table = new JXTable(); // Creates an empty JXTable (from SwingX 1.6.1) for now.
        JScrollPane sf = new JScrollPane(table); // Creates a Scroll Pane where the table will be.
        model = new CutTableModel(Main.allCutData.filterByArchive(false)); // Create a TableModel with all the cuts that are mot archived.
        table.setModel(model); // Set the TableModel to the table.
        setTableProperties(table); // Set the table properites.

        pnlTable.setLayout(new BorderLayout()); // Set the panel layout to BorderLayout.
        pnlTable.add(sf); // Add the panel with the table to it.

        btnUndo.setEnabled(false); // Disable 'Undo cut' button by default. 
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Table properties ">
    /**
     * Set different properties for the given table.
     *
     * @param table The table to which we set the properties.
     */
    public void setTableProperties(JXTable table) {
        table.setDragEnabled(false); // Dragging is disabled.
        table.setColumnControlVisible(true); // Column control settings are enabled.
        table.packAll(); // Packs the table.
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only one selection is allowed per table.
        table.setSortOrderCycle(SortOrder.ASCENDING, SortOrder.DESCENDING, SortOrder.UNSORTED); // Sets the sorting order to ASC > DESC > Unsorted.
        table.addMouseListener(new MouseAdapter() { // Listen for clicks on the table.
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedCut = null; // Set the selected cut to null.
                if (e.getSource().equals(HistoryFrame.this.table)) { // If the event was performed on the table...
                    // Select the currently selected Cut and assign it to the selectedCut variable.
                    selectedCut = model.getCutByRow(HistoryFrame.this.table.convertRowIndexToModel(HistoryFrame.this.table.getSelectedRow()));
                }
                if (selectedCut != null) { // If there is a selected cut...
                    btnUndo.setEnabled(true); // Enable the 'Undo' button.
                } else { // If there is no selected cut.
                    btnUndo.setEnabled(false); // Disable the 'Undo' button.
                }
            }
        });
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Initialize components (Auto-generated) ">
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlTable = new javax.swing.JPanel();
        btnXMLExport = new javax.swing.JButton();
        btnUndo = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout pnlTableLayout = new javax.swing.GroupLayout(pnlTable);
        pnlTable.setLayout(pnlTableLayout);
        pnlTableLayout.setHorizontalGroup(
            pnlTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 480, Short.MAX_VALUE)
        );
        pnlTableLayout.setVerticalGroup(
            pnlTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 573, Short.MAX_VALUE)
        );

        btnXMLExport.setText("Export to XML");
        btnXMLExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXMLExportActionPerformed(evt);
            }
        });

        btnUndo.setText("Undo Cut");
        btnUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUndoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnXMLExport, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                    .addComponent(btnUndo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnUndo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnXMLExport, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Export to XML Button ">
    private void btnXMLExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXMLExportActionPerformed
        XMLWriter xml = new XMLWriter(); // Create an XML Writer.
        if (xml.write(Main.allCutData)) { // Try to write allCutData to XML, if it succseeds... 
            JOptionPane.showMessageDialog(null, "You successfully saved the cutting history to an XML file!", "Successfull XML save", JOptionPane.INFORMATION_MESSAGE);
        } else { // If it fails...
            JOptionPane.showMessageDialog(null, "The program couldn't save your XML file!", "Error in XML save", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnXMLExportActionPerformed
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Undo Button ">
    private void btnUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUndoActionPerformed
        if (selectedCut != null) { // If there's a selected Cut...
            selectedCut.getSleeve().setDone(false); // Set the sleeve to not done.
            selectedCut.getSleeve().save(); // Save it to the database.
            Main.allOrderData.update(); // Update allOrderData with the recently updated data from the database.

            selectedCut.setArchived(true); // Archive the Cut.
            selectedCut.save(); // Save it to the database.
            Main.allCutData.update(); // Update allCutData with the recently updated data from the database.
            model.setCutList(Main.allCutData.filterByArchive(false)); // Refresh the Cut table with all the Cuts that are NOT archived.

            parent.sleeveModel.setItemList(Main.allOrderData.getItemList().filterByDone(false)); // Update the Sleeve table in the Main UI as well.

            table.clearSelection(); // Clear the table selection in the Cut table.
            selectedCut = null; // Set the selectedCut to null (nothing is selected).

            btnUndo.setEnabled(false); // Disable the 'Undo' button.
        }
    }//GEN-LAST:event_btnUndoActionPerformed
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc=" More variables ">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnUndo;
    private javax.swing.JButton btnXMLExport;
    private javax.swing.JPanel pnlTable;
    // End of variables declaration//GEN-END:variables
    //</editor-fold>
}
