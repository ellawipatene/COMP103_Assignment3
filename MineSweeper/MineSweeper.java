// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2021T2, Assignment 3
 * Name:
 * Username:
 * ID:
 */

import ecs100.*;
import java.awt.Color;
import javax.swing.JButton;

/**
 *  Simple 'Minesweeper' program.
 *  There is a grid of squares, some of which contain a mine.
 *  
 *  The user can click on a square to either expose it or to
 *  mark/unmark it.
 *  
 *  If the user exposes a square with a mine, they lose.
 *  Otherwise, it is uncovered, and shows a number which represents the
 *  number of mines in the eight squares surrounding that one.
 *  If there are no mines adjacent to it, then all the unexposed squares
 *  immediately adjacent to it are exposed (and so on)
 *
 *  If the user marks a square, then they cannot expose the square,
 *  (unless they unmark it first)
 *  When all the squares without mines are exposed, the user has won.
 */
public class MineSweeper {

    public static final int ROWS = 15;
    public static final int COLS = 15;

    public static final double LEFT = 10; 
    public static final double TOP = 10;
    public static final double SQUARE_SIZE = 20;

    // Fields
    private boolean marking;

    private Square[][] squares;

    private JButton mrkButton;
    private JButton expButton;
    Color defaultColor;
    
    // For challenge
    int[][] AI_board; 
    int flagged_bombs = 0; 
    boolean win = false; 
    boolean running = true; 
    boolean filled_AI_board = false; 

    /** 
     * Construct a new MineSweeper object
     * and set up the GUI
     */
    public static void main(String[] arguments){
        MineSweeper ms = new MineSweeper();
        ms.setupGUI();
        ms.setMarking(false);
        ms.makeGrid();
    }

