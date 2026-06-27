package com.vbforge.springcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo-06 — Spring Core and Spring Web
 *
 * After startup explore the demo via:
 *   GET /demo                                 → index of all endpoints
 *
 *   GET /demo/beans/what-is-a-bean            → Q64  what is a Spring bean
 *   GET /demo/beans/application-context       → Q65  ApplicationContext, getBean()
 *   GET /demo/beans/injection-styles          → Q66  constructor / setter / field injection
 *   GET /demo/beans/scopes                    → Q67  singleton, prototype, request, session
 *   GET /demo/beans/post-construct            → Q68  @PostConstruct lifecycle hook
 *   GET /demo/beans/qualifier                 → Q69  @Qualifier — resolve by name
 *   GET /demo/beans/primary                   → Q70  @Primary — default candidate
 *   GET /demo/beans/order                     → Q71  @Order — list injection, filters
 *   GET /demo/beans/startup-hooks             → Q72  ApplicationRunner, CommandLineRunner, events
 *   GET /demo/beans/circular-dependency       → Q73  circular deps and how to resolve them
 *
 *   GET /demo/web/http-methods                → Q74  GET/POST/PUT/PATCH/DELETE/HEAD/OPTIONS
 *   GET /demo/web/cookies-headers-session     → Q75  cookies, headers, HTTP session
 *   GET /demo/web/cors                        → Q76  CORS, preflight, @CrossOrigin
 *   GET /demo/web/idempotency                 → Q77  idempotency per HTTP method
 */
@SpringBootApplication
public class MainApp {
    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }
}
