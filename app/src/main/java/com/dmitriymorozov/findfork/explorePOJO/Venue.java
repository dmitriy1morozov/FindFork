package com.dmitriymorozov.findfork.explorePOJO;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Venue{

	@SerializedName("hours")
	private Hours hours;

	@SerializedName("verified")
	private boolean verified;

	@SerializedName("rating")
	private double rating;

	@SerializedName("photos")
	private Photos photos;

	@SerializedName("url")
	private String url;

	@SerializedName("beenHere")
	private BeenHere beenHere;

	@SerializedName("hereNow")
	private HereNow hereNow;

	@SerializedName("ratingSignals")
	private int ratingSignals;

	@SerializedName("stats")
	private Stats stats;

	@SerializedName("venuePage")
	private VenuePage venuePage;

	@SerializedName("price")
	private Price price;

	@SerializedName("contact")
	private Contact contact;

	@SerializedName("name")
	private String name;

	@SerializedName("ratingColor")
	private String ratingColor;

	@SerializedName("location")
	private Location location;

	@SerializedName("id")
	private String id;

	@SerializedName("categories")
	private List<CategoriesItem> categories;

	@SerializedName("allowMenuUrlEdit")
	private boolean allowMenuUrlEdit;

	public void setHours(Hours hours){
		this.hours = hours;
	}

	public Hours getHours(){
		return hours;
	}

	public void setVerified(boolean verified){
		this.verified = verified;
	}

	public boolean isVerified(){
		return verified;
	}

	public void setRating(double rating){
		this.rating = rating;
	}

	public double getRating(){
		return rating;
	}

	public void setPhotos(Photos photos){
		this.photos = photos;
	}

	public Photos getPhotos(){
		return photos;
	}

	public void setUrl(String url){
		this.url = url;
	}

	public String getUrl(){
		return url;
	}

	public void setBeenHere(BeenHere beenHere){
		this.beenHere = beenHere;
	}

	public BeenHere getBeenHere(){
		return beenHere;
	}

	public void setHereNow(HereNow hereNow){
		this.hereNow = hereNow;
	}

	public HereNow getHereNow(){
		return hereNow;
	}

	public void setRatingSignals(int ratingSignals){
		this.ratingSignals = ratingSignals;
	}

	public int getRatingSignals(){
		return ratingSignals;
	}

	public void setStats(Stats stats){
		this.stats = stats;
	}

	public Stats getStats(){
		return stats;
	}

	public void setVenuePage(VenuePage venuePage){
		this.venuePage = venuePage;
	}

	public VenuePage getVenuePage(){
		return venuePage;
	}

	public void setPrice(Price price){
		this.price = price;
	}

	public Price getPrice(){
		return price;
	}

	public void setContact(Contact contact){
		this.contact = contact;
	}

	public Contact getContact(){
		return contact;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setRatingColor(String ratingColor){
		this.ratingColor = ratingColor;
	}

	public String getRatingColor(){
		return ratingColor;
	}

	public void setLocation(Location location){
		this.location = location;
	}

	public Location getLocation(){
		return location;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setCategories(List<CategoriesItem> categories){
		this.categories = categories;
	}

	public List<CategoriesItem> getCategories(){
		return categories;
	}

	public void setAllowMenuUrlEdit(boolean allowMenuUrlEdit){
		this.allowMenuUrlEdit = allowMenuUrlEdit;
	}

	public boolean isAllowMenuUrlEdit(){
		return allowMenuUrlEdit;
	}

	@Override
 	public String toString(){
		return 
			"Venue{" + 
			"hours = '" + hours + '\'' + 
			",verified = '" + verified + '\'' + 
			",rating = '" + rating + '\'' + 
			",photos = '" + photos + '\'' + 
			",url = '" + url + '\'' + 
			",beenHere = '" + beenHere + '\'' + 
			",hereNow = '" + hereNow + '\'' + 
			",ratingSignals = '" + ratingSignals + '\'' + 
			",stats = '" + stats + '\'' + 
			",venuePage = '" + venuePage + '\'' + 
			",price = '" + price + '\'' + 
			",contact = '" + contact + '\'' + 
			",name = '" + name + '\'' + 
			",ratingColor = '" + ratingColor + '\'' + 
			",location = '" + location + '\'' + 
			",id = '" + id + '\'' + 
			",categories = '" + categories + '\'' + 
			",allowMenuUrlEdit = '" + allowMenuUrlEdit + '\'' + 
			"}";
		}
}