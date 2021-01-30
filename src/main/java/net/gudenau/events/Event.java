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
import org.jetbrains.annotations.Nullable;

/**
 * The base type of event for {@link EventDispatcher}.
 *
 * @param <T> The type of the event
 */
public abstract class Event<T>{
    private final T originalValue;
    private T value = null;
    private boolean canceled = false;
    
    /**
     * Constructs an event without a starting value.
     */
    public Event(){
        this(null);
    }
    
    /**
     * Constructs an event with a starting value.
     *
     * @param originalValue The starting value
     */
    public Event(@Nullable T originalValue){
        this.originalValue = originalValue;
    }
    
    /**
     * Gets the original value of this event.
     *
     * @return The original value
     */
    public final Optional<T> getOriginalValue(){
        return Optional.ofNullable(originalValue);
    }
    
    /**
     * Checks if this event has had a value set.
     *
     * @return True if the value was set, false if otherwise
     */
    public final boolean hasValue(){
        return value != null;
    }
    
    /**
     * Gets the set value.
     *
     * @return The value if set, otherwise empty
     */
    public final Optional<T> getValue(){
        return Optional.ofNullable(value);
    }
    
    /**
     * Sets the value if the following conditions are met
     * - The value has not been set
     * - This event is overrideable
     *
     * If you pass null and this event can be set the value will be reset.
     *
     * @param value The value to set
     *
     * @return True if the value was set, false otherwise
     */
    public final boolean setValue(@Nullable T value){
        if(isOverridable() || this.value == null){
            this.value = value;
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * Determines if this event can have it's value overridden once set.
     *
     * @return True if the value can be overridden, false if otherwise
     */
    protected boolean isOverridable(){
        return false;
    }
    
    /**
     * Cancels this event if it can be canceled.
     *
     * @throws IllegalStateException If this event can't be canceled
     */
    public final void cancel(){
        if(isCancelable()){
            canceled = true;
        }else{
            throw new IllegalStateException("Can not cancel a non-cancelable event");
        }
    }
    
    /**
     * Cancels and tries to set the value.
     *
     * This is a helper that calls:
     *  cancel();
     *  setValue(value);
     *
     * @param value The value to set
     *
     * @return True if the value was set, false otherwise
     *
     * @throws IllegalStateException If this event can't be canceled
     */
    public final boolean cancel(@Nullable T value){
        cancel();
        return setValue(value);
    }
    
    /**
     * Checks if this event was canceled.
     *
     * @return True if this event was canceled, false otherwise
     */
    public final boolean wasCanceled(){
        return canceled;
    }
    
    /**
     * Returns true if this event is cancelable.
     *
     * @return True if this event is canceable, false otherwise
     */
    public abstract boolean isCancelable();
    
    /**
     * Gets the result of this event, it will be one of the following in order:
     *  - The set value, if present
     *  - The original value, if present
     *  - Empty
     *
     * @return The result if set, or the original value if set
     */
    public final Optional<T> getResult(){
        return hasValue() ? getValue() : getOriginalValue();
    }
    
    /**
     * An event that can be canceled.
     *
     * The is the same as overriding {@link Event#isCancelable()} with a final method to return true.
     *
     * @param <T> The type of the event
     */
    public static abstract class Cancelable<T> extends Event<T>{
        /**
         * Constructs a cancelable event without an original value.
         */
        public Cancelable(){
            this(null);
        }
    
        /**
         * Constructs a cancelable event.
         *
         * @param value The original value
         */
        public Cancelable(@Nullable T value){
            super(value);
        }
    
        @Override
        final public boolean isCancelable(){
            return true;
        }
    }
    
    /**
     * An event that can be not be canceled.
     *
     * The is the same as overriding {@link Event#isCancelable()} with a final method to return false.
     *
     * @param <T> The type of the event
     */
    public static abstract class NonCancelable<T> extends Event<T>{
        /**
         * Constructs a cancelable event without an original value.
         */
        public NonCancelable(){
            this(null);
        }
        
        /**
         * Constructs a cancelable event.
         *
         * @param value The original value
         */
        public NonCancelable(@Nullable T value){
            super(value);
        }
        
        @Override
        final public boolean isCancelable(){
            return false;
        }
    }
}
