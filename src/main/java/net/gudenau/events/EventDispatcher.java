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
import net.gudenau.events.impl.EventDispatcherImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Allows for the dispatch and handling of events.
 *
 * @param <T> The return type of the event
 * @param <E> The type of the event
 */
public interface EventDispatcher<T, E extends Event<?>>{
    /**
     * Creates an event dispatcher.
     *
     * @param <T> The return type of the event
     * @param <E> The type of the event
     * @return The dispatcher
     */
    static <T, E extends Event<T>> @NotNull EventDispatcher<T, E> createInstance(){
        return new EventDispatcherImpl<>();
    }
    
    /**
     * Registers an event handler to this event dispatcher.
     *
     * @param handler The event handler
     */
    void registerHandler(@NotNull Consumer<E> handler);
    
    /**
     * Tries to removes a registered event handler from this dispatcher.
     *
     * @param handler The event handler
     */
    void removeHandler(@NotNull Consumer<E> handler);
    
    /**
     * Submits an event to be handled.
     *
     * @param event The event to dispatch
     *
     * @return The result of the event
     */
    Optional<T> dispatchEvent(@NotNull E event);
}
