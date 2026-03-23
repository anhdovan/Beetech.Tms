package beetech.app.core.dto;

import java.io.Serializable;

//@Entity
public  class Location implements  Serializable {
  //  @Id
    public long id;
    public long parentId;
    public  String name;
    public String barCode;
    public  String rfidCode;
    public  String coordinates;
}
