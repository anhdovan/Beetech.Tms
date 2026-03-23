package beetech.app.core.data;

import static com.unitech.lib.v2.lib.util.diagnotics.EALog.INFO;

import android.content.Context;
import android.content.SharedPreferences;


import com.unitech.lib.v2.lib.util.StringUtil;
import com.unitech.lib.v2.lib.util.diagnotics.EALog;
import com.unitech.lib.v2.transport.types.ConnectType;

import java.util.Locale;

public class GlobalData {
	
	private static final String TAG = GlobalData.class.getSimpleName();

	private static final String KEY_DEVICE_NAME = "device name";
	private static final String KEY_DEVICE_ADDRESS = "device address";
	
	private static final String DEFAULT_VALUE_NAME = "";
	private static final String DEFAULT_VALUE_ADDRESS = "";
	
	// ------------------------------------------------------------------------
	// Global Data
	// ------------------------------------------------------------------------
	
	public static boolean isSupportBluetooth = false;
	
	public static boolean isEnableBluetooth = false;


	// ------------------------------------------------------------------------
	// Load/Save Configuration
	// ------------------------------------------------------------------------

	// Load Config
	public static synchronized DeviceItem loadDeviceInfo(Context context, ConnectType type) {
		String key = null;
		String pakageName = context.getPackageName();
		SharedPreferences prefs = context.getSharedPreferences(pakageName, Context.MODE_PRIVATE);
		
		key = String.format(Locale.US, "%s%s", type.toString(), KEY_DEVICE_NAME);
		String name = prefs.getString(key, DEFAULT_VALUE_NAME);
		
		EALog.i(TAG, INFO, "INFO. loadDeviceInfo() - ConnectType[%s] Name [%s]",
				type.toString(), name);
		
		key = String.format(Locale.US, "%s%s", type.toString(), KEY_DEVICE_ADDRESS);
		String address = prefs.getString(key, DEFAULT_VALUE_ADDRESS);
		
		EALog.i(TAG, INFO, "INFO. loadDeviceInfo() - ConnectType[%s] Address [%s]",
				type.toString(), address);
		
		DeviceItem item = null;
		if (!StringUtil.isNullOrEmpty(name) && !StringUtil.isNullOrEmpty(address)) {
			item = new DeviceItem(type, name, address);
		}
		
		return item;
	}

	
	// Save Config
	public static synchronized boolean saveDeviceInfo(Context context, DeviceItem item) {
		boolean result = true;
		
		String key = null;
		String pakageName = context.getPackageName();
		SharedPreferences prefs = context.getSharedPreferences(pakageName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		
		key = String.format(Locale.US, "%s%s", item.getConnectType().toString(), KEY_DEVICE_NAME);
		editor.putString(key, item.getName());
		EALog.i(TAG, INFO, "INFO. saveDeviceInfo() - ConnectType[%s] Name [%s]",
				item.getConnectType().toString(), item.getName());
		
		key = String.format(Locale.US, "%s%s", item.getConnectType().toString(), KEY_DEVICE_ADDRESS);
		editor.putString(key, item.getAddress());
		EALog.i(TAG, INFO, "INFO. saveDeviceInfo() - ConnectType[%s] Name [%s]",
				item.getConnectType().toString(), item.getAddress());
		
		result = editor.commit();
		EALog.i(TAG, INFO, "INFO. saveDeviceInfo() - [%s]", result);
		return result;
	}
}
