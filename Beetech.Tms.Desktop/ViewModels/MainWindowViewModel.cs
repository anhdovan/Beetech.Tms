using System;
using System.Linq;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Threading.Tasks;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Beetech.Tms.Desktop.Services;
using Avalonia.Media;
using Avalonia.Threading;
using Avalonia;
using Avalonia.Controls.ApplicationLifetimes;
using Avalonia.Controls;

using Beetech.Tms.Desktop.Models;
using Beetech.Tms.Core.Models;
using Beetech.Adv.Common.Rfid;

namespace Beetech.Tms.Desktop.ViewModels;

public partial class MainWindowViewModel : ViewModelBase
{
    private readonly ReaderManager _reader;

    [ObservableProperty] private object? _currentView;
    [ObservableProperty] private bool _isBulkScanViewVisible = true;
    [ObservableProperty] private bool _isBulkScanViewActive = true;
    [ObservableProperty] private bool _isRegistrationViewActive = false;
    [ObservableProperty] private bool _isSettingsViewActive = false;
    
    [ObservableProperty] private bool _isDirtyInboundActive = false;
    [ObservableProperty] private bool _isCleanInboundActive = false;
    // Active state for operations
    [ObservableProperty] private bool _isInboundDirtyActive;
    [ObservableProperty] private bool _isInboundHotelActive;
    [ObservableProperty] private bool _isOutboundDirtyActive;
    [ObservableProperty] private bool _isOutboundLaundryActive;
    [ObservableProperty] private bool _isInventoryAuditActive;
    [ObservableProperty] private bool _isCondemnedActive;
    [ObservableProperty] private bool _isWashingActive;
    [ObservableProperty] private bool _isDryingActive;
    [ObservableProperty] private bool _isIroningActive;
    [ObservableProperty] private bool _isFoldingActive;
    [ObservableProperty] private bool _isPackingActive;
    [ObservableProperty] private bool _isReturnActive;

    // Visibility control
    [ObservableProperty] private bool _isInboundDirtyVisible = true;
    [ObservableProperty] private bool _isInboundHotelVisible = true;
    [ObservableProperty] private bool _isOutboundDirtyVisible = true;
    [ObservableProperty] private bool _isOutboundLaundryVisible = true;
    [ObservableProperty] private bool _isTagRegistrationVisible = true;
    [ObservableProperty] private bool _isInventoryAuditVisible = true;
    [ObservableProperty] private bool _isCondemnedVisible = true;
    [ObservableProperty] private bool _isWashingVisible = true;
    [ObservableProperty] private bool _isDryingVisible = true;
    [ObservableProperty] private bool _isIroningVisible = true;
    [ObservableProperty] private bool _isFoldingVisible = true;
    [ObservableProperty] private bool _isPackingVisible = true;
    [ObservableProperty] private bool _isReturnVisible = true;

    // Inventory Audit specific
    [ObservableProperty] private bool _isInventoryAuditMode;
    [ObservableProperty] private int _expectedCount;
    [ObservableProperty] private int _matchedCount;
    [ObservableProperty] private int _wrongLocationCount;
    [ObservableProperty] private int _unknownCount;
    [ObservableProperty] private string _auditSummaryText = "";
    private readonly HashSet<string> _expectedEpcs = new();
    private readonly Dictionary<string, TextileItemDto> _auditItemCache = new();
    private string? _auditLocationName;
    [ObservableProperty] private bool _isSettingsVisible = true;

    // Status bar
    [ObservableProperty] private string _readerStatus = "Disconnected";
    [ObservableProperty] private string _apiStatus = "Ready";
    [ObservableProperty] private string _currentUser = "";
    [ObservableProperty] private string _summaryText = "0 scans";
    [ObservableProperty] private string _actionStatus = "Ready";

    [ObservableProperty] private bool _isSourceTransactionRequired;
    [ObservableProperty] private ObservableCollection<TransactionDto> _sourceTransactions = new();
    [ObservableProperty] private TransactionDto? _selectedSourceTransaction;
    [ObservableProperty] private int? _sourceTransactionId;

    [ObservableProperty] private ObservableCollection<CustomerDto> _customers = new();
    [ObservableProperty] private CustomerDto? _selectedCustomer;
    [ObservableProperty] private bool _isCustomerRequired;

    // Configuration from Settings
    [ObservableProperty] private string _readerIp = "";
    [ObservableProperty] private string _apiUrl = "";
    [ObservableProperty] private bool _isTestMode = false;

    // Left panel / Scan Panel
    [ObservableProperty] private IBrush _connectionColor = Brushes.Gray;
    [ObservableProperty] private string _statusText = "DISCONNECTED";
    [ObservableProperty] private bool _isReaderConnected;
    [ObservableProperty] private bool _isInventoryActive;
    [ObservableProperty] private string _inventoryButtonText = "Start Scanning";

    // Operation type selection
    [ObservableProperty] private string _currentOperationName = "";
    [ObservableProperty] private string _currentOperationSubtitle = "Scan and process items using the reader";
    [ObservableProperty] private string _selectedOperationType = "";
    public LanguageManager L => LanguageManager.Instance;
    [ObservableProperty] private LocationDto? _selectedLocation;
    [ObservableProperty] private bool _isLocationEnabled = false;
    [ObservableProperty] private ObservableCollection<LocationDto> _locations = new();
    [ObservableProperty] private string _notes = "";

