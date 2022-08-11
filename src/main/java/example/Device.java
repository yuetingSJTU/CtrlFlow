package example;

import java.util.Optional;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import example.DeviceManager.DeviceRegistered;
import example.DeviceManager.RequestTrackDevice;

public class Device extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    final String groupId;

    final String deviceId;

    public Device(String groupId, String deviceId) {
        this.groupId = groupId;
        this.deviceId = deviceId;
    }

    public static Props props(String groupId, String deviceId) {
        return Props.create(Device.class, () -> new Device(groupId, deviceId));
    }

    public static final class RecordTemperature {
        final long requestId;
        final double value;

        public RecordTemperature(long requestId, double value) {
            this.requestId = requestId;
            this.value = value;
        }
    }

    public static final class TemperatureRecorded {
        public final long requestId;

        public TemperatureRecorded(long requestId) {
            this.requestId = requestId;
        }
    }

    public static final class ReadTemperature {
        final long requestId;

        public ReadTemperature(long requestId) {
            this.requestId = requestId;
        }
    }

    public static final class RespondTemperature {
        public final long requestId;
        public final Optional<Double> value;

        public RespondTemperature(long requestId, Optional<Double> value) {
            this.requestId = requestId;
            this.value = value;
        }
    }

    Optional<Double> lastTemperatureReading = Optional.empty();

    @Override
    public void preStart() {
        log.info("Device actor {}-{} started", groupId, deviceId);
    }

    @Override
    public void postStop() {
        log.info("Device actor {}-{} stopped", groupId, deviceId);
    }

    @Override
    public Receive createReceive() {
        // 用于创建消息的工厂类
        return receiveBuilder()
                // 判断 build 的是哪种信息
                .match(
                        RequestTrackDevice.class,
                        r -> {
                            if (this.groupId.equals(r.groupId) && this.deviceId.equals(r.deviceId)) {
                                getSender().tell(new DeviceRegistered(), getSelf());
                            } else {
                                log.warning(
                                        "Ignoring TrackDevice request for {}-{}.This actor is responsible for {}-{}.",
                                        r.groupId,
                                        r.deviceId,
                                        this.groupId,
                                        this.deviceId);
                            }
                        })
                .match( // 如果需要 build Record 消息，就记录状态并且向自己发送 TemperatureRecorded 消息
                        RecordTemperature.class,
                        r -> {
                            log.info("Recorded temperature reading {} with {}", r.value, r.requestId);
                            lastTemperatureReading = Optional.of(r.value);
                            getSender().tell(new TemperatureRecorded(r.requestId), getSelf());
                        })
                .match( // 如果需要 build Read 消息，就向自己发送 Respond 消息
                        ReadTemperature.class,
                        r -> {
                            getSender()
                                    .tell(new RespondTemperature(r.requestId, lastTemperatureReading), getSelf());
                        })
                .build();
    }
}
