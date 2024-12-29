package info.faljse.broadcast;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BroadcastDay {

	@JsonProperty("start")
	private String start;

	@JsonProperty("date")
	private String date;

	@JsonProperty("dateISO")
	private String dateISO;

	@JsonProperty("dateOffset")
	private String dateOffset;

	@JsonProperty("broadcasts")
	private List<Broadcast> broadcasts;

	@JsonProperty("day")
	private int day;

	public String getStart(){
		return start;
	}

	public List<Broadcast> getBroadcasts(){
		return broadcasts;
	}

	public int getDay(){
		return day;
	}

	public String getDate() {
		return date;
	}

	public String getDateISO() {
		return dateISO;
	}
}