using System;
using System.Globalization;
using System.Threading.Tasks;
using Avalonia.Data.Converters;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Beetech.Tms.Desktop.Services;
using Material.Icons;

namespace Beetech.Tms.Desktop.ViewModels;

public partial class LoginViewModel : ObservableObject
{
    public LanguageManager L => LanguageManager.Instance;

    [ObservableProperty]
    private string _username = "admin@beetech.com";

    [ObservableProperty]
    private string _password = "User@123";

    [ObservableProperty]
    private string _serverUrl = "http://localhost:5269/";

    [ObservableProperty]
    private string _errorMessage = string.Empty;

    [ObservableProperty]
    private bool _isBusy;

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(PasswordIcon))]
    private bool _isPasswordVisible;

    public MaterialIconKind PasswordIcon => IsPasswordVisible ? MaterialIconKind.EyeOff : MaterialIconKind.Eye;

    public bool LoginSucceeded { get; private set; }
    public event Action<bool>? OnLoginFinished;

    [RelayCommand]
    private void TogglePasswordVisibility()
    {
        IsPasswordVisible = !IsPasswordVisible;
    }

    [RelayCommand]
    private async Task LoginAsync()
    {
        ErrorMessage = string.Empty;

        if (string.IsNullOrWhiteSpace(Username) || string.IsNullOrWhiteSpace(Password))
        {
            ErrorMessage = "Username and password are required.";
            return;
        }

        if (string.IsNullOrWhiteSpace(ServerUrl))
        {
            ErrorMessage = "Server URL is required.";
            return;
        }

        IsBusy = true;
        try
        {
            var result = await AuthService.LoginAsync(ServerUrl, Username, Password);
            if (result != null)
            {
                LoginSucceeded = true;
                OnLoginFinished?.Invoke(true);
            }
            else
            {
                ErrorMessage = "Invalid username or password.";
            }
        }
        catch (Exception ex)
        {
            ErrorMessage = $"Connection error: {ex.Message}";
        }
        finally
        {
            IsBusy = false;
        }
    }
}