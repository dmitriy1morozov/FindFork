package com.dmitriymorozov.findfork.model.explorePOJO;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class SuggestedBounds{

	@SerializedName("sw")
	private Sw sw;

	@SerializedName("ne")
	private Ne ne;

	public void setSw(Sw sw){
		this.sw = sw;
	}

	public Sw getSw(){
		return sw;
	}

	public void setNe(Ne ne){
		this.ne = ne;
	}

	public Ne getNe(){
		return ne;
	}

	@Override
 	public String toString(){
		return 
			"SuggestedBounds{" + 
			"sw = '" + sw + '\'' + 
			",ne = '" + ne + '\'' + 
			"}";
		}
}