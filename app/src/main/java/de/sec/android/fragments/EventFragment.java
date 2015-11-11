package de.sec.android.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import de.sec.android.R;
import de.sec.android.models.pojo.Event;

public class EventFragment extends Fragment {

    private boolean isFavorite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        final Bundle bundle = getArguments();
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        // Find the views

        // first presenter
        TextView speakerView = (TextView) view.findViewById(R.id.speaker);
        TextView speakerRoleView = (TextView) view.findViewById(R.id.speaker_role);
        ImageView linkedin = (ImageView) view.findViewById(R.id.linkedin);
        ImageView twitter = (ImageView) view.findViewById(R.id.twitter);

        TextView speakerView2 = (TextView) view.findViewById(R.id.speaker2);
        TextView speakerRoleView2 = (TextView) view.findViewById(R.id.speaker_role2);
        ImageView linkedin2 = (ImageView) view.findViewById(R.id.linkedin2);
        ImageView twitter2 = (ImageView) view.findViewById(R.id.twitter2);
        RelativeLayout globalSpeaker2 = (RelativeLayout) view.findViewById(R.id.speaker2_global);

        final ImageView favoriteButton = (ImageView) view.findViewById(R.id.favorite_button);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        TextView timeView = (TextView) view.findViewById(R.id.time);
        TextView dayView = (TextView) view.findViewById(R.id.day);
        TextView locationView = (TextView) view.findViewById(R.id.location);
        TextView descriptionView = (TextView) view.findViewById(R.id.description);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);

        // Set the bundle arguments as the content for the views
        speakerView.setText(prepareString(bundle.getString("artists")));
        speakerRoleView.setText(prepareString(bundle.getString("speaker_role")));
        setLinkedInContent(linkedin, bundle.getString("linkedin_url"));
        setTwitterContent(twitter, bundle.getString("twitter_handle"));


        if (isNullOrEmpty(bundle.getString("artists_2"))) {
            globalSpeaker2.setVisibility(View.GONE);
            speakerRoleView2.setVisibility(View.GONE);
        } else
        {
            globalSpeaker2.setVisibility(View.VISIBLE);
            speakerRoleView2.setVisibility(View.VISIBLE);
            speakerView2.setText(prepareString(bundle.getString("artists_2")));
            speakerRoleView2.setText(prepareString(bundle.getString("speaker_role_2")));
            setLinkedInContent(linkedin2, bundle.getString("linkedin_url_2"));
            setTwitterContent(twitter2, bundle.getString("twitter_handle_2"));
        }

        titleView.setText(bundle.getString("title"));
        timeView.setText(makeTimeString(
            bundle.getString("start_time"), bundle.getString("end_time"))
        );
        dayView.setText(bundle.getString("day"));
        locationView.setText(bundle.getString("location"));
        descriptionView.setText(processDescriptionString(bundle.getString("description")));
        setImageViewContent(imageView, bundle.getString("image_url"));

        final String _id = bundle.getString("_id");
        isFavorite = Event.getIsFavoriteFromPreferences(getActivity(), _id);
        setFavoriteButtonStatus(favoriteButton, isFavorite);
        favoriteButton.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
            isFavorite = !isFavorite;
            setFavoriteButtonStatus(favoriteButton, isFavorite);
            Event.setIsFavoriteFromPreferences(getActivity(), _id, isFavorite);
        }});

        return view;
    }

    private void setFavoriteButtonStatus(ImageView favoriteButton, boolean isFavorite) {
        favoriteButton.setSelected(isFavorite);
        if (isFavorite) {
            favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_black));
        }
        else {
            favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_star));
        }
    }

    private void setImageViewContent(ImageView imageView, String image_url) {
        if (!isNullOrEmpty(image_url)) {
            Picasso.with(getActivity())
                .load(image_url)
                .error(R.drawable.event_placeholder)
                .into(imageView);
        }
        else {
            Picasso.with(getActivity())
                .load(R.drawable.event_placeholder)
                .into(imageView);
        }
    }

    private void setLinkedInContent(View view, final String linkedin_url) {
        if (!isNullOrEmpty(linkedin_url)) {
            view.setAlpha(1);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedin_url));
                    startActivity(browserIntent);
                }
            });
        }
        else {
            view.setAlpha(0.5f);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(),
                            getActivity().getResources().getString(R.string.no_linkedin),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setTwitterContent(View view, final String twitter_handle) {
        if (!isNullOrEmpty(twitter_handle)) {
            view.setAlpha(1);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String twitter_url = "https://twitter.com/" + twitter_handle;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitter_url));
                    startActivity(browserIntent);
                }
            });
        }
        else {
            view.setAlpha(0.5f);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(),
                            getActivity().getResources().getString(R.string.no_twitter),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private static boolean isNullOrEmpty(final String input) {
        return (input == null || input.length() <= 0);
    }

    private static String prepareString(final String input) {
        if (input == null) {
            return "";
        }
        else {
            return input;
        }
    }

    private String processDescriptionString(String s) {
        if (s == null) { return ""; }
        return s.replaceAll("\\\\n", "\n");
    }

    private String makeTimeString(final String startInput, final String endInput) {
        DateTime startDateTime = new DateTime(startInput, DateTimeZone.forID("Europe/Berlin"));
        DateTime endDateTime = new DateTime(endInput, DateTimeZone.forID("Europe/Berlin"));
        String startOutput = startDateTime.toString("HH:mm");
        String endOutput = endDateTime.toString("HH:mm");
        return startOutput + "\u2014" + endOutput;
    }
}
