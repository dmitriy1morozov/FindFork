package com.dmitriymorozov.findfork.explorePOJO;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class FiltersItem{

	@SerializedName("name")
	private String name;

	@SerializedName("key")
	private String key;

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setKey(String key){
		this.key = key;
	}

	public String getKey(){
		return key;
	}

	@Override
 	public String toString(){
		return 
			"FiltersItem{" + 
			"name = '" + name + '\'' + 
			",key = '" + key + '\'' + 
			"}";
		}
}