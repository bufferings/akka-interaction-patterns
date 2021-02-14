package com.example.patterns.pattern4;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;

public class Dave extends AbstractBehavior<Dave.Command> {

  public interface Command {}

  // this is a part of the protocol that is internal to the actor itself
  private record AdaptedResponse(String message) implements Command {}

  public static Behavior<Command> create(ActorRef<Hal.Command> hal) {
    return Behaviors.setup(context -> new Dave(context, hal));
  }

  private Dave(ActorContext<Command> context, ActorRef<Hal.Command> hal) {
    super(context);

    context.ask(
        Hal.HalResponse.class,
        hal,
        Duration.ofSeconds(3),
        Hal.OpenThePodBayDoorsPlease::new,
        (response, throwable) -> {
          if (response != null) {
            return new AdaptedResponse(response.message());
          } else {
            return new AdaptedResponse("Request failed");
          }
        });

    final int requestId = 1;
    context.ask(
        Hal.HalResponse.class,
        hal,
        Duration.ofSeconds(3),
        // construct the outgoing message
        Hal.OpenThePodBayDoorsPlease::new,
        // adapt the response (or failure to respond)
        (response, throwable) -> {
          if (response != null) {
            return new AdaptedResponse(requestId + ": " + response.message());
          } else {
            return new AdaptedResponse(requestId + ": Request failed");
          }
        });
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        // the adapted message ends up being processed like any other
        // message sent to the actor
        .onMessage(AdaptedResponse.class, this::onAdaptedResponse)
        .build();
  }

  private Behavior<Command> onAdaptedResponse(AdaptedResponse response) {
    getContext().getLog().info("Got response from HAL: {}", response.message);
    return this;
  }
}