    // Target Quantity for Laundry Stages
    [ObservableProperty] private int _targetQuantity;
    [ObservableProperty] private string _targetQuantityText = "";
    [ObservableProperty] private bool _isMismatchDetected;

    // Right panel
    [ObservableProperty] private ObservableCollection<ScanEntry> _scans = new();
    [ObservableProperty] private ObservableCollection<ScanEntry> _filteredScans = new();
    [ObservableProperty] private string _searchText = "";
    [ObservableProperty] private ObservableCollection<string> _statusFilterOptions = new() { "All", "Matched", "Wrong Location", "Wrong Flow", "Unknown", "Scanned", "Duplicate", "Error" };
    [ObservableProperty] private string _selectedStatusFilter = "All";
    
    // Packing Units
    [ObservableProperty] private ObservableCollection<PackingUnitDto> _currentPackingUnits = new();
    [ObservableProperty] private PackingUnitDto? _selectedPackingUnit;
    [ObservableProperty] private bool _isPackingSpecificVisible;

    // Group visibility for sidebar
    [ObservableProperty] private bool _isReceptionVisible;
    [ObservableProperty] private bool _isProcessingVisible;
    [ObservableProperty] private bool _isDispatchVisible;

    private int _scanCount = 0;
    private TmsReaderRole CurrentRole => SettingsService.Current.Role ?? TmsReaderRole.Standard;

    public MainWindowViewModel()
    {
        _reader = new ReaderManager();
        _reader.OnReaderStatusChanged += HandleReaderStatusChanged;
        _reader.OnConnectionStatusChanged += SyncReaderState;
        SettingsService.OnSettingsChanged += ApplyReaderRole;

        // Load Settings
        ReaderIp = SettingsService.Current.ReaderIp;
        ApiUrl = SettingsService.Current.ApiUrl;
        IsTestMode = SettingsService.Current.IsTestMode;

        if (AuthService.IsLoggedIn)
        {
            CurrentUser = $"{AuthService.CurrentSession!.FullName} ({AuthService.CurrentSession.Role})";
            ApiStatus = "Connected";
        }

        _ = LoadConfigurationDataAsync();
        ApplyReaderRole();
    }

    private void ApplyReaderRole()
    {
        Dispatcher.UIThread.Post(() =>
        {
            var role = SettingsService.Current.Role ?? TmsReaderRole.Standard;
            
            // Reset visibility
            IsInboundDirtyVisible = false;
            IsInboundHotelVisible = false;
            IsOutboundDirtyVisible = false;
            IsOutboundLaundryVisible = false;
            IsTagRegistrationVisible = false;
            IsInventoryAuditVisible = false;
            IsCondemnedVisible = false;
            IsWashingVisible = false;
            IsDryingVisible = false;
            IsIroningVisible = false;
            IsFoldingVisible = false;
            IsPackingVisible = false;
            IsReturnVisible = false;
            IsSettingsVisible = true;
            
            // Group visibility
            IsReceptionVisible = false;
            IsProcessingVisible = false;
            IsDispatchVisible = false;

            switch (role)
            {
                case TmsReaderRole.InboundDirty:
                    IsInboundDirtyVisible = true;
                    if (CurrentView == null) 
                    {
                        CurrentOperationName = "Dirty Inbound (Laundry Receive)";
                        SelectedOperationType = "Inbound (Laundry Receive)";
                    }
                    break;
                case TmsReaderRole.InboundHotel:
                    IsInboundHotelVisible = true;
                    if (CurrentView == null)
                    {
                        CurrentOperationName = "Clean Inbound (Hotel Receive)";
                        SelectedOperationType = "Inbound (Hotel Receive)";
                    }
                    break;
                case TmsReaderRole.OutboundDirty:
                    IsOutboundDirtyVisible = true;
                    if (CurrentView == null)
                    {
                        CurrentOperationName = "Dirty Outbound (Hotel Send)";
                        SelectedOperationType = "Outbound (Hotel Send)";
                    }
                    break;
                case TmsReaderRole.OutboundLaundry:
                    IsOutboundLaundryVisible = true;
                    if (CurrentView == null) 
                    {
                        CurrentOperationName = "Clean Outbound (Laundry Send)";
                        SelectedOperationType = "Outbound (Laundry Send)";
                    }
                    break;
                case TmsReaderRole.TagRegistry:
                    IsTagRegistrationVisible = true;
                    if (CurrentView == null) ShowRegistration();
                    break;
                case TmsReaderRole.InventoryAudit:
                    IsInventoryAuditVisible = true;
                    if (CurrentView == null) NavigateToInventoryAudit();
                    break;
                case TmsReaderRole.Condemned:
                    IsCondemnedVisible = true;
                    if (CurrentView == null) NavigateToCondemned();
                    break;
                case TmsReaderRole.Washing:
                    IsWashingVisible = true;
                    if (CurrentView == null) _ = NavigateToWashing();
                    break;
                case TmsReaderRole.Drying:
                    IsDryingVisible = true;
                    if (CurrentView == null) _ = NavigateToDrying();
                    break;
                case TmsReaderRole.Ironing:
                    IsIroningVisible = true;
                    if (CurrentView == null) _ = NavigateToIroning();
                    break;
                case TmsReaderRole.Folding:
                    IsFoldingVisible = true;
                    if (CurrentView == null) _ = NavigateToFolding();
                    break;
                case TmsReaderRole.Packing:
                    IsPackingVisible = true;
                    if (CurrentView == null) _ = NavigateToPacking();
                    break;
                case TmsReaderRole.Return:
                    IsReturnVisible = true;
                    if (CurrentView == null) _ = NavigateToReturn();
                    break;
                case TmsReaderRole.PackingMonitor:
                    IsPackingVisible = true;
                    IsReturnVisible = true;
                    if (CurrentView == null) _ = NavigateToPacking();
                    break;
                default: // Standard
                    IsInboundDirtyVisible = true;
                    IsInboundHotelVisible = true;
                    IsOutboundDirtyVisible = true;
                    IsOutboundLaundryVisible = true;
                    IsTagRegistrationVisible = true;
                    IsInventoryAuditVisible = true;
                    IsCondemnedVisible = true;
                    IsWashingVisible = true;
                    IsDryingVisible = true;
                    IsIroningVisible = true;
                    IsFoldingVisible = true;
                    IsPackingVisible = true;
                    IsReturnVisible = true;
                    
                    if (CurrentView == null)
                    {
                        CurrentOperationName = L["DirtyInbound"];
                        CurrentOperationSubtitle = GetLocalizedSubtitle("Inbound (Laundry Receive)");
                        SelectedOperationType = "Inbound (Laundry Receive)";
                    }
                    break;
            }

            // Finalize group visibility
            IsReceptionVisible = IsInboundDirtyVisible || IsInboundHotelVisible;
            IsProcessingVisible = IsWashingVisible || IsDryingVisible || IsIroningVisible || IsFoldingVisible;
            IsDispatchVisible = IsOutboundDirtyVisible || IsOutboundLaundryVisible || IsPackingVisible || IsReturnVisible;
        });
    }

