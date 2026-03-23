using Avalonia;
using Avalonia.Controls;
using Avalonia.Input;

namespace Beetech.Tms.Desktop.Views;

public partial class MainWindow : Window
{
    public MainWindow()
    {
        InitializeComponent();
    }

    private void OnPointerPressed(object? sender, PointerPressedEventArgs e)
    {
        if (e.ClickCount == 2)
        {
            ToggleMaximize();
        }
        else
        {
            this.BeginMoveDrag(e);
        }
    }

    private void ToggleMaximize()
    {
        WindowState = WindowState == WindowState.Maximized 
            ? WindowState.Normal 
            : WindowState.Maximized;
    }

    private void Minimize_Click(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
    {
        WindowState = WindowState.Minimized;
    }

    private void Maximize_Click(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
    {
        ToggleMaximize();
    }

    private void Close_Click(object? sender, Avalonia.Interactivity.RoutedEventArgs e)
    {
        Close();
    }
}