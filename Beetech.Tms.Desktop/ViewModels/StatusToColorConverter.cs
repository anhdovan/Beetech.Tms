using System;
using System.Globalization;
using Avalonia.Data.Converters;
using Avalonia.Media;

namespace Beetech.Tms.Desktop.ViewModels;

public class StatusToColorConverter : IValueConverter
{
    public object Convert(object? value, Type targetType, object? parameter, CultureInfo culture)
    {
        string status = value?.ToString() ?? "";
        
        // Use Application Resources to get the high-contrast brushes defined in App.axaml
        if (App.Current?.Resources.TryGetResource(status.Contains("✅") ? "TmsGreenBrush" : 
                                                 status.Contains("❌") ? "TmsRedBrush" : 
                                                 status.Contains("Checking") ? "TmsOrangeBrush" : "Gray", 
                                                 App.Current.ActualThemeVariant, out var brush) == true)
        {
            return brush!;
        }

        if (status.Contains("✅")) return Brushes.Green;
        if (status.Contains("❌")) return Brushes.Red;
        if (status.Contains("Checking")) return Brushes.Orange;
        return Brushes.Gray;
    }

    public object ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture)
    {
        throw new NotSupportedException();
    }
}
