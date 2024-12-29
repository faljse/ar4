package info.faljse.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StreamsItem {

    @JsonProperty("duration")
    private int duration;

    @JsonProperty("loopStreamId")
    private String loopStreamId;

    @JsonProperty("urls")
    private Urls urls;

    @JsonProperty("uriTemplates")
    private UriTemplates uriTemplates;

    @JsonProperty("start")
    private String start;

    @JsonProperty("offsetStart")
    private int offsetStart;

    @JsonProperty("host")
    private String host;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("timestampISO")
    private String timestampISO;

    @JsonProperty("offsetEnd")
    private int offsetEnd;

    @JsonProperty("end")
    private String end;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("timestampOffset")
    private long timestampOffset;

    @JsonProperty("endOffset")
    private int endOffset;

    @JsonProperty("startOffset")
    private int startOffset;

    @JsonProperty("alias")
    private String alias;

    @JsonProperty("title")
    private Object title;

    @JsonProperty("endISO")
    private String endISO;

    @JsonProperty("startISO")
    private String startISO;

    @JsonProperty("type")
    private String type;

    public int getDuration() {
        return duration;
    }

    public String getLoopStreamId() {
        return loopStreamId;
    }

    public Urls getUrls() {
        return urls;
    }

    public UriTemplates getUriTemplates() {
        return uriTemplates;
    }

    public String getStart() {
        return start;
    }

    public int getOffsetStart() {
        return offsetStart;
    }

    public String getHost() {
        return host;
    }

    public String getChannel() {
        return channel;
    }

    public int getOffsetEnd() {
        return offsetEnd;
    }

    public String getEnd() {
        return end;
    }

    public String getStartISO() {
        return startISO;
    }

    public String getEndISO() {
        return endISO;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public Object getTitle() {
        return title;
    }

    public String getAlias() {
        return alias;
    }

    public String getType() {
        return type;
    }
}