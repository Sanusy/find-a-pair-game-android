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
	
	//UI
	private Context context;
	private Drawable backImage;
	private Card[][] cards;
	private List<Drawable> images;
	private CardButtonListener cardCardButtonListener;
	private TableLayout mainTable;
	private TableRow game_grid_container;
	private TextView countdown_view;
	private TextView score_view;
	private TableRow end_game_message_container;
	private TextView end_game_message;
	private TableRow reset_btn_container;
	private Button reset_btn;
	
	private int computer_score;
	private int user_score;
	private int time_count;
	private Card current_card;
	private Card matching_card;
	private ScheduledFuture future_task;
	private ScheduledExecutorService countdown_timer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// set view ref's
		mainTable = (TableLayout) findViewById(R.id.main_table);
		game_grid_container = (TableRow) findViewById(R.id.game_grid_container);
		end_game_message_container = (TableRow) findViewById(R.id.end_game_message_container);
		end_game_message = (TextView)findViewById(R.id.end_game_message);
		reset_btn_container = (TableRow) findViewById(R.id.reset_btn_container);
		reset_btn =  (Button) findViewById(R.id.reset_btn);
		countdown_view = (TextView)findViewById(R.id.countdown);
		score_view = (TextView)findViewById(R.id.score);
		context = mainTable.getContext();

		// add listeners
		cardCardButtonListener = new CardButtonListener();
		reset_btn.setOnClickListener(new ResetButtonListener());
		
		// create countdaown timer
		countdown_timer = Executors.newSingleThreadScheduledExecutor();
		
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
		// reset vars
		current_card = null;
		matching_card = null;
		computer_score = 0;
		user_score = 0;

		// reset UI
		score_view.setText(user_score  + ":" + computer_score);
		game_grid_container.setVisibility(View.VISIBLE);
		end_game_message_container.setVisibility(View.GONE);
		reset_btn_container.setVisibility(View.GONE);
		
		// cancel countdown timer task if it's scheduled
		if(future_task != null) future_task.cancel(false);

		// create deck
		createShuffledDeck();
		
		// add cards to container view
		placeCardsOnTheTable();
	}

	private void createShuffledDeck() {
		cards = new Card[COL_COUNT][ROW_COUNT];
		
		ArrayList<Integer> list = new ArrayList<Integer>();

		int size = ROW_COUNT * COL_COUNT;

		for (int i = 0; i < size; i++) {
			list.add(Integer.valueOf(i));
		}

		Random r = new Random();

		// make a random deck of paired cards
		for (int i = size - 1; i >= 0; i--) {
			int t = 0;
			if (i > 0) {
				t = r.nextInt(i);
			}
			t = list.remove(t).intValue();
			
			cards[i % COL_COUNT][i / COL_COUNT] = createCard(t % (size / 2));
		}
	}
	
	private Card createCard(int id){
		Card card = new Card(context, id);
		card.setBackgroundDrawable(backImage);
		card.setOnClickListener(cardCardButtonListener);
		return card;
	}
	
	private void placeCardsOnTheTable(){
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
	
	// listener for a card button
	class CardButtonListener implements OnClickListener {

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
			}
			
			
		}
	}
}