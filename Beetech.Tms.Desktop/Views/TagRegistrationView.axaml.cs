using Avalonia.Controls;
using Avalonia.Markup.Xaml;

namespace Beetech.Tms.Desktop.Views;

public partial class TagRegistrationView : UserControl
{
    public TagRegistrationView()
    {
        InitializeComponent();
    }

    private void InitializeComponent()
    {
        AvaloniaXamlLoader.Load(this);
    }
}
