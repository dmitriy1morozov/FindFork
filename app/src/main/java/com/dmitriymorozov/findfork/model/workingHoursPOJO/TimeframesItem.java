package com.dmitriymorozov.findfork.model.workingHoursPOJO;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class TimeframesItem{

	@SerializedName("includesToday")
	private boolean includesToday;

	@SerializedName("days")
	private List<Integer> days;

	@SerializedName("open")
	private List<OpenItem> open;

	@SerializedName("segments")
	private List<Object> segments;

	public void setIncludesToday(boolean includesToday){
		this.includesToday = includesToday;
	}

	public boolean isIncludesToday(){
		return includesToday;
	}

	public void setDays(List<Integer> days){
		this.days = days;
	}

	public List<Integer> getDays(){
		return days;
	}

	public void setOpen(List<OpenItem> open){
		this.open = open;
	}

	public List<OpenItem> getOpen(){
		return open;
	}

	public void setSegments(List<Object> segments){
		this.segments = segments;
	}

	public List<Object> getSegments(){
		return segments;
	}

	@Override
 	public String toString(){
		return 
			"TimeframesItem{" + 
			"includesToday = '" + includesToday + '\'' + 
			",days = '" + days + '\'' + 
			",open = '" + open + '\'' + 
			",segments = '" + segments + '\'' + 
			"}";
		}
}