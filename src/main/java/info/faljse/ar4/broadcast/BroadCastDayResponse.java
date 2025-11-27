package info.faljse.ar4.broadcast;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class BroadCastDayResponse{

	@JsonProperty("timezoneOffset")
	private int timezoneOffset;
    @Getter
	@JsonProperty("payload")
    private List<Broadcast> broadcasts;
}