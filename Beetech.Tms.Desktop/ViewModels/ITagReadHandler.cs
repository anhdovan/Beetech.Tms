using System.Collections.Generic;
using Beetech.Adv.Common.Rfid;

namespace Beetech.Tms.Desktop.ViewModels;

public interface ITagReadHandler
{
    void HandleDeviceTagRead(string epc, string status, string message, string? itemName, string? categoryName, string? locationName);
    void HandleDeviceBatchTagsRead(List<TagResult> tags);
}
