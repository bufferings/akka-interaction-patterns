package com.example.patterns.pattern2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.IOException;

public class RequestResponse extends AbstractBehavior<RequestResponse.Command> {

  @SuppressWarnings("DuplicatedCode")
  public static void main(String[] args) {
    final ActorSystem<Command> system =
        ActorSystem.create(create(), "sample-system");

    system.tell(StartDemo.INSTANCE);

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      //noinspection ResultOfMethodCallIgnored
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      system.terminate();
    }
  }

  public interface Command {}

  public enum StartDemo implements Command {INSTANCE}

  public record Response(String message) implements Command {}

  public static Behavior<Command> create() {
    return Behaviors.setup(RequestResponse::new);
  }

  private final ActorRef<CookieFabric.Request> cookieFabric;

  public RequestResponse(ActorContext<Command> context) {
    super(context);
    this.cookieFabric = context.spawn(CookieFabric.create(), "cookie-fabric");
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(StartDemo.class, notUsed -> onStartDemo())
        .onMessage(Response.class, this::onResponse)
        .build();
  }

  private Behavior<Command> onStartDemo() {
    ActorRef<Response> replyTo = getContext().getSelf().narrow();
    cookieFabric.tell(new CookieFabric.Request("give me cookies1", replyTo));
    cookieFabric.tell(new CookieFabric.Request("give me cookies2", replyTo));
    cookieFabric.tell(new CookieFabric.Request("give me cookies3", replyTo));
    return this;
  }

  private Behavior<Command> onResponse(Response response) {
    getContext().getLog().info(response.message());
    return this;
  }
}
