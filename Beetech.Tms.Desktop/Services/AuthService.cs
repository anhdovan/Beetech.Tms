using System;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;
using Beetech.Tms.Desktop.Models;

namespace Beetech.Tms.Desktop.Services;

public static class AuthService
{
    private static readonly HttpClient _http = new();

    public static LoginResult? CurrentSession { get; private set; }
    public static bool IsLoggedIn => CurrentSession != null;
    public static string ServerUrl { get; private set; } = string.Empty;
    public static string BearerToken => CurrentSession?.Token ?? "";

    public static async Task<LoginResult?> LoginAsync(string baseUrl, string username, string password)
    {
        try
        {
            ServerUrl = baseUrl.TrimEnd('/');
            var url = $"{ServerUrl}/api/mobile/login";

            var payload = JsonSerializer.Serialize(new
            {
                username = username,
                password = password
            });

            var content = new StringContent(payload, Encoding.UTF8, "application/json");
            var response = await _http.PostAsync(url, content);

            if (!response.IsSuccessStatusCode)
                return null;

            var json = await response.Content.ReadAsStringAsync();
            var result = JsonSerializer.Deserialize<LoginResult>(json, new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true
            });

            CurrentSession = result;
            return result;
        }
        catch (Exception)
        {
            return null;
        }
    }

    public static void Logout()
    {
        CurrentSession = null;
    }
}