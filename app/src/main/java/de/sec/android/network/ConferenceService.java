package de.sec.android.network;

import java.util.List;

import de.sec.android.models.pojo.Event;

import de.sec.android.models.pojo.Info;
import retrofit.http.GET;

import rx.Observable;

/**
 * Endpoint for the Search Engine REST API.
 * Using Retrofit annotations.
 */
public interface ConferenceService {

    @GET("/events")
    public Observable<List<Event>> getAllEvents();

    @GET("/info")
    public Observable<List<Info>> getAllInfo();
}
