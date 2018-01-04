package com.dmitriymorozov.findfork.explorePOJO;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Ne{

	@SerializedName("lng")
	private double lng;

	@SerializedName("lat")
	private double lat;

	public void setLng(double lng){
		this.lng = lng;
	}

	public double getLng(){
		return lng;
	}

	public void setLat(double lat){
		this.lat = lat;
	}

	public double getLat(){
		return lat;
	}

	@Override
 	public String toString(){
		return 
			"Ne{" + 
			"lng = '" + lng + '\'' + 
			",lat = '" + lat + '\'' + 
			"}";
		}
}