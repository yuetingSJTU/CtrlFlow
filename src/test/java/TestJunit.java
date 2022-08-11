import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import example.Device;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;


public class TestJunit {
    public ActorSystem system;

    @Before
    public void initializeSystem() {
        // 新建测试所需的系统
        ActorSystem system = ActorSystem.create("iot-system");
        this.system = system;
    }
    @Test
    public void testReplyWithLatestTemperatureReading() {
        TestKit probe = new TestKit(system);
        ActorRef deviceActor = system.actorOf(Device.props("group", "device1"));
        ActorRef deviceActor1 = system.actorOf(Device.props("group", "device"));

        // device 将 Record 信息告诉 probe
        deviceActor.tell(new Device.RecordTemperature(1L, 24.0), probe.getRef());
        deviceActor1.tell(new Device.RecordTemperature(5L, 29.0), probe.getRef());
        // probe 向 device 收取 Recorded 信息, 表示 probe 已经收到了信息
        // 同级节点目前是随机访问，所以探针目前没有必要区分节点
        assertEquals(5L, probe.expectMsgClass(Device.TemperatureRecorded.class).requestId);

        // device 将 Read 信息告诉 probe
        deviceActor.tell(new Device.ReadTemperature(2L), probe.getRef());
        // probe 向 device 收取 Response 信息
        Device.RespondTemperature response1 = probe.expectMsgClass(Device.RespondTemperature.class);
        assertEquals(2L, response1.requestId);
        assertEquals(Optional.of(24.0), response1.value);

        deviceActor.tell(new Device.RecordTemperature(3L, 55.0), probe.getRef());
        assertEquals(3L, probe.expectMsgClass(Device.TemperatureRecorded.class).requestId);


        deviceActor.tell(new Device.ReadTemperature(4L), probe.getRef());
        Device.RespondTemperature response2 = probe.expectMsgClass(Device.RespondTemperature.class);
        assertEquals(4L, response2.requestId);
        assertEquals(Optional.of(55.0), response2.value);
    }
}
