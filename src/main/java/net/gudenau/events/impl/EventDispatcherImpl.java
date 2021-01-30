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

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import net.gudenau.events.Event;
import net.gudenau.events.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Does all of the grunt work.
 *
 * Responsible for:
 *  - Generating classes
 *  - Dispatching events
 *  - Managing event handlers
 */
public final class EventDispatcherImpl<T, E extends Event<T>> implements EventDispatcher<T, E>{
    private static final MapLocker<String, Class<?>> HANDLER_CLASSES = MapLocker.create();
    
    private final SetLocker<Consumer<E>> handlers = SetLocker.create();
    
    private final ReadWriteLock handlerLock = new ReentrantReadWriteLock();
    private EventConsumer<T, E> consumer = null;
    
    @Override
    public void registerHandler(@NotNull Consumer<E> handler){
        if(this.consumer != null){
            Lock lock = handlerLock.writeLock();
            lock.lock();
            this.consumer = null;
            lock.unlock();
        }
        handlers.add(handler);
    }
    
    @Override
    public void removeHandler(@NotNull Consumer<E> handler){
        if(this.consumer != null){
            Lock lock = handlerLock.writeLock();
            lock.lock();
            this.consumer = null;
            lock.unlock();
        }
        handlers.remove(handler);
    }
    
    @Override
    public Optional<T> dispatchEvent(@NotNull E event){
        Lock lock = handlerLock.readLock();
        lock.lock();
        if(consumer == null){
            lock.unlock();
            Lock writeLock = handlerLock.writeLock();
            writeLock.lock();
            consumer = createConsumer(event.isCancelable());
            writeLock.unlock();
            lock.lock();
        }
        consumer.consumeEvent(event);
        lock.unlock();
        return event.getResult();
    }
    
    /**
     * Creates a new event consumer for this dispatcher.
     *
     * @param cancelable Weather or not the event is cancelable
     *
     * @return The event new consumer
     */
    @SuppressWarnings("unchecked")
    private EventConsumer<T, E> createConsumer(boolean cancelable){
        try{
            int size = handlers.size();
            String className = ("net.gudenau.events.gen.Handler" + (cancelable ? "Cancelable" : "") + size).intern();
            // Generate and load the class if required.
            Class<? extends EventConsumer<T, E>> klass = (Class<? extends EventConsumer<T, E>>)HANDLER_CLASSES.computeIfAbsent(className, this::generateClass);
            Class<?>[] params = new Class[size];
            Arrays.fill(params, Consumer.class);
            return klass.getDeclaredConstructor(params).newInstance((Object[])handlers.toArray(Consumer[]::new));
        }catch(ReflectiveOperationException e){
            throw new RuntimeException("Failed to create event handler", e);
        }
    }
    
