package beetech.app.core.utils;


import com.unitech.lib.v2.lib.types.DeviceType;
import com.unitech.lib.v2.transport.types.ConnectType;

import beetech.app.rfidreader.R;

public class ResUtil {

	public static int getProductImage(DeviceType type) {
		switch (type) {
			case RP902:
			return R.drawable.ic_product_rp902;
		default:
			return R.drawable.ic_unknown;
		}
	}
	
	public static int getConnectTypeImage(ConnectType type) {
		switch (type) {
		case Bluetooth:
			return R.drawable.ic_connect_type_bluetooth;
		case USB:
			return R.drawable.ic_connect_type_usb;
		default:
			return R.drawable.ic_unknown;
		}
	}
}
