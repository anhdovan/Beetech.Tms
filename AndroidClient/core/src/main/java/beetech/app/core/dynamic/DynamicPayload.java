package beetech.app.core.dynamic;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DynamicPayload implements Serializable {
    public DynamicSchema schema;
    public List<DynamicEntity> entities;
    public Map<String, Object> meta;

    public static DynamicPayload parse(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, DynamicPayload.class);
    }
}
