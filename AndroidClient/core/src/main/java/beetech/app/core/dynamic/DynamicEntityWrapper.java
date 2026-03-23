package beetech.app.core.dynamic;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DynamicEntityWrapper implements Serializable {
    private JsonObject internalJson;

    public DynamicEntityWrapper(JsonObject json) {
        this.internalJson = json;
        if (this.internalJson == null) {
            this.internalJson = new JsonObject();
        }
    }

    public DynamicEntityWrapper(Object genericObject) {
        if (genericObject == null) {
            this.internalJson = new JsonObject();
        } else {
            try {
                Gson gson = new Gson();
                JsonElement elem = gson.toJsonTree(genericObject);
                if (elem.isJsonObject()) {
                    this.internalJson = elem.getAsJsonObject();
                } else {
                    this.internalJson = new JsonObject();
                }
            } catch (Exception e) {
                this.internalJson = new JsonObject();
            }
        }
    }

    public JsonObject getJson() {
        return internalJson;
    }

    // Standard ID accessor
    public int getId() {
        return getInt("Id");
    }

    public void setId(int id) {
        internalJson.addProperty("Id", id);
    }

    // Typed Accessors
    public String getString(String key) {
        if (internalJson.has(key) && !internalJson.get(key).isJsonNull()) {
            return internalJson.get(key).getAsString();
        }
        return "";
    }

    public int getInt(String key) {
        if (internalJson.has(key) && !internalJson.get(key).isJsonNull()) {
            try {
                return internalJson.get(key).getAsInt();
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    public double getDouble(String key) {
        if (internalJson.has(key) && !internalJson.get(key).isJsonNull()) {
            try {
                return internalJson.get(key).getAsDouble();
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public boolean getBool(String key) {
        if (internalJson.has(key) && !internalJson.get(key).isJsonNull()) {
             try {
                return internalJson.get(key).getAsBoolean();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public boolean has(String key) {
        return internalJson.has(key);
    }
    
    public Set<String> keys() {
        return internalJson.keySet();
    }

    public void set(String key, String value) {
        internalJson.addProperty(key, value);
    }
    
    public void set(String key, Number value) {
        internalJson.addProperty(key, value);
    }
    
    public void set(String key, Boolean value) {
        internalJson.addProperty(key, value);
    }

    public DynamicEntity toDynamicEntity(int typeId) {
        DynamicEntity entity = new DynamicEntity();
        entity.typeId = typeId;
        entity.entityId = getId();
        entity.values = new HashMap<>();

        for (String key : internalJson.keySet()) {
            if (key.equalsIgnoreCase("Id")) continue;

            JsonElement elem = internalJson.get(key);
            if (elem.isJsonNull()) {
                entity.values.put(key, null);
            } else if (elem.isJsonPrimitive()) {
                JsonPrimitive p = elem.getAsJsonPrimitive();
                if (p.isBoolean()) entity.values.put(key, p.getAsBoolean());
                else if (p.isNumber()) entity.values.put(key, p.getAsDouble());
                else if (p.isString()) entity.values.put(key, p.getAsString());
            }
        }
        return entity;
    }

    @Override
    public String toString() {
        return internalJson.toString();
    }
}
