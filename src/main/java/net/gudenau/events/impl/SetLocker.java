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

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntFunction;

/**
 * Bare-bones thread safe Set without using synchronize
 */
final class SetLocker<T>{
    public static <T> SetLocker<T> create(){
        return new SetLocker<>(Utils.createSet());
    }
    
    private final Set<T> set;
    private final Lock readLock;
    private final Lock writeLock;
    
    private SetLocker(Set<T> set){
        this.set = set;
        
        ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }
    
    public boolean add(T value){
        writeLock.lock();
        boolean result = set.add(value);
        writeLock.unlock();
        return result;
    }
    
    public int size(){
        readLock.lock();
        int size = set.size();
        readLock.unlock();
        return size;
    }
    
    public T[] toArray(IntFunction<T[]> generator){
        readLock.lock();
        T[] result = set.toArray(generator.apply(0));
        readLock.unlock();
        return result;
    }
    
    public boolean remove(T element){
        writeLock.lock();
        boolean result = set.remove(element);
        writeLock.unlock();
        return result;
    }
}
