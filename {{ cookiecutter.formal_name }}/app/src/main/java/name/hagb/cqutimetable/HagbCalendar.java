package name.hagb.cqutimetable;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.beeware.android.MainActivity;

public class HagbCalendar {
    public static IPythonCal pythonCal;

    public HagbCalendar(IPythonCal pythonCal) {
        this.pythonCal = pythonCal;
        cr = MainActivity.singletonThis.getContentResolver();
    }

    ContentResolver cr;

    public boolean requirePermission() {
        if (ContextCompat.checkSelfPermission(
                MainActivity.singletonThis,
                Manifest.permission.READ_CALENDAR) ==
                PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                    MainActivity.singletonThis,
                    new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                    1);
            return false;
        }
        return true;
    }

    public boolean getCalendars() {
        Cursor cur = null;
        Uri uri = Calendars.CONTENT_URI;
        String[] EVENT_PROJECTION = new String[]{
                Calendars._ID,
                Calendars.CALENDAR_DISPLAY_NAME,
                Calendars.ACCOUNT_NAME,
                Calendars.OWNER_ACCOUNT,
                Calendars.CALENDAR_ACCESS_LEVEL
        };
        cur = cr.query(uri, EVENT_PROJECTION, "", null, null);
        while (cur.moveToNext()) {
            int level = cur.getInt(4);
            this.pythonCal.addCal(cur.getLong(0), cur.getString(1), cur.getString(2));
        }
        return true;
    }

    public long addEvent(long calId,
                         String title,
                         String location,
                         String description,
                         long dtstart,
                         long dtend,
                         boolean all_day,
                         String rrule) {
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, dtstart);
        values.put(Events.DTEND, dtend);
        values.put(Events.TITLE, title);
        if (!location.isEmpty()) values.put(Events.EVENT_LOCATION, location);
        if (!description.isEmpty()) values.put(Events.DESCRIPTION, description);
        values.put(Events.CALENDAR_ID, calId);
        values.put(Events.ALL_DAY, all_day ? 1 : 0);
        if (!rrule.isEmpty()) values.put(Events.RRULE, rrule);
        values.put(Events.EVENT_TIMEZONE, "Asia/Shanghai");
        return Long.parseLong(cr.insert(Events.CONTENT_URI, values).getLastPathSegment());
    }

    public void delEvent(long eventId) {
        cr.delete(ContentUris.withAppendedId(Events.CONTENT_URI, eventId), null, null);
    }
}
