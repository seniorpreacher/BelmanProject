package dk.easv.belman.GUI;

//<editor-fold defaultstate="collapsed" desc=" Imports ">
import dk.easv.belman.BE.Cut;
import dk.easv.belman.BE.Item;
import dk.easv.belman.BE.Operator;
import dk.easv.belman.BE.ProductionOrder;
import dk.easv.belman.BE.SalesOrder;
import dk.easv.belman.BE.SalesOrderList;
import dk.easv.belman.BE.StockItem;
import dk.easv.belman.BE.StockItemList;
import dk.easv.belman.BLL.ListManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
//</editor-fold>

public class MainGui extends javax.swing.JFrame {

    // Sleeve/Item table and it's model.
    private JXTable tblSleeves;
    private SleeveTableModel sleeveModel;
    // Currently selected Item/Sleeve from the table.
    private Item selectedItem;
    // Stock table and it's model.
    private JXTable tblStock;
    private StockTableModel stockModel;
    // Currently selected StockItem from the table.
    private StockItem selectedStockItem;
    // Timers for calculating time for each cut;
    private Date startTime;
    private Date endTime;
    private Timer timer;
    // Is there a cut already in progress?
    private boolean cutInProgress = false;

    /**
     * Constructor for the Main Form.
     */
    public MainGui() {
        initComponents(); // Initialize the components created by the NetBeans Designer.
        init(); // Initialize all the other components and variables created by us.
    }

    /**
     *
     * @param showNewPopup
     */
    public void scheduledUpdate(boolean newOrders) {
        if (newOrders) {
            stockModel.setStockList(Main.allStockData.getOnlyUsable().filterBySleeve(selectedItem));
            sleeveModel.setItemList(Main.allOrderData.filterByDone(false).filterByStockItem(selectedStockItem));
        }
    }

