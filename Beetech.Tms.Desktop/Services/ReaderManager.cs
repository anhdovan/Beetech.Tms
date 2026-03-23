using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Beetech.Adv.Common.Rfid;
using Beetech.Adv.Readers.Fixeds;
using RestSharp;
using NAudio.Wave;
using NAudio.Wave.SampleProviders;
using System.IO;
using Avalonia.Platform;
using Beetech.Tms.Core.Models;

namespace Beetech.Tms.Desktop.Services;

public class ReaderManager
{
    private GeneralReader? _reader;
    private RestClient? _apiClient;
    private string? _currentBearerToken;
    private readonly HashSet<string> _processedTags = new();
    private readonly Dictionary<string, ushort> _tagPcBits = new();
    private DateTime _lastReadTime = DateTime.MinValue;

    public string ReaderIp { get; set; } = "192.168.0.101";
    public string ApiUrl { get; set; } = "";
    
    private bool _isTestMode = false;
    public bool IsTestMode
    {
        get => _isTestMode;
        set
        {
            if (_isTestMode != value)
            {
                _isTestMode = value;
                OnConnectionStatusChanged?.Invoke();
            }
        }
    }

    /// <summary>
    /// The item status required for the current operation. Null = no check.
    /// E.g. "Soiled" when in Washing mode.
    /// </summary>
    public string? ExpectedItemStatus { get; set; }

    public bool IsConnected => _reader != null && _reader.IsConnected();
    public bool IsScanning { get; private set; }

    public event Action<string, string, string, string?, string?, string?>? OnTagRead;
    public event Action<List<TagResult>>? OnBatchTagsRead;
    public event Action<string>? OnReaderStatusChanged;
    public event Action? OnConnectionStatusChanged;

    public void InitializeReader()
    {
        if (_reader != null)
        {
            if (_reader.IsConnected())
            {
                _reader.Stop();
                _reader.Disconnect();
            }
            _reader.OnTagRead -= HandleHardwareTagRead;
        }

        // Default to ImpinjR420 as per DocMgmt pattern
        _reader = new ImpinjR420(1);
        _reader.OnTagRead += HandleHardwareTagRead;
        _reader.OnBatchTagRead += HandleHardwareBatchTagRead;
    }

    public async Task<bool> ConnectAsync()
    {
        try
        {
            OnReaderStatusChanged?.Invoke("Connecting...");

            EnsureApiClient();

            InitializeReader();
            bool connected = await _reader!.Connect(ReaderIp);

            if (connected)
            {
                _reader.Stop();
                OnReaderStatusChanged?.Invoke("Connected" + (IsTestMode ? " (Test Mode)" : ""));
                OnConnectionStatusChanged?.Invoke();
            }
            else
            {
                OnReaderStatusChanged?.Invoke("Connection Failed");
            }

            return connected;
        }
        catch (Exception ex)
        {
            OnReaderStatusChanged?.Invoke($"Error: {ex.Message}");
            return false;
        }
    }

    public void Disconnect()
    {
        if (_reader != null)
        {
            _reader.Stop();
            _reader.Disconnect();
            _reader.OnTagRead -= HandleHardwareTagRead;
            _reader.OnBatchTagRead -= HandleHardwareBatchTagRead;
        }
        _processedTags.Clear();
        OnReaderStatusChanged?.Invoke("Disconnected");
        OnConnectionStatusChanged?.Invoke();
    }

    public void StartInventory()
    {
        _reader.SetProfile("Default");
        _reader?.Start();
        IsScanning = true;
        OnReaderStatusChanged?.Invoke("Scanning..." + (IsTestMode ? " (Test Mode)" : ""));
        OnConnectionStatusChanged?.Invoke();
    }

    public void StopInventory()
    {
        _reader?.Stop();
        IsScanning = false;
        OnReaderStatusChanged?.Invoke("Connected" + (IsTestMode ? " (Test Mode)" : ""));
        OnConnectionStatusChanged?.Invoke();
    }

