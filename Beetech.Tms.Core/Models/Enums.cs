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
    Relocated
}

public enum TransactionType
{
    Inbound,
    Outbound,
    Delivery,
    Receipt,
    LaundrySend,
    LaundryReceive,
    Discard,
    InventoryAudit
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
