package com.example.patterns.pattern1;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.IOException;

public class FireAndForget extends AbstractBehavior<FireAndForget.Command> {

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
    return Behaviors.setup(FireAndForget::new);
  }

  private final ActorRef<Printer.PrintMe> printer;

  public FireAndForget(ActorContext<Command> context) {
    super(context);
    this.printer = context.spawn(Printer.create(), "printer");
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(StartDemo.class, notUsed -> onStartDemo())
        .build();
  }

  private Behavior<Command> onStartDemo() {
    printer.tell(new Printer.PrintMe("Hello1!"));
    printer.tell(new Printer.PrintMe("Hello2!"));
    printer.tell(new Printer.PrintMe("Hello3!"));
    return this;
  }

}
