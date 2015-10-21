package de.serviceexperiencecamp.android.models.pojo;

import android.content.Context;
import android.os.Bundle;

import de.serviceexperiencecamp.android.utils.PreferenceUtils;

public class Event {
    public String _id;
    public String title;
    public String start_time;
    public String end_time;
    public String location;
    public String description;
    public String speaker_role;
    public String twitter_handle;
    public String linkedin_url;
    public String image_url;
    public String speaker_image_url;
    public String speaker_image_url_2;
    public Boolean bar_camp;
    public String day;
    public String artists;
    public String artists_2;
    public String speaker_role_2;
    public String twitter_handle_2;
    public String linkedin_url_2;
    public Integer starred_count;

    public Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("_id", _id);
        bundle.putString("title", title);
        bundle.putString("artists", artists);
        bundle.putString("artists_2", artists_2);
        bundle.putString("start_time", start_time);
        bundle.putString("end_time", end_time);
        bundle.putString("image_url", image_url);
        bundle.putString("day", day);
        bundle.putString("location", location);
        bundle.putString("speaker_role", speaker_role);
        bundle.putString("linkedin_url", linkedin_url);
        bundle.putString("twitter_handle", twitter_handle);
        bundle.putString("speaker_role_2", speaker_role_2);
        bundle.putString("linkedin_url_2", linkedin_url_2);
        bundle.putString("twitter_handle_2", twitter_handle_2);
        bundle.putString("description", description);
        return bundle;
    }

    public static Boolean getIsFavoriteFromPreferences(Context context, String _id) {
        if (isNullOrEmpty(_id)) {
            return false;
        }
        String value = PreferenceUtils.getValue(context, _id);
        if ("true".equals(value)) {
            return true;
        }
        else if ("false".equals(value)) {
            return false;
        }
        setIsFavoriteFromPreferences(context, _id, false);
        return false;
    }

    public static void setIsFavoriteFromPreferences(Context context, String _id, Boolean value) {
        if (isNullOrEmpty(_id)) { return; }
        PreferenceUtils.setValue(context, _id, value ? "true" : "false");
    }

    private static boolean isNullOrEmpty(final String input) {
        return (input == null || input.length() <= 0);
    }
}
