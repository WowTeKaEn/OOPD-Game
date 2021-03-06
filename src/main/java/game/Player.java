package game;

import java.util.List;

import nl.han.ica.oopg.collision.CollidedTile;
import nl.han.ica.oopg.collision.CollisionSide;
import nl.han.ica.oopg.collision.ICollidableWithTiles;
import nl.han.ica.oopg.exceptions.TileNotFoundException;
import nl.han.ica.oopg.objects.Sprite;
import nl.han.ica.oopg.objects.SpriteObject;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import tiles.SpikeTile;
import tiles.WallTile;
import tiles.WinTile;

public class Player extends SpriteObject implements ICollidableWithTiles {
	private Game world;
	private int speed = 0;
	private boolean grounded, isHolding, isJumping, leftIsDown, rightIsDown;
	private int jumpTime = 25;
	private PImage playerSprite;
	private PImage playerVisible;
	private PImage playerInVisible;
	private float speedMult = 0.05f, yPos = -1;

	public Player(Game world) {

		super(new Sprite(Game.MEDIA_URL.concat("Player_Invisible.png")));
		playerVisible = world.loadImage(Game.MEDIA_URL.concat("Player_Visible.png"));
		playerInVisible = world.loadImage(Game.MEDIA_URL.concat("Player_Invisible.png"));
		playerSprite = playerInVisible;

		setGravity(1);
		setFriction(0.1f);
		this.world = world;
	}
	
	@Override
	public void draw(PGraphics g)
	{
		g.image(playerSprite, x, y);
	}
	
	public void setVisible(boolean visible) {
		if(visible) {
			playerSprite = playerVisible;
		}else {
			playerSprite = playerInVisible;
		}
	}

	@Override
	public void keyPressed(int keyCode, char key) {
		if (keyCode == world.LEFT) {
			speed = -10;
			leftIsDown = true;
		}
		if (keyCode == world.RIGHT) {
			speed = 10;
			rightIsDown = true;
		}
		if (keyCode == world.UP && !isJumping) {
			isJumping = true;
			jumpTime = 25;
		}
		if (keyCode == world.UP) {
			isHolding = true;
			update();
		}
	}

	public void moveIt() {
		if (rightIsDown || leftIsDown) {
			setxSpeed(speed * speedMult);
			if (speedMult < 1) {
				speedMult += 0.10f;
			}
		}
	}

	private void jumpIt() {
		if (isJumping && jumpTime >= 0 && isHolding) {
			setySpeed(-jumpTime);
		}
		jumpTime--;
	}

	public void speedMultReset() {
		speedMult = 0.05f;
	}

	@Override
	public void update() {
		moveIt();
		jumpIt();
		checkViewPort();
	}
	
	 
	
	public void checkViewPort() {
		if(world.getView().getViewport().getX() > this.getX() + (this.getWidth() / 2)  || world.getView().getViewport().getY() > this.getY() + (this.getHeight() / 2) || world.getView().getViewport().getY() + world.getView().getViewport().getZoomHeight() < this.getY() + (this.getHeight() / 2)) {
			playerSprite = playerVisible;
			world.gameOver();
		}
	}

	@Override
	public void tileCollisionOccurred(List<CollidedTile> collidedTiles) {

		grounded = false;

		PVector vector;
		for (CollidedTile ct : collidedTiles) {
			vector = world.getTileMap().getTilePixelLocation(ct.getTile());
			if (ct.getTile() instanceof WallTile) {
				
				try {
					
					if (ct.getCollisionSide() == CollisionSide.TOP) {
						if (getX() + getWidth() > vector.x && getX() < vector.x + ct.getTile().getSprite().getWidth()) {
							setY(vector.y - getHeight());
							setySpeed(0);
							grounded = true;
							isJumping = false;
						}
					}
					if (ct.getCollisionSide() == CollisionSide.RIGHT) {
						if (getY() < vector.y + ct.getTile().getSprite().getHeight()
								&& getY() + getHeight() > vector.y) {
							setxSpeed(0);
							setX(vector.x + getWidth());
						}

					}
					if (ct.getCollisionSide() == CollisionSide.LEFT) {
						if (getY() < vector.y + ct.getTile().getSprite().getHeight()
								&& getY() + getHeight() > vector.y) {
							setxSpeed(0);
							setX(vector.x - getWidth());
						}
					}
					if (ct.getCollisionSide() == CollisionSide.BOTTOM) {
						if (getX() + getWidth() > vector.x && getX() < vector.x + ct.getTile().getSprite().getWidth()) {
							setY(vector.y + getHeight());
							setySpeed(0);
						}
					}
				} catch (TileNotFoundException e) {
					e.printStackTrace();
				}
				

			}
			if (ct.getTile() instanceof SpikeTile) {
				if (getX() + getWidth() > vector.x && getX() < vector.x + ct.getTile().getSprite().getWidth() && getY() < vector.y + ct.getTile().getSprite().getHeight()
						&& getY() + getHeight() > vector.y) {
					playerSprite = playerVisible;
					world.gameOver();
				}
			}
			if (ct.getTile() instanceof WinTile) {
				playerSprite = playerVisible;
				world.getPauseMenu().nextLevelPause();
			}
		}
	}

	@Override
	public void keyReleased(int keyCode, char key) {
		if (keyCode == world.LEFT) {
			leftIsDown = false;
			speedMultReset();
		}
		if (keyCode == world.RIGHT) {
			rightIsDown = false;
			speedMultReset();
		}

		if (!rightIsDown && !leftIsDown) {
			speedMultReset();
		}
		if (keyCode == world.UP) {
			isHolding = false;
		}
	}

}