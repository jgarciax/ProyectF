package model;

public class Cell {
    private int row;
    private int col;
    private String rawValue;
    private String displayValue;

    public Cell right;
    public Cell down;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.rawValue = "";
        this.displayValue = "";
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public String getRawValue() { return rawValue; }
    public void setRawValue(String v) { rawValue = v; }

    public String getDisplayValue() { return displayValue; }
    public void setDisplayValue(String v) { displayValue = v; }
}
