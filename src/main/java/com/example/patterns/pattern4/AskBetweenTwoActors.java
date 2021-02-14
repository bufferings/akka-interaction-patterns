package com.example.patterns.pattern4;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.IOException;

public class AskBetweenTwoActors extends AbstractBehavior<AskBetweenTwoActors.Command> {

  @SuppressWarnings({"DuplicatedCode", "ResultOfMethodCallIgnored"})
  public static void main(String[] args) {
    final ActorSystem<Command> system = ActorSystem.create(create(), "sample-system");

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
    return Behaviors.setup(AskBetweenTwoActors::new);
  }

  private final ActorRef<Hal.Command> hal;

  private AskBetweenTwoActors(ActorContext<Command> context) {
    super(context);
    this.hal = context.spawn(Hal.create(), "hal");
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(StartDemo.class, notUsed -> onStartDemo())
        .build();
  }

  private Behavior<Command> onStartDemo() {
    getContext().spawn(Dave.create(hal), "dave");
    getContext().spawn(Dave.create(hal), "dave2");
    return this;
  }

}
