package view;

import controller.SpreadsheetController;
import model.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class HashView extends JDialog {
    private HashTable hashTable;
    private JTable displayTable;
    private HashDisplayModel tableModel;
    private JTextField inputField;
    private JLabel statusLabel;

    private final List<String> insertedKeys = new ArrayList<>();

    public HashView(Frame owner, FormulaEngine engine, OrthoMatrix currentMatrix) {
        super(owner, "Tabla Hash", false);
        hashTable = new HashTable();
        buildUI();
        setSize(700, 500);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void buildUI() {
        setLayout(new BorderLayout(6, 6));

        JPanel topPanel = new JPanel(new BorderLayout(4, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));

        JLabel lbl = new JLabel("Dato a insertar: ");
        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JButton btnInsert = new JButton("Insertar");
        JButton btnClear = new JButton("Limpiar");

        btnInsert.addActionListener(e -> insertKey());
        btnClear.addActionListener(e -> clearAll());
        inputField.addActionListener(e -> insertKey());

        JPanel btnArea = new JPanel(new GridLayout(1, 2, 4, 0));
        btnArea.add(btnInsert);
        btnArea.add(btnClear);

        topPanel.add(lbl, BorderLayout.WEST);
        topPanel.add(inputField, BorderLayout.CENTER);
        topPanel.add(btnArea, BorderLayout.EAST);

        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 8));
        statusLabel.setForeground(new Color(60, 100, 180));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC));

        tableModel = new HashDisplayModel();
        displayTable = new JTable(tableModel);
        displayTable.setRowHeight(22);
        displayTable.setFont(new Font("Monospaced", Font.PLAIN, 13));
        displayTable.getTableHeader().setReorderingAllowed(false);
        displayTable.setGridColor(new Color(210, 210, 210));

        // Column A wider
        displayTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        displayTable.getColumnModel().getColumn(1).setPreferredWidth(160);

        JScrollPane scroll = new JScrollPane(displayTable);

        JPanel bottomInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomInfo.add(new JLabel("Función hash: h(k) = (k[0]*31^n + ... + k[n]) mod capacidad  —  Encadenamiento separado"));

        add(topPanel, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.AFTER_LINE_ENDS);
        add(scroll, BorderLayout.CENTER);
        add(bottomInfo, BorderLayout.SOUTH);
    }

    private void insertKey() {
        String key = inputField.getText().trim();
        if (key.isEmpty()) return;

        if (hashTable.contains(key)) {
            statusLabel.setText("'" + key + "' ya existe en la tabla (índice " + hashTable.getIndex(key) + ")");
            inputField.selectAll();
            return;
        }

        hashTable.insert(key);
        insertedKeys.add(key);
        statusLabel.setText("Insertado: '" + key + "'  →  índice " + hashTable.getIndex(key));
        inputField.setText("");
        tableModel.refresh(hashTable);
        inputField.requestFocus();
    }

    private void clearAll() {
        hashTable = new HashTable();
        insertedKeys.clear();
        statusLabel.setText(" ");
        tableModel.refresh(hashTable);
    }

    private static class HashDisplayModel extends AbstractTableModel {
        private List<Object[]> rows = new ArrayList<>();
        private static final String[] COL_NAMES = {"A", "Índice", "Cadena (colisiones)", "Estado"};

        void refresh(HashTable ht) {
            rows.clear();
            String[][] snapshot = ht.snapshot();
            for (int i = 0; i < snapshot.length; i++) {
                String[] chain = snapshot[i];
                if (chain.length == 0) {
                    rows.add(new Object[]{i + 1, i, "(vacío)", ""});
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < chain.length; j++) {
                        if (j > 0) sb.append(" → ");
                        sb.append(chain[j]);
                    }
                    String estado = chain.length > 1 ? "⚡ colisión" : "";
                    rows.add(new Object[]{i + 1, i, sb.toString(), estado});
                }
            }
            fireTableDataChanged();
        }

        public int getRowCount() { return rows.size(); }
        public int getColumnCount() { return COL_NAMES.length; }
        public String getColumnName(int c) { return COL_NAMES[c]; }
        public boolean isCellEditable(int r, int c) { return false; }

        public Object getValueAt(int r, int c) {
            if (r >= rows.size()) return "";
            return rows.get(r)[c];
        }

        public Class<?> getColumnClass(int c) {
            return String.class;
        }
    }
}
