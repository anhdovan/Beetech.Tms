package beetech.app.core.dynamic;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamicSchema implements Serializable {
    public String name;
    public String displayName;
    public String layout;
    public int typeId;
    public int version;
    public List<DynamicSchemaGroup> groups;
}

