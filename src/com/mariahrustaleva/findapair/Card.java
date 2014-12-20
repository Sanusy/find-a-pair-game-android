package com.mariahrustaleva.findapair;

import android.content.Context;
import android.widget.Button;


public class Card extends Button{

	public int id;
	public int group_id;
	public Boolean isUp = false;
	public Boolean isOnTable = true;

	public Card(Context context, int id, int group_id) {
		super(context);

		this.id = id;
		this.group_id = group_id;
	}

}
