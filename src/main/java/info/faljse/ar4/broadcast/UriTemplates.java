package info.faljse.ar4.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UriTemplates{

	@JsonProperty("progressive")
	private String progressive;

	@JsonProperty("hls")
	private String hls;

	public String getProgressive(){
		return progressive;
	}

	public String getHls(){
		return hls;
	}
}