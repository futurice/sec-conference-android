package de.sec.android.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import de.sec.android.models.pojo.Event;
import de.sec.android.utils.Constant;

public class DaySchedule {

    public static final String[] ALL_ROOMS = {"Galerie", "Raum 2", "Raum 3", "Raum 4", "Loft", "Raum 5", "Atelier"};
    private String conferenceDay;
    private Map<String, List<Event>> eventsByLocation = new TreeMap<String, List<Event>>();
    private DateTime earliestTime;
    private DateTime latestTime;

    public DaySchedule(String conferenceDay, List<Event> events) {
        this(conferenceDay, events, Constant.EventType.ALL);
    }

    private boolean isBarCamp (Event event) {
        return event.bar_camp;
    }

    private boolean isKeytalk (Event event) {
        return event.key_talk;
    }

    public DaySchedule(String conferenceDay, List<Event> events, Constant.EventType eventType) {
        this.conferenceDay = conferenceDay;
        this.eventsByLocation = new HashMap<String, List<Event>>();

        // Initialize eventsByLocation with all the rooms, ordered correctly
        for (String location : ALL_ROOMS) {
            this.eventsByLocation.put(location, new ArrayList<Event>());
        }

        // Organize given list of events into eventsByLocation
        for (Event ev : events) {
            if (ev.day == null) { continue; }
            if (eventType == Constant.EventType.BAR_CAMP && !isBarCamp(ev)) { continue; }
            if (eventType == Constant.EventType.KEY_TALK && !isKeytalk(ev)) { continue; }
            if (!ev.day.equals(conferenceDay)) { continue; }

            if (this.eventsByLocation.get(ev.location) == null) {
                continue;
            }
            this.eventsByLocation.get(ev.location).add(ev);
        }
        setEarliestAndLatestTimes();
    }

    public String getConferenceDay() {
        return conferenceDay;
    }

    public List<Event> getEvents() {
        ArrayList<Event> list = new ArrayList<Event>();
        for (List<Event> subList : eventsByLocation.values()) {
            list.addAll(subList);
        }
        return list;
    }

    public Map<String, List<Event>> getEventsByLocation() {
        return eventsByLocation;
    }

    public DateTime getEarliestTime() {
        return earliestTime;
    }

    public DateTime getLatestTime() {
        return latestTime;
    }

    public List<String> getStages() {
        return new ArrayList<String>(eventsByLocation.keySet());
    }

    private void setEarliestAndLatestTimes() {
        earliestTime = new DateTime(2050,1,1,0,0);
        latestTime = new DateTime(1980,1,1,0,0);

        for (Map.Entry<String, List<Event>> entry : eventsByLocation.entrySet()) {
            for (Event event : entry.getValue()) {
                try {
                    DateTime startTime = new DateTime(event.start_time, DateTimeZone.forID("Europe/Berlin"));
                    DateTime endTime = new DateTime(event.end_time, DateTimeZone.forID("Europe/Berlin"));
                    if (startTime.isBefore(earliestTime)) {
                        earliestTime = startTime;
                    }
                    if (endTime.isAfter(latestTime)) {
                        latestTime = endTime;
                    }
                }
                catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
