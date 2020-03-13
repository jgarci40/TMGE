package com.INF122.TMGE;

import com.INF122.TMGE.tetris.TetrisShapeFactory;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Controller {

    Board board;
    int tileSize;
    private List<Shape> prototypeShapes = new ArrayList<Shape>();
    private Shape currentActiveShape;
    private double time;


    public Controller(Board board) {
        this.board = board;
        this.tileSize = board.tileSize;
    }

    /*public void addProtoypeShape(Tile... tiles){
      Shape shape = new Shape(tiles);
      prototypeShapes.add(shape);
    }*/

    public Group create(){

        System.out.println();

        //Pane pane = new Pane();
        //root.setPrefSize(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);
        //parent.setPrefSize(this.board.gridWidth * this.board.tileSize,
                              //this.board.gridHeight * this.board.tileSize
                            //);
        Group group = new Group();

        //Canvas canvas = new Canvas(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);
        //g = canvas.getGraphicsContext2D();

        //root.getChildren().addAll(canvas);

        //spawn
        generateShape();
        render(group);


        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                time += 0.017;

                if (time >= 0.5) {
                    moveShape(Direction.DOWN);
                    render(group);
                    time = 0;
                }
            }
        };
        timer.start();


        return group;
    }

    public void render(Group group){
        group.getChildren().clear();
        for(int colIndex=0; colIndex<this.board.gridWidth; colIndex++){
            for(int rowIndex=0; rowIndex<this.board.gridHeight; rowIndex++){
                if(this.board.boardGrid[colIndex][rowIndex]!=null){
                    //System.out.println(this.board.boardGrid[colIndex][rowIndex].rectangle.);
                    group.getChildren().add(this.board.boardGrid[colIndex][rowIndex].rectangle);

                }
            }

        }
    }



    public void addPrototypeShape() {

    }

    public boolean isColliding(List<Tile> newTileList){
        boolean isCollidingWithSelf = false;
        for(Tile newTile: newTileList){
            if(this.board.boardGrid[newTile.columnIndex][newTile.rowIndex]!=null){
                for(Tile activeTile: this.currentActiveShape.tiles){
                    isCollidingWithSelf = false;
                    if((activeTile.rowIndex==newTile.rowIndex) && (activeTile.columnIndex==newTile.columnIndex)){
                        isCollidingWithSelf= true;
                        break;
                    }
                }
                //if its not colliding with itself
                //but there is a existing object,
                //then return isColliding=true
                if(!isCollidingWithSelf){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isBottom(List<Tile> newTileList){
        for(Tile newTile: newTileList){
            if( newTile.rowIndex <0 || newTile.rowIndex >= board.gridHeight){
                return true;
            }
        }
        return false;
    }

    public boolean isLeftOrRightWall(List<Tile> newTileList){
        for(Tile newTile: newTileList){
            if( newTile.columnIndex <0 || newTile.columnIndex >= board.gridWidth){
                return true;
            }
        }
        return false;
    }



    public void moveShape(Direction direction) {


        //calculate new center tile coordinates
        int newColIndex = this.currentActiveShape.centerPieceColumnIndex + direction.colIndex;
        int newRowIndex = this.currentActiveShape.centerPieceRowIndex + direction.rowIndex;


        //Creating new shape
        List<Tile> newTileList = new ArrayList<Tile>();
        for(Tile tile : this.currentActiveShape.tiles){
            Tile newTile = new Tile(tile.tileSize, tile.tileColor, newColIndex, newRowIndex, tile.position , tile.directions);
            newTileList.add(newTile);
        }

        //Update Board if not out of bounds and no collision
        if(direction==Direction.LEFT || direction==Direction.RIGHT){
            if( !isLeftOrRightWall(newTileList) && !isColliding(newTileList) ){
                updateBoard(newTileList);
            }
        } else if(direction==Direction.DOWN){
            //check for out of bounds and collision
            if( !isBottom(newTileList) && !isColliding(newTileList) ){
                updateBoard(newTileList);
            } else {
                //spawn new shape
                generateShape();
            }
        }


    }

    private void updateBoard(List<Tile> newTileList){
        //Remove old shape
        for(Tile tile : this.currentActiveShape.tiles){
            //Remove old tile from board
            this.board.boardGrid[tile.columnIndex][tile.rowIndex]=null;

            //set old tiles to null
            tile = null;
        }

        this.currentActiveShape = null;

        //Create a new shape
        Shape newShape = new Shape(newTileList);
        this.currentActiveShape = newShape;

        //Update board
        for(Tile newTile: this.currentActiveShape.tiles){
            //Update board with new tile
            this.board.boardGrid[newTile.columnIndex][newTile.rowIndex]=newTile;
        }
    }

    public void rotateShape(){

        if(this.currentActiveShape.centerPieceRowIndex>0){ //don't allow rotation when shape is at the top of board
            List<Tile> newTileList = new ArrayList<Tile>();
            for(Tile tile : this.currentActiveShape.tiles){
                List<Direction> newDirections = new ArrayList<>();
                for(Direction direction: tile.directions){
                    Direction newDirection = direction.next();
                    newDirections.add(newDirection);
                }

                Tile newTile = new Tile(tile.tileSize, tile.tileColor, tile.centerPieceColumnIndex, tile.centerPieceRowIndex, tile.position , newDirections);
                newTileList.add(newTile);

            }

            for(Tile tile : this.currentActiveShape.tiles){
                //Remove old tile from board
                this.board.boardGrid[tile.columnIndex][tile.rowIndex]=null;

                //set old tiles to null
                tile = null;
            }

            this.currentActiveShape = null;

            //Create a new shape
            Shape newShape = new Shape(newTileList);
            this.currentActiveShape = newShape;

            //Update board
            for(Tile newTile: this.currentActiveShape.tiles){
                //Update board with new tile
                this.board.boardGrid[newTile.columnIndex][newTile.rowIndex]=newTile;
            }


        }
    }


    /*
       Spawns new shape from top of grid
    */
    public void generateShape() {


        Shape newShape = TetrisShapeFactory.getRandomShape(this.board);

        //set newly created shape as the currently active shape
        this.currentActiveShape = newShape;

        //check if spawn area is occupied
        boolean isOccupied =false;
        for (Tile newTile : this.currentActiveShape.tiles) {
            if(this.board.boardGrid[newTile.columnIndex][newTile.rowIndex]!=null){
                isOccupied = true;
            }
        }

        //TODO
        //check if shape reaches top
        if(!isOccupied){
            //add tiles to board
            for (Tile newTile : this.currentActiveShape.tiles) {
                this.board.boardGrid[newTile.columnIndex][newTile.rowIndex] = newTile;
            }
        } else {
            //TODO later add Game Over JavaFx message
            System.out.println("GAME OVER");
            System.exit(0);
        }

    }
}