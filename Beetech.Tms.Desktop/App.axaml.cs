using Avalonia;
using Avalonia.Threading;
using Avalonia.Controls.ApplicationLifetimes;
using Avalonia.Data.Core;
using Avalonia.Data.Core.Plugins;
using System.Linq;
using Avalonia.Markup.Xaml;
using Beetech.Tms.Desktop.ViewModels;
using Beetech.Tms.Desktop.Views;
using System;

namespace Beetech.Tms.Desktop;

public partial class App : Application
{
    public override void Initialize()
    {
        AvaloniaXamlLoader.Load(this);
        
        // UI Thread Exception Handler
        Dispatcher.UIThread.UnhandledException += (s, e) =>
        {
            LogException(e.Exception);
            e.Handled = true; // Try to keep the app alive
        };
    }

    private void LogException(Exception ex)
    {
        if (ex == null) return;
        try
        {
            var logFile = System.IO.Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "crash.log");
            var message = $"[{DateTime.Now}] UI EXCEPTION:\n{ex}\n\n";
            System.IO.File.AppendAllText(logFile, message);
        }
        catch { }
    }

    public override async void OnFrameworkInitializationCompleted()
    {
        if (ApplicationLifetime is IClassicDesktopStyleApplicationLifetime desktop)
        {
            DisableAvaloniaDataAnnotationValidation();

            // Force load settings early (applies language and config)
            Services.SettingsService.Load();

            // 1. Show Splash screen
            var splash = new SplashWindow();
            desktop.MainWindow = splash;
            splash.Show();

            // 2. Wait for splash (and potentially load data)
            await System.Threading.Tasks.Task.Delay(2500);

            // 3. Start Login/Main loop
            ShowLogin(desktop);
        }

        base.OnFrameworkInitializationCompleted();
    }

    private void ShowLogin(IClassicDesktopStyleApplicationLifetime desktop)
    {
        var loginWindow = new LoginWindow();
        var oldWindow = desktop.MainWindow;
        desktop.MainWindow = loginWindow;
        loginWindow.Show();
        oldWindow?.Close();

        loginWindow.Closed += (_, _) =>
        {
            if (Services.AuthService.IsLoggedIn)
            {
                var mainWindow = new MainWindow
                {
                    DataContext = new MainWindowViewModel(),
                };
                desktop.MainWindow = mainWindow;
                mainWindow.Show();

                // Handle Logout or Close
                mainWindow.Closed += (s, e) =>
                {
                    if (!Services.AuthService.IsLoggedIn)
                    {
                        // User logged out, return to login
                        ShowLogin(desktop);
                    }
                    else
                    {
                        // Real exit
                        desktop.Shutdown();
                    }
                };
            }
            else
            {
                desktop.Shutdown();
            }
        };
    }

    private void DisableAvaloniaDataAnnotationValidation()
    {
        var dataValidationPluginsToRemove =
            BindingPlugins.DataValidators.OfType<DataAnnotationsValidationPlugin>().ToArray();

        foreach (var plugin in dataValidationPluginsToRemove)
        {
            BindingPlugins.DataValidators.Remove(plugin);
        }
    }
}