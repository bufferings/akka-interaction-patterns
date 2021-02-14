package com.example.patterns.pattern3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.net.URI;

public class Backend extends AbstractBehavior<Backend.Request> {

  public interface Request {}

  public record StartTranslationJob(int taskId, URI site, ActorRef<Response> replyTo) implements Request {}

  public interface Response {}

  public record JobStarted(int taskId) implements Response {}

  public record JobProgress(int taskId, double progress) implements Response {}

  public record JobCompleted(int taskId, URI result) implements Response {}

  public static Behavior<Request> create() {
    return Behaviors.setup(Backend::new);
  }

  public Backend(ActorContext<Request> context) {
    super(context);
  }

  @Override
  public Receive<Request> createReceive() {
    return newReceiveBuilder()
        .onMessage(StartTranslationJob.class, this::onStartTranslationJob)
        .build();
  }

  private Behavior<Request> onStartTranslationJob(StartTranslationJob job) {
    try {
      job.replyTo().tell(new JobStarted(job.taskId()));
      Thread.sleep(1000);
      job.replyTo().tell(new JobProgress(job.taskId(), 20.0));
      Thread.sleep(1000);
      job.replyTo().tell(new JobProgress(job.taskId(), 40.0));
      Thread.sleep(1000);
      job.replyTo().tell(new JobProgress(job.taskId(), 60.0));
      Thread.sleep(1000);
      job.replyTo().tell(new JobProgress(job.taskId(), 80.0));
      Thread.sleep(1000);
      job.replyTo().tell(new JobCompleted(job.taskId(), URI.create(job.site().toString() + "/finished")));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return this;
  }
}
