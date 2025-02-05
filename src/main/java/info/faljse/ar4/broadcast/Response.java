package info.faljse.ar4.broadcast;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Response{

	@JsonProperty("timezoneOffset")
	private int timezoneOffset;

	@JsonProperty("payload")
	private List<BroadcastDay> payload;

	public int getTimezoneOffset(){
		return timezoneOffset;
	}

	public List<BroadcastDay> getPayload(){
		return payload;
	}
}