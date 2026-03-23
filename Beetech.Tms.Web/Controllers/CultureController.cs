using Microsoft.AspNetCore.Localization;
using Microsoft.AspNetCore.Mvc;

namespace Beetech.Tms.Web.Controllers;

[Route("[controller]/[action]")]
public class CultureController : Controller
{
    public IActionResult SetCulture(string culture, string redirectUri)
    {
        if (culture != null)
        {
            Response.Cookies.Append(
                CookieRequestCultureProvider.DefaultCookieName,
                CookieRequestCultureProvider.MakeCookieValue(new RequestCulture(culture)));
        }

        return LocalRedirect(redirectUri);
    }
}
