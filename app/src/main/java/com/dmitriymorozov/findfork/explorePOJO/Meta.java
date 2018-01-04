package com.dmitriymorozov.findfork.explorePOJO;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Meta{

	@SerializedName("code")
	private int code;

	@SerializedName("requestId")
	private String requestId;

	public void setCode(int code){
		this.code = code;
	}

	public int getCode(){
		return code;
	}

	public void setRequestId(String requestId){
		this.requestId = requestId;
	}

	public String getRequestId(){
		return requestId;
	}

	@Override
 	public String toString(){
		return 
			"Meta{" + 
			"code = '" + code + '\'' + 
			",requestId = '" + requestId + '\'' + 
			"}";
		}
}