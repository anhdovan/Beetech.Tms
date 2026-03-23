using Avalonia.Controls;
using Avalonia.Markup.Xaml;

namespace Beetech.Tms.Desktop.Views;

public partial class SplashWindow : Window
{
    public SplashWindow()
    {
        InitializeComponent();
        DataContext = Beetech.Tms.Desktop.Services.LanguageManager.Instance;
    }

    private void InitializeComponent()
    {
        AvaloniaXamlLoader.Load(this);
    }
}
