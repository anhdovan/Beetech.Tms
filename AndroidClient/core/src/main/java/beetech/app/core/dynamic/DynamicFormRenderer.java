package beetech.app.core.dynamic;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DynamicFormRenderer {

    public interface ValueSink {
        void onValueChanged(String fieldName, Object value);
    }

    public static class RenderResult {
        public View root;
        public Map<String, View> fieldViews = new HashMap<>();
    }

    private final LookupRegistry registry;

    public DynamicFormRenderer(LookupRegistry registry) {
        this.registry = registry;
    }

    public RenderResult render(Context ctx, DynamicSchema schema, ViewGroup root,
                               Map<String, Object> initialValues, ValueSink sink) {
        RenderResult result = new RenderResult();
        LinearLayout container = new LinearLayout(ctx);
        container.setOrientation(LinearLayout.VERTICAL);

        // Sort groups: default group first, then by displayOrder
        List<DynamicSchemaGroup> groups = new ArrayList<>(schema.groups.stream()
                .filter(g -> g.disabled == false).collect(Collectors.toList()));
        groups.sort((g1, g2) -> {
            if (g1.groupId == 0) return -1;
            if (g2.groupId == 0) return 1;
            return Integer.compare(g1.displayOrder, g2.displayOrder);
        });

        for (DynamicSchemaGroup group : groups) {
            // Fieldset header
            TextView header = new TextView(ctx);
            header.setText(group.displayName);
            header.setTextSize(18);
            header.setPadding(0, 24, 0, 8);
            container.addView(header);

            // Group container
            LinearLayout groupLayout = new LinearLayout(ctx);
            groupLayout.setOrientation(LinearLayout.VERTICAL);
            groupLayout.setPadding(16, 8, 16, 8);

            for (DynamicSchemaField f : group.fields) {
                if(f.disabled) continue;
                View fieldView = createField(ctx, f, initialValues, sink);
                groupLayout.addView(fieldView);
                result.fieldViews.put(f.name, fieldView);
            }

            container.addView(groupLayout);
        }

        result.root = container;
        root.addView(container);
        return result;
    }

    // Collect values back
    public Map<String, Object> collectValues(RenderResult rr, DynamicSchema schema) {
        Map<String, Object> values = new HashMap<>();
        for (DynamicSchemaGroup g : schema.groups) {
            for (DynamicSchemaField f : g.fields) {
                values.put(f.name, readValue(rr.fieldViews.get(f.name), f));
            }
        }
        return values;
    }

    // Validate values
    public ValidationResult validate(RenderResult rr, DynamicSchema schema) {
        ValidationResult res = new ValidationResult();
        for (DynamicSchemaGroup g : schema.groups) {
            for (DynamicSchemaField f : g.fields) {
                Object value = readValue(rr.fieldViews.get(f.name), f);
                if (f.isRequired && (value == null || (value instanceof String && ((String) value).isEmpty()))) {
                    res.errors.add(new ValidationError(f.name, f.requiredMessage != null ? f.requiredMessage : f.displayName + " is required"));
                }
            }
        }
        return res;
    }

    // Create field control
    private View createField(Context ctx, DynamicSchemaField f, Map<String, Object> initialValues, ValueSink sink) {
        LinearLayout wrapper = new LinearLayout(ctx);
        wrapper.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(ctx);
        label.setText(f.displayName + (f.isRequired ? " *" : ""));
        wrapper.addView(label);

        Object initial = initialValues != null ? initialValues.get(f.name) : null;
        AtomicReference<View> control = new AtomicReference<>();

        switch (f.controlType.toLowerCase()) {
            case "textbox":
            case "textarea":
                // Use AppCompatEditText with global style
                AppCompatEditText editText = new AppCompatEditText(ctx, null);

                editText.setHint(f.displayName);

                // Apply initial value if present
                Object value = initialValues.get(f.name);
                if (value != null) {
                    editText.setText(String.valueOf(value));
                }

                // Listen for changes
                editText.addTextChangedListener(new SimpleTextWatcher(null) {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        sink.onValueChanged(f.name, s.toString());
                    }
                });

                control.set(editText);


                /*

                EditText et = new EditText(ctx);

                et.setHint(f.placeholder);
                if (initial instanceof String) et.setText((String) initial);
                et.addTextChangedListener(new SimpleTextWatcher(s -> {
                    if (sink != null) sink.onValueChanged(f.name, s);
                }));
                control.set(et);
                 */
                break;
            case "numeric":
                EditText num = new EditText(ctx);
                num.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                if (initial != null) num.setText(initial.toString());
                num.addTextChangedListener(new SimpleTextWatcher(s -> {
                    if (sink != null) sink.onValueChanged(f.name, parseDoubleOrNull(s));
                }));
                control.set(num);
                break;
            case "dropdown":
                Spinner sp = new Spinner(ctx);
                List<String> opts = f.allowedValues != null ? f.allowedValues : Collections.emptyList();
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, opts);
                sp.setAdapter(adapter);
                sp.setOnItemSelectedListener(new SimpleItemSelectedListener(pos -> {
                    if (sink != null) sink.onValueChanged(f.name, opts.get(pos));
                }));
                control.set(sp);
                break;
            case "checkbox":
                CheckBox cb = new CheckBox(ctx);
                cb.setChecked(initial instanceof Boolean && (Boolean) initial);
                cb.setOnCheckedChangeListener((b, checked) -> {
                    if (sink != null) sink.onValueChanged(f.name, checked);
                });
                control.set(cb);
                break;
            case "datepicker":
                EditText date = new EditText(ctx);
                date.setFocusable(false);
                date.setOnClickListener(v -> {
                    Calendar c = Calendar.getInstance();
                    DatePickerDialog dp = new DatePickerDialog(ctx, (view, y, m, d) -> {
                        String val = String.format("%04d-%02d-%02d", y, m + 1, d);
                        date.setText(val);
                        if (sink != null) sink.onValueChanged(f.name, val);
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                    dp.show();
                });
                control.set(date);
                break;
            case "objectlookup":
                Spinner lookup = new Spinner(ctx);
                List<LookupItem> items;
                if (f.refStaticType != null) {
                    LookupProvider p = registry.getProvider(f.refStaticType);
                    if (p != null) 
                        p.getItemsAsync(its -> {
                            ArrayAdapter<LookupItem> ad = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, its);
                            lookup.setAdapter(ad);
                            lookup.setOnItemSelectedListener(new SimpleItemSelectedListener(pos -> {
                                LookupItem sel = its.get(pos);
                                if (sink != null) sink.onValueChanged(f.name, sel.id);
                            }));
                            control.set(lookup);
                            wrapper.addView(control.get());
                        });
                    else {
                        items = Collections.emptyList();
                    }
                } else {
                    items = Collections.emptyList();
                }
                
                break;
            default:
                EditText def = new EditText(ctx);
                if (initial instanceof String) def.setText((String) initial);
                control.set(def);
                break;
        }
        if(control!=null&&control.get()!=null)
            wrapper.addView(control.get());
        return wrapper;
    }

    private Object readValue(View wrapper, DynamicSchemaField f) {
        if (!(wrapper instanceof LinearLayout)) return null;
        LinearLayout ll = (LinearLayout) wrapper;
        if (ll.getChildCount() < 2) return null;
        View control = ll.getChildAt(1);

        switch (f.controlType.toLowerCase()) {
            case "textbox":
            case "textarea":
                return control instanceof EditText ? ((EditText) control).getText().toString() : null;
            case "numeric":
                return control instanceof EditText ? parseDoubleOrNull(((EditText) control).getText().toString()) : null;
            case "dropdown":
                return control instanceof Spinner ? ((Spinner) control).getSelectedItem() : null;
            case "checkbox":
                return control instanceof CheckBox && ((CheckBox) control).isChecked();
            case "datepicker":
                return control instanceof EditText ? ((EditText) control).getText().toString() : null;
            case "objectlookup":
                if (control instanceof Spinner) {
                    Object item = ((Spinner) control).getSelectedItem();
                    if (item instanceof LookupItem) return ((LookupItem) item).id;
                }
                return null;
            default:
                return control instanceof EditText ? ((EditText) control).getText().toString() : null;
        }
    }

    private static Double parseDoubleOrNull(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }


    private static class SimpleTextWatcher implements TextWatcher {
        private final Consumer<String> onChange;

        SimpleTextWatcher(Consumer<String> onChange) {
            this.onChange = onChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (onChange != null) onChange.accept(s.toString());
        }
    }

    private static class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
        private final Consumer<Integer> onSelected;

        SimpleItemSelectedListener(Consumer<Integer> onSelected) {
            this.onSelected = onSelected;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (onSelected != null) onSelected.accept(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
}


