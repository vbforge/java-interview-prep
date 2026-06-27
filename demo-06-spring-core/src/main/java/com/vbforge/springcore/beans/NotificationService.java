package com.vbforge.springcore.beans;

/**
 * Shared interface used across Q69 (@Qualifier) and Q70 (@Primary) demos.
 * Having multiple implementations of the same interface is exactly the scenario
 * where Spring needs help deciding which bean to inject.
 */
public interface NotificationService {

    String send(String message);
    String name();

}
