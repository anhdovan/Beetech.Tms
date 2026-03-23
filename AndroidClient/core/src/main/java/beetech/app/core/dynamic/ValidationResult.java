package beetech.app.core.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationResult {
    public final List<ValidationError> errors = new ArrayList<>();
    public boolean isValid() { return errors.isEmpty(); }

    public String flattenErrors() {
        return errors.stream()
                .map(ValidationError::toString) // or use a specific getter like getMessage()
                .collect(Collectors.joining("\n"));
    }

}
