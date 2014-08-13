package de.serviceexperiencecamp.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import de.serviceexperiencecamp.android.R;
import de.serviceexperiencecamp.android.models.DaySchedule;
import de.serviceexperiencecamp.android.models.EventsModel;
import de.serviceexperiencecamp.android.models.pojo.Event;
import de.serviceexperiencecamp.android.utils.SubscriptionUtils;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.serviceexperiencecamp.android.views.EventTimelineView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class ScheduleFragment extends Fragment {

    final private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private EventsModel eventsModel;
    private Observable<Event> firstEvent$ = Observable.empty(); // instead of null as default

    private TextView bookNameTextView;

    private static int ROW_HEIGHT = 66; // dp
    private static final int TIMELINE_END_OFFSET = 30; // minutes
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
    private int hourMarkerWidthPx = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsModel = EventsModel.getInstance();
        hourMarkerWidthPx = getResources().getDimensionPixelSize(R.dimen.timeline_hour_marker_width);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        HorizontalScrollView scrollView = (HorizontalScrollView) view.findViewById(R.id.timelineScrollView);

        // Gestures
        gestureDetector = new GestureDetector(getActivity(), new GuitarSwipeListener());
        gestureListener = new View.OnTouchListener() { public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }};
        scrollView.setOnTouchListener(gestureListener);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookNameTextView = (TextView) getView().findViewById(R.id.title);
        firstEvent$ = getFirstEvent$(eventsModel.getEvents$());
        getDaySchedule$(eventsModel.getEvents$())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<DaySchedule>() { @Override public void call(DaySchedule daySchedule) {
                addTimeline(daySchedule);
                addGigs(daySchedule);
            }});
    }

    @Override
    public void onResume() {
        super.onResume();
        subscribeTextView(getEventTitle$(firstEvent$), bookNameTextView);
    }

    private static Observable<Event> getFirstEvent$(Observable<List<Event>> events$) {
        return events$
            .map(new Func1<List<Event>, Event>() { @Override public Event call(List<Event> events) {
                return events.get(0);
            }});
    }

    private static Observable<String> getEventTitle$(Observable<Event> event$) {
        return event$
            .map(new Func1<Event, String>() { @Override public String call(Event event) {
                return event.title;
            }})
            .startWith("Loading...");
    }

    private Observable<DaySchedule> getDaySchedule$(Observable<List<Event>> events$) {
        return events$
            .map(new Func1<List<Event>, DaySchedule>() { @Override public DaySchedule call(List<Event> events) {
                return new DaySchedule("Saturday", events);
            }});
    }

    private DateTime getTimelineStartMoment(DaySchedule daySchedule) {
        return daySchedule.getEarliestTime().minusMinutes(TIMELINE_END_OFFSET);
    }

    private DateTime getTimelineEndMoment(DaySchedule daySchedule) {
        return daySchedule.getLatestTime().plusMinutes(TIMELINE_END_OFFSET);
    }

    private void updateCurrentTimeline(DaySchedule daySchedule) {
        DateTime timelineStartMoment = getTimelineStartMoment(daySchedule);
        DateTime timelineEndMoment = getTimelineEndMoment(daySchedule);
        DateTime now = daySchedule.getEarliestTime().plusMinutes(50);  // DateTime.now();
        View line = getView().findViewById(R.id.timelineNowLine);
        line.bringToFront();
        if (now.isAfter(timelineStartMoment) && now.isBefore(timelineEndMoment)) {
            line.setVisibility(View.VISIBLE);
            TextView marginView = (TextView) getView().findViewById(R.id.timelineNowMargin);
            int duration = getDurationInMinutes(timelineStartMoment, now);
            int leftMargin = duration * dpToPx(EventTimelineView.MINUTE_WIDTH) - hourMarkerWidthPx/2 - 3;
            //initialScrollTo = leftMargin - getWindowManager().getDefaultDisplay().getWidth()/2;
            marginView.setWidth(leftMargin);
        } else {
            line.setVisibility(View.GONE);
        }
    }

    private void addTimeline(DaySchedule daySchedule) {
        DateTime timelineStartMoment = getTimelineStartMoment(daySchedule);
        DateTime timelineEndMoment = getTimelineEndMoment(daySchedule);

        LinearLayout numbersLayout = (LinearLayout) getView().findViewById(R.id.timelineNumbers);
        LinearLayout timelineVerticalLines = (LinearLayout) getView().findViewById(R.id.timelineVerticalLines);

        DateTime cursor = timelineStartMoment;

        // Left margin on the earliest time number layout
        int minutes = 60 - cursor.getMinuteOfHour();
        TextView tv = new TextView(getActivity());
        tv.setHeight(dpToPx(ROW_HEIGHT));
        tv.setWidth(dpToPx(EventTimelineView.MINUTE_WIDTH) * minutes);
        numbersLayout.addView(tv);

        // Left margin on the earliest time vertical line
        tv = new TextView(getActivity());
        tv.setWidth(dpToPx(EventTimelineView.MINUTE_WIDTH) * minutes);
        timelineVerticalLines.addView(tv);
        cursor = cursor.plusMinutes(minutes);

        while (cursor.isBefore(timelineEndMoment)) {
            minutes = getDurationInMinutes(cursor, timelineEndMoment);
            minutes = (minutes < 60) ? minutes : 60;

            // Add the hour text
            tv = new TextView(this.getActivity());
            tv.setTextColor(getResources().getColor(R.color.orange));
            String hour = "" + cursor.getHourOfDay() + ":00";
            if (hour.length() == 4) {
                hour = "0" + hour;
            }
            tv.setText(hour);
            tv.setGravity(Gravity.LEFT|Gravity.BOTTOM);
            tv.setWidth(dpToPx(EventTimelineView.MINUTE_WIDTH) * minutes);
            tv.setHeight(getResources().getDimensionPixelSize(R.dimen.spacing_large));
            numbersLayout.addView(tv);

            // Add vertical line
            this.getActivity().getLayoutInflater().inflate(R.layout.timeline_hour_marker, timelineVerticalLines);

            // Add right margin for the vertical line
            tv = new TextView(this.getActivity());
            tv.setWidth(dpToPx(EventTimelineView.MINUTE_WIDTH) * minutes - hourMarkerWidthPx);
            timelineVerticalLines.addView(tv);

            cursor = cursor.plusHours(1);
        }
        updateCurrentTimeline(daySchedule);
    }

    private static int getDurationInMinutes(DateTime before, DateTime after) {
        if (before.isAfter(after)) {
            throw new InvalidParameterException("Dont invert the order of duration DateTime params");
        }
        return (int) (new Duration(before, after).getStandardMinutes());
    }

    private void addGigs(DaySchedule daySchedule) {
        DateTime timelineStartMoment = getTimelineStartMoment(daySchedule);
        Map<String, List<Event>> eventsByLocation = daySchedule.getEventsByLocation();

        ViewGroup gigLayout = (ViewGroup) getView().findViewById(R.id.gigLayout);
        TextView textView = new TextView(this.getActivity());
        textView.setText("");
        textView.setHeight(dpToPx(ROW_HEIGHT));
        textView.setPadding(1, 10, 1, 1);
        gigLayout.addView(textView);

        List<String> locations = new ArrayList<String>(eventsByLocation.keySet());

        for (String location : locations) {
            LinearLayout stageRow = new LinearLayout(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
//            params.setMargins(0, 2, 0, 2);
            stageRow.setLayoutParams(params);
            stageRow.setOrientation(LinearLayout.HORIZONTAL);

            DateTime previousTime = timelineStartMoment;
            for (Event event : getSortedEventsOfLocation(eventsByLocation, location)) {
                DateTime eventStartTime = new DateTime(event.start_time);
                DateTime eventEndTime = new DateTime(event.end_time);
                if (previousTime.isBefore(eventStartTime)) {
                    int duration = getDurationInMinutes(previousTime, eventStartTime);
                    int margin = dpToPx(EventTimelineView.MINUTE_WIDTH) * duration;
                    TextView tv = new TextView(getActivity());
                    tv.setHeight(dpToPx(ROW_HEIGHT));
                    tv.setWidth(margin);
                    stageRow.addView(tv);
                }

                EventTimelineView gigWidget = new EventTimelineView(
                    getActivity(),
                    null,
                    event,
                    previousTime.toDate()
                );
                stageRow.addView(gigWidget);
                if (eventEndTime.equals(daySchedule.getLatestTime())) {
                    int margin = dpToPx(EventTimelineView.MINUTE_WIDTH) * TIMELINE_END_OFFSET;
                    TextView tv = new TextView(getActivity());
                    tv.setHeight(dpToPx(ROW_HEIGHT));
                    tv.setWidth(margin);
                    stageRow.addView(tv);
                }

                //gigWidget.setOnClickListener(gigWidgetClickListener);
                previousTime = eventEndTime;
            }
            gigLayout.addView(stageRow);
        }
    }

    private static List<Event> getSortedEventsOfLocation(
        Map<String, List<Event>> eventsByLocation,
        String location)
    {
        List<Event> listEvents = eventsByLocation.get(location);
        java.util.Collections.sort(listEvents, new Comparator<Event>() { @Override public int compare(Event lhs, Event rhs) {
            DateTime lhsStart = new DateTime(lhs.start_time);
            DateTime rhsStart = new DateTime(rhs.start_time);
            if (lhsStart.isAfter(rhsStart))
                return 1;
            else if (lhsStart.isBefore(rhsStart))
                return -1;
            else
                return 0;
        }});
        return listEvents;
    }

    class GuitarSwipeListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float distanceX = Math.abs(e1.getX() - e2.getX());
                float distanceY = Math.abs(e1.getY() - e2.getY());
                if (distanceX == 0 || distanceY / distanceX > 5) {
                    if (distanceY > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                    {
                        boolean upwardMotion = e1.getY() - e2.getY() > 0;
                        HashMap<String, String> swipeMap = new HashMap<String, String>();
                        swipeMap.put("direction", upwardMotion ? "up" : "down");
                    }
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float displayDensity = metrics.density;
        return (int) (dp * displayDensity);
    }

    private int pxToDp(int px) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float displayDensity = metrics.density;
        return (int) (px / displayDensity);
    }

    private void subscribeTextView(Observable<String> observable, final TextView textView) {
        compositeSubscription.add(SubscriptionUtils.subscribeTextViewText(observable, textView));
    }

    @Override
    public void onPause() {
        super.onPause();
        compositeSubscription.clear();
    }
}
