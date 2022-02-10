package pl.edu.pw.ee;

public class DictKey {
    int key;
    byte by;

    public DictKey(int key, byte by) {
        this.key = key;
        this.by = by;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.key;
        hash = 17 * hash + this.by;
        return hash;
    }

    public boolean equals(Object o) {
        return o instanceof DictKey && ((DictKey)o).key == key && ((DictKey)o).by == by;
    }
}
