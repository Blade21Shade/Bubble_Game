package com.bubbles.game;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class MatchingBubbles extends ApplicationAdapter {
	ShapeRenderer renderer;

	static int gridSize = 5; // Dimensions of grid, gridSize x gridSize grid

	static int moveVariable = 0; // Basically a timer for moving bubbles

	static Bubble[][] bubbles = new Bubble[gridSize][gridSize]; // Grid of bubbles

	static Bubble[] switchBubbles = new Bubble[2]; // Used when a user tries to switch bubbles with each other

	static boolean canSwitch = false; // Whether two selected bubbles by the user are valid switch targets

	static boolean moving = false; // Whether bubbles are currently moving, if so the user can't make moves

	@Override
	public void create() {
		renderer = new ShapeRenderer();

		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				// The very first value is the offset from the left edge
				bubbles[i][j] = new Bubble(320 + 70 * j, 440 - 70 * i, i, j);
			}
		}

		ClickInput inputProcessor = new ClickInput();
		Gdx.input.setInputProcessor(inputProcessor);
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear screen

		// Draw the bubbles
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				bubbles[i][j].draw(renderer);
			}
		}

		// Check if stuff is moving, if not check to see if any bubbles can be destroyed,
		// if not check to see if the bubbles currently clicked on were switched would cause bubbles to be able to be destroyed
		if (moving == false) {

			// checkGridForDestroy(bubbles) before checking canSwitch (or with an else) in case
			// new bubbles have spawned that are 3 in a row
			moving = checkGridForDestroy(bubbles); // This will set to true/false
			if (canSwitch && !moving) { // May need to mark canSwitch to false at some point in the case it is true, not sure where !
				if (gridSwitchCheck()) { // If bubbles would be able to be destroyed (checked via a fake grid)
					// Then run the real grid with the switch of the bubbles clicked
					int i = switchBubbles[0].i;
					int j = switchBubbles[0].j;
					bubbles[i][j] = new Bubble(switchBubbles[1]);
					i = switchBubbles[1].i;
					j = switchBubbles[1].j;
					bubbles[i][j] = new Bubble(switchBubbles[0]);

					// Move the grid and set the variable
					moving = checkGridForDestroy(bubbles); // Should always set moving to true because of gridSwitchCheck()

					// Once swapped, reset switchBubbles array
					switchBubbles[0] = null;
					switchBubbles[1] = null;
					canSwitch = false; // Set canSwitch to false
				}
			}
		} else { // Moving is true, which means we need to move bubbles down

			int maxMove = 7; // Graphical and logical alignment 

			// Move bubbles
			if (moveVariable >= 0 && moveVariable < maxMove) { // Move bubbles
				moveBubbles();
				moveVariable++;
			} else if (moveVariable == maxMove) { // Update bubbles
				moveVariable = -1; // Set to -1 for bubble generation
				updateBubbles();
			} else { // Generate new bubbles if necessary
				if (createNewBubbles()){ // If bubbles were generated, see if stuff needs to move again
					moveVariable = 0;
				} else { // No new bubbles were created
					moving = false; // Set moving to false
					moveVariable = 0; // Reset the counter for next run
				}
			}
		}

		// Close renderer
		renderer.end();
	}

	// Checks whether two bubbles are next to each other
	// Swap criteria is that a second bubble is in the same row as the first and
	// directly to the left or right of it
	// or the second bubble is in the same column as the first and directly above or
	// below it
	private static void checkNextTo() {
		// Check to see if the bubbles are next to each other
		if ((switchBubbles[1].i == switchBubbles[0].i - 1 || switchBubbles[1].i == switchBubbles[0].i + 1)
				&& switchBubbles[1].j == switchBubbles[0].j) {
			// Second bubble is in the column to the left or right of the first bubble
			// and is the the same row as the first bubble
			canSwitch = true; // Global variable for whether player chosen bubbles can switch
		} else if ((switchBubbles[1].j == switchBubbles[0].j - 1 || switchBubbles[1].j == switchBubbles[0].j + 1)
				&& switchBubbles[1].i == switchBubbles[0].i) {
			// Second bubble is in the first row above or below of the first bubble
			// and is the the same column as the first bubble
			canSwitch = true;
		} else { // The bubbles pressed aren't valid switch targets
			// The second bubble doesn't match the switch criteria
			switchBubbles[0] = null;
			switchBubbles[1] = null;
			canSwitch = false;
		}

	}

	// Check whether the grid can move based on the current bubbles clicked
	private boolean gridSwitchCheck() {

		Bubble[][] bubblesCheck = new Bubble[gridSize][gridSize];

		int i = 0;
		int j = 0;

		for (i = 0; i < gridSize; i++) { // Make the temp array a copy, but not reference, of the original
			for (j = 0; j < gridSize; j++) {
				bubblesCheck[i][j] = new Bubble(bubbles[i][j]);
			}
		}

		// Swap bubbles that need to be checked
		i = switchBubbles[0].i;
		j = switchBubbles[0].j;

		bubblesCheck[i][j] = new Bubble(switchBubbles[1]);

		i = switchBubbles[1].i;
		j = switchBubbles[1].j;

		bubblesCheck[i][j] = new Bubble(switchBubbles[0]);

		boolean checkGridSwitch = checkGridForDestroy(bubblesCheck);
		return checkGridSwitch;
	}

	// Check whether the grid can have any bubbles destroyed (3+ in a row)
	// Returns whether it can (true) or not (false)
	private boolean checkGridForDestroy(Bubble[][] bubbles) {

		// Used to see if bubbles need to be destroyed
		// If so, bubbles are marked for destruction within the code
		boolean destroyAny = false;

		// !!! i is row, j is position in column (row)
		// array[][] = array[which array][position in array]

		// Used for which color we want to check for
		char colorCheck = 'n'; // n stands for none as a placeholder

		// Used for destroying stuff later if needed
		ArrayList<int[]> destroyBubbles = new ArrayList<>();

		// Records i and j positions for bubbles that need to be destroyed, these are
		// passed into an arrayList
		int[][][] ints = new int[5][5][2];
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				ints[i][j][0] = i;
				ints[i][j][1] = j;
			}
		}

		// Scanning grid to see if bubbles need to be marked for destruction
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {

				// If the bubble is not on the right edge or 1 from ledge, start looking right
				if (j < gridSize - 2) {
					colorCheck = bubbles[i][j].color;
					for (int temp = j; temp < gridSize; temp++) { // temp = j so the bubble can add itself
						if (bubbles[i][temp].color == colorCheck) {
							destroyBubbles.add(0, ints[i][temp]);
						} else {
							break;
						}
					}
				}

				// Mark bubbles to be destroyed
				// This has to occur twice in this for-loop as otherwise incorrect bubbles could be added into
				// destroyBubbles, this ensures that every check is clean
				if (destroyBubbles.size() >= 3) {
					int[] ints2 = new int[2];
					for (int k = 0; k < destroyBubbles.size(); k++) {
						ints2 = destroyBubbles.get(k);
						bubbles[ints2[0]][ints2[1]].markDestroyBubble();
					}
					destroyAny = true;
				}

				// Always clear the list, even if there were less than 3 bubbles
				destroyBubbles.clear();

				// If the bubble is not in the bottom row or 1 from bottom, start looking down
				if (i < gridSize - 2) {
					colorCheck = bubbles[i][j].color;
					for (int temp = i; temp < gridSize; temp++) { // temp = i so the bubble can add itself
						if (bubbles[temp][j].color == colorCheck) {
							destroyBubbles.add(0, ints[temp][j]);
						} else {
							break;
						}
					}
				}

				// Mark bubbles to be destroyed
				if (destroyBubbles.size() >= 3) {
					int[] ints2 = new int[2];
					for (int k = 0; k < destroyBubbles.size(); k++) {
						ints2 = destroyBubbles.get(k);
						bubbles[ints2[0]][ints2[1]].markDestroyBubble();
					}
					destroyAny = true;
				}

				// Always clear the list, even if there were less than 3 bubbles
				destroyBubbles.clear();
			}

		} // End of for meant to mark bubbles to be destroyed

		// Now do actual deleting (really just make bubbles clear)
		if (destroyAny) { // See if anything needed to be destroyed
			for (int i = 0; i < gridSize; i++) {
				for (int j = 0; j < gridSize; j++) {
					// Delete bubbles
					// Search through bubbles[][] for any bubble marked with "destroyBubble"
					// If that bubble is destroyed, delete it (turn it's color to clear)
					if (bubbles[i][j].destroyBubble == true) {
						bubbles[i][j].setColor('c');
					}
				}
			}
		}

		// Whether anything needed to be destroyed
		return destroyAny;
	}

	// Moves bubbles down as needed
	private void moveBubbles() {
		for (int i = 0; i < gridSize - 1; i++) { // Don't check bottom row for moving, they can't move further down
			for (int j = 0; j < gridSize; j++) {
				// Check if any bubble in the current column below this one is destroyed, if so then this bubble needs to move
				for (int k = 1; k < gridSize - i; k++) { // K is the # of rows after the current row we are checking
					if (bubbles[i + k][j].destroyBubble == true) {
						bubbles[i][j].updateBubblePosition();
						break;
					}
				}
			}
		}
	}

	// Update bubbles as needed
	private void updateBubbles() {

		int[] lowestDestroyedBubble = {-1,-1,-1,-1,-1}; // Holds row positions for bubbles, -1 is invalid
		// The index if this is in column position

		// Update lowest destroyed bubble array to reflect which bubble in a column is the lowest one which needs to be destroyed
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				if (bubbles[i][j].destroyBubble == true && i > lowestDestroyedBubble[j]) {
					// If this bubble is destroyed, and its row position has a greater i value than the current one, update the array
					lowestDestroyedBubble[j] = i;
				}
			}
		}

		// Lowest destroyed bubble will now hold the row value of the lowest destroyed bubble in each column
		// Which can be used for the below statement

		// If this bubble or any bubble below was destroyed, become bubble above

		// This updates from the bottom towards the top, otherwise top bubbles just get replicated down all the way
		for (int i = gridSize - 1; i > 0; i--) { // Don't check top row for updating, updates are based on the row above
			for (int j = 0; j < gridSize; j++) {
					if (bubbles[i][j].destroyBubble == true || bubbles[i][j].i < lowestDestroyedBubble[j]) {
					// If this bubble was destroyed 	or any bubble below it was destroyed, become bubble above
					bubbles[i][j] = new Bubble(bubbles[i - 1][j]); // - 1 looks up
					bubbles[i][j].i = i; // This could effectively be done in the copy function in the Bubble class, but leaving it here for now
				}
			}
		}


		// If any bubble in a column was destroyed, set the top bubble to be technically destroyed
		// This will make it so createNewBubbles() can check the top row for generation of new bubbles
		for (int j = 0; j < gridSize; j++) {
			if (lowestDestroyedBubble[j] != -1) {
				bubbles[0][j].destroyBubble = true;
			}
		}

	}

	// Creates new bubbles as needed
	private boolean createNewBubbles() {
		boolean bubblesCreated = false;
		// No row check since bubbles are created in the top row
		for (int j = 0; j < gridSize; j++) {
			if (bubbles[0][j].destroyBubble == true) {
				bubbles[0][j] = new Bubble(320 + 70 * j/*i*/, 440 - 70 * 0, 0, j); // * 0 for row 0 (i)
				bubblesCreated = true; // At least one bubble was created	
			}
		}
		return bubblesCreated;
	}

	// Used to fill the switch bubble array, what bubbles the player clicks
	static public void fillSwitchBubble(int xPos, int yPos) {
		if (!moving) { // If the board is moving, player can't choose new bubbles
			for (int i = 0; i < gridSize; i++) {
				for (int j  = 0; j < gridSize; j++) {
					if (bubbles[i][j].checkClick(xPos, yPos)) { // !!! Make sure checkClick works correctly, finds the bubble clicked on
						if (switchBubbles[0] == null) { // If the first position is empty
							switchBubbles[0] = new Bubble(bubbles[i][j]);
							return; // If this point has been reached the function has finished its purpose so return
						} else { // If the first position is not empty
							switchBubbles[1] = new Bubble(bubbles[i][j]);

							checkNextTo(); // See if the bubbles can switch, returns it to the global variable
							return; // If this point has been reached the function has finished its purpose so return
						}
					}
				}
			}
		}
	}
}
