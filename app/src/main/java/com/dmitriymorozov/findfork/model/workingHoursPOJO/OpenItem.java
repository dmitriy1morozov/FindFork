package com.dmitriymorozov.findfork.model.workingHoursPOJO;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class OpenItem{

	@SerializedName("start")
	private String start;

	@SerializedName("end")
	private String end;

	public void setStart(String start){
		this.start = start;
	}

	public String getStart(){
		return start;
	}

	public void setEnd(String end){
		this.end = end;
	}

	public String getEnd(){
		return end;
	}

	@Override
 	public String toString(){
		return 
			"OpenItem{" + 
			"start = '" + start + '\'' + 
			",end = '" + end + '\'' + 
			"}";
		}
}