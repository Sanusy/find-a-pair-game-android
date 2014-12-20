package com.mariahrustaleva.findapair;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableRow;
import android.widget.TextView;

class NormalGameCardClickListener implements OnClickListener {
	Card matching_card;
	Card current_card;

	private int computer_score;
	private int user_score;
	private int time_count;

	private Activity activity;

	private Card[][] cards;
	private List<Drawable> images;

	private ScheduledFuture future_task;
	private ScheduledExecutorService countdown_timer = Executors
			.newSingleThreadScheduledExecutor();
	private TextView countdown_view;
	private TextView score_view;
	private TableRow end_game_message_container;
	private TextView end_game_message;
	private TableRow reset_btn_container;
	private TableRow game_grid_container;

	public NormalGameCardClickListener(Activity activity, Card[][] cards,
			List<Drawable> images) {

		this.activity = activity;
		this.cards = cards;
		this.images = images;

		countdown_view = (TextView) this.activity.findViewById(R.id.countdown);
		score_view = (TextView) this.activity.findViewById(R.id.score);

		end_game_message_container = (TableRow) this.activity
				.findViewById(R.id.end_game_message_container);

		end_game_message = (TextView) this.activity
				.findViewById(R.id.end_game_message);

		reset_btn_container = (TableRow) this.activity
				.findViewById(R.id.reset_btn_container);

		game_grid_container = (TableRow) this.activity
				.findViewById(R.id.game_grid_container);
	}

	@Override
	public void onClick(View v) {
		synchronized (MainActivity.lock) {

			Card card = (Card) v;

			// turning a card (no match available)
			if (matching_card == null) {
				// Iterate through the cards array to find matching card
				Card c;
				for (int y = 0; y < MainActivity.ROW_COUNT; y++) {
					for (int x = 0; x < MainActivity.COL_COUNT; x++) {
						c = cards[y][x];

						// if there's a match available, store it and set the
						// countdown timer
						if (c.isUp && c.id == card.id && c != card) {

							// store the match
							matching_card = c;
							current_card = card;

							// start timer
							time_count = MainActivity.MAX_TIME;
							future_task = countdown_timer.scheduleAtFixedRate(
									new Task(), 0, 1000, TimeUnit.MILLISECONDS);
						}
					}
				}

				// turn the card over
				card.setBackgroundDrawable(images.get(card.id));
				card.isUp = true;
			}

			// match is available, user clicks a matching card
			else if (card == matching_card) {
				user_score++;
				endTurn();
			}
		}
	}

	// implementation for countdown timer
	// tics every second
	// when time_count reaches 0 ( initial time_count value = MAX_TIME )
	// computer gets the score
	class Task implements Runnable {

		@Override
		public void run() {

			// all UI changes need to be run on UI thread,
			// so using a wrapper
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					time_count--;
					countdown_view.setText("Time left to find a match: "
							+ time_count);

					if (time_count == 0) {
						computer_score++;
						endTurn();
					}

				}
			});
		}
	}

	private void endTurn() {
		future_task.cancel(true);

		// clear countdown
		countdown_view.setText("");

		// update score
		score_view.setText(user_score + ":" + computer_score);

		// remove matching cards
		matching_card.setVisibility(View.INVISIBLE);
		current_card.setVisibility(View.INVISIBLE);
		matching_card = null;
		current_card = null;

		// check end game
		int turns = user_score + computer_score;
		if (turns == MainActivity.ROW_COUNT * MainActivity.COL_COUNT / 2) {
			String msg;

			if (user_score < computer_score) {
				msg = "Defeat...";
			} else if (user_score > computer_score) {
				msg = "Victory!";
			} else {
				msg = "Draw.";
			}

			// show end game messages
			end_game_message.setText(msg);
			game_grid_container.setVisibility(View.GONE);
			end_game_message_container.setVisibility(View.VISIBLE);
			reset_btn_container.setVisibility(View.VISIBLE);

			reset();
		}
	}

	private void reset() {
		computer_score = 0;
		user_score = 0;

		// cancel countdown timer task if it's scheduled
		if (future_task != null)
			future_task.cancel(false);
	}
}