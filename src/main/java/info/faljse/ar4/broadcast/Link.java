package info.faljse.ar4.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Link{

	@JsonProperty("url")
	private String url;

	@JsonProperty("text")
	private String text;


	public String getUrl(){
		return url;
	}

	public String getText() {
		return text;
	}
}