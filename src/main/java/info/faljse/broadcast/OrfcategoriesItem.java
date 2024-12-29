package info.faljse.broadcast;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrfcategoriesItem{

	@JsonProperty("hashCode")
	private int hashCode;

	@JsonProperty("id")
	private String id;

	@JsonProperty("categories")
	private List<String> categories;

	public int getHashCode(){
		return hashCode;
	}

	public String getId(){
		return id;
	}

	public List<String> getCategories(){
		return categories;
	}
}