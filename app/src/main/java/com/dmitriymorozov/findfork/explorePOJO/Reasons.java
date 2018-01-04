package com.dmitriymorozov.findfork.explorePOJO;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Reasons{

	@SerializedName("count")
	private int count;

	@SerializedName("items")
	private List<ItemsItem> items;

	public void setCount(int count){
		this.count = count;
	}

	public int getCount(){
		return count;
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
			"Reasons{" + 
			"count = '" + count + '\'' + 
			",items = '" + items + '\'' + 
			"}";
		}
}