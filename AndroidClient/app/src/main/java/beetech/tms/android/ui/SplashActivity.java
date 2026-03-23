package beetech.tms.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import beetech.tms.android.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LinearLayout llBranding = findViewById(R.id.ll_branding);
        LinearLayout llFooter = findViewById(R.id.ll_footer);

        Animation fadeInUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        llBranding.startAnimation(fadeInUp);
        llFooter.startAnimation(fadeInUp);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}
