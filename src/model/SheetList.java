package model;

public class SheetList {
    private Sheet head;
    private int size;
    private int sheetCounter;

    public SheetList() {
        size = 0;
        sheetCounter = 0;
        addSheet();
    }

    public Sheet addSheet() {
        sheetCounter++;
        Sheet s = new Sheet("Hoja " + sheetCounter, 100, 26);
        if (head == null) {
            head = s;
        } else {
            Sheet cur = head;
            while (cur.next != null) cur = cur.next;
            cur.next = s;
        }
        size++;
        return s;
    }

    public boolean removeSheet(int index) {
        if (size <= 1 || index < 0 || index >= size) return false;
        if (index == 0) {
            head = head.next;
        } else {
            Sheet prev = getSheet(index - 1);
            if (prev == null) return false;
            prev.next = prev.next.next;
        }
        size--;
        return true;
    }

    public Sheet getSheet(int index) {
        if (index < 0 || index >= size) return null;
        Sheet cur = head;
        for (int i = 0; i < index; i++) cur = cur.next;
        return cur;
    }

    public int size() { return size; }

    public int indexOf(Sheet target) {
        Sheet cur = head;
        int i = 0;
        while (cur != null) {
            if (cur == target) return i;
            cur = cur.next;
            i++;
        }
        return -1;
    }
}
