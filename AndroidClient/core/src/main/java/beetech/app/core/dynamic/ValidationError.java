package beetech.app.core.dynamic;

public class ValidationError {
    public final String fieldName;
    public final String message;
    public ValidationError(String fieldName, String message) {
        this.fieldName = fieldName;
        this.message = message;
    }
    @Override
    public String toString() {
        return fieldName + ": " + message;
    }
}
