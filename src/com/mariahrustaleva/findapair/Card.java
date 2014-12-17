package com.mariahrustaleva.findapair;

import android.content.Context;
import android.widget.Button;


public class Card extends Button{

	public int id;
	public Boolean isUp = false;

	public Card(Context context, int id) {
		super(context);

		this.id = id;
	}

}
