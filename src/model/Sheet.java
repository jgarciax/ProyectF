package model;

public class Sheet {
    private String name;
    private OrthoMatrix matrix;
    public Sheet next;

    public Sheet(String name, int rows, int cols) {
        this.name = name;
        this.matrix = new OrthoMatrix(rows, cols);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public OrthoMatrix getMatrix() { return matrix; }
}
