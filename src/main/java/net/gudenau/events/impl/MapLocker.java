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

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Bare-bones thread safe Map without using synchronize
 */
final class MapLocker<K, V>{
    public static <K, V> MapLocker<K, V> create(){
        return new MapLocker<>(Utils.createMap());
    }
    
    private final Map<K, V> map;
    private final Lock readLock;
    private final Lock writeLock;
    
    private MapLocker(Map<K, V> map){
        this.map = map;
    
        ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }
    
    @NotNull
    public V computeIfAbsent(K key, Function<K, V> factory){
        readLock.lock();
        V value = map.get(key);
        readLock.unlock();
        if(value == null){
            writeLock.lock();
            value = map.computeIfAbsent(key, factory);
            writeLock.unlock();
        }
        return value;
    }
    
    @Nullable
    public V putIfAbsent(K key, V value){
        readLock.lock();
        V oldValue = map.get(key);
        readLock.unlock();
        if(oldValue != null){
            return oldValue;
        }
        writeLock.lock();
        value = map.putIfAbsent(key, value);
        writeLock.unlock();
        return value;
    }
    
    @Nullable
    public V get(K key){
        readLock.lock();
        V value = map.get(key);
        readLock.unlock();
        return value;
    }
}
