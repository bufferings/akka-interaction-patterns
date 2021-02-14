package com.example.patterns.pattern3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class Frontend extends AbstractBehavior<Frontend.Command> {

  public interface Command {}

  public record Translate(URI site, ActorRef<AdaptedResponse.TranslationCompleted> replyTo) implements Command {}

  private record WrappedBackendResponse(Backend.Response response) implements Command {}

  public static Behavior<Frontend.Command> create() {
    return Behaviors.setup(Frontend::new);
  }

  private final ActorRef<Backend.Response> backendResponseAdapter;

  private final Map<Integer, ActorRef<AdaptedResponse.TranslationCompleted>> inProgress = new HashMap<>();

  private int taskIdCounter = 0;

  public Frontend(ActorContext<Command> context) {
    super(context);
    this.backendResponseAdapter =
        context.messageAdapter(Backend.Response.class, WrappedBackendResponse::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(Translate.class, this::onTranslate)
        .onMessage(WrappedBackendResponse.class, this::onWrappedBackendResponse)
        .build();
  }

  private Behavior<Command> onTranslate(Translate cmd) {
    taskIdCounter += 1;
    inProgress.put(taskIdCounter, cmd.replyTo());

    ActorRef<Backend.Request> backend = getContext().spawn(Backend.create(), "backend" + taskIdCounter);
    backend.tell(new Backend.StartTranslationJob(taskIdCounter, cmd.site(), backendResponseAdapter));
    return this;
  }

  private Behavior<Command> onWrappedBackendResponse(WrappedBackendResponse wrapped) {
    Backend.Response response = wrapped.response();
    if (response instanceof Backend.JobStarted rsp) {
      getContext().getLog().info("Started {}", rsp.taskId());
    } else if (response instanceof Backend.JobProgress rsp) {
      getContext().getLog().info("Progress {} {}%", rsp.taskId(), rsp.progress());
    } else if (response instanceof Backend.JobCompleted rsp) {
      getContext().getLog().info("Completed {}", rsp.taskId());
      inProgress.get(rsp.taskId()).tell(new AdaptedResponse.TranslationCompleted(rsp.result()));
      inProgress.remove(rsp.taskId());
    } else {
      return Behaviors.unhandled();
    }
    return this;
  }
}
