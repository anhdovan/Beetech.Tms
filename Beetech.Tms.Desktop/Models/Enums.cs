namespace Beetech.Tms.Desktop.Models;

public enum TmsReaderRole
{
    Standard = 0,        // Full access
    InboundDirty = 1,    // Laundry Receive
    InboundHotel = 2,    // Hotel Receive
    OutboundDirty = 3,   // Hotel Send
    OutboundLaundry = 4, // Laundry Send
    TagRegistry = 5,     // Registration
    InventoryAudit = 6,
    Condemned = 7,
    Washing = 8,
    Drying = 9,
    Ironing = 10,
    Folding = 11,
    Packing = 12,
    Return = 13,
    PackingMonitor = 14
}
