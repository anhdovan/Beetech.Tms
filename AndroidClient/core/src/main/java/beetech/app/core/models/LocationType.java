package beetech.app.core.models;

/**
 * Location
 */

public enum LocationType {
  Warehouse(4),
  Floor(6),
  Zone(7),
  Aisle(10),
  Rack_Shelf(8),
  Level(6),
  Bin(11, true);

  private final int value;
  private boolean isContainer;
  LocationType(int value, boolean isContainer) {

    this.value = value;
    this.isContainer = isContainer;
  }
  LocationType(int value) {

    this(value, false);
  }

  public int getValue() {
    return value;
  }

  public static LocationType fromValue(int value) {
    for (LocationType locationType : LocationType.values()) {
      if (locationType.getValue() == value) {
        return locationType;
      }
    }
    return null;
  }

  public boolean getIsContainer() {
    return isContainer;
  }
}