package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PersistenceManager {

    public static void save(SheetList sheetList, File file) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            int size = sheetList.size();
            out.writeInt(size);
            for (int s = 0; s < size; s++) {
                Sheet sheet = sheetList.getSheet(s);
                out.writeUTF(sheet.getName());
                OrthoMatrix m = sheet.getMatrix();
                out.writeInt(m.getRows());
                out.writeInt(m.getCols());
                for (int r = 0; r < m.getRows(); r++) {
                    for (int c = 0; c < m.getCols(); c++) {
                        String raw = m.getRawValue(r, c);
                        out.writeUTF(raw == null ? "" : raw);
                    }
                }
            }
        }
    }

    public static void load(SheetList sheetList, File file, FormulaEngine engine) throws IOException {
        // Clear existing sheets except leave the list empty, then repopulate
        int existing = sheetList.size();
        for (int i = existing - 1; i >= 0; i--)
            sheetList.removeSheet(i);

        // Force-clear: remove all remaining (size may still be 1 due to guard)
        // We'll reconstruct entirely by adding
        while (sheetList.size() > 0) sheetList.removeSheet(0);

        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            int sheetCount = in.readInt();
            for (int s = 0; s < sheetCount; s++) {
                String name = in.readUTF();
                int rows = in.readInt();
                int cols = in.readInt();

                Sheet sheet = sheetList.addSheet();
                sheet.setName(name);

                String[][] data = new String[rows][cols];
                for (int r = 0; r < rows; r++)
                    for (int c = 0; c < cols; c++)
                        data[r][c] = in.readUTF();

                sheet.getMatrix().importFromArray(data, engine);
            }
        }
    }
}