    /**
     * Our special ClassLoader that generates our classes.
     *
     * This is where the dispatch speed comes from.
     */
    private final ClassLoader classLoader = new ClassLoader(EventDispatcherImpl.class.getClassLoader()){
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException{
            if(!name.startsWith("net.gudenau.events.gen.Handler")){
                throw new ClassNotFoundException(name);
            }
    
            String className = name.replaceAll("\\.", "/");
    
            // Get number of handlers from name
            int index = className.length() - 1;
            while(Character.isDigit(className.charAt(index))){
                index--;
            }
            int eventCount = Integer.parseInt(className.substring(index + 1));
    
            // Get event type info
            boolean cancelable = name.contains("Cancelable");
    
            ClassWriter writer = new ClassWriter(0);
            
            writer.visit(
                V1_8, ACC_PUBLIC | ACC_FINAL | ACC_SYNTHETIC, className,
                "<T:Ljava/lang/Object;E:Lnet/gudenau/events/Event<TT;>;>Ljava/lang/Object;Lnet/gudenau/events/impl/EventConsumer<TT;TE;>;",
                "java/lang/Object",
                new String[]{ "net/gudenau/events/impl/EventConsumer" }
            );
            
            for(int i = 0; i < eventCount; i++){
                writer.visitField(ACC_PRIVATE | ACC_FINAL, "consumer" + i, "Ljava/util/function/Consumer;", "Ljava/util/function/Consumer<TE;>;", null);
            }
            
            { // <init>
                StringBuilder initDescriptor = new StringBuilder("(");
                StringBuilder initSignature = new StringBuilder("(");
                
                for(int i = 0; i < eventCount; i++){
                    initDescriptor.append("Ljava/util/function/Consumer;");
                    initSignature.append("Ljava/util/function/Consumer<TE;>;");
                }
                
                initDescriptor.append(")V");
                initSignature.append(")V");
                
                MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "<init>", initDescriptor.toString(), initSignature.toString(), null);
                method.visitCode();
                
                Label start = new Label();
                method.visitLabel(start);
                
                method.visitVarInsn(ALOAD, 0);
                method.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                
                for(int i = 0; i < eventCount; i++){
                    method.visitVarInsn(ALOAD, 0);
                    method.visitVarInsn(ALOAD, i + 1);
                    method.visitFieldInsn(PUTFIELD, className, "consumer" + i, "Ljava/util/function/Consumer;");
                }
                
                method.visitInsn(RETURN);
                
                Label end = new Label();
                method.visitLabel(end);
                
                method.visitLocalVariable("this", "L" + className + ";", null, start, end, 0);
                for(int i = 0; i < eventCount; i++){
                    method.visitLocalVariable("consumer" + i, "Ljava/util/function/Consumer;", "L" + className + "<TT;TE;>;", start, end, i + 1);
                }
                
                method.visitMaxs(2, eventCount + 1);
                method.visitEnd();
            }
            
            { // consumeEvent(Event)
                MethodVisitor method = writer.visitMethod(ACC_PUBLIC | ACC_FINAL, "consumeEvent", "(Lnet/gudenau/events/Event;)V", "(TE;)V", null);
                
                Label start = new Label();
                method.visitLabel(start);
    
                for(int i = 0; i < eventCount; i++){
                    method.visitVarInsn(ALOAD, 0);
                    method.visitFieldInsn(GETFIELD, className, "consumer" + i, "Ljava/util/function/Consumer;");
                    method.visitVarInsn(ALOAD, 1);
                    method.visitMethodInsn(INVOKEINTERFACE, "java/util/function/Consumer", "accept", "(Ljava/lang/Object;)V", true);
                    if(cancelable && i < eventCount - 1){
                        method.visitVarInsn(ALOAD, 1);
                        method.visitMethodInsn(INVOKEVIRTUAL, "net/gudenau/events/Event", "wasCanceled", "()V", false);
                        Label jump = new Label();
                        method.visitJumpInsn(IFEQ, jump);
                        method.visitInsn(RETURN);
                        method.visitLabel(jump);
                        method.visitFrame(F_SAME, 0, null, 0, null);
                    }
                    
                    method.visitInsn(RETURN);
                    
                    Label end = new Label();
                    method.visitLabel(end);
                    
                    method.visitLocalVariable("this", "L" + className + ";", null, start, end, 0);
                    method.visitLocalVariable("event", "Lnet/gudenau/events/Event;", "L" + className + "<TT;TE;>;", start, end, 1);
                    
                    method.visitMaxs(2, 2);
                    method.visitEnd();
                }
            }
            
            byte[] bytecode = writer.toByteArray();
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    };
    
    /**
     * Generates a new class.
     *
     * All this does is call our special ClassLoader.
     *
     * @param name The name of the class
     *
     * @return The new class
     */
    private Class<?> generateClass(String name){
        try{
            return AccessController.doPrivileged((PrivilegedExceptionAction<Class<?>>)()->
                classLoader.loadClass(name)
            );
        }catch(Exception e){
            throw new RuntimeException("Failed to generate handler class: " + name.substring(name.lastIndexOf('.') + 1), e);
        }
    }
}
