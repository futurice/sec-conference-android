package de.sec.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.sec.android.MainActivity;
import de.sec.android.R;
import de.sec.android.models.DaySchedule;
import de.sec.android.models.EventsModel;
import de.sec.android.models.pojo.Event;
import de.sec.android.utils.Constant;
import de.sec.android.utils.DateUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class EventListFragment extends Fragment {

    final private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private EventsModel eventsModel;
    private LinearLayout saturdayList;
    private LinearLayout sundayList;
    private Constant.EventType eventType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsModel = EventsModel.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);
        saturdayList = (LinearLayout) view.findViewById(R.id.saturday_list);
        sundayList = (LinearLayout) view.findViewById(R.id.sunday_list);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        compositeSubscription.add(getDaySchedule$(Constant.FIRST_DAY, eventsModel.getEvents$())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new FillEventListAction())
        );
        compositeSubscription.add(getDaySchedule$(Constant.SECOND_DAY, eventsModel.getEvents$())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new FillEventListAction())
        );
    }

    public void setEventType(Constant.EventType eventType) {
        this.eventType = eventType;
    }

    private Observable<DaySchedule> getDaySchedule$(final String day, Observable<List<Event>> events$) {
        return events$
            .map(new Func1<List<Event>, DaySchedule>() { @Override public DaySchedule call(List<Event> events) {
                return new DaySchedule(day, events, eventType);
            }});
    }

    private class FillEventListAction implements Action1<DaySchedule> {
        @Override
        public void call(DaySchedule daySchedule) {
            LinearLayout dayList;
            if (Constant.FIRST_DAY.equals(daySchedule.getConferenceDay())) {
                dayList = saturdayList;
            }
            else if (Constant.SECOND_DAY.equals(daySchedule.getConferenceDay())) {
                dayList = sundayList;
            }
            else {
                return;
            }

            dayList.removeAllViews();
            List<Event> listEvents = daySchedule.getEvents();
            DateUtils.sortEventsByStartTime(listEvents);
            boolean firstWasRendered = false;
            for (final Event event : listEvents) {
                    if (firstWasRendered) {
                        dayList.addView(makeHorizontalLine(dayList));
                    }
                    dayList.addView(makeEventListItem(event));
                    firstWasRendered = true;
            }
        }
    }

    private View makeHorizontalLine(final ViewGroup container) {
        return LayoutInflater.from(getActivity()).inflate(
            R.layout.view_horizontal_line,
            container,
            false
        );
    }

    private View makeEventListItem(final Event event) {
        View view = LayoutInflater.from(getActivity())
            .inflate(R.layout.view_event_list_item, null, false);
        TextView primaryText = (TextView) view.findViewById(R.id.primary);
        TextView secondaryText = (TextView) view.findViewById(R.id.secondary);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        View star = view.findViewById(R.id.star);

        primaryText.setText(event.title);
        secondaryText.setText(makeSecondaryString(event));
        if (event.speaker_image_url != null && event.speaker_image_url.length() > 0) {
            Picasso.with(getActivity())
                .load(event.speaker_image_url)
                .error(R.drawable.person_placeholder)
                .into(imageView);
        }
        else {
            Picasso.with(getActivity())
                .load(R.drawable.person_placeholder)
                .into(imageView);
        }

        view.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
            MainActivity activity = (MainActivity) getActivity();
            EventFragment fragment = new EventFragment();
            fragment.setArguments(event.getBundle());
            activity.fragment$.onNext(fragment);
        }});

        if (Event.getIsFavoriteFromPreferences(getActivity(), event._id)) {
            star.setVisibility(View.VISIBLE);
        }
        else {
            star.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    private static String makeSecondaryString(Event event) {
        String secondaryString = "";
        if (event.artists != null && event.artists.length() > 0) {
            secondaryString += event.artists;
        }
        if (event.speaker_role != null && event.speaker_role.length() > 0) {
            if (secondaryString.length() > 0) {
                secondaryString += ", ";
            }
            secondaryString += event.speaker_role;
        }
        return secondaryString;
    }

    @Override
    public void onPause() {
        super.onPause();
        compositeSubscription.clear();
    }
}
