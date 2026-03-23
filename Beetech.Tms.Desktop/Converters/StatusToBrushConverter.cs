using Avalonia.Data.Converters;
using Avalonia.Media;
using System;
using System.Globalization;

namespace Beetech.Tms.Desktop.Converters;

public class StatusToBrushConverter : IValueConverter
{
    public object? Convert(object? value, Type targetType, object? parameter, CultureInfo culture)
    {
        var status = value?.ToString() ?? "";
        return status switch
        {
            "Scanned" => new SolidColorBrush(Color.Parse("#3B82F6")), // Blue
            "Writing..." => new SolidColorBrush(Color.Parse("#F59E0B")), // Yellow/Orange
            "Success" => new SolidColorBrush(Color.Parse("#10B981")), // Green
            "Write Failed" => new SolidColorBrush(Color.Parse("#EF4444")), // Red
            _ => Brushes.Gray
        };
    }

    public object? ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture)
    {
        throw new NotImplementedException();
    }
}
