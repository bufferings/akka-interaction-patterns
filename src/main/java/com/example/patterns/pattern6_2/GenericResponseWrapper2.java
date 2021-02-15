package com.example.patterns.pattern6_2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.SpawnProtocol;
import akka.actor.typed.javadsl.AskPattern;
import akka.pattern.StatusReply;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class GenericResponseWrapper2 {

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

    CompletionStage<CookieFabric.Cookies> result =
        AskPattern.askWithStatus(
            cookieFabric,
            replyTo -> new CookieFabric.GiveMeCookies(cookieCount, replyTo),
            Duration.ofSeconds(3),
            system.scheduler());

    result.whenComplete(
        (reply, failure) -> {
          if (reply != null)
            System.out.println("Yay, " + reply.count() + " cookies!");
          else if (failure instanceof StatusReply.ErrorMessage)
            System.out.println("No cookies for me. " + failure.getMessage());
          else System.out.println("Boo! didn't get cookies in time. " + failure);
        });
  }

  public static void askAndPrint2(
      ActorSystem<SpawnProtocol.Command> system, ActorRef<CookieFabric.Command> cookieFabric, int cookieCount) {

    CompletionStage<CookieFabric.Cookies> cookies =
        AskPattern.askWithStatus(
            cookieFabric,
            replyTo -> new CookieFabric.GiveMeCookies(cookieCount, replyTo),
            Duration.ofSeconds(3),
            system.scheduler());

    cookies.whenComplete(
        (cookiesReply, failure) -> {
          if (cookiesReply != null)
            System.out.println("Yay, " + cookiesReply.count() + " cookies!");
          else
            System.out.println("Boo! didn't get cookies in time. " + failure);
        });
  }
}