    [RelayCommand]
    private async Task NavigateToDirtyInbound() => await ApplyNavigationState("Inbound (Laundry Receive)");

    [RelayCommand]
    private async Task NavigateToCleanInbound() => await ApplyNavigationState("Inbound (Hotel Receive)");

    [RelayCommand]
    private async Task NavigateToDirtyOutbound() => await ApplyNavigationState("Outbound (Hotel Send)");

    [RelayCommand]
    private async Task NavigateToCleanOutbound() => await ApplyNavigationState("Outbound (Laundry Send)");

    [RelayCommand]
    private async Task NavigateToInventoryAudit() => await ApplyNavigationState("Inventory Audit");

    [RelayCommand]
    private async Task NavigateToCondemned() => await ApplyNavigationState("Condemned");

    [RelayCommand]
    private async Task NavigateToWashing() => await ApplyNavigationState("Washing");

    [RelayCommand]
    private async Task NavigateToDrying() => await ApplyNavigationState("Drying");

    [RelayCommand]
    private async Task NavigateToIroning() => await ApplyNavigationState("Ironing");

    [RelayCommand]
    private async Task NavigateToFolding() => await ApplyNavigationState("Folding");

    [RelayCommand]
    private async Task NavigateToPacking() => await ApplyNavigationState("Packing");

    [RelayCommand]
    private async Task NavigateToReturn() => await ApplyNavigationState("Return");

    private async Task ApplyNavigationState(string operation)
    {
        CurrentView = null;
        IsBulkScanViewVisible = true;
        IsBulkScanViewActive = true;
        IsRegistrationViewActive = false;
        IsSettingsViewActive = false;

        SelectedOperationType = operation;
        CurrentOperationName = GetLocalizedOperationName(operation);
        CurrentOperationSubtitle = GetLocalizedSubtitle(operation);
        
        IsInboundDirtyActive = operation == "Inbound (Laundry Receive)";
        IsInboundHotelActive = operation == "Inbound (Hotel Receive)";
        IsOutboundDirtyActive = operation == "Outbound (Hotel Send)";
        IsOutboundLaundryActive = operation == "Outbound (Laundry Send)";
        IsCondemnedActive = operation == "Condemned";
        IsWashingActive = operation == "Washing";
        IsDryingActive = operation == "Drying";
        IsIroningActive = operation == "Ironing";
        IsFoldingActive = operation == "Folding";
        IsPackingActive = operation == "Packing";
        IsReturnActive = operation == "Return";
        IsInventoryAuditMode = operation == "Inventory Audit";
        
        ClearScans();
        
        // Handle Source Transaction Requirement
        IsSourceTransactionRequired = operation switch
        {
            "Washing" => true,
            "Drying" => true,
            "Ironing" => true,
            "Folding" => true,
            "Packing" => true,
            "Return" => true,
            _ => false
        };

        IsCustomerRequired = operation == "Inbound (Laundry Receive)";
        IsPackingSpecificVisible = operation == "Packing";

        // Set workflow order gate — item must be in this status to be accepted
        _reader.ExpectedItemStatus = operation switch
        {
            "Washing"  => "Soiled",
            "Drying"   => "Washing",
            "Ironing"  => "Drying",
            "Folding"  => "Ironing",
            "Packing"  => "Folding",
            "Return"   => "Packing",
            _          => null   // Intake, Audit, Condemned — no prerequisite check
        };

        if (IsInventoryAuditMode)
        {
            await LoadExpectedItemsForAuditAsync();
        }
        else if (IsSourceTransactionRequired)
        {
            await FetchSourceTransactionsAsync(operation);
        }
        else
        {
            SourceTransactions.Clear();
            SelectedSourceTransaction = null;
            SourceTransactionId = null;
            await UpdateTargetQuantityAsync();
        }
    }

