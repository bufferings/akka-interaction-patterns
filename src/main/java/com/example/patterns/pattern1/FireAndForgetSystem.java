package com.example.patterns.pattern1;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.IOException;

public class FireAndForgetSystem extends AbstractBehavior<FireAndForgetSystem.Command> {

  public interface Command {}

  public enum ShowMeDemo implements Command {INSTANCE}

  public static Behavior<Command> create() {
    return Behaviors.setup(FireAndForgetSystem::new);
  }

  private final ActorRef<Printer.PrintMe> printer;

  public FireAndForgetSystem(ActorContext<Command> context) {
    super(context);
    printer = context.spawn(Printer.create(), "printer");
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(ShowMeDemo.class, notUsed -> this.onStart())
        .build();
  }

  private Behavior<Command> onStart() {
    printer.tell(new Printer.PrintMe("Hello1!"));
    printer.tell(new Printer.PrintMe("Hello2!"));
    printer.tell(new Printer.PrintMe("Hello3!"));
    return this;
  }

  public static void main(String[] args) {
    final ActorSystem<Command> system = ActorSystem.create(create(), "sample-system");

    system.tell(ShowMeDemo.INSTANCE);

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      //noinspection ResultOfMethodCallIgnored
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      system.terminate();
    }
  }
}
