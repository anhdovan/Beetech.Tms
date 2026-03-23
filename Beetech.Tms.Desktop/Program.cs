using Avalonia;
using System;
using System.IO;
using System.Threading.Tasks;

namespace Beetech.Tms.Desktop;

sealed class Program
{
    [STAThread]
    public static void Main(string[] args)
    {
        try
        {
            // Global handlers for non-UI threads
            AppDomain.CurrentDomain.UnhandledException += (s, e) => LogException(e.ExceptionObject as Exception);
            TaskScheduler.UnobservedTaskException += (s, e) => LogException(e.Exception);

            BuildAvaloniaApp().StartWithClassicDesktopLifetime(args);
        }
        catch (Exception ex)
        {
            LogException(ex);
        }
    }

    public static AppBuilder BuildAvaloniaApp()
        => AppBuilder.Configure<App>()
            .UsePlatformDetect()
            .WithInterFont()
            .LogToTrace();

    private static void LogException(Exception? ex)
    {
        if (ex == null) return;
        try
        {
            var logFile = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "crash.log");
            var message = $"[{DateTime.Now}] FATAL EXCEPTION:\n{ex}\n\n";
            File.AppendAllText(logFile, message);
        }
        catch { }
    }
}