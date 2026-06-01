package controller;

import model.*;
import view.MainView;

import javax.swing.*;
import java.io.File;

public class SpreadsheetController {
    private SheetList sheetList;
    private FormulaEngine engine;
    private MainView view;
    private int activeSheetIndex;

    public SpreadsheetController() {
        sheetList = new SheetList();
        engine = new FormulaEngine(sheetList);
        activeSheetIndex = 0;
    }

    public void attachView(MainView view) {
        this.view = view;
    }

    public SheetList getSheetList() { return sheetList; }

    public Sheet getActiveSheet() {
        return sheetList.getSheet(activeSheetIndex);
    }

    public int getActiveSheetIndex() { return activeSheetIndex; }

    public void setActiveSheet(int index) {
        if (index >= 0 && index < sheetList.size()) {
            activeSheetIndex = index;
            view.refreshTable();
            view.refreshSheetTabs();
        }
    }

    public void addSheet() {
        sheetList.addSheet();
        view.refreshSheetTabs();
        setActiveSheet(sheetList.size() - 1);
    }

    public void removeSheet(int index) {
        if (!sheetList.removeSheet(index)) {
            view.showError("No se puede eliminar la única hoja.");
            return;
        }
        if (activeSheetIndex >= sheetList.size())
            activeSheetIndex = sheetList.size() - 1;
        view.refreshTable();
        view.refreshSheetTabs();
    }

    public void applyFormula(int row, int col, String formula) {
        if (row < 0 || col < 0) {
            view.showError("Selecciona una celda primero.");
            return;
        }
        String display = engine.evaluate(formula, getActiveSheet().getMatrix());
        getActiveSheet().getMatrix().setValue(row, col, formula, display);
        view.refreshTable();
    }

    public String getRawValue(int row, int col) {
        return getActiveSheet().getMatrix().getRawValue(row, col);
    }

    public String getDisplayValue(int row, int col) {
        return getActiveSheet().getMatrix().getDisplayValue(row, col);
    }

    public FormulaEngine getEngine() { return engine; }

    public void saveToFile(File file) {
        try {
            PersistenceManager.save(sheetList, file);
            view.showInfo("Archivo guardado correctamente.");
        } catch (Exception e) {
            view.showError("Error al guardar: " + e.getMessage());
        }
    }

    public void loadFromFile(File file) {
        try {
            PersistenceManager.load(sheetList, file, engine);
            activeSheetIndex = 0;
            view.refreshTable();
            view.refreshSheetTabs();
            view.showInfo("Archivo cargado correctamente.");
        } catch (Exception e) {
            view.showError("Error al cargar: " + e.getMessage());
        }
    }
}
