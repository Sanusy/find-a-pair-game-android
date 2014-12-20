package com.mariahrustaleva.findapair;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;


public class Card extends Button{

	public int id;
	public int group_id;
	public Boolean isUp;
	public Boolean isOnTable;

	public Card(Context context) {
		super(context);
	}
	
	public void set(int id, int group_id, Drawable backImage, OnClickListener cardListener){
		isUp = false;
		isOnTable = true;
		this.id = id;
		this.group_id = group_id;
		setVisibility(View.VISIBLE);
		setBackgroundDrawable(backImage);
		setOnClickListener(cardListener);
	}

}
