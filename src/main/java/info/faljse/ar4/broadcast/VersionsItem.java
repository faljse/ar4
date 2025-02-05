package info.faljse.ar4.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VersionsItem{

	@JsonProperty("path")
	private String path;

	@JsonProperty("hashCode")
	private int hashCode;

	@JsonProperty("width")
	private int width;

	public String getPath(){
		return path;
	}

	public int getHashCode(){
		return hashCode;
	}

	public int getWidth(){
		return width;
	}
}