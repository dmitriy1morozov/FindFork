package com.dmitriymorozov.findfork.explorePOJO;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class RichStatus{

	@SerializedName("entities")
	private List<Object> entities;

	@SerializedName("text")
	private String text;

	public void setEntities(List<Object> entities){
		this.entities = entities;
	}

	public List<Object> getEntities(){
		return entities;
	}

	public void setText(String text){
		this.text = text;
	}

	public String getText(){
		return text;
	}

	@Override
 	public String toString(){
		return 
			"RichStatus{" + 
			"entities = '" + entities + '\'' + 
			",text = '" + text + '\'' + 
			"}";
		}
}