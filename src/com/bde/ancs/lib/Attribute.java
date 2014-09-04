package com.bde.ancs.lib;

public class Attribute {

	public String name;
	public int categoryID;
	public String appIdentifier;
	public String title;
	public String subtitle;
	public String message;
	public String messageSize;
	public String date;
	
	public Attribute() {
		
	}
	public Attribute(Attribute attribute) {
		name = attribute.getName();
		categoryID = attribute.getCategoryID();
		appIdentifier = attribute.getAppIdentifier();
		title = attribute.getTitle();
		subtitle = attribute.getSubtitle();
		message = attribute.getMessage();
		messageSize = attribute.getMessageSize();
		date = attribute.getDate();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}
	public String getAppIdentifier() {
		return appIdentifier;
	}
	public void setAppIdentifier(String appIdentifier) {
		this.appIdentifier = appIdentifier;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubtitle() {
		return subtitle;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessageSize() {
		return messageSize;
	}
	public void setMessageSize(String messageSize) {
		this.messageSize = messageSize;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	
}
