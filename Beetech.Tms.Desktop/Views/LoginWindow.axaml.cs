using Avalonia.Controls;
using Avalonia.Interactivity;
using Beetech.Tms.Desktop.ViewModels;

namespace Beetech.Tms.Desktop.Views;

public partial class LoginWindow : Window
{
    public LoginWindow()
    {
        InitializeComponent();
        var viewModel = new LoginViewModel();
        DataContext = viewModel;

        viewModel.OnLoginFinished += (success) =>
        {
            Close(success);
        };
    }
}