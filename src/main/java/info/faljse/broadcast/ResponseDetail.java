package info.faljse.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ResponseDetail {

	@JsonProperty("timezoneOffset")
	private int timezoneOffset;

	@JsonProperty("payload")
	private Broadcast broadcast;

	public int getTimezoneOffset(){
		return timezoneOffset;
	}

	public Broadcast getBroadcast(){
		return broadcast;
	}
}