package model;

public class OrthoMatrix {
    private Cell[] rowHeaders;
    private Cell[] colHeaders;
    private int rows;
    private int cols;

    public OrthoMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        rowHeaders = new Cell[rows];
        colHeaders = new Cell[cols];

        for (int r = 0; r < rows; r++)
            rowHeaders[r] = new Cell(r, -1);
        for (int c = 0; c < cols; c++)
            colHeaders[c] = new Cell(-1, c);
    }

    public Cell getCell(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return null;

        Cell cur = rowHeaders[row].right;
        while (cur != null) {
            if (cur.getCol() == col) return cur;
            if (cur.getCol() > col) break;
            cur = cur.right;
        }
        return null;
    }

    public Cell getOrCreate(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return null;

        Cell existing = getCell(row, col);
        if (existing != null) return existing;

        Cell newCell = new Cell(row, col);

        // Insert into row linked list (sorted by col)
        Cell rowPrev = rowHeaders[row];
        while (rowPrev.right != null && rowPrev.right.getCol() < col)
            rowPrev = rowPrev.right;
        newCell.right = rowPrev.right;
        rowPrev.right = newCell;

        // Insert into col linked list (sorted by row)
        Cell colPrev = colHeaders[col];
        while (colPrev.down != null && colPrev.down.getRow() < row)
            colPrev = colPrev.down;
        newCell.down = colPrev.down;
        colPrev.down = newCell;

        return newCell;
    }

    public void setValue(int row, int col, String rawValue, String displayValue) {
        Cell c = getOrCreate(row, col);
        if (c != null) {
            c.setRawValue(rawValue);
            c.setDisplayValue(displayValue);
        }
    }

    public String getDisplayValue(int row, int col) {
        Cell c = getCell(row, col);
        return c == null ? "" : c.getDisplayValue();
    }

    public String getRawValue(int row, int col) {
        Cell c = getCell(row, col);
        return c == null ? "" : c.getRawValue();
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    // Exports to string array for saving
    public String[][] exportToArray() {
        String[][] data = new String[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                data[r][c] = getRawValue(r, c);
        return data;
    }

    public void importFromArray(String[][] data, FormulaEngine engine) {
        for (int r = 0; r < Math.min(rows, data.length); r++)
            for (int c = 0; c < Math.min(cols, data[r].length); c++)
                if (data[r][c] != null && !data[r][c].isEmpty())
                    setValue(r, c, data[r][c], engine.evaluate(data[r][c], this));
    }
}
