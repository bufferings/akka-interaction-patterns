package com.example.patterns.pattern6_1;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;

public class Hal extends AbstractBehavior<Hal.Command> {

  public interface Command {}

  public record OpenThePodBayDoorsPlease(ActorRef<StatusReply<String>> respondTo) implements Command {}

  public static Behavior<Command> create() {
    return Behaviors.setup(Hal::new);
  }

  private Hal(ActorContext<Command> context) {
    super(context);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(Hal.OpenThePodBayDoorsPlease.class, this::onOpenThePodBayDoorsPlease)
        .build();
  }

  private Behavior<Hal.Command> onOpenThePodBayDoorsPlease(
      Hal.OpenThePodBayDoorsPlease message) {
    message.respondTo.tell(StatusReply.error("I'm sorry, Dave. I'm afraid I can't do that."));
    return this;
  }
}
