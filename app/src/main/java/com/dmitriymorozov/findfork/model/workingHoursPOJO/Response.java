package com.dmitriymorozov.findfork.model.workingHoursPOJO;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Response{

	@SerializedName("hours")
	private Hours hours;

	@SerializedName("popular")
	private Popular popular;

	public void setHours(Hours hours){
		this.hours = hours;
	}

	public Hours getHours(){
		return hours;
	}

	public void setPopular(Popular popular){
		this.popular = popular;
	}

	public Popular getPopular(){
		return popular;
	}

	@Override
 	public String toString(){
		return 
			"Response{" + 
			"hours = '" + hours + '\'' + 
			",popular = '" + popular + '\'' + 
			"}";
		}
}