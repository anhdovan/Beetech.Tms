namespace Beetech.Tms.Core.Models;

public enum ItemStatus
{
    Available,
    InUse,
    Soiled,
    Condemned,
    Lost,
    Damaged,
    Missing,
    Relocated,
    Unregistered,
    Washing,
    Drying,
    Ironing,
    Folding,
    Clean,
    Packing,
    Returned
}

public enum TransactionType
{
    Inbound,
    Outbound,
    Delivery,
    Receipt,
    LaundrySend,
    LaundryReceive,
    InternalTransfer,
    Discard,
    InventoryAudit,
    Washing,
    Drying,
    Ironing,
    Folding,
    Packing,
    PackingMonitor,
    Return
}

public enum LocationType
{
    Laundry,
    Hotel,
    Hospital,
    Storage,
    Room,
    Ward,
    Van
}

public enum AuditAction
{
    Created,
    Updated,
    Deleted,
    Viewed,
    TagWritten,
    TagVerified,
    CheckedIn,
    CheckedOut,
    Inventoried,
    Located
}

public enum PackageType
{
    Carton = 1,
    NilonBag = 2,
    Other = 3
}
