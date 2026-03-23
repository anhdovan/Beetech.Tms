package beetech.tms.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

import beetech.app.core.AdvBaseReader;
import beetech.app.core.BaseActivity;
import beetech.app.core.interfaces.IBarcodeReceiver;
import beetech.app.core.interfaces.IKeyReceiver;
import beetech.tms.android.ui.BaseFragment;
import beetech.tms.android.ui.ItemFragment;
import beetech.tms.android.ui.LoginActivity;
import beetech.tms.android.ui.WriteTagFragment;
import beetech.tms.android.ui.InventoryFragment;
import beetech.tms.android.ui.LocateFragment;
import beetech.tms.android.ui.SettingsFragment;
import beetech.tms.android.ui.TagManagementFragment;
import beetech.tms.android.ui.WorkflowFragment;
import beetech.tms.android.utils.SettingsManager;
import beetech.tms.android.ui.AppInfoFragment;

public class MainActivity extends BaseActivity {

    private AdvBaseReader reader;
    private ArrayList<IKeyReceiver> mkeyReceivers = new ArrayList<>();
    private ArrayList<IBarcodeReceiver> mbarcodeReceivers = new ArrayList<>();

    public void registerBarcodeReceiver(IBarcodeReceiver listener) {
        mbarcodeReceivers.add(listener);
    }

    public void registerKeyReceiver(IKeyReceiver listener) {
        mkeyReceivers.add(listener);
    }

    public void unregisterKeyReceiver(IKeyReceiver receiver) {
        mkeyReceivers.remove(receiver);
    }

    public void unregisterBarcodeReceiver(IBarcodeReceiver receiver) {
        mbarcodeReceivers.remove(receiver);
    }

    public AdvBaseReader getReader() {
        if (reader == null)
            initReader();
        return reader;
    }

    private void initReader() {
        if (reader != null && reader.inited)
            return;
        try {
            // Defaulting to Urovo DT50P as per old project's active logic
            String readerClass = "beetech.app.readers.UrovoDT50P";
            Class<?> t = Class.forName(readerClass);
            reader = (AdvBaseReader) t.getDeclaredConstructor().newInstance();
            Log.i("MainActivity", "initReader: setting activity and initializing soundPool");
            reader.activity = this;
            reader.initReader(0); // Standard init

            // Apply settings from SettingsManager
            SettingsManager settings = SettingsManager.getInstance();
            reader.setPower(settings.getReaderPower());
            reader.isMute = !settings.isBeepEnabled();

            Log.i("MainActivity", "initReader: calling initSoundPool()");
            reader.initSoundPool();
            reader.inited = true; // Mark as inited to prevent redundant calls
        } catch (Exception e) {
            Log.e("MainActivity", "initReader failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public beetech.app.core.network.WebSocketConnector getConnector() {
        return null; // TODO: Implement if needed
    }

    public beetech.app.core.dto.ReaderSettings getReaderSettings() {
        return null; // TODO: Implement if needed
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        android.util.Log.d("MainActivity", "onKeyDown: keyCode=" + keyCode);
        for (IKeyReceiver k : mkeyReceivers) {
            k.onActivityKeyDown(keyCode, event);
        }
        // Only return true (consuming the event) if it's a known trigger key
        if (keyCode == 520 || keyCode == 521 || keyCode == 523 || 
            keyCode == 139 || keyCode == 142 || keyCode == 280 || 
            keyCode == 291 || keyCode == 292 || keyCode == 293 || keyCode == 298) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        for (IKeyReceiver k : mkeyReceivers) {
            k.onActivityKeyUp(keyCode, event);
        }
        if (keyCode == 520 || keyCode == 521 || keyCode == 523 || 
            keyCode == 139 || keyCode == 142 || keyCode == 280 || 
            keyCode == 291 || keyCode == 292 || keyCode == 293 || keyCode == 298) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TmsApp.getInstance().setMainActivity(this);
        setContentView(R.layout.activity_main);

        setupToolbar();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            Bundle bundle = new Bundle();
            int id = item.getItemId();
            
            if (id == R.id.nav_items) {
                fragment = new ItemFragment();
            } else if (id == R.id.nav_audit) {
                fragment = new InventoryFragment();
            } else if (id == R.id.nav_tagging) {
                fragment = new TagManagementFragment();
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
            } else {
                fragment = new WorkflowFragment();
                if (id == R.id.nav_washing) bundle.putString("OPERATION", "Washing");
                else if (id == R.id.nav_drying) bundle.putString("OPERATION", "Drying");
                else if (id == R.id.nav_ironing) bundle.putString("OPERATION", "Ironing");
                else if (id == R.id.nav_folding) bundle.putString("OPERATION", "Folding");
                else if (id == R.id.nav_packing) bundle.putString("OPERATION", "Packing");
                else if (id == R.id.nav_return) bundle.putString("OPERATION", "Return");
                else if (id == R.id.nav_inbound_laundry) bundle.putString("OPERATION", "LaundryReceive");
                else if (id == R.id.nav_inbound_hotel) bundle.putString("OPERATION", "Inbound");
                else if (id == R.id.nav_outbound_hotel) bundle.putString("OPERATION", "Outbound");
                else if (id == R.id.nav_outbound_laundry) bundle.putString("OPERATION", "LaundrySend");
                else if (id == R.id.nav_internal_transfer) bundle.putString("OPERATION", "InternalTransfer");
                else if (id == R.id.nav_condemned) bundle.putString("OPERATION", "Condemned");
                
                fragment.setArguments(bundle);
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        // Default fragment
        if (savedInstanceState == null) {
            navView.setCheckedItem(R.id.nav_inbound_laundry);
            Fragment f = new WorkflowFragment();
            Bundle b = new Bundle();
            b.putString("OPERATION", "LaundryReceive");
            f.setArguments(b);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).commit();
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        SharedPreferences prefs = getSharedPreferences("beetech_tms_prefs", Context.MODE_PRIVATE);
        String fullName = prefs.getString("full_name", "User");
        toolbar.setSubtitle(fullName);

        toolbar.inflateMenu(R.menu.toolbar_menu);

        // Apply tint to menu icons programmatically for compatibility
        if (toolbar.getMenu() != null) {
            for (int i = 0; i < toolbar.getMenu().size(); i++) {
                android.view.MenuItem item = toolbar.getMenu().getItem(i);
                android.graphics.drawable.Drawable icon = item.getIcon();
                if (icon != null && item.getItemId() != R.id.action_info) {
                    icon.setTint(getResources().getColor(android.R.color.white));
                }
            }
        }

        // Tint overflow icon (three dots)
        if (toolbar.getOverflowIcon() != null) {
            toolbar.getOverflowIcon().setTint(getResources().getColor(android.R.color.white));
        }

        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_logout) {
                handleLogout();
                return true;
            } else if (id == R.id.action_info) {
                showAppInfo();
                return true;
            }
            return false;
        });
    }

    private void showAppInfo() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AppInfoFragment())
                .addToBackStack(null)
                .commit();
    }

    private void handleLogout() {
        SharedPreferences prefs = getSharedPreferences("beetech_tms_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void selectWriteTag(Bundle bundle) {
        WriteTagFragment fragment = new WriteTagFragment();
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void selectItems(Bundle bundle) {
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setCheckedItem(R.id.nav_items);
        
        ItemFragment fragment = new ItemFragment();
        if (bundle != null) fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
