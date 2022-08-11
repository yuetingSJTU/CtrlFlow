package example;

public class DeviceManager {
    public static final class RequestTrackDevice {
        public final String groupId;
        public final String deviceId;

        public RequestTrackDevice(String groupId, String deviceId) {
            this.groupId = groupId;
            this.deviceId = deviceId;
        }
    }

    public static final class DeviceRegistered {}
}