    partial void OnSelectedLocationChanged(LocationDto? value)
    {
        if (IsInventoryAuditMode)
        {
            _ = LoadExpectedItemsForAuditAsync();
        }
    }

    private async Task LoadExpectedItemsForAuditAsync()
    {
        _expectedEpcs.Clear();
        _auditItemCache.Clear();
        ExpectedCount = 0;
        MatchedCount = 0;
        WrongLocationCount = 0;
        UnknownCount = 0;
        _auditLocationName = SelectedLocation?.Name;
        UpdateAuditSummary();

        if (SelectedLocation == null) return;

        ActionStatus = "Loading expected items for audit location...";
        var items = await _reader.GetItemsAsync(SelectedLocation.Id);
        Dispatcher.UIThread.Post(() =>
        {
            foreach (var item in items)
            {
                if (!string.IsNullOrEmpty(item.Epc))
                {
                    _expectedEpcs.Add(item.Epc);
                    _auditItemCache[item.Epc] = item;
                }
            }
            ExpectedCount = _expectedEpcs.Count;
            TargetQuantity = ExpectedCount;
            TargetQuantityText = $"Expected: {ExpectedCount}";
            ActionStatus = $"Loaded {ExpectedCount} expected items for '{SelectedLocation.Name}'";
            UpdateAuditSummary();
        });
    }

    private void UpdateAuditSummary()
    {
        if (!IsInventoryAuditMode) return;
        AuditSummaryText = $"✓ {MatchedCount} matched  ⚠ {WrongLocationCount} wrong location  ? {UnknownCount} unknown  / {ExpectedCount} expected";
        SummaryText = $"{Scans.Count} scans";
    }

    private void RefreshFilter()
    {
        var filter = SelectedStatusFilter;
        var search = SearchText?.Trim().ToLowerInvariant() ?? "";

        var filtered = Scans.Where(s =>
        {
            bool statusMatch = filter == "All" || s.Status == filter;
            bool searchMatch = string.IsNullOrEmpty(search) ||
                               s.Epc.Contains(search, StringComparison.OrdinalIgnoreCase) ||
                               s.ItemName.Contains(search, StringComparison.OrdinalIgnoreCase) ||
                               s.CategoryName.Contains(search, StringComparison.OrdinalIgnoreCase) ||
                               s.CurrentLocation.Contains(search, StringComparison.OrdinalIgnoreCase);
            return statusMatch && searchMatch;
        }).ToList();

        FilteredScans.Clear();
        foreach (var entry in filtered)
            FilteredScans.Add(entry);
    }

    partial void OnSelectedStatusFilterChanged(string value) => RefreshFilter();
    partial void OnSearchTextChanged(string value) => RefreshFilter();

    private async Task FetchSourceTransactionsAsync(string operation)
    {
        TransactionType? sourceType = operation switch
        {
            "Washing" => TransactionType.LaundryReceive,
            "Drying" => TransactionType.Washing,
            "Ironing" => TransactionType.Drying,
            "Folding" => TransactionType.Ironing,
            "Packing" => TransactionType.Folding,
            "Return" => TransactionType.Packing,
            _ => null
        };

        if (sourceType.HasValue)
        {
            ActionStatus = $"Fetching previous {sourceType} transactions...";
            var txs = await _reader.GetTransactionsAsync(sourceType.Value);
            Dispatcher.UIThread.Post(() => {
                SourceTransactions.Clear();
                foreach (var tx in txs) SourceTransactions.Add(tx);
                SelectedSourceTransaction = SourceTransactions.FirstOrDefault();
            });
        }
    }

    partial void OnSelectedSourceTransactionChanged(TransactionDto? value)
    {
        if (value != null)
        {
            TargetQuantity = value.ItemCount;
            TargetQuantityText = $"Target from {value.TransactionNumber}: {TargetQuantity}";
            SourceTransactionId = value.Id;
        }
        else if (IsSourceTransactionRequired)
        {
            TargetQuantity = 0;
            TargetQuantityText = "Please select source transaction";
            SourceTransactionId = null;
        }
    }

    private async Task UpdateTargetQuantityAsync()
    {
        // For simplicity, we fetch the count of items in the 'previous' stage
        // In reality, this would be more complex (e.g., selecting a specific load)
        string previousStatus = SelectedOperationType switch
        {
            "Washing" => "Soiled",
            "Drying" => "Washing",
            "Ironing" => "Drying",
            "Folding" => "Ironing",
            "Packing" => "Folding",
            "Return" => "Packing",
            _ => ""
        };

        if (string.IsNullOrEmpty(previousStatus))
        {
            TargetQuantity = 0;
            TargetQuantityText = "";
            return;
        }

        ActionStatus = $"Fetching target count for {SelectedOperationType}...";
        // We'll need a way to get this from the API. For now, let's assume a method exists or we use a default.
        // I'll add a method to ReaderManager to fetch counts by status.
        var items = await _reader.GetItemsAsync(); // This is already in ReaderManager
        TargetQuantity = items.Count(i => i.Status == previousStatus);
        TargetQuantityText = $"Target: {TargetQuantity}";
        ActionStatus = "";
    }