    public void ClearProcessedTags()
    {
        _processedTags.Clear();
    }

    private async void HandleHardwareTagRead(TagResult tag)
    {
        // Debounce: Ignore same tag within 5 seconds
        if (_processedTags.Contains(tag.Epc) && (DateTime.Now - _lastReadTime).TotalSeconds < 5)
        {
            OnTagRead?.Invoke(tag.Epc, "Duplicate", "Tag already scanned", null, null, null);
            return;
        }

        _processedTags.Add(tag.Epc);
        _tagPcBits[tag.Epc] = tag.PcBits;
        _lastReadTime = DateTime.Now;

        // Play beep
        PlayBeep();

        // Validate with API
        var result = await ValidateTagWithApiAsync(tag.Epc);
        OnTagRead?.Invoke(tag.Epc, result.Status, result.Message, result.ItemName, result.CategoryName, result.LocationName);
    }

    private void HandleHardwareBatchTagRead(List<TagResult> tags)
    {
        OnBatchTagsRead?.Invoke(tags);
    }

    private async Task<TagValidationResult> ValidateTagWithApiAsync(string epc)
    {
        if (IsTestMode)
        {
            return new TagValidationResult
            {
                Status = "Scanned",
                Message = "Test Mode",
                ItemName = $"Test-{epc[^4..]}",
                CategoryName = "Test",
                LocationName = "Test Location"
            };
        }

        EnsureApiClient();
        if (_apiClient == null)
        {
            return new TagValidationResult { Status = "Unknown", Message = "API not set" };
        }

        try
        {
            var request = new RestRequest($"api/textileitems/by-code/{epc}", Method.Get);
            var response = await _apiClient.ExecuteAsync<TextileItemDto>(request);

            if (response.IsSuccessful && response.Data != null)
            {
                var item = response.Data;

                // Workflow order check
                if (!string.IsNullOrEmpty(ExpectedItemStatus) &&
                    !string.Equals(item.Status, ExpectedItemStatus, StringComparison.OrdinalIgnoreCase))
                {
                    return new TagValidationResult
                    {
                        Status = "Wrong Flow",
                        Message = $"Expected status '{ExpectedItemStatus}', item is '{item.Status}'",
                        ItemName = item.Code,
                        CategoryName = item.CategoryName,
                        LocationName = item.Location
                    };
                }

                return new TagValidationResult
                {
                    Status = "Scanned",
                    Message = $"Found: {item.CategoryName}",
                    ItemName = item.Code,
                    CategoryName = item.CategoryName,
                    LocationName = item.Location
                };
            }

            return new TagValidationResult { Status = "Unknown", Message = "Tag not registered" };
        }
        catch (Exception ex)
        {
            return new TagValidationResult { Status = "Error", Message = ex.Message };
        }
    }

    public async Task<List<LocationDto>> GetLocationsAsync()
    {
        EnsureApiClient();
        if (_apiClient == null) return new List<LocationDto>();
        try
        {
            var request = new RestRequest("api/mobile/locations", Method.Get);
            var response = await _apiClient.ExecuteAsync<List<LocationDto>>(request);
            return response.Data ?? new List<LocationDto>();
        }
        catch { return new List<LocationDto>(); }
    }

    public async Task<List<DepartmentDto>> GetDepartmentsAsync()
    {
        EnsureApiClient();
        if (_apiClient == null) return new List<DepartmentDto>();
        try
        {
            var request = new RestRequest("api/mobile/departments", Method.Get);
            var response = await _apiClient.ExecuteAsync<List<DepartmentDto>>(request);
            return response.Data ?? new List<DepartmentDto>();
        }
        catch { return new List<DepartmentDto>(); }
    }

