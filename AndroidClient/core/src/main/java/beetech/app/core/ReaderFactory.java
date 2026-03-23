package beetech.app.core;

import beetech.app.core.data.DeviceItem;

public class ReaderFactory {

    // Get reader by class name (Reflection)
//    public static AdvBaseReader getReaderByClassName(String className, int type, String address) {
//        try {
//            Class<?> readerClass = Class.forName(className);
//            AdvBaseReader reader = (AdvBaseReader) readerClass.getDeclaredConstructor().newInstance();
//            reader.initReader(type, address);
//            return reader;
//        } catch (ClassNotFoundException e) {
//            throw new IllegalArgumentException("Class not found: " + className, e);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to create instance for: " + className, e);
//        }
//    }

    public static AdvBaseReader getReader(DeviceItem di) {
        try {
            Class<?> readerClass = Class.forName(di.getClassName());
            AdvBaseReader reader = (AdvBaseReader) readerClass.getDeclaredConstructor().newInstance();
            reader.initReader(di);
            return reader;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found: " + di.getClassName(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance for: " + di.getClassName(), e);
        }
    }
}