    [RelayCommand]
    private async Task ShowBulkScan()
    {
        ApplyReaderRole(); 
        CurrentView = null; 
        IsBulkScanViewVisible = true;
        IsBulkScanViewActive = true;
        IsRegistrationViewActive = false;
        IsSettingsViewActive = false;
        
        // Default to first active based on role
        if (IsInboundDirtyVisible) await ApplyNavigationState("Inbound (Laundry Receive)");
        else if (IsInboundHotelVisible) await ApplyNavigationState("Inbound (Hotel Receive)");
        else if (IsOutboundDirtyVisible) await ApplyNavigationState("Outbound (Hotel Send)");
        else if (IsOutboundLaundryVisible) await ApplyNavigationState("Outbound (Laundry Send)");
        else if (IsInventoryAuditVisible) await ApplyNavigationState("Inventory Audit");
        else if (IsCondemnedVisible) await ApplyNavigationState("Condemned");
        else if (IsWashingVisible) await ApplyNavigationState("Washing");
        else if (IsDryingVisible) await ApplyNavigationState("Drying");
        else if (IsIroningVisible) await ApplyNavigationState("Ironing");
        else if (IsFoldingVisible) await ApplyNavigationState("Folding");
        else if (IsPackingVisible) await ApplyNavigationState("Packing");
        else if (IsReturnVisible) await ApplyNavigationState("Return");
        else if (IsTagRegistrationVisible) ShowRegistration();
        else if (IsSettingsVisible) ShowSettings();
    }

    [RelayCommand]
    private void ShowRegistration()
    {
        CurrentView = new TagRegistrationViewModel(_reader);
        IsBulkScanViewVisible = false;
        IsBulkScanViewActive = false;
        IsRegistrationViewActive = true;
        IsSettingsViewActive = false;
        
        IsInboundDirtyActive = false;
        IsInboundHotelActive = false;
        IsOutboundDirtyActive = false;
        IsOutboundLaundryActive = false;
        IsCondemnedActive = false;
        IsWashingActive = false;
        IsDryingActive = false;
        IsIroningActive = false;
        IsFoldingActive = false;
    }

    [RelayCommand]
    private void ShowSettings()
    {
        CurrentView = new SettingsViewModel(_reader);
        IsBulkScanViewVisible = false;
        IsBulkScanViewActive = false;
        IsRegistrationViewActive = false;
        IsSettingsViewActive = true;
        
        IsInboundDirtyActive = false;
        IsInboundHotelActive = false;
        IsOutboundDirtyActive = false;
        IsOutboundLaundryActive = false;
        IsCondemnedActive = false;
        IsWashingActive = false;
        IsDryingActive = false;
        IsIroningActive = false;
        IsFoldingActive = false;
    }

    private async Task LoadConfigurationDataAsync()
    {
        try
        {
            _reader.ApiUrl = ApiUrl;
            var locs = await _reader.GetLocationsAsync();
            var custs = await _reader.GetCustomersAsync();

            Dispatcher.UIThread.Post(() =>
            {
                Locations.Clear();
                foreach (var loc in locs) Locations.Add(loc);

                Customers.Clear();
                foreach (var cust in custs) Customers.Add(cust);
                SelectedCustomer = Customers.FirstOrDefault();
                
                var defaultLocName = SettingsService.Current.Location;
                if (!string.IsNullOrEmpty(defaultLocName))
                {
                    SelectedLocation = Locations.FirstOrDefault(l => l.Name == defaultLocName) ?? Locations.FirstOrDefault();
                }
                else
                {
                    SelectedLocation = Locations.FirstOrDefault();
                }
            });
        }
        catch { /* Handle error or log */ }
    }

    [RelayCommand]
    private async Task ConnectAsync()
    {
        if (_reader.IsConnected)
        {
            if (IsInventoryActive) ToggleInventory();
            _reader.Disconnect();
            SyncReaderState();
        }
        else
        {
            _reader.ReaderIp = ReaderIp;
            _reader.ApiUrl = ApiUrl;
            _reader.IsTestMode = IsTestMode;

            var result = await _reader.ConnectAsync();
            if (result)
            {
                // Save successful settings
                SettingsService.Current.ReaderIp = ReaderIp;
                SettingsService.Current.ApiUrl = ApiUrl;
                SettingsService.Current.IsTestMode = IsTestMode;
                SettingsService.Save();
            }
            
            SyncReaderState();
        }
    }

    [RelayCommand]
    private void ToggleInventory()
    {
        if (IsInventoryActive)
        {
            _reader.OnTagRead -= HandleTagRead;
            _reader.OnBatchTagsRead -= HandleBatchTagsRead;
            _reader.StopInventory();
        }
        else
        {
            _reader.OnTagRead += HandleTagRead;
            _reader.OnBatchTagsRead += HandleBatchTagsRead;
            _reader.StartInventory();
        }
        SyncReaderState();
    }

