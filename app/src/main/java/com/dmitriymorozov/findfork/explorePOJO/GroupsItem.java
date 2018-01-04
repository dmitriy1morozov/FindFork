package com.dmitriymorozov.findfork.explorePOJO;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class GroupsItem{

	@SerializedName("name")
	private String name;

	@SerializedName("type")
	private String type;

	@SerializedName("items")
	private List<ItemsItem> items;

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setItems(List<ItemsItem> items){
		this.items = items;
	}

	public List<ItemsItem> getItems(){
		return items;
	}

	@Override
 	public String toString(){
		return 
			"GroupsItem{" + 
			"name = '" + name + '\'' + 
			",type = '" + type + '\'' + 
			",items = '" + items + '\'' + 
			"}";
		}
}