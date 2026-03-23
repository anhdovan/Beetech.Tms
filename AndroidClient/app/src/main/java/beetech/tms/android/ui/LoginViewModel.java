package beetech.tms.android.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import beetech.tms.android.api.TmsApi;
import beetech.tms.android.api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<TmsApi.LoginResponse> loginResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<TmsApi.LoginResponse> getLoginResponse() {
        return loginResponse;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void login(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            errorMessage.setValue("Vui lòng nhập tên đăng nhập và mật khẩu");
            return;
        }

        isLoading.setValue(true);
        TmsApi.LoginRequest request = new TmsApi.LoginRequest(username, password);

        RetrofitClient.getApi().login(request).enqueue(new Callback<TmsApi.LoginResponse>() {
            @Override
            public void onResponse(Call<TmsApi.LoginResponse> call, Response<TmsApi.LoginResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    loginResponse.setValue(response.body());
                } else {
                    errorMessage.setValue("Sai tên đăng nhập hoặc mật khẩu");
                }
            }

            @Override
            public void onFailure(Call<TmsApi.LoginResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}
