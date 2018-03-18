package com.dmitriymorozov.findfork.model.explorePOJO;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Menu{

	@SerializedName("externalUrl")
	private String externalUrl;

	@SerializedName("anchor")
	private String anchor;

	@SerializedName("label")
	private String label;

	@SerializedName("mobileUrl")
	private String mobileUrl;

	@SerializedName("type")
	private String type;

	@SerializedName("url")
	private String url;

	public void setExternalUrl(String externalUrl){
		this.externalUrl = externalUrl;
	}

	public String getExternalUrl(){
		return externalUrl;
	}

	public void setAnchor(String anchor){
		this.anchor = anchor;
	}

	public String getAnchor(){
		return anchor;
	}

	public void setLabel(String label){
		this.label = label;
	}

	public String getLabel(){
		return label;
	}

	public void setMobileUrl(String mobileUrl){
		this.mobileUrl = mobileUrl;
	}

	public String getMobileUrl(){
		return mobileUrl;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setUrl(String url){
		this.url = url;
	}

	public String getUrl(){
		return url;
	}

	@Override
 	public String toString(){
		return 
			"Menu{" + 
			"externalUrl = '" + externalUrl + '\'' + 
			",anchor = '" + anchor + '\'' + 
			",label = '" + label + '\'' + 
			",mobileUrl = '" + mobileUrl + '\'' + 
			",type = '" + type + '\'' + 
			",url = '" + url + '\'' + 
			"}";
		}
}