    private void SyncReaderState()
    {
        Dispatcher.UIThread.Post(() =>
        {
            IsReaderConnected = _reader.IsConnected;
            IsInventoryActive = _reader.IsScanning; // Wait, I need to check if ReaderManager has IsScanning
            IsTestMode = _reader.IsTestMode;

            if (!IsReaderConnected)
            {
                ConnectionColor = Brushes.Gray;
                StatusText = "DISCONNECTED";
                InventoryButtonText = "Start Scanning";
                ReaderStatus = "Disconnected";
            }
            else if (IsInventoryActive)
            {
                ConnectionColor = Brushes.Blue;
                StatusText = "SCANNING...";
                InventoryButtonText = "Stop Scanning";
                ReaderStatus = "Scanning" + (IsTestMode ? " (Test)" : "");
            }
            else
            {
                ConnectionColor = Brushes.Green;
                StatusText = "CONNECTED\nReady";
                InventoryButtonText = "Start Scanning";
                ReaderStatus = "Connected" + (IsTestMode ? " (Test)" : "");
            }
        });
    }

    [RelayCommand]
    private void ClearScans()
    {
        Scans.Clear();
        FilteredScans.Clear();
        _scanCount = 0;
        SummaryText = "0 scans";
        _reader.ClearProcessedTags();
        IsMismatchDetected = false;
        CurrentPackingUnits.Clear();
        SelectedPackingUnit = null;
        
        if (CurrentView is TagRegistrationViewModel regVm)
        {
            regVm.Clear();
        }
    }

    [RelayCommand]
    private void AddPackingUnit()
    {
        var count = CurrentPackingUnits.Count + 1;
        var unit = new PackingUnitDto 
        { 
            Code = $"PKG-{DateTime.Now:MMdd}-{count:D2}", 
            Type = 1, // Default to Carton
            Weight = 0
        };
        CurrentPackingUnits.Add(unit);
        SelectedPackingUnit = unit;
    }

    [RelayCommand]
    private void RemovePackingUnit(PackingUnitDto unit)
    {
        CurrentPackingUnits.Remove(unit);
        if (SelectedPackingUnit == unit) SelectedPackingUnit = CurrentPackingUnits.LastOrDefault();
        
        foreach (var scan in Scans.Where(s => s.PackingUnitCode == unit.Code))
        {
            scan.PackingUnitCode = null;
        }
    }

    [RelayCommand]
    private void Logout()
    {
        AuthService.Logout();
        if (Application.Current?.ApplicationLifetime is IClassicDesktopStyleApplicationLifetime desktop)
        {
            desktop.MainWindow?.Close();
        }
    }



    [RelayCommand]
    private async Task SubmitScansAsync()
    {
        if (Scans.Count == 0) { ActionStatus = "No scans to submit"; return; }
        
        ActionStatus = "Submitting...";
        
        // Map operation to target status
        string targetStatus = SelectedOperationType switch
        {
            "Inbound (Laundry Receive)" => "Soiled",
            "Inbound (Hotel Receive)" => "Clean",
            "Outbound (Hotel Send)" => "Soiled",
            "Outbound (Laundry Send)" => "Clean",
            "Condemned" => "Condemned",
            "Washing" => "Washing",
            "Drying" => "Drying",
            "Ironing" => "Ironing",
            "Folding" => "Folding",
            "Packing" => "Packing",
            "Return" => "Returned",
            "Inventory Audit" => "Audited", // Assuming a status for audit
            _ => "Scanned"
        };

        var success = await _reader.SubmitTransactionAsync(
            targetStatus,
            SelectedLocation?.Id,
            null, // Removed Department from UI
            Scans.Select(s => s.Epc).ToList(),
            Notes,
            TargetQuantity,
            SourceTransactionId,
            Scans.Select(s => new TransactionItemDto 
            { 
                Epc = s.Epc, 
                Notes = s.Details,
                PackingUnitCode = s.PackingUnitCode
            }).ToList(),
            SelectedCustomer?.Id,
            CurrentPackingUnits.ToList()
        );

        if (success) 
        { 
            ActionStatus = "Transaction complete!"; 
            ClearScans(); 
            Notes = "";
        }
        else { ActionStatus = "Submission Failed"; }
    }
    
    [RelayCommand]
    private void ResetSession()
    {
        ClearScans();
        Notes = "";
        ActionStatus = "Session reset";
    }

    private void HandleBatchTagsRead(List<TagResult> tags)
    {
        if (CurrentView is ITagReadHandler handler)
        {
            // Dispatch to sub-view (e.g. TagRegistrationViewModel)
            handler.HandleDeviceBatchTagsRead(tags);
        }
        else
        {
            // On BulkScan view — process each tag directly into our Scans list.
            // Batch reads come raw without API validation, so status is "Scanned".
            Dispatcher.UIThread.Post(() =>
            {
                foreach (var tag in tags)
                {
                    var epc = tag.Epc;
                    var existing = Scans.FirstOrDefault(s => s.Epc == epc);
                    if (existing != null)
                    {
                        existing.Count++;
                        continue;
                    }

                    // Determine audit match status when in Inventory Audit mode
                    string auditStatus = "Scanned";
                    string categoryName = "";
                    string currentLocation = "";
                    string itemName = "";
                    if (IsInventoryAuditMode)
                    {
                        if (_expectedEpcs.Contains(epc))
                        {
                            auditStatus = "Matched";
                            MatchedCount++;
                            if (_auditItemCache.TryGetValue(epc, out var cached))
                            {
                                categoryName = cached.CategoryName;
                                currentLocation = cached.Location;
                                itemName = cached.Code;
                            }
                        }
                        else
                        {
                            auditStatus = "Unknown";
                            UnknownCount++;
                        }
                        UpdateAuditSummary();
                    }

                    _scanCount++;
                    Scans.Insert(0, new ScanEntry
                    {
                        Number = _scanCount,
                        Time = DateTime.Now.ToString("HH:mm:ss"),
                        Epc = epc,
                        Status = auditStatus,
                        Details = IsInventoryAuditMode ? auditStatus : "Batch scan",
                        Count = 1,
                        CategoryName = categoryName,
                        CurrentLocation = currentLocation,
                        ItemName = itemName
                    });
                    RefreshFilter();
                }
                if (!IsInventoryAuditMode)
                    SummaryText = $"{Scans.Count} items scanned";
                IsMismatchDetected = TargetQuantity > 0 && Scans.Count != TargetQuantity;
            });
        }
    }

