package event;

import event.delete.DeleteEventListener;
import event.load.LoadEventListener;
import event.save.SaveEventListener;
import event.update.UpdateEventListener;

public enum EventType {
    LOAD("load", LoadEventListener.class),
    SAVE("save", SaveEventListener.class),
    UPDATE("update", UpdateEventListener.class),
    DELETE("delete", DeleteEventListener.class);

    private final String eventName;
    private final Class<?> eventListenerClass;

    EventType(String eventName, Class<?> eventListenerClass) {
        this.eventName = eventName;
        this.eventListenerClass = eventListenerClass;
    }
}
