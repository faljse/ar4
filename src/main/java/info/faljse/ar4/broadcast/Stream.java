package info.faljse.ar4.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Stream{

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

	@JsonProperty("offsetEnd")
	private int offsetEnd;

	@JsonProperty("end")
	private String end;

	public int getDuration(){
		return duration;
	}

	public String getLoopStreamId(){
		return loopStreamId;
	}

	public Urls getUrls(){
		return urls;
	}

	public UriTemplates getUriTemplates(){
		return uriTemplates;
	}

	public String getStart(){
		return start;
	}

	public int getOffsetStart(){
		return offsetStart;
	}

	public String getHost(){
		return host;
	}

	public String getChannel(){
		return channel;
	}

	public int getOffsetEnd(){
		return offsetEnd;
	}

	public String getEnd(){
		return end;
	}
}