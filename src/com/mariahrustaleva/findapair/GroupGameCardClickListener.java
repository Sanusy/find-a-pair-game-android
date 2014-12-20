package com.mariahrustaleva.findapair;

import java.util.ArrayList;
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

class GroupGameCardClickListener implements OnClickListener {
	private ArrayList<Card> current_group;
	private ArrayList<Integer> matching_groups_ids = new ArrayList<Integer>();

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

	public GroupGameCardClickListener(Activity activity, Card[][] cards,
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

	private ArrayList<Card> getGroupById(int id) {
		Card c;
		ArrayList<Card> card_group = new ArrayList<Card>();

		for (int y = 0; y < MainActivity.ROW_COUNT; y++) {
			for (int x = 0; x < MainActivity.COL_COUNT; x++) {
				c = cards[y][x];
				if (c.group_id == id) {
					card_group.add(c);
				}
			}
		}
		return card_group;
	}

	private ArrayList<Integer> findMatchingGroupsIds(ArrayList<Card> group) {
		Card c;
		ArrayList<Integer> result = new ArrayList<Integer>();

		for (Card card : group) {
			for (int y = 0; y < MainActivity.ROW_COUNT; y++) {
				for (int x = 0; x < MainActivity.COL_COUNT; x++) {
					c = cards[y][x];

					if (c.isOnTable && c.isUp && c.id == card.id && c != card
							&& !result.contains(c.group_id)) {
						result.add(c.group_id);
					}
				}
			}
		}

		return result;
	}

	private void turnGroupOver(ArrayList<Card> group) {
		for (Card card : group) {
			card.setBackgroundDrawable(images.get(card.id));
			card.isUp = true;
		}
	}

	private void removeGroupFromTable(ArrayList<Card> group) {
		for (Card card : group) {
			card.setVisibility(View.INVISIBLE);
			card.isOnTable = false;
		}
	}

	@Override
	public void onClick(View v) {
		synchronized (MainActivity.lock) {

			Card card = (Card) v;

			if (matching_groups_ids.size() == 0) {

				ArrayList<Card> cg = getGroupById(card.group_id);
				matching_groups_ids = findMatchingGroupsIds(cg);

				turnGroupOver(cg);

				if (matching_groups_ids.size() != 0) {
					current_group = cg;

					time_count = MainActivity.MAX_TIME;
					future_task = countdown_timer.scheduleAtFixedRate(
							new Task(), 0, 1000, TimeUnit.MILLISECONDS);
				}

				else {
					checkEndGame(2000);
				}

			}

			// match is available
			else if (matching_groups_ids.contains(card.group_id)) {
				user_score++;

				if (matching_groups_ids.size() == 1) {
					endTurn();
				} else {
					matching_groups_ids.remove(matching_groups_ids
							.indexOf(card.group_id));
					removeGroupFromTable(getGroupById(card.group_id));
					updateScore();
				}
			}
		}
	}

	class Task implements Runnable {

		@Override
		public void run() {

			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					time_count--;
					countdown_view.setText("Time left to find a match: "
							+ time_count);

					if (time_count == 0) {
						computer_score += matching_groups_ids.size();
						endTurn();
					}

				}
			});
		}
	}

	private void updateScore() {
		score_view.setText(user_score + ":" + computer_score);
	}

	private void endTurn() {
		future_task.cancel(true);

		countdown_view.setText("");

		updateScore();

		for (int id : matching_groups_ids) {
			removeGroupFromTable(getGroupById(id));
		}
		removeGroupFromTable(current_group);
		matching_groups_ids.clear();
		current_group = null;

		checkEndGame(1000);
	}

	private void checkEndGame(int delay) {
		int count_open = 0;
		Card c;
		for (int y = 0; y < MainActivity.ROW_COUNT; y++) {
			for (int x = 0; x < MainActivity.COL_COUNT; x++) {
				c = cards[y][x];

				if (c.isUp) {
					count_open++;
				}
			}
		}
		if (count_open == MainActivity.ROW_COUNT * MainActivity.COL_COUNT) {
			final String msg;

			if (user_score < computer_score) {
				msg = "Defeat...";
			} else if (user_score > computer_score) {
				msg = "Victory!";
			} else {
				msg = "Draw.";
			}

			countdown_timer.schedule(new Runnable() {
				@Override
				public void run() {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {

							end_game_message.setText(msg);
							game_grid_container.setVisibility(View.GONE);
							end_game_message_container
									.setVisibility(View.VISIBLE);
							reset_btn_container.setVisibility(View.VISIBLE);

							reset();
						}
					});
				}
			}, delay, TimeUnit.MILLISECONDS);
		}
	}

	private void reset() {
		computer_score = 0;
		user_score = 0;

		if (future_task != null)
			future_task.cancel(false);
	}

}