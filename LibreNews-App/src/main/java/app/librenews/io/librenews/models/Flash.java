package app.librenews.io.librenews.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

public class Flash {
    String channel;
    String id;
    String link;
    String source;
    String text;
    Date date;

    public Flash(String channel, String id, String link, String source, String text, Date date) {
        this.channel = channel;
        this.id = id;
        this.link = link;
        this.source = source;
        this.text = text;
        this.date = date;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getIdAsInteger() {
        try{
            return Integer.parseInt(id);
        }catch(NumberFormatException e){
            return id.hashCode();
        }
    }

    public String getId(){
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getHumanReadableRelativeTime() {
        PrettyTime p = new PrettyTime();
        return p.format(getDate());
    }

    public JSONObject serialize() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("channel", channel);
        object.put("id", id);
        object.put("link", link);
        object.put("text", text);
        object.put("time", date);
        object.put("source", source);
        return object;
    }

    public static Date getTwitterDate(String date) throws ParseException {
        final String twitter_format = "EEE MMM dd HH:mm:ss Z yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitter_format, Locale.ENGLISH);
        sf.setLenient(true);
        return sf.parse(date);
    }

    public static Flash deserialize(JSONObject jsonFlash) throws JSONException, ParseException {
        Flash flash = new Flash(
                (String) jsonFlash.get("channel"),
                (String) jsonFlash.get("id"),
                (String) jsonFlash.get("link"),
                (String) jsonFlash.get("source"),
                (String) jsonFlash.get("text"),
                getTwitterDate((String) jsonFlash.get("time"))
        );
        return flash;
    }
}
