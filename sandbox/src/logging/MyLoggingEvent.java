package logging;

import org.slf4j.event.LoggingEvent;

public interface MyLoggingEvent extends LoggingEvent {
//    default String myAdditionalInfo() {
//        return getMarker().toString();
//    }
    String myAdditinalInfo();
}