package com.mariahrustaleva.findapair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends Activity {
	private static int ROW_COUNT = 6;
	private static int COL_COUNT = 6;
	private static int MAX_TIME = 5;
	private static Object lock = new Object();

	// UI
	private Context context;
	private Drawable backImage;
	private Card[][] cards;
	private List<Drawable> images;
	
	private OnClickListener cardButtonListener;
	private OnClickListener groupButtonListener;
	private OnClickListener pvpButtonListener;
	private OnClickListener cardListener;
	
	private OnClickListener selectGameListener;
	
	private TableLayout mainTable;
	private TableRow game_grid_container;
	private TextView countdown_view;
	private TextView score_view;
	private TableRow end_game_message_container;
	private TextView end_game_message;
	private TableRow reset_btn_container;
	private Button reset_btn;
	
	private TableRow normal_game_btn_container;
	private TableRow group_game_btn_container;
	private TableRow pvp_game_btn_container;
	
	private Button normal_game_btn;
	private Button group_game_btn;
	private Button pvp_game_btn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// set view ref's
		mainTable = (TableLayout) findViewById(R.id.main_table);
		game_grid_container = (TableRow) findViewById(R.id.game_grid_container);
		end_game_message_container = (TableRow) findViewById(R.id.end_game_message_container);
		end_game_message = (TextView) findViewById(R.id.end_game_message);
		reset_btn_container = (TableRow) findViewById(R.id.reset_btn_container);
		reset_btn = (Button) findViewById(R.id.reset_btn);
		countdown_view = (TextView) findViewById(R.id.countdown);
		score_view = (TextView) findViewById(R.id.score);
		context = mainTable.getContext();
		
		normal_game_btn_container = (TableRow) findViewById(R.id.normal_game_btn_container);
		group_game_btn_container = (TableRow) findViewById(R.id.group_game_btn_container);
		pvp_game_btn_container = (TableRow) findViewById(R.id.pvp_game_btn_container);
		
		normal_game_btn = (Button) findViewById(R.id.normal_game_btn);
		group_game_btn = (Button) findViewById(R.id.group_game_btn);
		pvp_game_btn = (Button) findViewById(R.id.pvp_game_btn); 
		
		// add listeners
		cardButtonListener = new CardButtonListener();
		groupButtonListener = new GroupButtonListener();
		pvpButtonListener = new PVPListener();
		
		selectGameListener = new SelectGameListener();
		
		normal_game_btn.setOnClickListener(selectGameListener);
		group_game_btn.setOnClickListener(selectGameListener);
		pvp_game_btn.setOnClickListener(selectGameListener);
		
		reset_btn.setOnClickListener(new ResetButtonListener());

		loadImages();
		newGame();
	}

	private void loadImages() {
		backImage = getResources().getDrawable(R.drawable.icon);

		images = new ArrayList<Drawable>();
		Drawable drawable;
		for (int i = 1; i < 22; i++) {
			drawable = getResources().getDrawable(
					getResources().getIdentifier("card" + i, "drawable",
							getPackageName()));
			images.add(drawable);
		}
	}
	
	
	private void newGame() {
		score_view.setVisibility(View.INVISIBLE);
		reset_btn_container.setVisibility(View.GONE);
		game_grid_container.setVisibility(View.GONE);
		end_game_message_container.setVisibility(View.GONE);
		
		normal_game_btn_container.setVisibility(View.VISIBLE);
		group_game_btn_container.setVisibility(View.VISIBLE);
		pvp_game_btn_container.setVisibility(View.VISIBLE);
	}

	private void startGame(String gameType) {
		if(gameType == "normal") {
			cardListener = cardButtonListener;
		}
		else if(gameType == "group") {
			cardListener = groupButtonListener;
		}
		else {
			cardListener = pvpButtonListener;
		}

		score_view.setVisibility(View.VISIBLE);
		
		normal_game_btn_container.setVisibility(View.GONE);
		group_game_btn_container.setVisibility(View.GONE);
		pvp_game_btn_container.setVisibility(View.GONE);
		
		// reset UI
		score_view.setVisibility(View.VISIBLE);
		score_view.setText(0 + ":" + 0);
		game_grid_container.setVisibility(View.VISIBLE);
		end_game_message_container.setVisibility(View.GONE);


		// create deck
		createShuffledDeck();

		// add cards to container view
		placeCardsOnTheTable();
	}

	private void createShuffledDeck() {
		cards = new Card[COL_COUNT][ROW_COUNT];
		Random random = new Random();
		ArrayList<Integer> listA = new ArrayList<Integer>();
		ArrayList<Integer> listB = new ArrayList<Integer>();

		ArrayList<Integer> two_extra = new ArrayList<Integer>();

		int size = ROW_COUNT * COL_COUNT, rand, val, index, row, col;

		for (int i = 0; i < size / 2; i++) {
			listA.add(Integer.valueOf(i));
			listB.add(Integer.valueOf(i));
		}

		ArrayList<Integer> currentList = listA;
		ArrayList<Integer> pendingList = listB;

		for (int i = 0; i < size; i += 4) {

			// top left element of every group
			col = (i / 4) % (COL_COUNT / 2) * 2;
			row = (i / 4) / (COL_COUNT / 2) * 2;

			for (int j = 0; j < 4; j++) {
				rand = currentList.size() > 1 ? random.nextInt(currentList
						.size()) : 0;
				val = currentList.remove(rand);

				// store first 2 values
				if (i == 0 && j < 2) {
					two_extra.add(pendingList.remove(pendingList.indexOf(val)));
				}

				// current element of a group
				int col_i = col + j % 2;
				int row_i = row + j / 2;

				cards[col_i][row_i] = createCard(val, i / 4);
			}

			if (i == 0) {
				currentList.add(two_extra.get(0));
				currentList.add(two_extra.get(1));
			}

			if (currentList == listA) {
				currentList = listB;
			} else {
				currentList = listA;
			}

		}
	}

	private Card createCard(int id, int group_id) {
		Card card = new Card(context, id, group_id);
		card.setBackgroundDrawable(backImage);
		card.setOnClickListener(cardListener);
		return card;
	}

	private void placeCardsOnTheTable() {
		game_grid_container.removeAllViews();

		mainTable = new TableLayout(context);
		game_grid_container.addView(mainTable);

		for (int y = 0; y < ROW_COUNT; y++) {
			TableRow row = new TableRow(context);
			row.setHorizontalGravity(Gravity.CENTER);

			for (int x = 0; x < COL_COUNT; x++) {
				row.addView(cards[x][y]);
			}

			mainTable.addView(row);
		}
	}

	// listener for reset button
	class ResetButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			newGame();
		}
	}
	
	// listener for reset button
	class SelectGameListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if(v == normal_game_btn){
				startGame("normal");
			}
			
			else if(v == group_game_btn){
				startGame("group");
			}
			
			else {
				startGame("pvp");
			}
		}
	}

	//SINGLE PAIRS LOGIC START
	// listener for a card button
	class CardButtonListener implements OnClickListener {
		Card matching_card;
		Card current_card;
		
		private int computer_score;
		private int user_score;
		private int time_count;
		
		private ScheduledFuture future_task;
		private ScheduledExecutorService countdown_timer = Executors
				.newSingleThreadScheduledExecutor();

		@Override
		public void onClick(View v) {
			synchronized (lock) {
				
				Card card = (Card)v; 
				
				// turning a card (no match available)  
				if(matching_card == null){
					// Iterate through the cards array to find matching card
					Card c;
					for (int y = 0; y < ROW_COUNT; y++) {
						for (int x = 0; x < COL_COUNT; x++) {
							c = cards[y][x];
							
							// if there's a match available, store it and set the countdown timer
							if(c.isUp && c.id == card.id && c != card){
								
								//store the match
								matching_card = c;
								current_card = card; 
								
								// start timer
								time_count = MAX_TIME;
								future_task = countdown_timer.scheduleAtFixedRate(new Task(), 0, 1000, TimeUnit.MILLISECONDS);
							}
						}
					}
					
					// turn the card over
					card.setBackgroundDrawable(images.get(card.id));
					card.isUp = true;
				}
				
				// match is available, user clicks a matching card
				else if(card == matching_card){
					user_score ++;
					endTurn();
				}
			}
		}
		
		// implementation for countdown timer
		// tics every second
		// when time_count reaches 0 ( initial time_count value = MAX_TIME ) computer gets the score
		class Task implements Runnable {

			@Override
			public void run() {
				
				// all UI changes need to be run on UI thread, 
				// so using a wrapper
				runOnUiThread(new Runnable() {
				     @Override
				     public void run() {
				    	time_count --;
				    	countdown_view.setText("Time left to find a match: " + time_count);
						
						if(time_count == 0) {
							computer_score ++;
					    	endTurn();
						}
				    	 
				     }
				});
			}
		}
		
		private void endTurn(){
			future_task.cancel(true);
			
			//clear countdown
			countdown_view.setText("");
			
			//update score
			score_view.setText(user_score  + ":" + computer_score);
			
			//remove matching cards
			matching_card.setVisibility(View.INVISIBLE);
			current_card.setVisibility(View.INVISIBLE);
			matching_card = null;
			current_card = null;
			
			//check end game
			int turns = user_score + computer_score;
			if(turns == ROW_COUNT * COL_COUNT / 2){
				String msg;
				
				if(user_score < computer_score) {
					msg = "Defeat...";
				}
				else if(user_score > computer_score) {
					msg = "Victory!";
				}
				else {
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
		
		private void  reset(){
			computer_score = 0;
			user_score = 0;
			
			// cancel countdown timer task if it's scheduled
			if (future_task != null)
				future_task.cancel(false);
		}
	}
	//SINGLE PAIRS LOGIC END
	
	// GROUP PAIRS LOGIC START
	class GroupButtonListener implements OnClickListener {
		private int computer_score;
		private int user_score;
		private int time_count;
		
		private ScheduledFuture future_task;
		private ScheduledExecutorService countdown_timer = Executors
				.newSingleThreadScheduledExecutor();

		private ArrayList<Card> current_group;
		private ArrayList<Integer> matching_groups_ids = new ArrayList<Integer>();

		private ArrayList<Card> getGroupById(int id) {
			Card c;
			ArrayList<Card> card_group = new ArrayList<Card>();

			for (int y = 0; y < ROW_COUNT; y++) {
				for (int x = 0; x < COL_COUNT; x++) {
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
				for (int y = 0; y < ROW_COUNT; y++) {
					for (int x = 0; x < COL_COUNT; x++) {
						c = cards[y][x];

						if (c.isOnTable && c.isUp && c.id == card.id
								&& c != card && !result.contains(c.group_id)) {
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
			synchronized (lock) {

				Card card = (Card) v;

				if (matching_groups_ids.size() == 0) {

					ArrayList<Card> cg = getGroupById(card.group_id);
					matching_groups_ids = findMatchingGroupsIds(cg);

					// turn the group over
					turnGroupOver(cg);

					if (matching_groups_ids.size() != 0) {
						current_group = cg;

						// start timer
						time_count = MAX_TIME;
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

		// implementation for countdown timer
		// tics every second
		// when time_count reaches 0 ( initial time_count value = MAX_TIME )
		// computer gets the score
		class Task implements Runnable {

			@Override
			public void run() {

				// all UI changes need to be run on UI thread,
				// so using a wrapper
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						time_count--;
						countdown_view.setText("Time left to find a match: "
								+ time_count);

						if (time_count == 0) {
							computer_score += matching_groups_ids.size() - 1;
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

			// clear countdown
			countdown_view.setText("");

			updateScore();

			// remove matching cards
			for (int id : matching_groups_ids) {
				removeGroupFromTable(getGroupById(id));
			}
			removeGroupFromTable(current_group);
			matching_groups_ids.clear();
			current_group = null;

			// check end game
			checkEndGame(1000);
		}

		private void checkEndGame(int delay) {
			int count_open = 0;
			Card c;
			for (int y = 0; y < ROW_COUNT; y++) {
				for (int x = 0; x < COL_COUNT; x++) {
					c = cards[y][x];

					if (c.isUp) {
						count_open++;
					}
				}
			}
			if (count_open == ROW_COUNT * COL_COUNT) {
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
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// show end game messages
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
		
		private void  reset(){
			computer_score = 0;
			user_score = 0;
			
			// cancel countdown timer task if it's scheduled
			if (future_task != null)
				future_task.cancel(false);
		}
	}
	//GROUP PAIRS LOGIC END
	
	//PVP LOGIC START
	// listener for a card button
	class PVPListener implements OnClickListener {
		Card first_card;
		Card second_card;
		
		private int opponent_score = 0;
		private int user_score = 0;
		private Boolean current_player_is_user = true;
		
		private ScheduledExecutorService timer = Executors
				.newSingleThreadScheduledExecutor();

		@Override
		public void onClick(View v) {
			synchronized (lock) {
				
				Card card = (Card)v;
				
				if(first_card == null){
					first_card = card;
					first_card.setBackgroundDrawable(images.get(card.id));
				} 
				
				else if (first_card != null && second_card==null && card != first_card){
					second_card = card;
					second_card.setBackgroundDrawable(images.get(card.id));
					
					timer.schedule(new Runnable() {
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
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
		
		private void endTurn(){
			if(first_card.id == second_card.id){
				if(current_player_is_user) {
					user_score++;
				}
				else{
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
			first_card = null;
			second_card = null;
			
			//update score
			score_view.setText(user_score  + ":" + opponent_score);
			
			//check end game
			int turns = user_score + opponent_score;
			if(turns == ROW_COUNT * COL_COUNT / 2){
				String msg;
				
				if(user_score < opponent_score) {
					msg = "Opponent wins!";
				}
				else if(user_score > opponent_score) {
					msg = "You won!";
				}
				else {
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
		
		private void  reset(){
			opponent_score = 0;
			user_score = 0;
		}
	}
	//PVP LOGIC END


}