    /**
     * Initializes the components that were not generated by the NetBeans
     * Designer.
     */
    private void init() {
        // Sleeve table
        tblSleeves = new JXTable(); // Creates an empty JXTable (from SwingX 1.6.1) for now.
        JScrollPane sp = new JScrollPane(tblSleeves); // Creates a Scroll Pane where the table will be.
        sleeveModel = new SleeveTableModel(Main.allOrderData); // Initializes the SleeveTableModel.
        //sleeveModel = new SleeveTableModel(Main.allOrderData.filterByDone(false)); // Initializes the SleeveTableModel.
        tblSleeves.setModel(sleeveModel); // Sets the table model.
        tblSleeves.setDragEnabled(false); // Dragging is disabled.
        tblSleeves.setColumnControlVisible(true); // Column control settings are enabled.
        tblSleeves.packAll(); // Packs the table.
        tblSleeves.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only one selection is allowed per table.
        tblSleeves.setSortOrderCycle(SortOrder.ASCENDING, SortOrder.DESCENDING, SortOrder.UNSORTED); // Sorts the table in ascending, then descending order, finally it goes back to unsorted.

        final HighlightPredicate highlightUgent = new HighlightPredicate() {
            @Override
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                return ListManager.isUrgent(adapter.getString(0)) == 1;
            }
        };
        final HighlightPredicate highlightPast = new HighlightPredicate() {
            @Override
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                return ListManager.isUrgent(adapter.getString(0)) == -1;
            }
        };
        ColorHighlighter highlighter1 = new ColorHighlighter(highlightUgent, Color.RED, null);
        ColorHighlighter highlighter2 = new ColorHighlighter(highlightPast, Color.MAGENTA, null);
        tblSleeves.addHighlighter(highlighter1);
        tblSleeves.addHighlighter(highlighter2);

        pnlCenter.setLayout(new BorderLayout()); // Sets the layout for the center JPanel.
        pnlCenter.add(sp); // Adds the Scroll Pane with the table to the JPanel on the center.
        addListeners(tblSleeves); // Add key and click listeners.

        // Stock table
        tblStock = new JXTable(); // Creates an empty JXTable (from SwingX 1.6.1) for now.
        JScrollPane sf = new JScrollPane(tblStock); // Creates a Scroll Pane where the table will be.
        stockModel = new StockTableModel(Main.allStockData.getOnlyUsable()); // Initializes the StockTableModel.
        tblStock.setModel(stockModel); // Sets the table model.
        tblSleeves.setDragEnabled(false); // Dragging is disabled.
        tblStock.setColumnControlVisible(true); // Column control settings are enabled.
        tblStock.packAll(); // Packs the table.
        tblStock.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only one selection is allowed per table.
        tblStock.setSortOrderCycle(SortOrder.ASCENDING, SortOrder.DESCENDING, SortOrder.UNSORTED); // Sorts the table in ascending, then descending order, finally it goes back to unsorted.

        pnlWest.setLayout(new BorderLayout()); // Sets the layout for the west JPanel.
        pnlWest.add(sf); // Adds the Scroll Pane with the table to the JPanel on the west.
        addListeners(tblStock); // Add key and click listeners.

        // Populate OperatorComboBox on the jpCut
        pupulateOperatorComboBox();
    }

    private class UpdateUITask extends TimerTask {

        long time = System.currentTimeMillis() + (1000 * 60 * 60);
        DateFormat df = new SimpleDateFormat("HH:mm:ss");

        @Override
        public void run() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    lblTimer.setText(String.valueOf(df.format(new Date(System.currentTimeMillis() - time))));
                }
            });
        }
    }

    private void timerStart() {
        timer = new Timer();
        lblTimer.setForeground(Color.green);
        timer.schedule(new UpdateUITask(), 0, 200);
    }

    private void timerStop() {
        timer.cancel();
        timer.purge();
        lblTimer.setForeground(Color.black);
        //lblTimer.setText("00:00:00");
    }

    /**
     * This method populates the cmbbxOperator with the Operators that do exist
     * in the database.
     */
    private void pupulateOperatorComboBox() {
        DefaultComboBoxModel operatorModel = new DefaultComboBoxModel();
        for (Operator op : Main.allOperatorData.getList()) {
            operatorModel.addElement(op);
        }
        cmbbxOperator.setModel(operatorModel);
    }

    /**
     * Adds Mouse- and KeyListener to the specified table.
     *
     * @param c The component to which the ActionListeners will be added.
     */
    private void addListeners(final JXTable c) {
        // Start of the MouseListener.
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // In case of any clicks...
                if (e.getSource().equals(tblSleeves)) {
                    tblStock.clearSelection(); // Clear the selection in the other table.
                    selectedItem = sleeveModel.getItemByRow(tblSleeves.convertRowIndexToModel(tblSleeves.getSelectedRow())); // Set the new selection.
                } else {
                    tblSleeves.clearSelection(); // Clear the selection in the other table.
                    selectedStockItem = stockModel.getStockByRow(tblStock.convertRowIndexToModel(tblStock.getSelectedRow())); // Set the new selection.

                }

                if (e.getClickCount() == 2) {  // In case of a double click...
                    if (e.getSource().equals(tblSleeves)) {
                        selectedItem = sleeveModel.getItemByRow(tblSleeves.convertRowIndexToModel(tblSleeves.getSelectedRow())); // Set the selected Item/Sleeve.
                        // Filter the table with StockItems, by the currently selected Item/Sleeve.
                        stockModel.setStockList(Main.allStockData.getOnlyUsable().filterBySleeve(selectedItem));

                        // Set the selected Item/Sleeve ready-to-cut.
                        txtSleeve.setText(selectedItem.getParent().getDescription());
                        txtQuantity.setText(String.valueOf(selectedItem.getRemaningCuts()));
                    } else {
                        selectedStockItem = stockModel.getStockByRow(tblStock.convertRowIndexToModel(tblStock.getSelectedRow())); // Set the selected StockItem.
                        // Filter the table with Items/Sleeves, by the currently selected StockItem.
                        sleeveModel.setItemList(Main.allOrderData.filterByDone(false).filterByStockItem(selectedStockItem));
                        // Set the selected StockItem ready-to-cut.
                        txtStockItem.setText(selectedStockItem.getCode());
                    }
                    setCutAmount();
                }

            }
        });
        // End of the MouseListener.
    }

    /**
     * This method sets the txtCutAmount to the possible available cut amount.
     * If te possible amount is greater than the needed amount, the cut amount
     *
     * is set to the needed amount, if less it's set to the possible cut amount.
     */
    private void setCutAmount() {
        if (selectedItem != null && selectedStockItem != null) {
            if (selectedStockItem.canCut(selectedItem)) {
                int cutAmount = selectedStockItem.canCutHowMany(selectedItem); // Gets the actual amount possible to cut
                if (cutAmount > selectedItem.getRemaningCuts()) { // Checks if the possible amount is greater than the quantity needed
                    txtCutAmount.setText(String.valueOf(selectedItem.getRemaningCuts())); // In case possible amount is greater, set the amount to the needed amount
                } else {
                    txtCutAmount.setText(String.valueOf(cutAmount)); // In case possible amount is less, set the amount to the possible amount
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpCut = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtSleeve = new javax.swing.JTextField();
        txtStockItem = new javax.swing.JTextField();
        cmbbxOperator = new javax.swing.JComboBox();
        btnCutAction = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();
        txtCutAmount = new javax.swing.JTextField();
        lblTimer = new javax.swing.JLabel();
        pnlHeader = new javax.swing.JPanel();
        txtStockItemSearch = new javax.swing.JTextField();
        btnStockItemSearch = new javax.swing.JButton();
        btnHistory = new javax.swing.JButton();
        txtSleeveSearch = new javax.swing.JTextField();
        btnSleeveSearch = new javax.swing.JButton();
        btnHistory1 = new javax.swing.JButton();
        pnlCenter = new javax.swing.JPanel();
        pnlWest = new javax.swing.JPanel();
        pnlSpacing = new javax.swing.JPanel();
        lblSleeveTable = new javax.swing.JLabel();
        lblStockTable = new javax.swing.JLabel();
        cmbbxWeekLimit = new javax.swing.JComboBox();
        lblWeekLimit = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBounds(new java.awt.Rectangle(0, 0, 1000, 650));

        jpCut.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        jpCut.setForeground(new java.awt.Color(204, 204, 204));

        jLabel2.setText("Operator");

        jLabel3.setText("Selected Sleeve:");

        jLabel4.setText("Selected Stock Item: ");

        jLabel5.setText("Amount to cut");

        jLabel7.setText("Cutting handler");

        txtSleeve.setEditable(false);

        txtStockItem.setEditable(false);

        btnCutAction.setText("Start");
        btnCutAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCutActionActionPerformed(evt);
            }
        });

        jLabel6.setText("Actual cut amount");

        txtQuantity.setEditable(false);

        lblTimer.setFont(new java.awt.Font("Courier New", 0, 48)); // NOI18N
        lblTimer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTimer.setText("00:00:00");

        javax.swing.GroupLayout jpCutLayout = new javax.swing.GroupLayout(jpCut);
        jpCut.setLayout(jpCutLayout);
        jpCutLayout.setHorizontalGroup(
            jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpCutLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpCutLayout.createSequentialGroup()
                        .addGroup(jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jpCutLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                                .addComponent(txtStockItem, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpCutLayout.createSequentialGroup()
                                .addGroup(jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtSleeve)
                                    .addComponent(cmbbxOperator, 0, 161, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpCutLayout.createSequentialGroup()
                                .addGroup(jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtQuantity, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                                    .addComponent(txtCutAmount))))
                        .addGap(10, 10, 10))
                    .addGroup(jpCutLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpCutLayout.createSequentialGroup()
                        .addGroup(jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblTimer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCutAction, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jpCutLayout.setVerticalGroup(
            jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpCutLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(12, 12, 12)
                .addGroup(jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbbxOperator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtSleeve, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtStockItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jpCutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtCutAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnCutAction, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTimer)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlHeader.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153), 2));

        txtStockItemSearch.setToolTipText("Enter RP-Code / SO or PO's ID or Description");

        btnStockItemSearch.setText("OK");
        btnStockItemSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStockItemSearchActionPerformed(evt);
            }
        });

        btnHistory.setText("History");
        btnHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHistoryActionPerformed(evt);
            }
        });

        btnSleeveSearch.setText("OK");
        btnSleeveSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSleeveSearchActionPerformed(evt);
            }
        });

        btnHistory1.setText("Finished");
        btnHistory1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHistory1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtStockItemSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnStockItemSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 210, Short.MAX_VALUE)
                .addComponent(btnSleeveSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSleeveSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(196, 196, 196)
                .addComponent(btnHistory1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnHistory)
                .addContainerGap())
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtStockItemSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnStockItemSearch)
                    .addComponent(btnHistory)
                    .addComponent(txtSleeveSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSleeveSearch)
                    .addComponent(btnHistory1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCenter.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        pnlCenter.setPreferredSize(new java.awt.Dimension(400, 0));

        javax.swing.GroupLayout pnlCenterLayout = new javax.swing.GroupLayout(pnlCenter);
        pnlCenter.setLayout(pnlCenterLayout);
        pnlCenterLayout.setHorizontalGroup(
            pnlCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 348, Short.MAX_VALUE)
        );
        pnlCenterLayout.setVerticalGroup(
            pnlCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        pnlWest.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        pnlWest.setPreferredSize(new java.awt.Dimension(400, 862));

        javax.swing.GroupLayout pnlWestLayout = new javax.swing.GroupLayout(pnlWest);
        pnlWest.setLayout(pnlWestLayout);
        pnlWestLayout.setHorizontalGroup(
            pnlWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 266, Short.MAX_VALUE)
        );
        pnlWestLayout.setVerticalGroup(
            pnlWestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        pnlSpacing.setPreferredSize(new java.awt.Dimension(25, 0));

        javax.swing.GroupLayout pnlSpacingLayout = new javax.swing.GroupLayout(pnlSpacing);
        pnlSpacing.setLayout(pnlSpacingLayout);
        pnlSpacingLayout.setHorizontalGroup(
            pnlSpacingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        pnlSpacingLayout.setVerticalGroup(
            pnlSpacingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 539, Short.MAX_VALUE)
        );

        lblSleeveTable.setText("Sleeve");

        lblStockTable.setText("Stock Item");

        cmbbxWeekLimit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Show all", "1 week", "2 week", "3 week", "4 week" }));
        cmbbxWeekLimit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbbxWeekLimitActionPerformed(evt);
            }
        });

        lblWeekLimit.setText("List all within(weeks)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlWest, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlSpacing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblStockTable))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblSleeveTable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 152, Short.MAX_VALUE)
                        .addComponent(lblWeekLimit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbbxWeekLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlCenter, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jpCut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(pnlHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSleeveTable)
                    .addComponent(lblStockTable)
                    .addComponent(cmbbxWeekLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblWeekLimit))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlSpacing, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
                        .addGap(30, 30, 30))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pnlWest, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
                            .addComponent(pnlCenter, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jpCut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnStockItemSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStockItemSearchActionPerformed
        // StockItem search
        String search = txtStockItemSearch.getText();
        if (search.isEmpty()) {
            stockModel.setStockList(Main.allStockData.getOnlyUsable());
        } else {
            StockItemList sil = new StockItemList();

            for (StockItem s : Main.allStockData.getList()) {
                if (s.getCode().contains(search) || s.getBatchId().contains(search)) {
                    sil.add(s);
                }
            }
            if (sil.size() > 0) {
                stockModel.setStockList(sil.getOnlyUsable());
            } else {
                JOptionPane.showMessageDialog(this, "Nothing was found from your query", "Nothing found", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnStockItemSearchActionPerformed

    private void cmbbxWeekLimitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbbxWeekLimitActionPerformed
        int weeks = 0;
        switch (cmbbxWeekLimit.getSelectedIndex()) {
            case 0: // In case of 'View all' is selected...
                sleeveModel.setItemList(Main.allOrderData.getItemList().filterByDone(false)); // Set back the original SalesOrderList.
                break;
            case 4: // In case '4 week' is selected.
                ++weeks;
            case 3: // In case '3 week' is selected.
                ++weeks;
            case 2: // In case '2 week' is selected.
                ++weeks;
            case 1: // In case '1 week' is selected.
                ++weeks;
                sleeveModel.setItemList(Main.allOrderData.getItemList().filterByDone(false).filterByWeek(weeks));
                break;
        }
    }//GEN-LAST:event_cmbbxWeekLimitActionPerformed

    private void btnCutActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCutActionActionPerformed
        if (txtStockItem.getText().length() > 0 &&// Makes sure stock item has been set.
                txtSleeve.getText().length() > 0 &&// Makes sure that sleeve has been set.
                selectedItem.getRemaningCuts() > 0)// Makes sure that it still has something cut.
        {

            if (!cutInProgress) {
                startTime = new Date(); // Sets a start time for tracking the time for the cut
                timerStart();
                btnCutAction.setText("Stop");
                setEnabledTo(false, tblSleeves, tblStock, txtSleeveSearch, txtStockItemSearch, btnSleeveSearch, btnStockItemSearch, cmbbxWeekLimit, cmbbxOperator, txtCutAmount);
                cutInProgress = true;
            } else {
                endTime = new Date(); // Sets a end time for tracking the time for the cut
                timerStop();
                long time = endTime.getTime() - startTime.getTime(); // Is the time it took for the cut to finish
                btnCutAction.setText("Start");

                // Create cut entity based on the information we know.
                Cut cut = new Cut();
                cut.setSleeve(selectedItem);
                cut.setStockItem(selectedStockItem);
                cut.setOperator((Operator) cmbbxOperator.getSelectedItem());
                cut.setTimeSpent(time);
                Date currentDate = new Date();
                cut.setDate(currentDate.getTime());
                cut.setQuantity(Integer.valueOf(txtCutAmount.getText()));
                cut.setWaste(cut.getStockItem().getWidth() - cut.getSleeve().getWidth());
                cut.setArchived(false);
                int remainingQuantity = cut.getSleeve().getRemaningCuts();
                txtQuantity.setText(String.valueOf(remainingQuantity));
                setCutAmount();
                if (remainingQuantity == 0) {
                    selectedItem.setDone(true); // Sets the selected sleeve entity to done.
                    selectedItem.save(); // Updates the selected sleeve (sets it to done) in the database.
                }
                sleeveModel.setItemList(Main.allOrderData.filterByDone(false).filterByStockItem(selectedStockItem));
                cut.save();
                if (remainingQuantity == 0) { // If there are no more cuts to do for that Sleeve.
                    selectedItem.setDone(true); // Sets the selected sleeve entity to done.
                    selectedItem.save(); // Updates the selected sleeve (sets it to done) in the database.
                }
                cut.recordCut(); // Updates a StockItem entity and the database as well.
                stockModel.setStockList(Main.allStockData.getOnlyUsable().filterBySleeve(selectedItem)); // Refreshes the Stock table.
                sleeveModel.setItemList(Main.allOrderData.filterByDone(false).filterByStockItem(selectedStockItem)); // Refreshes the Sleeve table.
                setEnabledTo(true, tblSleeves, tblStock, txtSleeveSearch, txtStockItemSearch, btnSleeveSearch, btnStockItemSearch, cmbbxWeekLimit, cmbbxOperator, txtCutAmount);
                Main.allCuts = ListManager.getAllCuts();
                cutInProgress = false;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please make sure that values are set", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnCutActionActionPerformed

    private void btnHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHistoryActionPerformed
        HistoryFrame chf = new HistoryFrame(this);
    }//GEN-LAST:event_btnHistoryActionPerformed

    private void btnSleeveSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSleeveSearchActionPerformed
        // Sleeve search
        String search = txtSleeveSearch.getText();
        if (search.isEmpty()) {
            sleeveModel.setItemList(Main.allOrderData.getItemList().filterByDone(false));
        } else {
            SalesOrderList sol = new SalesOrderList();

            for (SalesOrder s : Main.allOrderData.getList()) {
                if (s.getDescription().contains(search) || String.valueOf(s.getId()).contains(search)) {
                    if (!sol.hasId(s.getId())) {
                        sol.add(s);
                    }
                }
                for (ProductionOrder p : s.getProductOrderList().getList()) {
                    if (p.getDescription().contains(search) || String.valueOf(p.getId()).contains(search)) {
                        if (!sol.hasId(s.getId())) {
                            sol.add(s);
                        }
                    }
                }
            }
            if (sol.size() > 0) {
                sleeveModel.setItemList(sol.getItemList());
            } else {
                JOptionPane.showMessageDialog(this, "Nothing was found from your query", "Nothing found", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnSleeveSearchActionPerformed

    private void btnHistory1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHistory1ActionPerformed
        FinishedProductionFrame fpf = new FinishedProductionFrame(this);
    }//GEN-LAST:event_btnHistory1ActionPerformed

    /**
     * Takes in multiple Components and sets the 'enable' option for all.
     *
     * @param visibility A boolean.
     * @param comp All the components.
     */
    private void setEnabledTo(boolean visibility, Component... comp) {
        for (Component c : comp) {
            c.setEnabled(visibility);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCutAction;
    private javax.swing.JButton btnHistory;
    private javax.swing.JButton btnHistory1;
    private javax.swing.JButton btnSleeveSearch;
    private javax.swing.JButton btnStockItemSearch;
    private javax.swing.JComboBox cmbbxOperator;
    private javax.swing.JComboBox cmbbxWeekLimit;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jpCut;
    private javax.swing.JLabel lblSleeveTable;
    private javax.swing.JLabel lblStockTable;
    private javax.swing.JLabel lblTimer;
    private javax.swing.JLabel lblWeekLimit;
    private javax.swing.JPanel pnlCenter;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlSpacing;
    private javax.swing.JPanel pnlWest;
    private javax.swing.JTextField txtCutAmount;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtSleeve;
    private javax.swing.JTextField txtSleeveSearch;
    private javax.swing.JTextField txtStockItem;
    private javax.swing.JTextField txtStockItemSearch;
    // End of variables declaration//GEN-END:variables
}
