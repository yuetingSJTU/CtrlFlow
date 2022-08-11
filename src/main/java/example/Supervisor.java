package example;

import akka.actor.AbstractActor;
import akka.actor.ActorLogging;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Supervisor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(Supervisor.class, Supervisor::new);
    }

    @Override
    public void preStart() {
        log.info("Application stated");
    }

    @Override
    public void postStop() {
        log.info("Application stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}
