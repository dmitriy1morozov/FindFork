package com.dmitriymorozov.findfork.model.workingHoursPOJO;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class FoursquareJSON{

	@SerializedName("meta")
	private Meta meta;

	@SerializedName("response")
	private Response response;

	public void setMeta(Meta meta){
		this.meta = meta;
	}

	public Meta getMeta(){
		return meta;
	}

	public void setResponse(Response response){
		this.response = response;
	}

	public Response getResponse(){
		return response;
	}

	@Override
 	public String toString(){
		return 
			"FoursquareJSON{" + 
			"meta = '" + meta + '\'' + 
			",response = '" + response + '\'' + 
			"}";
		}
}