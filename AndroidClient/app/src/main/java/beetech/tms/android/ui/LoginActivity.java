package beetech.tms.android.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import beetech.tms.android.MainActivity;
import beetech.tms.android.R;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private TextInputEditText etUsername, etPassword;
    private TextInputLayout tilUsername, tilPassword;
    private MaterialButton btnLogin;
    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if already logged in
        SharedPreferences prefs = getSharedPreferences("beetech_tms_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token != null) {
            startMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        initViews();
        setupViewModel();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        btnLogin = findViewById(R.id.btn_login);
        pbLoading = findViewById(R.id.pb_loading);

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.getLoginResponse().observe(this, response -> {
            if (response != null) {
                saveUserData(response.token, response.username, response.fullName, response.role);
                fadeOutAndStartMainActivity();
            }
        });

        viewModel.getIsLoading().observe(this, loading -> {
            pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!loading);
            etUsername.setEnabled(!loading);
            etPassword.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isValid = true;
        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.error_field_empty));
            isValid = false;
        } else {
            tilUsername.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_field_empty));
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        if (isValid) {
            viewModel.login(username, password);
        }
    }

    private void saveUserData(String token, String username, String fullName, String role) {
        SharedPreferences.Editor editor = getSharedPreferences("beetech_tms_prefs", Context.MODE_PRIVATE).edit();
        editor.putString("token", token);
        editor.putString("username", username);
        editor.putString("full_name", fullName);
        editor.putString("role", role);
        editor.apply();
    }

    private void fadeOutAndStartMainActivity() {
        View rootView = findViewById(R.id.root_view);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startMainActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        rootView.startAnimation(fadeOut);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
