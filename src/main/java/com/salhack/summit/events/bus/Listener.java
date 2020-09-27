package com.salhack.summit.events.bus;

import net.jodah.typetools.TypeResolver;

import java.util.function.Consumer;

public class Listener<E> implements Consumer<E> {
    private final Consumer<E> consumer;
    private final Class<E> target;

    @SuppressWarnings("unchecked")
    public Listener(Consumer<E> consumer) {
        this.consumer = consumer;
        this.target = (Class<E>) TypeResolver.resolveRawArgument(Consumer.class, consumer.getClass());
    }

    @Override
    public void accept(E e) {
        consumer.accept(e);
    }

    public Class<E> getTarget() {
        return target;
    }
}
