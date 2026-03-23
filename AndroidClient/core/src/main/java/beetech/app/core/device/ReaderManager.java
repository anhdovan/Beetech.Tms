package beetech.app.core.device;


import com.unitech.lib.v2.device.RP100Reader;
import com.unitech.lib.v2.lib.types.DeviceType;
import com.unitech.lib.v2.lib.util.StringUtil;
import com.unitech.lib.v2.lib.util.diagnotics.EALog;
import com.unitech.lib.v2.reader.EAReader;
import com.unitech.lib.v2.transport.ATransport;
import com.unitech.lib.v2.transport.ATransportBluetooth;
import com.unitech.lib.v2.transport.ATransportBluetoothLe;
import com.unitech.lib.v2.transport.ATransportUsb;
import com.unitech.lib.v2.transport.types.ConnectType;

public class ReaderManager {
	private static final String TAG = ReaderManager.class.getSimpleName();

	private static final String DEVTYPE_RP100 = "RP100";
	private static final String DEVTYPE_RP200 = "RP200";
	private static final String DEVTYPE_RP902 = "RP902";

	private static EAReader mReader = null;
	
	public static DeviceType parseType(String name) {
	
		if (StringUtil.isNullOrEmpty(name))
			return DeviceType.Unknown;
		
		if (name.contains(DEVTYPE_RP100)) {
			return DeviceType.RP100;
		} else if (name.contains(DEVTYPE_RP200)) {
			return DeviceType.RP200;
		}else if (name.contains(DEVTYPE_RP902)){
			return DeviceType.RP902;
		}
		
		return DeviceType.Unknown;
	}
	
	public static ATransport getTransport(ConnectType connType, String name, String address , boolean autoConnect) {
		ATransport transport = null;
		
		DeviceType type = parseType(name);
		
		switch(connType) {
		case Bluetooth :
			transport = new ATransportBluetooth(type, name, address);
			break;

		case BluetoothLe:
			transport = new ATransportBluetoothLe(type, name, address);
			break;

		case USB :
			transport = new ATransportUsb(type, name, address, autoConnect);
			break;
			case  UART:
				transport = new ATransportUsb(type, name, address, autoConnect);
				break;
		default :
			EALog.e(TAG, "ERROR. getTransport([%s]) - Failed to not support connect type", connType.toString());
			return null;
		}

		return transport;
	}
	
	public static EAReader getReader(ConnectType connType, String name, String address, boolean autoConnect) {
		ATransport transport = null;
		
		if ((transport = getTransport(connType, name, address, autoConnect)) == null) {
			EALog.e(TAG, "ERROR. getReader(%s) - Unsupported connection type", connType.toString());
			return null;
		}
		
		switch (transport.getDeviceType()) {
		case RP100:
		case RP200:
		case RP902:
			mReader = new RP100Reader(transport);
			break;
			
		default :
			if (connType == ConnectType.USB||connType==ConnectType.UART) {
				mReader = new RP100Reader(transport);
			} else {
				EALog.e(TAG, "ERROR. getReader(%s) - Unsupported device type", name);
				return null;
			}
			
			break;
		}
		
		return mReader;
	}
	
	public static EAReader getReader(ATransport transport) {
		
		if (transport == null) {
			EALog.e(TAG, "ERROR. getReader(%s) - transport is null");
			return null;
		}
		
		switch (transport.getDeviceType()) {
		case RP100:
		case RP200:
			mReader = new RP100Reader(transport);
			break;
			
		default :
			if (transport.getConnectType() == ConnectType.USB) {
				mReader = new RP100Reader(transport);
			} else {
				EALog.e(TAG, "ERROR. getReader(%s) - Unsupported device type", transport.getDeviceName().toString());
				return null;
			}
			
			break;
		}
		
		return mReader;
	}
	
	public static EAReader getReader() {
		return mReader;
	}
}
