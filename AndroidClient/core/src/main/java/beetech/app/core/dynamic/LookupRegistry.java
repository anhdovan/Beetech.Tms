package beetech.app.core.dynamic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LookupRegistry {
    private final Map<String, LookupProvider> providers = new HashMap<>();

    @Inject
    public LookupRegistry(Set<LookupProvider> providerSet) {
        for (LookupProvider p : providerSet) {
            providers.put(p.getTypeName().toLowerCase(), p);
        }
    }

    public LookupProvider getProvider(String typeName) {
        return typeName == null ? null : providers.get(typeName.toLowerCase());
    }
}
