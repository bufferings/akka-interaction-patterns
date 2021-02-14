package com.example.patterns.pattern4;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Hal extends AbstractBehavior<Hal.Command> {

  public interface Command {}

  public record OpenThePodBayDoorsPlease(ActorRef<HalResponse> respondTo) implements Command {}

  public record HalResponse(String message) {}

  public static Behavior<Command> create() {
    return Behaviors.setup(Hal::new);
  }

  private Hal(ActorContext<Command> context) {
    super(context);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(OpenThePodBayDoorsPlease.class, this::onOpenThePodBayDoorsPlease)
        .build();
  }

  private Behavior<Command> onOpenThePodBayDoorsPlease(OpenThePodBayDoorsPlease message) {
    message.respondTo.tell(new HalResponse("I'm sorry, Dave. I'm afraid I can't do that."));
    return this;
  }
}
