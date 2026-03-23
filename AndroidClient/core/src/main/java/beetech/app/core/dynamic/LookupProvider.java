package beetech.app.core.dynamic;

import androidx.arch.core.util.Function;
import androidx.core.util.Consumer;
import androidx.core.util.Supplier;

import java.util.ArrayList;
import java.util.List;

public interface LookupProvider {
    String getTypeName();
    //List<LookupItem> getItems();
    void getItemsAsync(Consumer<List<LookupItem>> callback);
}

