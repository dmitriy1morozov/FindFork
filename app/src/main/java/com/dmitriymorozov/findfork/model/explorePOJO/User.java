package com.dmitriymorozov.findfork.model.explorePOJO;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class User{

	@SerializedName("firstName")
	private String firstName;

	@SerializedName("gender")
	private String gender;

	@SerializedName("photo")
	private Photo photo;

	@SerializedName("id")
	private String id;

	public void setFirstName(String firstName){
		this.firstName = firstName;
	}

	public String getFirstName(){
		return firstName;
	}

	public void setGender(String gender){
		this.gender = gender;
	}

	public String getGender(){
		return gender;
	}

	public void setPhoto(Photo photo){
		this.photo = photo;
	}

	public Photo getPhoto(){
		return photo;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	@Override
 	public String toString(){
		return 
			"User{" + 
			"firstName = '" + firstName + '\'' + 
			",gender = '" + gender + '\'' + 
			",photo = '" + photo + '\'' + 
			",id = '" + id + '\'' + 
			"}";
		}
}