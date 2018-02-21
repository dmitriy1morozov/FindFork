package com.dmitriymorozov.findfork.model.explorePOJO;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class ErrorResponse {

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
			"ErrorResponse{" +
			"meta = '" + meta + '\'' + 
			",response = '" + response + '\'' + 
			"}";
		}
}