package model;

public class HashTable {
    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    private Entry[] buckets;
    private int capacity;
    private int count;

    public HashTable() {
        capacity = DEFAULT_CAPACITY;
        buckets = new Entry[capacity];
        count = 0;
    }

    public void insert(String key) {
        if ((double) count / capacity > LOAD_FACTOR) rehash();
        int idx = hash(key);
        Entry entry = new Entry(key, idx);

        if (buckets[idx] == null) {
            buckets[idx] = entry;
        } else {
            Entry cur = buckets[idx];
            while (cur.next != null) {
                if (cur.key.equals(key)) return; // already present
                cur = cur.next;
            }
            if (!cur.key.equals(key)) cur.next = entry;
        }
        count++;
    }

    public boolean contains(String key) {
        int idx = hash(key);
        Entry cur = buckets[idx];
        while (cur != null) {
            if (cur.key.equals(key)) return true;
            cur = cur.next;
        }
        return false;
    }

    public int getIndex(String key) {
        return hash(key);
    }

    public int getCapacity() { return capacity; }

    // Returns all entries per bucket for display
    public String[][] snapshot() {
        String[][] result = new String[capacity][];
        for (int i = 0; i < capacity; i++) {
            int chainLen = chainLength(i);
            result[i] = new String[chainLen];
            Entry cur = buckets[i];
            int j = 0;
            while (cur != null) {
                result[i][j++] = cur.key;
                cur = cur.next;
            }
        }
        return result;
    }

    private int chainLength(int idx) {
        int len = 0;
        Entry cur = buckets[idx];
        while (cur != null) { len++; cur = cur.next; }
        return len;
    }

    private int hash(String key) {
        long h = 0;
        for (char c : key.toCharArray())
            h = (h * 31 + c) % capacity;
        return (int) Math.abs(h);
    }

    private void rehash() {
        int oldCap = capacity;
        capacity *= 2;
        Entry[] oldBuckets = buckets;
        buckets = new Entry[capacity];
        count = 0;

        for (int i = 0; i < oldCap; i++) {
            Entry cur = oldBuckets[i];
            while (cur != null) {
                insert(cur.key);
                cur = cur.next;
            }
        }
    }

    public static class Entry {
        String key;
        int bucketIndex;
        Entry next;

        Entry(String key, int bucketIndex) {
            this.key = key;
            this.bucketIndex = bucketIndex;
        }
    }
}
