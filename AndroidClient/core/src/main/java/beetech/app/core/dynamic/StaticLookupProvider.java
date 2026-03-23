package beetech.app.core.dynamic;

import android.os.Handler;
import android.os.Looper;

import java.util.function.Function;
import androidx.core.util.Consumer;
import androidx.core.util.Supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public abstract class StaticLookupProvider<T> implements LookupProvider {
    private final String typeName;
    private final Supplier<List<T>> loader;
    private final Function<T, LookupItem> mapper;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    protected StaticLookupProvider(String typeName,
                                   Supplier<List<T>> loader,
                                   Function<T, LookupItem> mapper) {
        this.typeName = typeName;
        this.loader = loader;
        this.mapper = mapper;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    /**
     * Asynchronous version: runs DAO query off the main thread
     * and posts results back to the UI thread.
     */
    @Override
    public void getItemsAsync(Consumer<List<LookupItem>> callback) {
        executor.execute(() -> {
            List<T> entities = loader.get(); // safe off main thread
            List<LookupItem> items = new ArrayList<>();
            for (T e : entities) {
                items.add(mapper.apply(e));
            }
            mainHandler.post(() -> callback.accept(items));
        });
    }
}