    public async Task<List<CustomerDto>> GetCustomersAsync()
    {
        EnsureApiClient();
        if (_apiClient == null) return new List<CustomerDto>();
        try
        {
            var request = new RestRequest("api/mobile/customers", Method.Get);
            var response = await _apiClient.ExecuteAsync<List<CustomerDto>>(request);
            return response.Data ?? new List<CustomerDto>();
        }
        catch { return new List<CustomerDto>(); }
    }

    public async Task<List<TextileItemDto>> GetItemsAsync(int? locationId = null)
    {
        EnsureApiClient();
        if (_apiClient == null) return new List<TextileItemDto>();
        try
        {
            var request = new RestRequest("api/mobile/items", Method.Get);
            if (locationId.HasValue) request.AddQueryParameter("locationId", locationId.Value.ToString());
            var response = await _apiClient.ExecuteAsync<List<TextileItemDto>>(request);
            return response.Data ?? new List<TextileItemDto>();
        }
        catch { return new List<TextileItemDto>(); }
    }

    public async Task<List<TransactionDto>> GetTransactionsAsync(TransactionType? type = null)
    {
        EnsureApiClient();
        if (_apiClient == null) return new List<TransactionDto>();
        try
        {
            var request = new RestRequest("api/mobile/transactions", Method.Get);
            if (type.HasValue) request.AddQueryParameter("type", ((int)type.Value).ToString());
            var response = await _apiClient.ExecuteAsync<List<TransactionDto>>(request);
            return response.Data ?? new List<TransactionDto>();
        }
        catch { return new List<TransactionDto>(); }
    }

    public async Task<bool> SubmitTransactionAsync(string operationType, int? locationId, int? departmentId, List<string> epcs, string notes, int targetQuantity = 0, int? sourceTransactionId = null, List<TransactionItemDto>? detailedItems = null, int? customerId = null, List<PackingUnitDto>? packingUnits = null)
    {
        if (IsTestMode) return true;
        
        EnsureApiClient();
        if (_apiClient == null) return false;

        try
        {
            var request = new RestRequest("api/mobile/transaction", Method.Post);
            request.AddJsonBody(new
            {
                Type = operationType,
                FromLocationId = locationId, // Simplified for now
                DepartmentId = departmentId,
                CustomerId = customerId,
                Epcs = epcs,
                DetailedItems = detailedItems,
                Notes = notes,
                TargetQuantity = targetQuantity,
                SourceTransactionId = sourceTransactionId,
                PackingUnits = packingUnits
            });

            var response = await _apiClient.ExecuteAsync(request);
            return response.IsSuccessful;
        }
        catch { return false; }
    }

    public async Task<Dictionary<string, bool>> BulkWriteTags(Dictionary<string, string> epcMapping)
    {
        if (_reader == null || IsTestMode) 
        {
            return epcMapping.ToDictionary(m => m.Key, _ => IsTestMode);
        }

        var results = new Dictionary<string, bool>();
        var completionSources = new Dictionary<ushort, TaskCompletionSource<bool>>();
        var opIdToOldEpc = new Dictionary<ushort, string>();

        void HandleOpComplete(TagOpResult opResult)
        {
            if (completionSources.TryGetValue(opResult.OpId, out var tcs))
            {
                tcs.TrySetResult(opResult.Success);
            }
        }

        _reader.OnTagOpComplete += HandleOpComplete;

        try
        {
            foreach (var mapping in epcMapping)
            {
                string oldEpc = mapping.Key;
                string newEpc = mapping.Value;
                ushort pcBits = _tagPcBits.GetValueOrDefault(oldEpc, (ushort)0x3000);

                var op = _reader.WriteTagEpc(oldEpc, pcBits, newEpc);
                if (op != null)
                {
                    var tcs = new TaskCompletionSource<bool>();
                    completionSources[op.OpId] = tcs;
                    opIdToOldEpc[op.OpId] = oldEpc;
                }
                else
                {
                    results[oldEpc] = false;
                }
            }

            // Wait for all operations with a timeout
            var tasks = completionSources.Values.Select(t => t.Task).ToList();
            if (tasks.Any())
            {
                var allTasks = Task.WhenAll(tasks);
                if (await Task.WhenAny(allTasks, Task.Delay(10000)) == allTasks)
                {
                    foreach (var kvp in completionSources)
                    {
                        results[opIdToOldEpc[kvp.Key]] = await kvp.Value.Task;
                    }
                }
                else
                {
                    // Timeout
                    foreach (var oldEpc in opIdToOldEpc.Values)
                    {
                        if (!results.ContainsKey(oldEpc)) results[oldEpc] = false;
                    }
                }
            }
        }
        finally
        {
            _reader.OnTagOpComplete -= HandleOpComplete;
        }

        return results;
    }

