package beetech.tms.android.data.models;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private int id;
    private String customerCode;
    private String name;
    private String phone;
    private String email;
    private String type;
    private List<TextileItem> items = new ArrayList<>();

    public Customer() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<TextileItem> getItems() {
        return items;
    }

    public void setItems(List<TextileItem> items) {
        this.items = items;
    }
}
