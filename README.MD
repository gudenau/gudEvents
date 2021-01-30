## gudEvents
A small event handler that is designed with speed in mind.

---

### How it works:
It generates classes that correspond to the amount of event handlers. This is handled when an event is dispatched so
there may be a larger-than-normal delay when an event is dispatched after the handlers change. But after that the
dispatch essentially becomes:
```Java
public void handleEvent(Event<T> event){
    handler0.handle(event);
    handler1.handle(event);
    handler2.handle(event);
    handler3.handle(event);
}
```
