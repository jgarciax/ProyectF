package model;

public class OrthoMatrix {
    private Cell[] rowHeaderCells;
    private Cell[] columnHeaderCells;
    private int rowCount;
    private int columnCount;

    public OrthoMatrix(int rows, int cols) {
        this.rowCount = rows;
        this.columnCount = cols;
        rowHeaderCells = new Cell[rows];
        columnHeaderCells = new Cell[cols];

        for (int rowIndex = 0; rowIndex < rows; rowIndex++)
            rowHeaderCells[rowIndex] = new Cell(rowIndex, -1);
        for (int columnIndex = 0; columnIndex < cols; columnIndex++)
            columnHeaderCells[columnIndex] = new Cell(-1, columnIndex);
    }

    public Cell getCell(int row, int col) {
        if (row < 0 || row >= rowCount || col < 0 || col >= columnCount) return null;

        Cell currentCellInRow = rowHeaderCells[row].right;
        while (currentCellInRow != null) {
            if (currentCellInRow.getCol() == col) return currentCellInRow;
            if (currentCellInRow.getCol() > col) break;
            currentCellInRow = currentCellInRow.right;
        }
        return null;
    }

    public Cell getOrCreate(int row, int col) {
        if (row < 0 || row >= rowCount || col < 0 || col >= columnCount) return null;

        Cell existingCell = getCell(row, col);
        if (existingCell != null) return existingCell;

        Cell cellToInsert = new Cell(row, col);

        // Insert into row linked list (sorted by col)
        Cell previousCellInRow = rowHeaderCells[row];
        while (previousCellInRow.right != null && previousCellInRow.right.getCol() < col)
            previousCellInRow = previousCellInRow.right;
        cellToInsert.right = previousCellInRow.right;
        previousCellInRow.right = cellToInsert;

        // Insert into col linked list (sorted by row)
        Cell previousCellInColumn = columnHeaderCells[col];
        while (previousCellInColumn.down != null && previousCellInColumn.down.getRow() < row)
            previousCellInColumn = previousCellInColumn.down;
        cellToInsert.down = previousCellInColumn.down;
        previousCellInColumn.down = cellToInsert;

        return cellToInsert;
    }

    public void setValue(int row, int col, String rawValue, String displayValue) {
        Cell targetCell = getOrCreate(row, col);
        if (targetCell != null) {
            targetCell.setRawValue(rawValue);
            targetCell.setDisplayValue(displayValue);
        }
    }

    public String getDisplayValue(int row, int col) {
        Cell targetCell = getCell(row, col);
        return targetCell == null ? "" : targetCell.getDisplayValue();
    }

    public String getRawValue(int row, int col) {
        Cell targetCell = getCell(row, col);
        return targetCell == null ? "" : targetCell.getRawValue();
    }

    public int getRows() { return rowCount; }
    public int getCols() { return columnCount; }

    // Exports to string array for saving
    public String[][] exportToArray() {
        String[][] data = new String[rowCount][columnCount];
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++)
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++)
                data[rowIndex][columnIndex] = getRawValue(rowIndex, columnIndex);
        return data;
    }

    public void importFromArray(String[][] data, FormulaEngine engine) {
        for (int rowIndex = 0; rowIndex < Math.min(rowCount, data.length); rowIndex++)
            for (int columnIndex = 0; columnIndex < Math.min(columnCount, data[rowIndex].length); columnIndex++)
                if (data[rowIndex][columnIndex] != null && !data[rowIndex][columnIndex].isEmpty())
                    setValue(rowIndex, columnIndex, data[rowIndex][columnIndex], engine.evaluate(data[rowIndex][columnIndex], this));
    }
}
