package view;

import controller.SpreadsheetController;
import model.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class MainView extends JFrame {
    private SpreadsheetController ctrl;
    private JTable grid;
    private CustomTableModel tableModel;
    private JTextField formulaBar;
    private JLabel cellLabel;
    private JPanel tabPanel;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private static final int DISPLAY_ROWS = 50;
    private static final int DISPLAY_COLS = 26;

    public MainView(SpreadsheetController ctrl) {
        this.ctrl = ctrl;
        ctrl.attachView(this);
        buildUI();
    }

    private void buildUI() {
        setTitle("Hoja Electrónica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 640);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());

        JPanel top = new JPanel(new BorderLayout(4, 0));
        top.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        cellLabel = new JLabel("   ");
        cellLabel.setPreferredSize(new Dimension(60, 24));
        cellLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)));

        formulaBar = new JTextField();
        formulaBar.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JButton btnApply = new JButton("Aplicar");
        JButton btnReject = new JButton("Rechazar");

        btnApply.addActionListener(e -> commitFormula());
        btnReject.addActionListener(e -> rejectFormula());
        formulaBar.addActionListener(e -> commitFormula());

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        btnPanel.add(btnApply);
        btnPanel.add(btnReject);

        top.add(cellLabel, BorderLayout.WEST);
        top.add(formulaBar, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.EAST);

        tableModel = new CustomTableModel(DISPLAY_ROWS, DISPLAY_COLS);
        grid = new JTable(tableModel);
        grid.setRowHeight(22);
        grid.getTableHeader().setReorderingAllowed(false);
        grid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        grid.setCellSelectionEnabled(true);
        grid.setGridColor(new Color(200, 200, 200));
        grid.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Row header column
        JTable rowHeader = buildRowHeader();
        JScrollPane scroll = new JScrollPane(grid);
        scroll.setRowHeaderView(rowHeader);
        scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JLabel(""));

        grid.getSelectionModel().addListSelectionListener(e -> onSelectionChanged());
        grid.getColumnModel().getSelectionModel().addListSelectionListener(e -> onSelectionChanged());

        tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        tabPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JPanel south = new JPanel(new BorderLayout());
        south.add(tabPanel, BorderLayout.CENTER);

        JButton addTabBtn = new JButton("+");
        addTabBtn.setMargin(new Insets(1, 6, 1, 6));
        addTabBtn.setToolTipText("Agregar hoja");
        addTabBtn.addActionListener(e -> ctrl.addSheet());
        south.add(addTabBtn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        refreshSheetTabs();
        refreshTable();
        setVisible(true);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu menuFile = new JMenu("Archivo");
        JMenuItem miSave = new JMenuItem("Guardar");
        JMenuItem miLoad = new JMenuItem("Abrir");
        JMenuItem miHash = new JMenuItem("Tabla Hash");
        JMenuItem miExit = new JMenuItem("Salir");

        miSave.addActionListener(e -> saveDialog());
        miLoad.addActionListener(e -> loadDialog());
        miHash.addActionListener(e -> openHashView());
        miExit.addActionListener(e -> System.exit(0));

        menuFile.add(miSave);
        menuFile.add(miLoad);
        menuFile.addSeparator();
        menuFile.add(miHash);
        menuFile.addSeparator();
        menuFile.add(miExit);

        JMenu menuInsert = new JMenu("Insertar");
        JMenuItem miSheet = new JMenuItem("Nueva hoja");
        miSheet.addActionListener(e -> ctrl.addSheet());
        menuInsert.add(miSheet);

        JMenu menuHelp = new JMenu("Ayuda");
        JMenuItem miAbout = new JMenuItem("Acerca de");
        miAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Hoja Electrónica\nProgramación III - UMG\n\n" +
            "Fórmulas: =suma(A1,B2)  =mult(A1,B2)\n" +
            "Referencia cruzada: =suma(Hoja1,(2,5),(2,6))",
            "Ayuda", JOptionPane.INFORMATION_MESSAGE));
        menuHelp.add(miAbout);

        bar.add(menuFile);
        bar.add(menuInsert);
        bar.add(menuHelp);
        return bar;
    }

    private JTable buildRowHeader() {
        DefaultTableModel m = new DefaultTableModel(DISPLAY_ROWS, 1) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (int i = 0; i < DISPLAY_ROWS; i++) m.setValueAt(i + 1, i, 0);
        JTable rh = new JTable(m);
        rh.setEnabled(false);
        rh.setBackground(new Color(240, 240, 240));
        rh.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rh.setPreferredScrollableViewportSize(new Dimension(40, 0));
        rh.getColumnModel().getColumn(0).setPreferredWidth(40);
        rh.setRowHeight(22);
        return rh;
    }

    private void onSelectionChanged() {
        int r = grid.getSelectedRow();
        int c = grid.getSelectedColumn();
        if (r >= 0 && c >= 0) {
            selectedRow = r;
            selectedCol = c;
            char col = (char) ('A' + c);
            cellLabel.setText(" " + col + (r + 1));
            formulaBar.setText(ctrl.getRawValue(r, c));
        }
    }

    private void commitFormula() {
        if (selectedRow < 0 || selectedCol < 0) return;
        ctrl.applyFormula(selectedRow, selectedCol, formulaBar.getText());
    }

    private void rejectFormula() {
        if (selectedRow >= 0 && selectedCol >= 0)
            formulaBar.setText(ctrl.getRawValue(selectedRow, selectedCol));
    }

    public void refreshTable() {
        Sheet active = ctrl.getActiveSheet();
        OrthoMatrix m = active.getMatrix();
        for (int r = 0; r < DISPLAY_ROWS; r++)
            for (int c = 0; c < DISPLAY_COLS; c++)
                tableModel.setValueAt(m.getDisplayValue(r, c), r, c);
    }

    public void refreshSheetTabs() {
        tabPanel.removeAll();
        int count = ctrl.getSheetList().size();
        for (int i = 0; i < count; i++) {
            final int idx = i;
            Sheet s = ctrl.getSheetList().getSheet(i);
            JButton tab = new JButton(s.getName());
            tab.setMargin(new Insets(2, 8, 2, 8));

            if (i == ctrl.getActiveSheetIndex()) {
                tab.setBackground(new Color(220, 235, 255));
                tab.setFont(tab.getFont().deriveFont(Font.BOLD));
            }

            tab.addActionListener(e -> ctrl.setActiveSheet(idx));
            tab.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu pop = new JPopupMenu();
                        JMenuItem rename = new JMenuItem("Renombrar");
                        JMenuItem remove = new JMenuItem("Eliminar");
                        rename.addActionListener(ev -> {
                            String name = JOptionPane.showInputDialog(MainView.this,
                                "Nuevo nombre:", s.getName());
                            if (name != null && !name.trim().isEmpty()) {
                                s.setName(name.trim());
                                refreshSheetTabs();
                            }
                        });
                        remove.addActionListener(ev -> ctrl.removeSheet(idx));
                        pop.add(rename);
                        pop.add(remove);
                        pop.show(tab, e.getX(), e.getY());
                    }
                }
            });

            tabPanel.add(tab);
        }
        tabPanel.revalidate();
        tabPanel.repaint();
    }

    private void saveDialog() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar hoja");
        fc.setSelectedFile(new File("hoja.dat"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            ctrl.saveToFile(fc.getSelectedFile());
    }

    private void loadDialog() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Abrir hoja");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            ctrl.loadFromFile(fc.getSelectedFile());
    }

    private void openHashView() {
        new HashView(this, ctrl.getEngine(), ctrl.getActiveSheet().getMatrix());
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class CustomTableModel extends DefaultTableModel {
        private final String[] colNames;

        CustomTableModel(int rows, int cols) {
            super(rows, cols);
            colNames = new String[cols];
            for (int i = 0; i < cols; i++) colNames[i] = String.valueOf((char) ('A' + i));
        }

        public String getColumnName(int col) { return colNames[col]; }
        public boolean isCellEditable(int r, int c) { return false; }
    }
}
