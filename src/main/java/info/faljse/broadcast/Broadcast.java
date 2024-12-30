package info.faljse.broadcast;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Broadcast {

    @JsonProperty("moderator")
    private Object moderator;

    @JsonProperty("isOnDemand")
    private boolean isOnDemand;

    @JsonProperty("link")
    private Link link;

    @JsonProperty("description")
    private String description;

    @JsonProperty("programKey")
    private String programKey;

    @JsonProperty("program")
    private String program;

    @JsonProperty("title")
    private String title;

    @JsonProperty("duration")
    private int duration;

    @JsonProperty("ressort")
    private Object ressort;

    @JsonProperty("station")
    private String station;

    @JsonProperty("startISO")
    private String startISO;

    @JsonProperty("endISO")
    private String endISO;

    @JsonProperty("startOffset")
    private String startOffset;

    @JsonProperty("endOffset")
    private String endOffset;

    @JsonProperty("programTitle")
    private String programTitle;

    @JsonProperty("end")
    private String end;

    @JsonProperty("href")
    private String href;

    @JsonProperty("id")
    private int id;

    @JsonProperty("state")
    private String state;

    @JsonProperty("url")
    private String url;

    @JsonProperty("urlText")
    private String urlText;


    @JsonProperty("expiry")
    private String expiry;

    @JsonProperty("niceTime")
    private String niceTime;

    @JsonProperty("niceTimeOffset")
    private String niceTimeOffset;

    @JsonProperty("scheduledStart")
    private String scheduledStart;

    @JsonProperty("scheduledStartISO")
    private String scheduledStartISO;

    @JsonProperty("scheduledEndISO")
    private String scheduledEndISO;

    @JsonProperty("scheduledStartOffset")
    private String scheduledStartOffset;

    @JsonProperty("scheduledEndOffset")
    private String scheduledEndOffset;

    @JsonProperty("niceTimeISO")
    private String niceTimeISO;

    @JsonProperty("oe1tags")
    private Object oe1tags;

    @JsonProperty("images")
    private List<ImagesItem> images;

    @JsonProperty("scheduledEnd")
    private String scheduledEnd;

    @JsonProperty("start")
    private String start;

    @JsonProperty("pressRelease")
    private Object pressRelease;

    @JsonProperty("isAdFree")
    private boolean isAdFree;

    @JsonProperty("orfcategories")
    private Object orfcategories;

    @JsonProperty("tags")
    private Object tags;

    @JsonProperty("isGeoProtected")
    private boolean isGeoProtected;

    @JsonProperty("broadcastDay")
    private int broadcastDay;

    @JsonProperty("subtitle")
    private String subtitle;

    @JsonProperty("entity")
    private String entity;

    @JsonProperty("akm")
    private String akm;

    @JsonProperty("items")
    private List<ItemsItem> items;

    @JsonProperty("streams")
    private List<StreamsItem> streams;

    @JsonProperty("marks")
    private List<StreamsItem> marks;

    public Object getModerator() {
        return moderator;
    }

    public boolean isIsOnDemand() {
        return isOnDemand;
    }

    public Link getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public String getProgramKey() {
        return programKey;
    }

    public String getProgram() {
        return program;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public Object getRessort() {
        return ressort;
    }

    public String getStation() {
        return station;
    }

    public String getEnd() {
        return end;
    }

    public String getHref() {
        return href;
    }

    public int getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public String getExpiry() {
        return expiry;
    }

    public String getNiceTime() {
        return niceTime;
    }

    public String getScheduledStart() {
        return scheduledStart;
    }

    public Object getOe1tags() {
        return oe1tags;
    }

    public List<ImagesItem> getImages() {
        return images==null?List.of():images;
    }

    public String getScheduledEnd() {
        return scheduledEnd;
    }

    public String getStart() {
        return start;
    }

    public Object getPressRelease() {
        return pressRelease;
    }

    public boolean isIsAdFree() {
        return isAdFree;
    }

    public Object getOrfcategories() {
        return orfcategories;
    }

    public Object getTags() {
        return tags;
    }

    public boolean isIsGeoProtected() {
        return isGeoProtected;
    }

    public int getBroadcastDay() {
        return broadcastDay;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getEntity() {
        return entity;
    }

    public String getStartISO() {
        return startISO;
    }

    public String getProgramTitle() {
        return programTitle;
    }

    public String getStartOffset() {
        return startOffset;
    }

    public String getScheduledStartISO() {
        return scheduledStartISO;
    }

    public String getScheduledStartOffset() {
        return scheduledStartOffset;
    }

    public String getEndISO() {
        return endISO;
    }

    public List<StreamsItem> getStreams() {
        return streams;
    }

    public String getEndOffset() {
        return endOffset;
    }

    public String getScheduledEndISO() {
        return scheduledEndISO;
    }

    public String getScheduledEndOffset() {
        return scheduledEndOffset;
    }

    public String getNiceTimeISO() {
        return niceTimeISO;
    }

    public String getNiceTimeOffset() {
        return niceTimeOffset;
    }

    public String getAkm() {
        return akm;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlText() {
        return urlText;
    }

    public List<ItemsItem> getItems() {
        return items;
    }
}