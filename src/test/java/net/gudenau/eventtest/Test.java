package net.gudenau.eventtest;

import java.util.function.Supplier;
import net.gudenau.events.Event;
import net.gudenau.events.EventDispatcher;

public class Test{
    private static final int handlerCount = 100;
    private static final int timedCount = Integer.MAX_VALUE;
    private static final int warmupCount = Integer.MAX_VALUE;
    
    private Test(){}
    
    private static final class TestEvent extends Event.NonCancelable<Void>{}
    private static final class CancelableEvent extends Event.Cancelable<Void>{}
    
    public static void main(String[] args){
        test(EventDispatcher.createInstance(), TestEvent::new, "non-cancelable");
        test(EventDispatcher.createInstance(), CancelableEvent::new, "cancelable");
    }
    
    private static <T, E extends Event<T>> void test(EventDispatcher<T, E> bus, Supplier<E> factory, String label){
        for(int i = 0; i < handlerCount; i++){
            bus.registerHandler((event)->{});
        }
    
        for(int i = 0; i < warmupCount; i++){
            bus.dispatchEvent(factory.get());
        }
    
        long totalTime = 0;
    
        for(int i = 0; i < timedCount; i++){
            E event = factory.get();
            long time = System.nanoTime();
            bus.dispatchEvent(event);
            totalTime += System.nanoTime() - time;
        }
    
        System.out.println("Time per " + label + "submission: " + (totalTime / timedCount) + "ns");
    }
}
