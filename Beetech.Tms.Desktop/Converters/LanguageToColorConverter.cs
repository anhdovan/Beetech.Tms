using System;
using System.Globalization;
using Avalonia.Data.Converters;
using Avalonia.Media;

namespace Beetech.Tms.Desktop.Converters;

public class LanguageToColorConverter : IValueConverter
{
    public object Convert(object? value, Type targetType, object? parameter, CultureInfo culture)
    {
        string currentCulture = value?.ToString() ?? "";
        string targetCulture = parameter?.ToString() ?? "";

        // Highlight yellow if active, otherwise white
        return currentCulture == targetCulture ? Brushes.Yellow : Brushes.White;
    }

    public object ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture)
    {
        throw new NotSupportedException();
    }
}