    private void HandleTagRead(string epc, string status, string message, string? itemName = null, string? categoryName = null, string? locationName = null)
    {
        if (CurrentView is ITagReadHandler handler)
        {
            // Sub-view handles it (e.g. TagRegistrationViewModel)
            handler.HandleDeviceTagRead(epc, status, message, itemName, categoryName, locationName);
        }

        // Also update the BulkScan Scans list (handles both BulkScan-only view and any overlay views)
        Dispatcher.UIThread.Post(() =>
        {
            var existing = Scans.FirstOrDefault(s => s.Epc == epc);
            if (existing != null)
            {
                existing.Count++;
                if (!IsInventoryAuditMode)
                    SummaryText = $"{Scans.Count} items, total {Scans.Sum(s => s.Count)} scans";
                return;
            }

            // Determine audit match status when in Inventory Audit mode
            string auditStatus = status;
            string auditDetails = message;
            if (IsInventoryAuditMode)
            {
                if (_expectedEpcs.Contains(epc))
                {
                    bool isCorrectLocation = string.IsNullOrEmpty(_auditLocationName) ||
                                            locationName == _auditLocationName;
                    if (isCorrectLocation)
                    {
                        auditStatus = "Matched";
                        MatchedCount++;
                    }
                    else
                    {
                        auditStatus = "Wrong Location";
                        WrongLocationCount++;
                        auditDetails = $"Expected at '{_auditLocationName}', found at '{locationName}'";
                    }
                }
                else
                {
                    auditStatus = "Unknown";
                    UnknownCount++;
                }
                UpdateAuditSummary();
            }

            _scanCount++;
            Scans.Insert(0, new ScanEntry 
            { 
                Number = _scanCount, 
                Time = DateTime.Now.ToString("HH:mm:ss"),
                Epc = epc, 
                Status = auditStatus, 
                Details = auditDetails,
                ItemName = itemName ?? "", 
                CategoryName = categoryName ?? "Unknown",
                CurrentLocation = locationName ?? "N/A",
                Count = 1,
                PackingUnitCode = IsInventoryAuditMode ? null : SelectedPackingUnit?.Code
            });
            RefreshFilter();
            
            if (!IsInventoryAuditMode)
            {
                SummaryText = $"{Scans.Count} items, total {Scans.Sum(s => s.Count)} scans";
                IsMismatchDetected = TargetQuantity > 0 && Scans.Count != TargetQuantity;
            }
            else
            {
                IsMismatchDetected = UnknownCount > 0 || WrongLocationCount > 0 || Scans.Count != ExpectedCount;
            }
        
            ActionStatus = $"Tag: {epc[..Math.Min(8, epc.Length)]}... → {auditStatus}";

            if (auditStatus == "Error" || auditStatus == "Unknown" || auditStatus == "Wrong Location")
            {
                _reader.PlayAlarm();
            }
        });
    }

    private string GetLocalizedOperationName(string op) => op switch
    {
        "Inbound (Laundry Receive)" => L["DirtyInbound"],
        "Inbound (Hotel Receive)" => L["CleanInbound"],
        "Outbound (Hotel Send)" => L["DirtyOutbound"],
        "Outbound (Laundry Send)" => L["CleanOutbound"],
        "Inventory Audit" => L["Inventory"],
        "Condemned" => L["Condemned"],
        "Washing" => L["Washing"],
        "Drying" => L["Drying"],
        "Ironing" => L["Ironing"],
        "Folding" => L["Folding"],
        "Packing" => L["Packing"],
        "Return" => L["Return"],
        _ => op
    };

