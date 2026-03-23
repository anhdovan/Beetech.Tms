package beetech.app.core.dynamic;

import java.io.Serializable;
import java.util.List;

public class DynamicSchemaGroup implements Serializable {
    public int groupId;
    public String name;
    public String displayName;
    public int displayOrder;
    public String icon;
    public boolean disabled = false;
    public List<DynamicSchemaField> fields;
}
