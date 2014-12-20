package com.mariahrustaleva.findapair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends Activity {
	public static int ROW_COUNT = 6;
	public static int COL_COUNT = 6;
	public static int MAX_TIME = 15;
	public static Object lock = new Object();

	private Card[][] cards = new Card[COL_COUNT][ROW_COUNT];

	// UI
	private Drawable backImage;
	private Drawable backImageInverted;
	private List<Drawable> images;

	private Context context;
	private OnClickListener normalGameCardClickListener;
	private OnClickListener groupGameCardClickListener;
	private OnClickListener pvpGameCardClickListener;
	private OnClickListener cardListener;

	private OnClickListener selectGameListener;

	private TableLayout mainTable;
	private TableRow game_grid_container;

	private TextView score_view;
	private TableRow end_game_message_container;
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

		setUIReferences();
		loadImages();
		createAndSetListeners();
		newGame();
	}

	private void loadImages() {
		backImage = getResources().getDrawable(R.drawable.icon);
		backImageInverted = getResources()
				.getDrawable(R.drawable.icon_inverted);

		images = new ArrayList<Drawable>();
		Drawable drawable;
		for (int i = 1; i < 22; i++) {
			drawable = getResources().getDrawable(
					getResources().getIdentifier("card" + i, "drawable",
							getPackageName()));
			images.add(drawable);
		}
	}

	private void setUIReferences() {
		mainTable = (TableLayout) findViewById(R.id.main_table);
		game_grid_container = (TableRow) findViewById(R.id.game_grid_container);
		end_game_message_container = (TableRow) findViewById(R.id.end_game_message_container);
		reset_btn_container = (TableRow) findViewById(R.id.reset_btn_container);
		reset_btn = (Button) findViewById(R.id.reset_btn);
		score_view = (TextView) findViewById(R.id.score);
		context = mainTable.getContext();
		normal_game_btn_container = (TableRow) findViewById(R.id.normal_game_btn_container);
		group_game_btn_container = (TableRow) findViewById(R.id.group_game_btn_container);
		pvp_game_btn_container = (TableRow) findViewById(R.id.pvp_game_btn_container);
		normal_game_btn = (Button) findViewById(R.id.normal_game_btn);
		group_game_btn = (Button) findViewById(R.id.group_game_btn);
		pvp_game_btn = (Button) findViewById(R.id.pvp_game_btn);
	}

	private void createAndSetListeners() {
		normalGameCardClickListener = new NormalGameCardClickListener(this,
				cards, images);
		groupGameCardClickListener = new GroupGameCardClickListener(this,
				cards, images);
		pvpGameCardClickListener = new PVPGameCardClickListener(this, images,
				backImage);
		selectGameListener = new SelectGameListener();

		normal_game_btn.setOnClickListener(selectGameListener);
		group_game_btn.setOnClickListener(selectGameListener);
		pvp_game_btn.setOnClickListener(selectGameListener);
		reset_btn.setOnClickListener(new ResetButtonListener());
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
		if (gameType == "normal") {
			cardListener = normalGameCardClickListener;
		} else if (gameType == "group") {
			cardListener = groupGameCardClickListener;
		} else {
			((PVPGameCardClickListener) pvpGameCardClickListener).getReady();
			cardListener = pvpGameCardClickListener;
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
		createShuffledDeck(gameType);

		// add cards to container view
		placeCardsOnTheTable();
	}

	private void createShuffledDeck(String gameType) {
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

				cards[col_i][row_i] = createCard(val, i / 4, gameType);
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

	private Card createCard(int id, int group_id, String gameType) {
		Card card = new Card(context, id, group_id);

		Drawable cover = (group_id % 2 != 0 && gameType == "group") ? backImageInverted
				: backImage;

		card.setBackgroundDrawable(cover);
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

	class SelectGameListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (v == normal_game_btn) {
				startGame("normal");
			}

			else if (v == group_game_btn) {
				startGame("group");
			}

			else {
				startGame("pvp");
			}
		}
	}
}