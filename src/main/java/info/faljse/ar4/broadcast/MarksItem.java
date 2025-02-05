package info.faljse.ar4.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarksItem{

	@JsonProperty("timestampOffset")
	private int timestampOffset;

	@JsonProperty("timestampISO")
	private String timestampISO;

	@JsonProperty("type")
	private String type;

	@JsonProperty("timestamp")
	private long timestamp;

	public int getTimestampOffset(){
		return timestampOffset;
	}

	public String getTimestampISO(){
		return timestampISO;
	}

	public String getType(){
		return type;
	}

	public long getTimestamp(){
		return timestamp;
	}
}