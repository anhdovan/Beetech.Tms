using System;
using System.IO;
using System.Text.Json;
using Beetech.Tms.Desktop.Models;

namespace Beetech.Tms.Desktop.Services;

public class AppSettings
{
    public string ReaderIp { get; set; } = "192.168.0.101";
    public string ReaderName { get; set; } = "Fixed Reader 01";
    public string ApiUrl { get; set; } = "http://localhost:5269/";
    public string Location { get; set; } = "Warehouse A";
    public bool IsTestMode { get; set; } = false;
    public string LastUsername { get; set; } = "admin";
    public string Language { get; set; } = "vi-VN";
    public TmsReaderRole? Role { get; set; } = TmsReaderRole.Standard;
}

public static class SettingsService
{
    private static readonly string SettingsPath = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
        "Beetech", "Tms", "settings.json");

    public static event Action? OnSettingsChanged;
    public static AppSettings Current { get; private set; } = new();

    static SettingsService()
    {
        Load();
    }

    public static void Load()
    {
        try
        {
            if (File.Exists(SettingsPath))
            {
                var json = File.ReadAllText(SettingsPath);
                Current = JsonSerializer.Deserialize<AppSettings>(json) ?? new AppSettings();
            }
        }
        catch
        {
            Current = new AppSettings();
        }
        
        // Ensure language is applied on load
        LanguageManager.Instance.SetLanguage(Current.Language);
    }

    public static void Save()
    {
        try
        {
            var directory = Path.GetDirectoryName(SettingsPath);
            if (directory != null && !Directory.Exists(directory))
            {
                Directory.CreateDirectory(directory);
            }

            var json = JsonSerializer.Serialize(Current, new JsonSerializerOptions { WriteIndented = true });
            File.WriteAllText(SettingsPath, json);
        }
        catch
        {
            // Ignore save errors
        }
        OnSettingsChanged?.Invoke();
    }
}
