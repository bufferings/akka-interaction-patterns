package com.example.patterns;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;

import java.io.IOException;

public class Main {

  public enum Message {INSTANCE}

  public static void main(String[] args) {
    ActorSystem<Message> system = ActorSystem.create(
        Behaviors.setup(context -> {
          return Behaviors.receive(Message.class)
              .onMessage(Message.class, notUsed -> {
                context.getLog().info("Hello");
                return Behaviors.same();
              })
              .build();
        }),
        "hello");

    system.tell(Message.INSTANCE);
    system.tell(Message.INSTANCE);
    system.tell(Message.INSTANCE);

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      //noinspection ResultOfMethodCallIgnored
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      system.terminate();
    }
  }
}
