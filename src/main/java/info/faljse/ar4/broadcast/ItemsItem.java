package info.faljse.ar4.broadcast;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemsItem{

	@JsonProperty("endOffset")
	private int endOffset;

	@JsonProperty("isOnDemand")
	private boolean isOnDemand;

	@JsonProperty("description")
	private String description;

	@JsonProperty("programKey")
	private String programKey;

	@JsonProperty("program")
	private String program;

	@JsonProperty("type")
	private String type;

	@JsonProperty("title")
	private String title;

	@JsonProperty("duration")
	private int duration;

	@JsonProperty("station")
	private String station;

	@JsonProperty("end")
	private String end;

	@JsonProperty("expiry")
	private String expiry;

	@JsonProperty("href")
	private String href;

	@JsonProperty("id")
	private int id;

	@JsonProperty("state")
	private String state;

	@JsonProperty("songId")
	private Object songId;

	@JsonProperty("isCompleted")
	private boolean isCompleted;

	@JsonProperty("oe1tags")
	private List<Object> oe1tags;

	@JsonProperty("images")
	private List<ImagesItem> images;

	@JsonProperty("start")
	private String start;

	@JsonProperty("stream")
	private Stream stream;

	@JsonProperty("interpreter")
	private String interpreter;

	@JsonProperty("isAdFree")
	private boolean isAdFree;

	@JsonProperty("tags")
	private List<Object> tags;

	@JsonProperty("orfcategories")
	private List<OrfcategoriesItem> orfcategories;


	@JsonProperty("isGeoProtected")
	private boolean isGeoProtected;

	@JsonProperty("startOffset")
	private int startOffset;

	@JsonProperty("broadcastDay")
	private int broadcastDay;

	@JsonProperty("endISO")
	private String endISO;

	@JsonProperty("entity")
	private String entity;

	@JsonProperty("startISO")
	private String startISO;

	@JsonProperty("moderator")
	private Object moderator;

	@JsonProperty("subtitle")
	private Object subtitle;

	public int getEndOffset(){
		return endOffset;
	}

	public boolean isIsOnDemand(){
		return isOnDemand;
	}

	public String getDescription(){
		return description;
	}

	public String getProgramKey(){
		return programKey;
	}

	public String getProgram(){
		return program;
	}

	public String getType(){
		return type;
	}

	public String getTitle(){
		return title==null?"":title;
	}

	public int getDuration(){
		return duration;
	}

	public String getStation(){
		return station;
	}

	public String getEnd(){
		return end;
	}

	public String getHref(){
		return href;
	}

	public int getId(){
		return id;
	}

	public String getState(){
		return state;
	}

	public Object getSongId(){
		return songId;
	}

	public boolean isIsCompleted(){
		return isCompleted;
	}

	public List<Object> getOe1tags(){
		return oe1tags;
	}

	public List<ImagesItem> getImages(){
		return images==null?List.of():images;
	}

	public String getStart(){
		return start;
	}

	public String getInterpreter(){
		return interpreter==null?"":interpreter;
	}

	public boolean isIsAdFree(){
		return isAdFree;
	}

	public List<Object> getTags(){
		return tags;
	}

	public boolean isIsGeoProtected(){
		return isGeoProtected;
	}

	public int getStartOffset(){
		return startOffset;
	}

	public int getBroadcastDay(){
		return broadcastDay;
	}

	public String getEndISO(){
		return endISO;
	}

	public String getEntity(){
		return entity;
	}

	public String getStartISO(){
		return startISO;
	}

	public Object getModerator(){
		return moderator;
	}

	public Object getSubtitle(){
		return subtitle;
	}

	public List<OrfcategoriesItem> getOrfcategories() {
		return orfcategories;
	}

	public String getExpiry() {
		return expiry;
	}

	public Stream getStream() {
		return stream;
	}
}