    /** Set up the GUI: buttons and mouse to play the game */
    public void setupGUI(){
        UI.setMouseListener(this::doMouse);
        UI.addButton("New Game", this::makeGrid);
        UI.addButton("Run AI", this::run_AI); 
        this.expButton = UI.addButton("Expose", ()->setMarking(false));
        this.mrkButton = UI.addButton("Mark", ()->setMarking(true));

        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.0);
    }

    /** Respond to mouse events */
    public void doMouse(String action, double x, double y) {
        if (action.equals("released")){
            int row = (int)((y-TOP)/SQUARE_SIZE);
            int col = (int)((x-LEFT)/SQUARE_SIZE);
            if (row>=0 && row < ROWS && col >= 0 && col < COLS){
                if (marking) { mark(row, col);}
                else         { tryExpose(row, col); }
            }
        }
    }


    // Other Methods

    /** 
     * The player has clicked on a square to expose it
     * - if it is already exposed or marked, do nothing.
     * - if it's a mine: lose (call drawLose()) 
     * - otherwise expose it (call exposeSquareAt)
     * then check to see if the player has won and call drawWon() if they have.
     * (This method is not recursive)
     */
    public void tryExpose(int row, int col){
        /*# YOUR CODE HERE */
        if(squares[row][col].isExposed() || squares[row][col].isMarked()){
             
        }else{
            if(squares[row][col].hasMine()){
                drawLose(); 
                running = false; 
            }
            else{
                exposeSquareAt(row, col);
                if (hasWon()){
                    drawWin();
                    win = true; 
                    running = false; 
                }
            } 
        }
    }

    /** 
     *  Ensures that the square at row and col is exposed.
     *  If it is already exposed, do nothing.
     *  Otherwise,
     *    Expose it and redraw it.
     *    If the number of adjacent mines of this square is 0, then none of
     *      its neighbours have mines, so
     *      expose all its eight neighbours 
     *      (and if they have no adjacent mines, expose their neighbours, and ....)
     *      (be careful not to go over the edges of the map)
     */
    public void exposeSquareAt(int row, int col){
        /*# YOUR CODE HERE */
        if(!squares[row][col].isExposed()){
            squares[row][col].setExposed(); 
            AI_board[row][col] = squares[row][col].getAdjacentMines(); // records the amount of adjacent mines in the AI's 'board'
            UI.println("Exposed square at " + row + ", " + col); 
            squares[row][col].draw(row, col); 
            if(squares[row][col].getAdjacentMines() == 0){
                if(col != 0 && row != 0){exposeSquareAt(row-1, col-1);} // diagonal (top left)
                if(row != 0){exposeSquareAt(row-1, col);} // dirrectly above
                if(row != 0 && col != squares[0].length -1){exposeSquareAt(row-1, col+1);} // diagonal (top right)
                if(col != 0){exposeSquareAt(row, col-1);} // left 
                if(col != squares[0].length -1){exposeSquareAt(row, col+1);} // right
                if(col != 0 && row != squares.length -1){exposeSquareAt(row+1, col-1);} // diagonal (bottom left)
                if(row != squares.length -1){exposeSquareAt(row+1, col);} // dirrectly bellow
                if(row != squares.length -1 && col != squares[0].length -1){ exposeSquareAt(row+1, col+1);} // diagonal (bottom right)
            }
        }

    }

    /** 
     * Returns true if the player has won:
     * If any square without a mine is not exposed, then the player has not won yet.
     * If all the squares without a mine have been exposed, then the player has won.
     * (It doesn't matter if the squares with a mine have been marked or not).
     */
    public boolean hasWon(){
        /*# YOUR CODE HERE */
        
        boolean won = true; 
        
        for(int i = 0; i < squares.length; i++){
            for(int j = 0; j < squares[i].length; j++){
                if(!squares[i][j].hasMine()){
                    boolean temp = squares[i][j].isExposed();
                    if(!temp){won = temp;}
                }
            }
        }

        return won;
    }
    
    public void run_AI(){
        if(!filled_AI_board){fill_AI_board();}
        int counter = 0; 
        while(!win && running){
            AI_move(); 
        } 
    }
    
    /**
     * Runs the AI/decides what moves it will do 
     */
    public void AI_move(){
        boolean made_move = false;
        boolean made_move2 = false;
        int surrounding_bombs = 0; 
        
        // Move One:
        for(int i = 0; i < squares.length; i++){
            for(int j = 0; j < squares[i].length; j++){
                if(AI_board[i][j] == -1){
                    surrounding_bombs = get_surrounding_bombs(i,j);
                    if (AI_board[i][j] == surrounding_bombs){
                        adj_equals_marked(i, j, surrounding_bombs); 
                        made_move = true; 
                        UI.println("move 1"); 
                    }
                }
            }
        } 
        
        if(!made_move){
            int surrounding_exposed = 0; 
            for(int i = 0; i < squares.length; i++){
                for(int j = 0; j < squares[i].length; j++){
                    surrounding_exposed = 0; 
                    surrounding_exposed = get_surrounding_nums(i, j);
                    UI.println(AI_board[i][j] );
                    UI.println(8 - surrounding_exposed);
                    if(AI_board[i][j] == (8 - surrounding_exposed)){
                        all_surr_are_bombs(i, j, surrounding_exposed);
                        made_move2 = true; 
                        UI.println("move 2");
                        break; 
                    }
                }
            } 
        }
        
        UI.println(made_move + " " + made_move2); 
        if(!made_move && !made_move2){
            int x,y; 
            x = (int)(Math.random() * squares[0].length);
            y = (int)(Math.random() * squares.length); 
            boolean valid_square = false; 
            while(AI_board[y][x] != -1){ // Makes sure that the square has not already been exposed or flagged
                x = (int)(Math.random() * squares[0].length); 
                y = (int)(Math.random() * squares.length);
                UI.println("TEXT"); 
            } 
            
            tryExpose(y, x); 
            UI.println("move 3"); 
        }
    }
    
    /**
     * Picks a random square to expose
     */
    public void pick_random(){
        int x,y; 
        x = (int)(Math.random() * squares[0].length);
        y = (int)(Math.random() * squares.length); 
        while(AI_board[y][x] > -1 || AI_board[y][x] != 10){ // Makes sure that the square has not already been exposed or flagged
            x = (int)(Math.random() * squares[0].length); 
            y = (int)(Math.random() * squares.length);
        } 
        
        tryExpose(y, x); 
    }
    
    /**
     * If the number on the tile is equal to the num of marked tiles surrounding it,
     * then expose all of the other surrounding tiles.
     */
    public void adj_equals_marked(int row, int col, int surrounding_bombs){
        if(AI_board[row][col] == surrounding_bombs){
            for(int a = -1; a < 2; a++){
                for(int b = -1; b < 2; b++){
                    // makes sure that the squares are on the board
                    if(row + a >= 0 && col + b >= 0 && row + a < squares.length && col + b < squares[0].length){ 
                        if(AI_board[row + a][col + b] == -1){
                            tryExpose(row + a, col + b); 
                        }
                    }
                }
            }
        }
    }
    
    /** 
     * Returns the amound of marked bombs around a square
     */
    public int get_surrounding_bombs(int row, int col){
        int surrounding_bombs = 0;
        for(int a = -1; a < 2; a++){
            for(int b = -1; b < 2; b++){
                // makes sure that the squares are on the board
                if(row + a >= 0 && col + b >= 0 && row + a < squares.length && col + b < squares[0].length){
                    if(AI_board[row][col] == 10){
                        surrounding_bombs++; 
                    }
                } 
            }
        }
        return surrounding_bombs; 
    }
    
    /**
     * Returns the amount of exposed squares 
     */
    public int get_surrounding_nums(int row, int col){
        int surrounding_nums = 0;
        for(int a = -1; a < 2; a++){
            for(int b = -1; b < 2; b++){
                // makes sure that the squares are on the board
                if(row + a >= 0 && col + b >= 0 && row + a < squares.length && col + b < squares[0].length){
                    if(!(a == 0 && b == 0)){ // makes sure it is not counting the square that it is currently on
                        if(AI_board[row][col] >= 0 && AI_board[row][col] <= 8){
                            surrounding_nums++;  
                        }
                    }
                }else{
                    surrounding_nums++; 
                }
            }
        }
        //UI.println("The square at " + row + ", " + col + ", has " + surrounding_nums + "numbers around it"); 
        return surrounding_nums;
    }
    
    /**
     * If the number on the square is equal to the amount of surrounding squares
     * Mark them all as bombs
     */
    public void all_surr_are_bombs(int row, int col, int surrounding_exposed){
        for(int a = -1; a < 2; a++){
            for(int b = -1; b < 2; b++){
                // makes sure that the squares are on the board
                if(row + a >= 0 && col + b >= 0 && row + a < squares.length - 1 && col + b < squares[0].length -1){ 
                    if(AI_board[row + a][col + b] == -1){
                        AI_board[row + a][col + b] = 10; 
                        mark(row, col); 
                        UI.println("Marked square " + row + ", " + col);
                        flagged_bombs++;
                    }
                }
            }
        }  
    
        
    }

    // completed methods
    
    /**
     * Mark/unmark the square.
     * If the square is exposed, don't do anything,
     * If it is marked, unmark it and redraw,
     * otherwise mark it and redraw.
     */
    public void mark(int row, int col){
        Square square = squares[row][col];
        if (square.isExposed())    { return; }
        else if (square.isMarked()){ square.unMark(); }
        else                       { square.mark(); }
        square.draw(row, col);
    }

    /**
     * Respond to the Mark and Expose buttons:
     * Remember whether the user is currently "Marking" or "Exposing"
     * Change the colour of the "Mark", "Expose" buttons
     */
    public void setMarking(boolean v){
        marking=v;
        if (marking) {
            mrkButton.setBackground(Color.red);
            expButton.setBackground(null);
        }
        else {
            expButton.setBackground(Color.red);
            mrkButton.setBackground(null);
        }
    }


    /**
     * Construct a grid with random mines.
     * Compute the number of adjacent mines in
     */
    public void makeGrid(){
        UI.clearGraphics();
        this.squares = new Square[ROWS][COLS];
        for (int row=0; row < ROWS; row++){
            for (int col=0; col<COLS; col++){
                boolean isMine = Math.random()<0.1;     // approx 1 in 10 squares is a mine 
                this.squares[row][col] = new Square(isMine);
                this.squares[row][col].draw(row, col);
            }
        }
        // now compute the number of adjacent mines for each square
        for (int row=0; row<ROWS; row++){
            for (int col=0; col<COLS; col++){
                int count = 0;
                //look at each square in the neighbourhood.
                for (int r=Math.max(row-1,0); r<Math.min(row+2, ROWS); r++){
                    for (int c=Math.max(col-1,0); c<Math.min(col+2, COLS); c++){
                        if (squares[r][c].hasMine())
                            count++;
                    }
                }
                if (this.squares[row][col].hasMine())
                    count--;  // we weren't suppose to count this square, just the adjacent ones.

                this.squares[row][col].setAdjacentMines(count);
            }
        }
    }

    /** Draw a message telling the player they have won */
    public void drawWin(){
        UI.setFontSize(28);
        UI.drawString("You Win!", LEFT + COLS*SQUARE_SIZE + 20, TOP + ROWS*SQUARE_SIZE/2);
        UI.setFontSize(12);
    }

    /**
     * Draw a message telling the player they have lost
     * and expose all the squares and redraw them
     */
    public void drawLose(){
        for (int row=0; row<ROWS; row++){
            for (int col=0; col<COLS; col++){
                squares[row][col].setExposed();
                squares[row][col].draw(row, col);
            }
        }
        UI.setFontSize(28);
        UI.drawString("You Lose!", LEFT + COLS*SQUARE_SIZE+20, TOP + ROWS*SQUARE_SIZE/2);
        UI.setFontSize(12);
    }

    /**
     * Return a grid of integers, showing the visible state of the board:
     * -1 for any square that is not exposed
     * 0 - 8 for any exposed square, saying how many mines are adjacent to it.
     */
    public int[][] getVisibleState(){
        int[][] ans = new int[ROWS][COLS];
        for (int r=0; r<ROWS ; r++){
            for (int c=0; c<COLS; c++){
                ans[r][c] = squares[r][c].isExposed()?(squares[r][c].getAdjacentMines()):-1;
            }
        }
        return ans;
    }
    
    /**
     * Fills the whole AI board with -1 (i.e. they are all not exposed)
     */
    public void fill_AI_board(){
        AI_board = new int[squares.length][squares[0].length]; 
        for(int i = 0; i < squares.length; i++){
            for(int j = 0; j < squares[i].length; j++){
                AI_board[i][j] = -1; 
            }
        }
        filled_AI_board = true; 
        UI.println("Filled board"); 
    }


}
