package com.bubbles.game;
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
			if (canSwitch && !moving) { // If user bubbles that were switched are valid and the grid isn't moving
				if (gridSwitchCheck()) { // If bubbles would be able to be destroyed (checked via a fake grid)
					// Then run the real grid with the switch of the bubbles clicked
					int i = switchBubbles[0].i;
					int j = switchBubbles[0].j;
					bubbles[i][j] = new Bubble(switchBubbles[1].color, switchBubbles[0].i, switchBubbles[0].j, switchBubbles[0].x, switchBubbles[0].y, switchBubbles[0].destroyBubble);
					// The above looks long but does one large update rather than a bunch of small ones, overall works nicer

					i = switchBubbles[1].i;
					j = switchBubbles[1].j;
					bubbles[i][j] = new Bubble(switchBubbles[0].color, switchBubbles[1].i, switchBubbles[1].j, switchBubbles[1].x, switchBubbles[1].y, switchBubbles[1].destroyBubble);

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
			// The second bubble doesn't match the switch criteria, null both out
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

		// This updates the new bubbles completely, even though it is a bit long
		bubblesCheck[i][j] = new Bubble(switchBubbles[1].color, switchBubbles[0].i, switchBubbles[0].j, switchBubbles[0].x, switchBubbles[0].y, switchBubbles[0].destroyBubble);

		i = switchBubbles[1].i;
		j = switchBubbles[1].j;

		// This updates the new bubbles completely, even though it is a bit long
		bubblesCheck[i][j] = new Bubble(switchBubbles[0].color, switchBubbles[1].i, switchBubbles[1].j, switchBubbles[1].x, switchBubbles[1].y, switchBubbles[1].destroyBubble);

		boolean checkGridSwitch = checkGridForDestroy(bubblesCheck);
		return checkGridSwitch;
	}

	// Check whether the grid can have any bubbles destroyed (3+ in a row)
	// Returns whether it can (true) or not (false)
	private boolean checkGridForDestroy(Bubble[][] bubbles) {

		// Used to see if bubbles need to be destroyed
		// If so, bubbles are marked for destruction within the code
		boolean destroyAny = false;

		// Make color array
		// This array is used so the bubbles can have their colors cleared when being marked for destruction, rather than afterward as was done before
		// This could potentially speed up the program, but because the program is so simple I doubt it would be meaningful
		char[][] colorArray = new char[gridSize][gridSize];
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				colorArray[i][j] = bubbles[i][j].color;
			}
		}

		// Check each row and column once to see if there are 3+ bubbles of the same color in a row
		// This logic replaced other logic which took longer
		// Specifically, the old logic did 18 checks while this one only does 10 (each row and column once)
		// The other also used an int[][][] and arrayList<int[]> to mark bubbles for destruction
		// While that worked fine, the new method is more direct and I assume to be faster (again for such a simple program, it realistically probably doesn't matter)

		// Row check
		for (int i = 0; i < gridSize; i++) {
			int sameColorCount = 1; // Number of bubbles the same color in a row
			// Start at 1 to mark first bubble in pattern as true

			char currentBubbleColorMatch = colorArray[i][0]; // The current bubble color we want to match
			
			// Check each bubble in the row, if the same as currentBubbleColorMatch increment sameColorCount
			// If they aren't the same, set the new color and reset sameColorCount 
			for (int j = 1; j < gridSize; j++) {

				// Check if the current bubble is the same as the color that is set
				if (colorArray[i][j] == currentBubbleColorMatch) {
					sameColorCount++; // Same color as the current value, so increment
				} else {
					currentBubbleColorMatch = colorArray[i][j]; // Change color check to new value
					sameColorCount = 1; // Reset sameColorCount
				}

				// 3+ in a row means we eliminate, check to see if there are 3+ in a row
				if (sameColorCount == 3) { // If we get three in a row, mark the three to be destroyed
					bubbles[i][j].markDestroyBubble();
					bubbles[i][j-1].markDestroyBubble();
					bubbles[i][j-2].markDestroyBubble();

					// Also set their colors to clear
					bubbles[i][j].setColor('c');
					bubbles[i][j-1].setColor('c');
					bubbles[i][j-2].setColor('c');

					destroyAny = true; // Set destroyAny
				} else if (sameColorCount > 3) { // More then 3 in a row, keep marking for destruction
					bubbles[i][j].markDestroyBubble();
					bubbles[i][j].setColor('c'); // Also set color to clear
				}
			}
		}

		// Column check
		for (int j = 0; j < gridSize; j++) { // j is used for columns in the rest of the program, so doing the same here
			int sameColorCount = 1; // Number of bubbles the same color in a column
			// Start at 1 to mark first bubble in pattern as true

			char currentBubbleColorMatch = colorArray[0][j]; // The current bubble color we want to match
			
			// Check each bubble in the column, if the same as currentBubbleColorMatch increment sameColorCount
			// If they aren't the same, set the new color and reset sameColorCount 
			for (int i = 1; i < gridSize; i++) {

				// Check if the current bubble is the same as the color that is set
				if (colorArray[i][j] == currentBubbleColorMatch) {
					sameColorCount++; // Same color as the current value, so increment
				} else {
					currentBubbleColorMatch = colorArray[i][j]; // Change color check to new value
					sameColorCount = 1; // Reset sameColorCount
				}

				// 3+ in a row means we eliminate, check to see if there are 3+ in a row (as in next to each other)
				if (sameColorCount == 3) { // If we get three in a row, mark the three to be destroyed
					bubbles[i][j].markDestroyBubble();
					bubbles[i-1][j].markDestroyBubble();
					bubbles[i-2][j].markDestroyBubble();

					// Also set their colors to clear
					bubbles[i][j].setColor('c');
					bubbles[i-1][j].setColor('c');
					bubbles[i-2][j].setColor('c');
					
					destroyAny = true; // Set destroyAny
				} else if (sameColorCount > 3) { // More then 3 in a row, keep marking for destruction
					bubbles[i][j].markDestroyBubble();
					bubbles[i][j].setColor('c');
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
					// If this bubble was destroyed or any bubble below it was destroyed, become bubble above
					// - 1 looks up
					// Once again use the large update function
					bubbles[i][j] = new Bubble(bubbles[i - 1][j].color, i, bubbles[i - 1][j].j, bubbles[i - 1][j].x, bubbles[i - 1][j].y, bubbles[i - 1][j].destroyBubble);
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
			for (int i = 0; i < gridSize; i++) { // This could be turned into a double if statement search rather than nested for loop
				for (int j  = 0; j < gridSize; j++) {
					if (bubbles[i][j].checkClick(xPos, yPos)) { // Finds the bubble clicked on
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
