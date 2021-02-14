package com.example.patterns.pattern3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.IOException;
import java.net.URI;

public class AdaptedResponse extends AbstractBehavior<AdaptedResponse.Command> {

  @SuppressWarnings({"DuplicatedCode", "ResultOfMethodCallIgnored"})
  public static void main(String[] args) {
    final ActorSystem<Command> system =
        ActorSystem.create(AdaptedResponse.create(), "sample-system");

    system.tell(AdaptedResponse.StartDemo.INSTANCE);

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

  public record TranslationCompleted(URI uri) implements Command {}

  public static Behavior<Command> create() {
    return Behaviors.setup(AdaptedResponse::new);
  }

  private final ActorRef<Frontend.Command> frontend;

  public AdaptedResponse(ActorContext<Command> context) {
    super(context);
    this.frontend = context.spawn(Frontend.create(), "frontend");
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(StartDemo.class, notUsed -> onStartDemo())
        .onMessage(TranslationCompleted.class, this::onTranslationComplete)
        .build();
  }

  private Behavior<Command> onStartDemo() {
    ActorRef<TranslationCompleted> replyTo = getContext().getSelf().narrow();
    frontend.tell(new Frontend.Translate(URI.create("http://example.com/aaa"), replyTo));
    frontend.tell(new Frontend.Translate(URI.create("http://example.com/bbb"), replyTo));
    frontend.tell(new Frontend.Translate(URI.create("http://example.com/ccc"), replyTo));
    return this;
  }

  private Behavior<Command> onTranslationComplete(TranslationCompleted translationCompleted) {
    getContext().getLog().info(translationCompleted.uri().toString());
    return this;
  }

}
