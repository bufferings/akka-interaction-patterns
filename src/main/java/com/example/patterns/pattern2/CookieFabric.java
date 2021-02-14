package com.example.patterns.pattern2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class CookieFabric extends AbstractBehavior<CookieFabric.Request> {

  public static Behavior<Request> create() {
    return Behaviors.setup(CookieFabric::new);
  }

  public record Request(String query, ActorRef<RequestResponse.Response> replyTo) {}

  public CookieFabric(ActorContext<Request> context) {
    super(context);
  }

  @Override
  public Receive<Request> createReceive() {
    return newReceiveBuilder()
        .onMessage(Request.class, this::onRequest)
        .build();
  }

  private Behavior<Request> onRequest(Request request) {
    request.replyTo().tell(new RequestResponse.Response("Here are the cookies for " + request.query()));
    return this;
  }

}
