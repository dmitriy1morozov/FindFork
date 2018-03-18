package com.dmitriymorozov.findfork.model.workingHoursPOJO;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Hours{

	@SerializedName("timeframes")
	private List<TimeframesItem> timeframes;

	public void setTimeframes(List<TimeframesItem> timeframes){
		this.timeframes = timeframes;
	}

	public List<TimeframesItem> getTimeframes(){
		return timeframes;
	}

	@Override
 	public String toString(){
		return 
			"Hours{" + 
			"timeframes = '" + timeframes + '\'' + 
			"}";
		}
}