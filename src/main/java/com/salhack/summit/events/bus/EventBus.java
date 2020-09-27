package com.salhack.summit.events.bus;

public interface EventBus {
    /**
     * Post an event so the listeners can pick up on it
     * @param event The event object to be posted
     */
    void post(Object event);

    /**
     * Subscribe a listener to listen to events
     * @param eventListener The listener to subscribe to events
     */
    void subscribe(EventListener eventListener);

    /**
     * Unsubscribe a listener (it will no longer listen to events)
     * @param eventListener The listener to unsubscribe to events
     */
    void unsubscribe(EventListener eventListener);
}
