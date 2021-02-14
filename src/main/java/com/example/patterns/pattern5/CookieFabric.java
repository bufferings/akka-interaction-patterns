package com.example.patterns.pattern5;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class CookieFabric extends AbstractBehavior<CookieFabric.Command> {

  public interface Command {}

  public record GiveMeCookies(int count, ActorRef<Reply> replyTo) implements Command {}

  public interface Reply {}

  public record Cookies(int count) implements Reply {}

  public record InvalidRequest(String reason) implements Reply {}

  public static Behavior<Command> create() {
    return Behaviors.setup(CookieFabric::new);
  }

  private CookieFabric(ActorContext<Command> context) {
    super(context);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(GiveMeCookies.class, this::onGiveMeCookies)
        .build();
  }

  private Behavior<Command> onGiveMeCookies(GiveMeCookies request) {
    if (request.count >= 5) request.replyTo.tell(new InvalidRequest("Too many cookies."));
    else request.replyTo.tell(new Cookies(request.count));

    return this;
  }
}
