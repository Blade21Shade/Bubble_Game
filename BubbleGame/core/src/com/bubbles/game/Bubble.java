package com.bubbles.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class Bubble {
    int i, j; // Position in grid
    int x, y; // Position in picture
    static int radius = 30; // How big bubbles are
    char color; // red, green, orange, or blue, or c for clear (for destroyed)

    static int movingSpeed = 10; // The speed at which bubbles move, both when a user is trying to make a match, and when bubbles fall

    boolean destroyBubble = false; // This will be marked as true if there are 3 or more bubbles in a row

    // x and y are pixel locations/offsets, i and j are grid locations
    public Bubble(int x, int y, int i, int j){
        this.x = x;
        this.y = y;
        this.i = i;
        this.j = j;
        
        // Set color of this bubble
        double random = Math.random();
        if (random <= .25) {
            color = 'r';
        } else if (random <= .5){
            color = 'g';
        } else if (random <= .75) {
            color = 'b';
        } else {
            color = 'o';
        }
    }

    // For copying purposes, called deep copying by GPT
    // Before this I was using the = to copy, but that was just copying a reference, which was dirty
    // Now it uses a copy constructor, which doesn't copy references but instead creates completely new objects 
    // This has become unused, but I'm leaving it in so I can look at it later or at least remember deep copying
    public Bubble(Bubble original) {
        this.x = original.x;
        this.y = original.y;
        this.i = original.i;
        this.j = original.j;
        this.color = original.color;
        this.destroyBubble = original.destroyBubble;
    }

    // This is used when a bubble needs to be copied but not have the same reference
    // There being a color and destroyBubble value differ it from standard constructor
    public Bubble(char colorU, int iU, int jU, int xU, int yU, boolean destroyU) { // U for updated, given the thing the bubble needs to become
        this.color = colorU;
        this.i = iU;
        this.j = jU;
        this.x = xU;
        this.y = yU;
        this.destroyBubble = destroyU;
    }

    // Draws a circle, depends on the circle's color
    public void draw(ShapeRenderer renderer) {
        
        // Set color of the circle to be drawn
        if (color == 'r') {
            renderer.setColor(Color.RED);
        } else if (color == 'b') {
            renderer.setColor(Color.BLUE);
        } else if (color == 'g') {
            renderer.setColor(Color.GREEN);
        } else if (color == 'o') {
            renderer.setColor(Color.ORANGE);
        } else if (color == 'c'){
            renderer.setColor(Color.CLEAR); // For destroyed circles
            return; // This way cleared circles don't overlap with non-cleared circles
        } else {
            renderer.setColor(Color.WHITE); // This should never occur, just for error checking
        }
        
        renderer.circle(x, y, radius);
    }

    // Checks to see if a click is within a circle's parameters
    public boolean checkClick(int xCheck, int yCheck) {
        if (xCheck >= x - radius && xCheck <= x + radius && yCheck >= y - radius && yCheck <= y + radius) {
            return true;
        } else {
            return false;
        }
    }

    // Can be used to set the color of a circle
    public void setColor(char charColor) {
        if (charColor == 'r') {
            color = 'r';
        } else if (charColor == 'b') {
            color = 'b';
        } else if (charColor == 'g') {
            color = 'g';
        } else if (charColor == 'o') {
            color = 'o';
        } else if (charColor == 'c') {
            color = 'c'; // Clear
        }
    }

    public void markDestroyBubble() {
        destroyBubble = true;
    }

    // Used to move bubbles down smoothly
    public void updateBubblePosition() {
        // No x movement, bubbles don't move horizontally
        y -= movingSpeed;
    }

    static public int getRadius() {
        return radius;
    }

}
