package beetech.app.core.dynamic;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class DynamicEntity implements Serializable {
    //@SerializedName("Id")
    public int entityId;

    //@SerializedName("TypeId")
    public int typeId;

    //@SerializedName("Values")
    public Map<String, Object> values;

    @SuppressWarnings("unchecked")
    public <T> T toEntity(Class<T> clazz) {
        try {
            DynamicEntity data = this;
            // Create new instance of target class
            T instance = clazz.getDeclaredConstructor().newInstance();

            // Assign common props if they exist
            try {
                var f = clazz.getDeclaredField("id");
                f.setAccessible(true);
                f.set(instance, data.entityId);
            } catch (NoSuchFieldException ignored) {}

            try {
                var f = clazz.getDeclaredField("typeId");
                f.setAccessible(true);
                f.set(instance, data.typeId);
            } catch (NoSuchFieldException ignored) {}

            // Assign values map to matching fields
            if (data.values != null) {
                for (Map.Entry<String, Object> entry : data.values.entrySet()) {
                    String key = Character.toLowerCase(entry.getKey().charAt(0)) + entry.getKey().substring(1);
                    Object value = entry.getValue();

                    try {
                        var field = clazz.getDeclaredField(key);
                        field.setAccessible(true);

                        // Handle primitive types and casting
                        Class<?> fieldType = field.getType();
                        if (value != null && !fieldType.isAssignableFrom(value.getClass())) {
                            // Try simple type conversions
                            if (fieldType == int.class || fieldType == Integer.class) {
                                value = (int) Double.parseDouble(value.toString());
                            } else if (fieldType == long.class || fieldType == Long.class) {
                                value = (long) Double.parseDouble(value.toString());
                            } else if (fieldType == double.class || fieldType == Double.class) {
                                value = Double.parseDouble(value.toString());
                            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                                value = Boolean.parseBoolean(value.toString());
                            } else if (fieldType == String.class) {
                                value = value.toString();
                            }
                        }

                        field.set(instance, value);
                    } catch (NoSuchFieldException ignored) {
                        // Skip if field not found in target class
                    }
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DynamicEntity to " + clazz.getSimpleName(), e);
        }
    }
}

