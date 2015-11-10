package de.serviceexperiencecamp.android;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.danlew.android.joda.JodaTimeAndroid;

import de.serviceexperiencecamp.android.fragments.EventListFragment;
import de.serviceexperiencecamp.android.fragments.InfoListFragment;
import de.serviceexperiencecamp.android.fragments.MenuFragment;
import de.serviceexperiencecamp.android.fragments.ScheduleFragment;
import de.serviceexperiencecamp.android.fragments.VenueFragment;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

public class MainActivity extends Activity {

    public BehaviorSubject<Fragment> fragment$ = BehaviorSubject.create((Fragment) new MenuFragment());

    public MenuFragment menuFragment;
    public ScheduleFragment scheduleFragment;
    public EventListFragment eventListFragment;
    public VenueFragment venueFragment;
    public InfoListFragment infoListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_main);
        menuFragment = new MenuFragment();
        scheduleFragment = new ScheduleFragment();
        eventListFragment = new EventListFragment();
        venueFragment = new VenueFragment();
        infoListFragment = new InfoListFragment();

        fragment$.subscribe(new Action1<Fragment>() { @Override public void call(Fragment frag) {
            MainActivity.this.getFragmentManager().beginTransaction()
                .replace(R.id.container, frag)
                .addToBackStack(null)
                .commit();
        }});
    }

    @Override
    public void onBackPressed() {
        getFragmentManager().popBackStackImmediate();
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
    }

}
