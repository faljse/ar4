package info.faljse.ar4.broadcast;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ImagesItem{

	@JsonProperty("mode")
	private String mode;

	@JsonProperty("copyright")
	private String copyright;

	@JsonProperty("versions")
	private List<VersionsItem> versions;

	@JsonProperty("hashCode")
	private int hashCode;

	@JsonProperty("alt")
	private String alt;

	@JsonProperty("text")
	private String text;

	@JsonProperty("category")
	private String category;

	public String getMode(){
		return mode;
	}

	public String getCopyright(){
		return copyright;
	}

	public List<VersionsItem> getVersions(){
		return versions;
	}

	public int getHashCode(){
		return hashCode;
	}

	public String getAlt(){
		return alt;
	}

	public String getText(){
		return text;
	}

	public String getCategory(){
		return category;
	}
}