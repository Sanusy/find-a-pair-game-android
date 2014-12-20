package com.mariahrustaleva.findapair;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableRow;
import android.widget.TextView;

class PVPGameCardClickListener implements OnClickListener {
	private Card first_card;
	private Card second_card;

	private int opponent_score = 0;
	private int user_score = 0;

	private Boolean current_player_is_user = true;

	private Activity activity;

	private List<Drawable> images;
	private Drawable backImage;

	private ScheduledExecutorService timer = Executors
			.newSingleThreadScheduledExecutor();

	private TextView score_view;
	private TableRow end_game_message_container;
	private TextView end_game_message;
	private TableRow reset_btn_container;
	private TableRow game_grid_container;
	private TextView opponent_label;
	private TextView user_label;
	private int textColor;

	public PVPGameCardClickListener(Activity activity, List<Drawable> images,
			Drawable backImage) {

		this.activity = activity;
		this.images = images;
		this.backImage = backImage;

		score_view = (TextView) this.activity.findViewById(R.id.score);

		end_game_message_container = (TableRow) this.activity
				.findViewById(R.id.end_game_message_container);

		end_game_message = (TextView) this.activity
				.findViewById(R.id.end_game_message);

		reset_btn_container = (TableRow) this.activity
				.findViewById(R.id.reset_btn_container);

		game_grid_container = (TableRow) this.activity
				.findViewById(R.id.game_grid_container);

		opponent_label = (TextView) this.activity
				.findViewById(R.id.opponent_label);
		user_label = (TextView) this.activity.findViewById(R.id.user_label);

		this.textColor = user_label.getCurrentTextColor();
	}

	public void getReady() {
		opponent_label.setText("Opponent");
		user_label.setTextColor(Color.parseColor("#FFFF00"));
	}

	@Override
	public void onClick(View v) {
		synchronized (MainActivity.lock) {

			Card card = (Card) v;

			if (first_card == null) {
				first_card = card;
				first_card.setBackgroundDrawable(images.get(card.id));
			}

			else if (first_card != null && second_card == null
					&& card != first_card) {
				second_card = card;
				second_card.setBackgroundDrawable(images.get(card.id));

				timer.schedule(new Runnable() {
					@Override
					public void run() {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								endTurn();
							}
						});
					}
				}, 1000, TimeUnit.MILLISECONDS);

			}
		}
	}

	private void endTurn() {
		if (first_card.id == second_card.id) {
			if (current_player_is_user) {
				user_score++;
			} else {
				opponent_score++;
			}

			first_card.setVisibility(View.INVISIBLE);
			second_card.setVisibility(View.INVISIBLE);
		}

		else {
			first_card.setBackgroundDrawable(backImage);
			second_card.setBackgroundDrawable(backImage);
		}

		current_player_is_user = !current_player_is_user;

		if (current_player_is_user) {
			user_label.setTextColor(Color.parseColor("#FFFF00"));
			opponent_label.setTextColor(this.textColor);
		} else {
			opponent_label.setTextColor(Color.parseColor("#FFFF00"));
			user_label.setTextColor(this.textColor);
		}

		first_card = null;
		second_card = null;

		score_view.setText(user_score + ":" + opponent_score);

		int turns = user_score + opponent_score;
		if (turns == MainActivity.ROW_COUNT * MainActivity.COL_COUNT / 2) {
			String msg;

			if (user_score < opponent_score) {
				msg = "Opponent wins!";
			} else if (user_score > opponent_score) {
				msg = "You won!";
			} else {
				msg = "Draw.";
			}

			end_game_message.setText(msg);
			game_grid_container.setVisibility(View.GONE);
			end_game_message_container.setVisibility(View.VISIBLE);
			reset_btn_container.setVisibility(View.VISIBLE);

			reset();
		}
	}

	private void reset() {
		opponent_score = 0;
		user_score = 0;

		opponent_label.setTextColor(this.textColor);
		user_label.setTextColor(this.textColor);

		opponent_label.setText("Computer");
	}
}
