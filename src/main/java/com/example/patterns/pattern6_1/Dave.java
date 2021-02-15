package com.example.patterns.pattern6_1;

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
  private record AdaptedResponse(String message) implements Dave.Command {}

  public static Behavior<Command> create(ActorRef<Hal.Command> hal) {
    return Behaviors.setup(context -> new Dave(context, hal));
  }

  private Dave(ActorContext<Command> context, ActorRef<Hal.Command> hal) {
    super(context);

    // asking someone requires a timeout, if the timeout hits without response
    // the ask is failed with a TimeoutException
    final Duration timeout = Duration.ofSeconds(3);

    context.askWithStatus(
        String.class,
        hal,
        timeout,
        // construct the outgoing message
        Hal.OpenThePodBayDoorsPlease::new,
        // adapt the response (or failure to respond)
        (response, throwable) -> {
          //noinspection ReplaceNullCheck
          if (response != null) {
            // a ResponseWithStatus.success(m) is unwrapped and passed as response
            return new AdaptedResponse(response);
          } else {
            // a ResponseWithStatus.error will end up as a StatusReply.ErrorMessage()
            // exception here
            return new AdaptedResponse("Request failed: " + throwable.getMessage());
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
