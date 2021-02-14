package com.example.patterns.pattern1;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Printer extends AbstractBehavior<Printer.PrintMe> {

  public record PrintMe(String message) {}

  public static Behavior<PrintMe> create() {
    return Behaviors.setup(Printer::new);
  }

  private Printer(ActorContext<PrintMe> context) {
    super(context);
  }

  @Override
  public Receive<PrintMe> createReceive() {
    return newReceiveBuilder()
        .onMessage(PrintMe.class, this::onPrintMe)
        .build();
  }

  private Behavior<PrintMe> onPrintMe(PrintMe printMe) {
    getContext().getLog().info(printMe.message());
    return this;
  }

}
