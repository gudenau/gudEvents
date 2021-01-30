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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;

public final class Utils{
    private static final List<String> MAP_IMPLEMENTATIONS = Arrays.asList(
        "it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap",
        "java.util.HashMap"
    );
    
    private static final MethodHandle Map$init;
    
    static{
        Map$init = findConstructor(MAP_IMPLEMENTATIONS, MethodType.methodType(void.class))
            .orElseThrow(()->new RuntimeException("Failed to find Map implementation"));
    }
    
    private static Optional<MethodHandle> findConstructor(List<String> owners, MethodType method){
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        for(String owner : owners){
            try{
                Class<?> klass = Class.forName(owner);
                return Optional.of(lookup.findConstructor(klass, method));
            }catch(ReflectiveOperationException ignored){}
        }
        return Optional.empty();
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> createMap(){
        try{
            return (Map<K, V>)Map$init.invoke();
        }catch(Throwable throwable){
            throw new RuntimeException("Failed to init Map", throwable);
        }
    }
    
    public static <T> Set<T> createSet(){
        return new HashSet<>();
    }
}