    public void PlayBeep()
    {
        PlaySound(1000, 100);
    }

    public void PlayAlarm()
    {
        PlaySound(2000, 500);
    }

    private void PlaySound(int frequency, int duration)
    {
        Task.Run(() =>
        {
            try
            {
                using var waveOut = new WaveOutEvent();
                var provider = new SignalGenerator(44100, 1)
                {
                    Frequency = frequency,
                    Type = SignalGeneratorType.Sin,
                    Gain = 0.1
                };
                waveOut.Init(provider);
                waveOut.Play();
                Thread.Sleep(duration);
            }
            catch { }
        });
    }

    private void EnsureApiClient()
    {
        if (string.IsNullOrEmpty(ApiUrl)) return;

        string? requiredToken = AuthService.IsLoggedIn ? AuthService.BearerToken : null;

        bool needsUpdate = _apiClient == null || 
                          _apiClient.Options.BaseUrl?.ToString().TrimEnd('/') != ApiUrl.TrimEnd('/') ||
                          _currentBearerToken != requiredToken;

        if (needsUpdate)
        {
            _apiClient = new RestClient(ApiUrl);
            if (!string.IsNullOrEmpty(requiredToken))
            {
                _apiClient.AddDefaultHeader("Authorization", $"Bearer {requiredToken}");
            }
            _currentBearerToken = requiredToken;
        }
    }
}

public class TagValidationResult
{
    public string Status { get; set; } = "";
    public string Message { get; set; } = "";
    public string? ItemName { get; set; }
    public string? CategoryName { get; set; }
    public string? LocationName { get; set; }
}

public class TextileItemDto
{
    public int Id { get; set; }
    public string Epc { get; set; } = "";
    public string Code { get; set; } = "";
    public string CategoryName { get; set; } = "";
    public string Location { get; set; } = "";
    public string Status { get; set; } = "";
}

public class CategoryDto 
{ 
    public int Id { get; set; } 
    public string Name { get; set; } = ""; 
}

public class LocationDto { public int Id { get; set; } public string Name { get; set; } = ""; }
public class DepartmentDto { public int Id { get; set; } public string Name { get; set; } = ""; }
public class CustomerDto { public int Id { get; set; } public string Name { get; set; } = ""; public bool IsInternal { get; set; } }

public class TransactionDto
{
    public int Id { get; set; }
    public string TransactionNumber { get; set; } = "";
    public string? Description { get; set; }
    public string Type { get; set; } = "";
    public DateTime TransactionDate { get; set; }
    public int ItemCount { get; set; }
    public string DisplayText => $"{TransactionNumber} {(string.IsNullOrEmpty(Description) ? "" : $"({Description}) ")} - {ItemCount} items";
}

public class TransactionItemDto
{
    public string Epc { get; set; } = "";
    public string? Notes { get; set; }
    public string? PackingUnitCode { get; set; }
}

public class PackingUnitDto
{
    public string Code { get; set; } = "";
    public int Type { get; set; } // 1: Carton, 2: NilonBag
    public decimal? Weight { get; set; }
}