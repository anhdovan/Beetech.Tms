package beetech.app.core.dynamic;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DynamicSchemaField implements Serializable {
    public int attributeId;
    public String name;
    public String displayName;
    public String dataType;
    public String controlType;
    public boolean isRequired;
    public Integer orderIndex;
    public Integer columnSpan;
    public String placeholder;
    public String tooltip;
    public String icon;
    public boolean disabled = false;

    public List<String> allowedValues;
    public Integer refTypeId;
    public String refStaticType;

    public Integer minLength;
    public Integer maxLength;
    public Double minValue;
    public Double maxValue;
    public String regexPattern;
    public String requiredMessage;
    public String regexMessage;
    public String lengthMessage;
    public String rangeMessage;

    public Map<String,Object> controlSettings;
}
