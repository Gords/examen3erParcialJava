package io.openshift.example;

import io.openshift.example.service.Store;
import io.openshift.example.service.impl.JdbcProductStore;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import rx.Single;

import java.util.NoSuchElementException;

import static io.openshift.example.Errors.error;

public class CrudApplication extends AbstractVerticle {

  private Store store;

  @Override
  public void start() {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route("/api/consoles/:id").handler(this::validateId);
    router.get("/api/consoles").handler(this::retrieveAll);
    router.post("/api/consoles").handler(this::addOne);
    router.get("/api/consoles/:id").handler(this::getOne);
    router.put("/api/consoles/:id").handler(this::updateOne);
    router.delete("/api/consoles/:id").handler(this::deleteOne);

    router.get("/health").handler(rc -> rc.response().end("OK"));
    router.get().handler(StaticHandler.create());


    JDBCClient jdbc = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", "jdbc:postgresql://" + getEnv("MY_DATABASE_SERVICE_HOST", "postgres://qxdurvzjbtroug:ac9d829a3e15955534de87eac6db43797d619b6655338d604fb207370c0e42a3@ec2-52-44-139-108.compute-1.amazonaws.com:5432/d4vgqvlivpiuo3") + ":5432/GamerSA")
      .put("driver_class", "org.postgresql.Driver")
      .put("user", getEnv("DB_USERNAME", "user"))
      .put("password", getEnv("DB_PASSWORD", "password"))
    );

    DBInitHelper.initDatabase(vertx, jdbc)
      .andThen(initHttpServer(router, jdbc))
      .subscribe(
        (http) -> System.out.println("Server ready on port " + http.actualPort()),
        Throwable::printStackTrace
      );
  }

  private Single<HttpServer> initHttpServer(Router router, JDBCClient client) {
    store = new JdbcProductStore(client);
    return vertx
      .createHttpServer()
      .requestHandler(router)
      .rxListen(8080);
  }

  private void validateId(RoutingContext ctx) {
    try {
      ctx.put("consoleId", Long.parseLong(ctx.pathParam("id")));
      ctx.next();
    } catch (NumberFormatException e) {
      error(ctx, 400, "invalid id: " + e.getCause());
    }
  }

  private void retrieveAll(RoutingContext ctx) {
    HttpServerResponse response = ctx.response()
      .putHeader("Content-Type", "application/json");
    JsonArray res = new JsonArray();
    store.readAll()
      .subscribe(
        res::add,
        err -> error(ctx, 415, err),
        () -> response.end(res.encodePrettily())
      );
  }


  private void getOne(RoutingContext ctx) {
    HttpServerResponse response = ctx.response()
      .putHeader("Content-Type", "application/json");

    store.read(ctx.get("consoleId"))
      .subscribe(
        json -> response.end(json.encodePrettily()),
        err -> {
          if (err instanceof NoSuchElementException) {
            error(ctx, 404, err);
          } else if (err instanceof IllegalArgumentException) {
            error(ctx, 415, err);
          } else {
            error(ctx, 500, err);
          }
        }
      );
  }

  private void addOne(RoutingContext ctx) {
    JsonObject item;
    try {
      item = ctx.getBodyAsJson();
    } catch (RuntimeException e) {
      error(ctx, 415, "invalid payload");
      return;
    }

    if (item == null) {
      error(ctx, 415, "invalid payload");
      return;
    }

    store.create(item)
      .subscribe(
        json ->
          ctx.response()
            .putHeader("Location", "/api/consoles/" + json.getLong("id"))
            .putHeader("Content-Type", "application/json")
            .setStatusCode(201)
            .end(json.encodePrettily()),
        err -> writeError(ctx, err)
      );
  }

  private void updateOne(RoutingContext ctx) {
    JsonObject item;
    try {
      item = ctx.getBodyAsJson();
    } catch (RuntimeException e) {
      error(ctx, 415, "invalid payload");
      return;
    }

    if (item == null) {
      error(ctx, 415, "invalid payload");
      return;
    }

    store.update(ctx.get("consoleId"), item)
      .subscribe(
        () ->
          ctx.response()
            .putHeader("Content-Type", "application/json")
            .setStatusCode(200)
            .end(item.put("id", ctx.<Long>get("consoleId")).encodePrettily()),
        err -> writeError(ctx, err)
      );
  }

  private void writeError(RoutingContext ctx, Throwable err) {
    if (err instanceof NoSuchElementException) {
      error(ctx, 404, err);
    } else if (err instanceof IllegalArgumentException) {
      error(ctx, 422, err);
    } else {
      error(ctx, 409, err);
    }
  }

  private void deleteOne(RoutingContext ctx) {
    store.delete(ctx.get("consoleId"))
      .subscribe(
        () ->
          ctx.response()
            .setStatusCode(204)
            .end(),
        err -> {
          if (err instanceof NoSuchElementException) {
            error(ctx, 404, err);
          } else {
            error(ctx, 415, err);
          }
        }
      );
  }

  private String getEnv(String key, String dv) {
    String s = System.getenv(key);
    if (s == null) {
      return dv;
    }
    return s;
  }
}
