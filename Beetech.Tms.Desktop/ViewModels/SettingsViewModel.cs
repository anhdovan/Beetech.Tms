using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Threading.Tasks;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Beetech.Tms.Desktop.Models;
using Beetech.Tms.Desktop.Services;
using RestSharp;

namespace Beetech.Tms.Desktop.ViewModels;

public partial class SettingsViewModel : ObservableObject
{
    public LanguageManager L => LanguageManager.Instance;

    [ObservableProperty] private string _apiUrl;
    [ObservableProperty] private string _readerName;
    [ObservableProperty] private string _readerIp;
    [ObservableProperty] private LocationDto? _selectedLocationDto;
    [ObservableProperty] private bool _isTestMode;
    [ObservableProperty] private string _statusMessage;
    [ObservableProperty] private bool _isBusy;
    [ObservableProperty] private int _selectedRoleIndex;
    [ObservableProperty] private int _selectedLanguageIndex;
    private readonly ReaderManager _reader;

    public ObservableCollection<LocationDto> Locations { get; } = new();
    public string[] ReaderRoles => new[] { 
        "Standard (Terminal)",
        "Laundry Receive Monitor",
        "Hotel Receive Monitor",
        "Hotel Send Monitor",
        "Laundry Send Monitor",
        "Tag Registry Station",
        "Inventory Audit Station",
        "Condemned Monitor",
        "Washing Monitor",
        "Drying Monitor",
        "Ironing Monitor",
        "Folding Monitor"
    };
    public string[] Languages => new[] { "Vietnamese (vi-VN)", "English (en-US)" };

    public SettingsViewModel(ReaderManager reader)
    {
        _reader = reader;
        var s = SettingsService.Current;
        _apiUrl = s.ApiUrl;
        _readerName = s.ReaderName;
        _readerIp = s.ReaderIp;
        _selectedRoleIndex = (int)(s.Role ?? TmsReaderRole.Standard);
        _selectedLanguageIndex = s.Language == "en-US" ? 1 : 0;
        _statusMessage = "Ready";

        // Load locations
        _ = LoadLocationsAsync(s.Location);
    }

    private async Task LoadLocationsAsync(string? defaultLocation = null)
    {
        var locations = await _reader.GetLocationsAsync();
        
        Avalonia.Threading.Dispatcher.UIThread.Post(() =>
        {
            Locations.Clear();
            foreach (var loc in locations) Locations.Add(loc);

            if (!string.IsNullOrEmpty(defaultLocation))
            {
                SelectedLocationDto = locations.FirstOrDefault(l => l.Name == defaultLocation);
            }
            
            if (SelectedLocationDto == null && locations.Any())
            {
                SelectedLocationDto = locations.First();
            }
        });
    }

    [RelayCommand]
    private async Task CheckSettingsAsync()
    {
        IsBusy = true;
        StatusMessage = "Checking connectivity...";
        
        try
        {
            // 1. Check API
            var client = new RestClient(ApiUrl);
            var request = new RestRequest("api/health", Method.Get); // Assuming a health endpoint exists or just ping
            var response = await client.ExecuteAsync(request);
            
            bool apiOk = response.IsSuccessful || response.StatusCode == System.Net.HttpStatusCode.NotFound; // NotFound is fine if endpoint doesn't exist but server responds
            
            // 2. Check Reader (Ping simulation or actual connect)
            bool readerOk = IsTestMode || await Task.Run(() => {
                try {
                    using var ping = new System.Net.NetworkInformation.Ping();
                    var reply = ping.Send(ReaderIp, 1000);
                    return reply.Status == System.Net.NetworkInformation.IPStatus.Success;
                } catch { return false; }
            });

            if (apiOk && readerOk)
            {
                StatusMessage = "✅ All systems functional!";
                _reader.ApiUrl = ApiUrl; // Update manager with new URL
                await LoadLocationsAsync();
            }
            else
            {
                StatusMessage = $"❌ Error: {(apiOk ? "" : "API UNREACHABLE; ")}{(readerOk ? "" : "READER NOT FOUND")}";
            }
        }
        catch (Exception ex)
        {
            StatusMessage = $"❌ Error: {ex.Message}";
        }
        finally
        {
            IsBusy = false;
            SaveSettings();
        }
    }

    private void SaveSettings()
    {
        var s = SettingsService.Current;
        s.ApiUrl = ApiUrl;
        s.ReaderName = ReaderName;
        s.ReaderIp = ReaderIp;
        s.Location = SelectedLocationDto?.Name;
        s.IsTestMode = IsTestMode;
        s.Role = (TmsReaderRole)SelectedRoleIndex;
        s.Language = SelectedLanguageIndex == 1 ? "en-US" : "vi-VN";
        
        LanguageManager.Instance.SetLanguage(s.Language);
        SettingsService.Save();
    }
}
