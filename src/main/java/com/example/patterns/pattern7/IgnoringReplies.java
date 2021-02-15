package com.example.patterns.pattern7;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.example.patterns.pattern2.CookieFabric;

import java.io.IOException;

public class IgnoringReplies extends AbstractBehavior<IgnoringReplies.Command> {

  @SuppressWarnings({"DuplicatedCode", "ResultOfMethodCallIgnored"})
  public static void main(String[] args) {
    final ActorSystem<Command> system =
        ActorSystem.create(create(), "sample-system");

    system.tell(StartDemo.INSTANCE);

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      system.terminate();
    }
  }

  public interface Command {}

  public enum StartDemo implements Command {INSTANCE}

  public static Behavior<Command> create() {
    return Behaviors.setup(IgnoringReplies::new);
  }

  private final ActorRef<CookieFabric.Request> cookieFabric;

  private IgnoringReplies(ActorContext<Command> context) {
    super(context);
    this.cookieFabric = context.spawn(CookieFabric.create(), "cookie-fabric");
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(StartDemo.class, notUsed -> onStartDemo())
        .build();
  }

  private Behavior<Command> onStartDemo() {
    cookieFabric.tell(new CookieFabric.Request("give me cookies1", getContext().getSystem().ignoreRef()));
    return this;
  }
}
