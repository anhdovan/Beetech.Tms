using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Threading.Tasks;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Beetech.Tms.Desktop.Services;
using RestSharp;

namespace Beetech.Tms.Desktop.ViewModels;

public partial class TagRegistrationViewModel : ObservableObject, ITagReadHandler
{
    public LanguageManager L => LanguageManager.Instance;

    private readonly ReaderManager _reader;
    private readonly RestClient _apiClient;

    [ObservableProperty] private CategoryDto? _selectedCategory;
    [ObservableProperty] private LocationDto? _selectedLocation;
    [ObservableProperty] private DepartmentDto? _selectedDepartment;
    [ObservableProperty] private string _statusMessage = "Ready to scan";
    [ObservableProperty] private bool _isBusy;
    [ObservableProperty] private bool _isScanning;
    [ObservableProperty] private int _totalScanned;
    [ObservableProperty] private int _totalSuccess;

    public ObservableCollection<CategoryDto> Categories { get; } = new();
    public ObservableCollection<LocationDto> Locations { get; } = new();
    public ObservableCollection<DepartmentDto> Departments { get; } = new();
    public ObservableCollection<TagScanModel> ScanResults { get; } = new();

    private readonly Dictionary<string, string> _epcToOldEpc = new();

    public TagRegistrationViewModel(ReaderManager reader)
    {
        _reader = reader;
        _apiClient = new RestClient(AuthService.ServerUrl);
        if (AuthService.IsLoggedIn)
        {
            _apiClient.AddDefaultHeader("Authorization", $"Bearer {AuthService.BearerToken}");
        }

        _reader.OnReaderStatusChanged += status => StatusMessage = status;
        _reader.OnConnectionStatusChanged += SyncScanningState;
        
        SyncScanningState();
        _ = InitAsync();
    }

    private void SyncScanningState()
    {
        IsScanning = _reader.IsScanning;
    }

    private async Task InitAsync()
    {
        IsBusy = true;
        try
        {
            var catsRequest = new RestRequest("api/mobile/categories");
            var catsResponse = await _apiClient.ExecuteAsync<List<CategoryDto>>(catsRequest);
            if (catsResponse.IsSuccessful && catsResponse.Data != null)
            {
                foreach (var c in catsResponse.Data) Categories.Add(c);
            }

            var locsRequest = new RestRequest("api/mobile/locations");
            var locsResponse = await _apiClient.ExecuteAsync<List<LocationDto>>(locsRequest);
            if (locsResponse.IsSuccessful && locsResponse.Data != null)
            {
                foreach (var l in locsResponse.Data) Locations.Add(l);
            }

            var deptsRequest = new RestRequest("api/mobile/departments");
            var deptsResponse = await _apiClient.ExecuteAsync<List<DepartmentDto>>(deptsRequest);
            if (deptsResponse.IsSuccessful && deptsResponse.Data != null)
            {
                foreach (var d in deptsResponse.Data) Departments.Add(d);
            }
        }
        catch (Exception ex) { StatusMessage = $"Init Error: {ex.Message}"; }
        finally { IsBusy = false; }
    }

    public void HandleDeviceBatchTagsRead(List<Beetech.Adv.Common.Rfid.TagResult> tags)
    {
        if (!IsScanning) return;
        foreach(var tag in tags)
        {
            HandleDeviceTagRead(tag.Epc, "Scanned", "", null, null, null);
        }
    }

    public void HandleDeviceTagRead(string epc, string status, string message, string? itemName, string? categoryName, string? locationName)
    {
        if (!IsScanning) return;

        Avalonia.Threading.Dispatcher.UIThread.InvokeAsync(() =>
        {
            var existing = ScanResults.FirstOrDefault(r => r.Epc == epc);
            if (existing == null)
            {
                ScanResults.Add(new TagScanModel { Epc = epc, Status = "Scanned" });
                TotalScanned = ScanResults.Count;
            }
        });
    }

    public void Clear()
    {
        ScanResults.Clear();
        TotalScanned = 0;
        TotalSuccess = 0;
        StatusMessage = "Ready to scan";
        _epcToOldEpc.Clear();
    }

    [RelayCommand]
    private async Task RegisterBulkAsync()
    {
        if (ScanResults.Count == 0) { StatusMessage = "No items scanned"; return; }
        if (SelectedCategory == null) { StatusMessage = "Please select a category"; return; }

        _reader.StopInventory();
        IsBusy = true;
        StatusMessage = "Step 1: Registering in system...";

        try
        {
            // 1. Get IDs for the items
            var request = new RestRequest("api/mobile/items/bulk-register", Method.Post);
            request.AddJsonBody(new
            {
                CategoryId = SelectedCategory.Id,
                LocationId = SelectedLocation?.Id,
                DepartmentId = SelectedDepartment?.Id,
                Count = ScanResults.Count
            });

            var response = await _apiClient.ExecuteAsync<List<ItemRegistrationDto>>(request);
            if (!response.IsSuccessful || response.Data == null)
            {
                StatusMessage = $"API Error: {response.StatusDescription}";
                return;
            }

            var registeredItems = response.Data;
            StatusMessage = "Step 2: Writing tags...";

            // 2. Physical write
            var epcMapping = new Dictionary<string, string>();
            for (int i = 0; i < registeredItems.Count; i++)
            {
                epcMapping[ScanResults[i].Epc] = registeredItems[i].Code;
                ScanResults[i].Status = "Writing...";
            }

            var writeResults = await _reader.BulkWriteTags(epcMapping);

            // 3. Confirm only successful writes
            StatusMessage = "Step 3: Verifying and confirming...";
            var successfulIds = new List<int>();
            for (int i = 0; i < ScanResults.Count; i++)
            {
                string originalEpc = ScanResults[i].Epc;
                if (writeResults.TryGetValue(originalEpc, out bool success) && success)
                {
                    ScanResults[i].Status = "Success";
                    ScanResults[i].RfidCode = epcMapping[originalEpc];
                    successfulIds.Add(registeredItems[i].Id);
                }
                else
                {
                    ScanResults[i].Status = "Write Failed";
                }
            }

            if (successfulIds.Any())
            {
                var confirmRequest = new RestRequest("api/mobile/items/confirm-registration", Method.Post);
                confirmRequest.AddJsonBody(new { ItemIds = successfulIds });
                await _apiClient.ExecuteAsync(confirmRequest);
            }

            TotalSuccess = successfulIds.Count;
            StatusMessage = $"Registration Complete: {TotalSuccess}/{TotalScanned} items processed.";
        }
        catch (Exception ex) { StatusMessage = $"Error: {ex.Message}"; }
        finally { IsBusy = false; }
    }
}

public class TagScanModel : ObservableObject
{
    private string _epc = "";
    public string Epc { get => _epc; set => SetProperty(ref _epc, value); }

    private string _rfidCode = "";
    public string RfidCode { get => _rfidCode; set => SetProperty(ref _rfidCode, value); }

    private string _status = "";
    public string Status { get => _status; set => SetProperty(ref _status, value); }
}

public class ItemRegistrationDto
{
    public int Id { get; set; }
    public string Code { get; set; } = "";
}
