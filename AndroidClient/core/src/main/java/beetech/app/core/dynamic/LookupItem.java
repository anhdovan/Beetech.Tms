package beetech.app.core.dynamic;

public class LookupItem {
    public final int id;
    public final String displayName;

    public LookupItem(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}