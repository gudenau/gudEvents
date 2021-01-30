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

package net.gudenau.events;

import java.util.Optional;
import java.util.function.Consumer;
import net.gudenau.events.impl.EventBusImpl;
import org.jetbrains.annotations.NotNull;

/**
 * An event bus interface for when you don't need speed and want convenience.
 *
 * This will be slower than using {@link EventDispatcher}
 */
public interface EventBus{
    /**
     * Gets the shared {@link EventBus} instance.
     *
     * @return The shared {@link EventBus}
     */
    static EventBus getInstance(){
        return EventBusImpl.INSTANCE;
    }
    
    /**
     * Creates a non-shared {@link EventBus} instance.
     *
     * @return The new {@link EventBus}
     */
    static EventBus createInstance(){
        return new EventBusImpl();
    }
    
    /**
     * Registers a new {@link Event} type.
     *
     * @param type The new {@link Event} type to register
     *
     * @throws IllegalStateException If the {@link Event} type was already registered
     */
    void registerEvent(@NotNull Class<? extends Event<?>> type);
    
    /**
     * Registers an event handler to this bus.
     *
     * @param event The type of the event the handler handles
     * @param handler The handler itself
     * @param <T> The result type of the event
     * @param <E> The type of the event
     *
     * @throws IllegalStateException If the {@link Event} type was not registered
     */
    <T, E extends Event<T>> void registerHandler(@NotNull Class<E> event, @NotNull Consumer<E> handler);
    
    /**
     * Attempts to remove an event handler from this bus.
     *
     * @param event The type of the event the handler handles
     * @param handler The handler to remove
     * @param <T> The result type of the event
     * @param <E> The type of the event
     */
    <T, E extends Event<T>> void removeHandler(@NotNull Class<E> event, @NotNull Consumer<E> handler);
    
    /**
     * Dispatches an {@link Event} to this {@link EventBus}
     *
     * @param event The {@link Event} to dispatch
     * @param <T> The result type of the event
     * @param <E> The type of the event
     *
     * @return The result of the event
     *
     * @throws IllegalStateException If the {@link Event} was not registered
     */
    <T, E extends Event<T>> Optional<T> dispatchEvent(@NotNull E event);
}
