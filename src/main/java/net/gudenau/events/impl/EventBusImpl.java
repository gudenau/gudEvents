/*
 * Copyright (c) 2021 gudenau
 *
 * This file is part of gudEvents.
 *
 * gudEvents is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gudEvents is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with gudEvents.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.gudenau.events.impl;

import java.util.Optional;
import java.util.function.Consumer;
import net.gudenau.events.Event;
import net.gudenau.events.EventBus;
import net.gudenau.events.EventDispatcher;
import org.jetbrains.annotations.NotNull;

public final class EventBusImpl implements EventBus{
    public static final EventBus INSTANCE = new EventBusImpl();
    
    private final MapLocker<Class<? extends Event<?>>, EventDispatcher<?, ? extends Event<?>>> dispatchers = MapLocker.create();
    
    @Override
    public void registerEvent(@NotNull Class<? extends Event<?>> type){
        if(dispatchers.putIfAbsent(type, EventDispatcher.createInstance()) != null){
            throw new IllegalStateException("Event " + type.getSimpleName() + " was already registered");
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T, E extends Event<T>> void registerHandler(@NotNull Class<E> event, @NotNull Consumer<E> handler){
        EventDispatcher<T, E> dispatcher = (EventDispatcher<T, E>)dispatchers.get(event);
        if(dispatcher != null){
            dispatcher.registerHandler(handler);
        }else{
            throw new IllegalStateException("Event " + event.getSimpleName() + " was not registered");
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T, E extends Event<T>> void removeHandler(@NotNull Class<E> event, @NotNull Consumer<E> handler){
        EventDispatcher<T, E> dispatcher = (EventDispatcher<T, E>)dispatchers.get(event);
        if(dispatcher != null){
            dispatcher.removeHandler(handler);
        }else{
            throw new IllegalStateException("Event " + event.getSimpleName() + " was not registered");
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T, E extends Event<T>> Optional<T> dispatchEvent(@NotNull E event){
        EventDispatcher<T, E> dispatcher = (EventDispatcher<T, E>)dispatchers.get((Class<E>)event.getClass());
        if(dispatcher != null){
            return dispatcher.dispatchEvent(event);
        }else{
            throw new IllegalStateException("Event " + event.getClass().getSimpleName() + " was not registered");
        }
    }
}
