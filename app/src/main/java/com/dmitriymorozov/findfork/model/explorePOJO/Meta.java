package com.dmitriymorozov.findfork.model.explorePOJO;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Meta{

	@SerializedName("code")
	private int code;

	@SerializedName("errorType")
	private String errorType;

	@SerializedName("requestId")
	private String requestId;

	@SerializedName("errorDetail")
	private String errorDetail;

	public void setCode(int code){
		this.code = code;
	}

	public int getCode(){
		return code;
	}

	public void setErrorType(String errorType){
		this.errorType = errorType;
	}

	public String getErrorType(){
		return errorType;
	}

	public void setRequestId(String requestId){
		this.requestId = requestId;
	}

	public String getRequestId(){
		return requestId;
	}

	public void setErrorDetail(String errorDetail){
		this.errorDetail = errorDetail;
	}

	public String getErrorDetail(){
		return errorDetail;
	}

	@Override
 	public String toString(){
		return 
			"Meta{" + 
			"code = '" + code + '\'' + 
			",errorType = '" + errorType + '\'' + 
			",requestId = '" + requestId + '\'' + 
			",errorDetail = '" + errorDetail + '\'' + 
			"}";
		}
}