package com.salhack.summit.events.bus;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EventManager implements EventBus {
    private final Multimap<Class<?>, Listener> listenerMultimap = Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);
    private final Multimap<EventListener, Listener> parentListeners = Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);

    @Override
    public void post(Object event) {
        try
        {
            listenerMultimap.get(event.getClass()).forEach(listener -> listener.accept(event));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //new ArrayList<>(listenerMultimap.get(event.getClass())).forEach(listener -> listener.accept(event));
    }

    @Override
    public void subscribe(EventListener eventListener) {
        Arrays.stream(eventListener.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(EventHandler.class) && Listener.class.isAssignableFrom(f.getType()))
                .forEach(field -> registerAsListener(eventListener, field));
    }

    private void registerAsListener(EventListener eventListener, Field field) {
        try {
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);

            Listener listener = (Listener) field.get(eventListener);

            field.setAccessible(isAccessible);

            if (listener != null) {
                parentListeners.get(eventListener).add(listener);
                listenerMultimap.get(listener.getTarget()).add(listener);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unsubscribe(EventListener eventListener) {
        parentListeners.get(eventListener).forEach(l -> {
            ArrayList<Class<?>> classList = new ArrayList<>(listenerMultimap.keySet());
            for (Class<?> clazz : classList) {
                listenerMultimap.get(clazz).remove(l);
            }
        });
    }
}
