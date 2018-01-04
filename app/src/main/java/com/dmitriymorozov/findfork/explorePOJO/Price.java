package com.dmitriymorozov.findfork.explorePOJO;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Price{

	@SerializedName("tier")
	private int tier;

	@SerializedName("currency")
	private String currency;

	@SerializedName("message")
	private String message;

	public void setTier(int tier){
		this.tier = tier;
	}

	public int getTier(){
		return tier;
	}

	public void setCurrency(String currency){
		this.currency = currency;
	}

	public String getCurrency(){
		return currency;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	@Override
 	public String toString(){
		return 
			"Price{" + 
			"tier = '" + tier + '\'' + 
			",currency = '" + currency + '\'' + 
			",message = '" + message + '\'' + 
			"}";
		}
}