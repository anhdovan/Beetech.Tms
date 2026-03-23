package beetech.app.core.dto;
//this class for sending barcode or RFID code to server
public class ScannedItemDto {
    public int id;
    public String code = "";//24 hex digits qrcode & rfid code
    public String internalCode = "";
    public int LocationId;
    public String location;
    public  int categoryId;
    public String category;
    public int departmentId;
    public String department;
    public int manufacturerId;
    public String manufacturer;
    public String name = "";
    public String notes;
    public String condition = "Good";
}