    private string GetLocalizedSubtitle(string op)
    {
        var isVi = L.CurrentCulture == "vi-VN";
        return op switch
        {
            "Inbound (Laundry Receive)" => isVi ? "Quét đồ vải bẩn nhận từ khách hàng để bắt đầu quy trình." : "Scan soiled linens received from customers to initiate the laundry cycle.",
            "Inbound (Hotel Receive)" => isVi ? "Xử lý và xác nhận đồ sạch trả lại cho các bộ phận khách sạn." : "Process and verify clean items being returned to the hotel departments.",
            "Outbound (Hotel Send)" => isVi ? "Gửi đồ bẩn đến cơ sở giặt bên ngoài để xử lý." : "Dispatch soiled linens to the external laundry facility for processing.",
            "Outbound (Laundry Send)" => isVi ? "Đóng gói và gửi đồ sạch lại cho khách hàng hoặc các bộ phận." : "Pack and send clean linens back to customers or departments.",
            "Inventory Audit" => isVi ? "Thực hiện quét toàn diện để đối soát tồn kho vật lý với hệ thống." : "Perform a comprehensive scan to reconcile physical inventory with the system.",
            "Condemned" => isVi ? "Loại bỏ các đồ vải bị hỏng không còn khả năng sử dụng." : "Decommission damaged items that are no longer fit for service.",
            "Washing" => isVi ? "Theo dõi giai đoạn làm sạch và khử trùng trong máy giặt." : "Track the cleaning and sanitization phase in the washing machines.",
            "Drying" => isVi ? "Giám sát sấy nhiệt độ cao để đảm bảo đồ sẵn sàng cho việc là." : "Monitor high-temperature drying to ensure items are ready for ironing.",
            "Ironing" => isVi ? "Ghi nhận việc là và ép chính xác để có thành phẩm cao cấp." : "Record precision pressing and ironing for a premium finish.",
            "Folding" => isVi ? "Phân loại và gấp đồ đã xử lý để lưu kho hoặc đóng gói." : "Group and fold processed items for organized storage or packing.",
            "Packing" => isVi ? "Đóng túi hoặc thùng an toàn để đảm bảo giao đồ sạch sẽ." : "Securely bag or box items to ensure clean delivery to their destination.",
            "Return" => isVi ? "Xử lý các trường hợp trả đồ đặc biệt do hỏng, lỗi hoặc sai sót." : "Handle specialized returns for damaged, rejected, or incorrect items.",
            _ => isVi ? "Quét và xử lý đồ vải bằng đầu đọc RFID" : "Scan and process items using the reader"
        };
    }

    [RelayCommand] private void SetLanguageEn() { L.SetLanguage("en-US"); RefreshLocalizedStrings(); }
    [RelayCommand] private void SetLanguageVi() { L.SetLanguage("vi-VN"); RefreshLocalizedStrings(); }

    private void RefreshLocalizedStrings()
    {
        OnPropertyChanged(nameof(L));
        CurrentOperationName = GetLocalizedOperationName(SelectedOperationType);
        CurrentOperationSubtitle = GetLocalizedSubtitle(SelectedOperationType);
        
        // Refresh dynamic status strings
        if (StatusText == "Ready" || StatusText == "Sẵn sàng") StatusText = L["Ready"];
        if (StatusText == "Scanning" || StatusText == "Đang quét") StatusText = L["Scanning"];
        if (StatusText == "Disconnected" || StatusText == "Mất kết nối") StatusText = L["Disconnected"];

        if (InventoryButtonText == "Start Scanning" || InventoryButtonText == "Bắt đầu quét") InventoryButtonText = L["StartScanning"];
        if (InventoryButtonText == "Stop Scanning" || InventoryButtonText == "Dừng quét") InventoryButtonText = L["StopScanning"];

        if (ReaderStatus == "Connected" || ReaderStatus == "Đã kết nối") ReaderStatus = L["Connected"];
        if (ReaderStatus == "Disconnected" || ReaderStatus == "Mất kết nối") ReaderStatus = L["Disconnected"];

        if (ApiStatus == "Connected" || ApiStatus == "Đã kết nối") ApiStatus = L["Connected"];
        if (ApiStatus == "Disconnected" || ApiStatus == "Mất kết nối") ApiStatus = L["Disconnected"];
    }

    private void HandleReaderStatusChanged(string status)
    {
        Dispatcher.UIThread.Post(() => { ReaderStatus = status; });
    }
}

public partial class ScanEntry : ObservableObject
{
    [ObservableProperty] private int _number;
    [ObservableProperty] private string _time = "";
    [ObservableProperty] private string _itemName = "";
    [ObservableProperty] private string _categoryName = "";
    [ObservableProperty] private string _currentLocation = "";
    [ObservableProperty] private string _epc = "";
    [ObservableProperty] private string _status = "";
    [ObservableProperty] private string _details = "";
    [ObservableProperty] private int _count;
    [ObservableProperty] private string? _packingUnitCode;

    public bool IsScanned => Status == "Scanned";
    public bool IsDuplicate => Status == "Duplicate";
    public bool IsError => Status == "Error";
    public bool IsUnknown => Status == "Unknown";
    public bool IsMatched => Status == "Matched";
    public bool IsWrongLocation => Status == "Wrong Location";
    public bool IsWrongFlow => Status == "Wrong Flow";

    public IBrush StatusColor => Status switch
    {
        "Matched"        => Brush.Parse("#059669"),
        "Scanned"        => Brush.Parse("#2563EB"),
        "Wrong Location" => Brush.Parse("#D97706"),
        "Wrong Flow"     => Brush.Parse("#C2410C"),
        "Unknown"        => Brush.Parse("#EF4444"),
        "Duplicate"      => Brush.Parse("#F59E0B"),
        "Error"          => Brush.Parse("#DC2626"),
        _                => Brushes.Gray
    };

    public IBrush RowBackground => Status switch
    {
        "Matched"        => Brush.Parse("#F0FDF4"),
        "Scanned"        => Brush.Parse("#EFF6FF"),
        "Wrong Location" => Brush.Parse("#FFFBEB"),
        "Wrong Flow"     => Brush.Parse("#FFF3EE"),
        "Unknown"        => Brush.Parse("#FFF1F2"),
        "Error"          => Brush.Parse("#FEE2E2"),
        "Duplicate"      => Brush.Parse("#FFF7ED"),
        _                => Brush.Parse("#FAFAFA")
    };
}