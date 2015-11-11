package de.sec.android.models;

import de.sec.android.models.pojo.Event;
import de.sec.android.network.SecApi;

import java.util.List;

import rx.Observable;

public class EventsModel {
    static private EventsModel instance;

    static public EventsModel getInstance() {
        if (instance == null) {
            instance = new EventsModel();
        }
        return instance;
    }

    private EventsModel() { }

    public Observable<List<Event>> getEvents$() {
        return SecApi.getInstance().getAllEvents();
    }

}
