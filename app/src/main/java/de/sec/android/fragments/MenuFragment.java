package de.sec.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.sec.android.MainActivity;
import de.sec.android.R;
import de.sec.android.utils.Constant;

public class MenuFragment extends Fragment {

    private View agendaView;
    private View keyTalksView;
    private View barCampsView;
    private View venue;
    private View info;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        agendaView = view.findViewById(R.id.agenda);
        keyTalksView = view.findViewById(R.id.keytalks);
        barCampsView = view.findViewById(R.id.barcamps);
        venue = view.findViewById(R.id.venue);
        info = view.findViewById(R.id.info);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final MainActivity activity = (MainActivity) getActivity();
        agendaView.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
            activity.fragment$.onNext(activity.scheduleFragment);
        }});
        keyTalksView.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
            activity.eventListFragment.setEventType(Constant.EventType.KEY_TALK);
            activity.fragment$.onNext(activity.eventListFragment);
        }});
        barCampsView.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
            activity.eventListFragment.setEventType(Constant.EventType.BAR_CAMP);
            activity.fragment$.onNext(activity.eventListFragment);
        }});
        venue.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
            activity.fragment$.onNext(activity.venueFragment);
        }});
        info.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
            activity.fragment$.onNext(activity.infoListFragment);
        }});
    }

    @Override
    public void onPause() {
        super.onPause();
        agendaView.setOnClickListener(null);
        keyTalksView.setOnClickListener(null);
    }
}
