package beetech.app.core.data;

import android.os.Parcel;
import android.os.Parcelable;


import com.unitech.lib.v2.lib.types.DeviceType;
import com.unitech.lib.v2.lib.util.StringUtil;
import com.unitech.lib.v2.transport.types.ConnectType;

import java.util.Locale;

public class DeviceItem implements Parcelable {

	private static final String DEVTYPE_RP100 = "RP100";
	private static final String DEVTYPE_RP200 = "RP200";
	private static final String DEVTYPE_RP902 = "RP902";

	private  volatile  String mclassName;
	private volatile ConnectType mConnType;
	private volatile DeviceType mType;
	private volatile String mName;
	private volatile String mMac;
	private volatile String mAddress;
	private volatile boolean mIsAutoConnect;
	
	public DeviceItem(ConnectType connType, String name, String address) {
		mConnType = connType;
		mType = parseType(name);
		mName = name;
		mMac = "";
		mAddress = address;
		mIsAutoConnect = false;
	}

	public DeviceItem(ConnectType connType, String name, String address, boolean autoConnect) {
		mConnType = connType;
		mType = parseType(name);
		mName = name;
		mMac = "";
		mAddress = address;
		mIsAutoConnect = autoConnect;
	}

	public DeviceItem(String className, ConnectType connType, String name, String address, boolean autoConnect) {
		mclassName = className;
		mConnType = connType;
		mType = parseType(name);
		mName = name;
		mMac = "";
		mAddress = address;
		mIsAutoConnect = autoConnect;
	}

	public DeviceItem(ConnectType connType, String name, String mac, String address) {
		mConnType = connType;
		mType = parseType(name);
		mName = name;
		mMac = mac;
		mAddress = address;
		mIsAutoConnect = false;
	}
	
	public DeviceItem(ConnectType connType, String name, String mac, String address, boolean autoConnect) {
		mConnType = connType;
		mType = parseType(name);
		mName = name;
		mMac = mac;
		mAddress = address;
		mIsAutoConnect = autoConnect;
	}

	public DeviceItem(DeviceType devType, ConnectType connType, String name, String mac, String address) {
		mConnType = connType;
		mType = devType;
		mName = name;
		mMac = mac;
		mAddress = address;
		mIsAutoConnect = false;
	}

	public DeviceItem(DeviceType devType, ConnectType connType, String name, String mac, String address, boolean autoConnect) {
		mConnType = connType;
		mType = devType;
		mName = name;
		mMac = mac;
		mAddress = address;
		mIsAutoConnect = autoConnect;
	}
	
	public DeviceItem(Parcel source) {
		mConnType = ConnectType.valueOf(source.readInt());
		mType = DeviceType.valueOf(source.readInt());
		mName = source.readString();
		mMac = source.readString();
		mAddress = source.readString();
		mIsAutoConnect = source.readInt() == 0 ? false : true;
	}

	public String getClassName() {
		return mclassName;
	}

	public ConnectType getConnectType() {
		return mConnType;
	}
	
	public DeviceType getType() {
		return mType;
	}

	public String getName() {
		return mName;
	}

	public String getMac() {
		return mMac;
	}

	public String getAddress() {
		return mAddress;
	}

	public boolean getAutoConnect() {
		return mIsAutoConnect;
	}
	
	@Override
	public String toString() {
		return String.format(Locale.US, "%s, %s, [%s], [%s], [%s], [%s]", 
				mConnType, mType, mName, mMac, mAddress, mIsAutoConnect);
	}

	@Override
	public boolean equals(Object obj) {
		DeviceItem item = (DeviceItem) obj;
		if (StringUtil.isNullOrEmpty(mMac)) {
			return mType == item.getType() && mName.equals(item.getName()) && mAddress.equals(item.getAddress());
		} else {
			return mType == item.getType() && mName.equals(item.getName()) && mMac.equals(item.getMac())
					&& mAddress.equals(item.getAddress());
		}
	}

	private static DeviceType parseType(String name) {
		if (StringUtil.isNullOrEmpty(name))
			return DeviceType.Unknown;
		
		name = name.toUpperCase(Locale.US);

		if (name.contains(DEVTYPE_RP100)) {
			return DeviceType.RP100;
		}else if (name.contains(DEVTYPE_RP200)) {
			return DeviceType.RP200;
		}else if(name.contains(DEVTYPE_RP902)){
			return DeviceType.RP902;
		}else if(name.startsWith("EA")){
		return DeviceType.RP100;
		}
		return DeviceType.Unknown;
	}
	
	public static boolean contains(String name) {
		return parseType(name) != DeviceType.Unknown;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mConnType.getCode());
		dest.writeInt(mType.getCode());
		dest.writeString(mName);
		dest.writeString(mMac);
		dest.writeString(mAddress);
		dest.writeInt(mIsAutoConnect ? 1 : 0);
	}
	
	public static final Creator<DeviceItem> CREATOR = new Creator<DeviceItem>() {

		@Override
		public DeviceItem createFromParcel(Parcel source) {
			return new DeviceItem(source);
		}

		@Override
		public DeviceItem[] newArray(int size) {
			return new DeviceItem[size];
		}
	};
}
