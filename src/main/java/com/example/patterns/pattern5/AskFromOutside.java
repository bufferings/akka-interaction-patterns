package com.example.patterns.pattern5;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.SpawnProtocol;
import akka.actor.typed.javadsl.AskPattern;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AskFromOutside {

  @SuppressWarnings({"DuplicatedCode", "ResultOfMethodCallIgnored"})
  public static void main(String[] args) {
    final ActorSystem<SpawnProtocol.Command> system = ActorSystem.create(SpawnProtocol.create(), "sample-system");

    CompletionStage<ActorRef<CookieFabric.Command>> spawnCookieFabric =
        AskPattern.ask(
            system,
            replyTo ->
                new SpawnProtocol.Spawn<>(CookieFabric.create(), "cookie-fabric", Props.empty(), replyTo),
            Duration.ofSeconds(3),
            system.scheduler());

    spawnCookieFabric.whenComplete((cookieFabric, throwable) -> {
      if (throwable != null) return;
      askAndPrint1(system, cookieFabric, 3);
      askAndPrint1(system, cookieFabric, 10);
      askAndPrint2(system, cookieFabric, 4);
      askAndPrint2(system, cookieFabric, 11);
    });

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      system.terminate();
    }
  }

  public static void askAndPrint1(
      ActorSystem<SpawnProtocol.Command> system, ActorRef<CookieFabric.Command> cookieFabric, int cookieCount) {

    CompletionStage<CookieFabric.Reply> result =
        AskPattern.ask(
            cookieFabric,
            replyTo -> new CookieFabric.GiveMeCookies(cookieCount, replyTo),
            Duration.ofSeconds(3),
            system.scheduler());

    result.whenComplete(
        (reply, failure) -> {
          if (reply instanceof CookieFabric.Cookies rep)
            System.out.println("Yay, " + rep.count() + " cookies!");
          else if (reply instanceof CookieFabric.InvalidRequest rep)
            System.out.println("No cookies for me. " + rep.reason());
          else
            System.out.println("Boo! didn't get cookies in time. " + failure);
        });
  }

  public static void askAndPrint2(
      ActorSystem<SpawnProtocol.Command> system, ActorRef<CookieFabric.Command> cookieFabric, int cookieCount) {

    CompletionStage<CookieFabric.Reply> result =
        AskPattern.ask(
            cookieFabric,
            replyTo -> new CookieFabric.GiveMeCookies(cookieCount, replyTo),
            Duration.ofSeconds(3),
            system.scheduler());

    CompletionStage<CookieFabric.Cookies> cookies =
        result.thenCompose(reply -> {
          if (reply instanceof CookieFabric.Cookies success) {
            return CompletableFuture.completedFuture(success);
          } else if (reply instanceof CookieFabric.InvalidRequest failure) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(failure.reason()));
          } else {
            throw new IllegalStateException("Unexpected reply: " + reply.getClass());
          }
        });

    cookies.whenComplete(
        (cookiesReply, failure) -> {
          if (cookiesReply != null)
            System.out.println("Yay, " + cookiesReply.count() + " cookies!");
          else
            System.out.println("Boo! didn't get cookies in time. " + failure);
        });
  }
}
