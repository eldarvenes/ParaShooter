package eldar.andengine.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;

import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;
import org.andengine.util.debug.Debug.DebugLevel;
import org.andengine.util.math.MathUtils;
import org.andengine.util.modifier.ModifierList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.widget.Toast;

public class MainActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener {

	private Game game = new Game();

	@Override
	public EngineOptions onCreateEngineOptions() {
		game.mCamera = new Camera(0, 0, game.mCameraWidth, game.mCameraHeight);
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
				game.mCameraWidth, game.mCameraHeight), game.mCamera);
		engineOptions.getAudioOptions().setNeedsSound(true);
		return engineOptions;
	}

		
	
	@Override
	protected void onCreateResources() {

		// Sound
		game.setupSoundResources(mEngine, this);

		try {
			game.mTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("gfx/turret.png");
				}
			});

			game.mTexture.load();
			game.mTurretTextureRegion = TextureRegionFactory.extractFromTexture(game.mTexture);
		} catch (IOException e) {
			Debug.e(e);
		}

		// HUD ->score,
		game.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256,
				Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL), 32);
		game.mFont.load();
		game.hud = new HUD();
		game.mCamera.setHUD(game.hud);

		// Set texture path
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		// Bullet Tiled Atlas and so on
		game.mBitmapTextureAtlasBullet = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.DEFAULT);

		game.mBulletTextureRegionTiled = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
				game.mBitmapTextureAtlasBullet, this, "bullet_tiled.png", 0, 0, 2, 1);

		// Para Tiled texture
		game.mParaTextureRegionTiled = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
				game.mBitmapTextureAtlasBullet, this, "para_tiled4.png", 32, 0, 4, 2);

		// Burst Tiled texture
		game.mBurstTextureRegionTiled = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
				game.mBitmapTextureAtlasBullet, this, "burst_tiled.png", 200, 200, 2, 1);

		
		// Load BulletAtlas
		game.mBitmapTextureAtlasBullet.load();

		// Bullet
		game.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 32, TextureOptions.DEFAULT);
		game.mBulletTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(game.mBitmapTextureAtlas, this,
				"bullet.png", 0, 0);
		game.mBitmapTextureAtlas.load();

		// Parallax Background
		game.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024);
		game.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(game.mAutoParallaxBackgroundTexture,
				this, "parallax_background_layer_back.png", 0, 188);
		game.mParallaxLayerMid = BitmapTextureAtlasTextureRegionFactory.createFromAsset(game.mAutoParallaxBackgroundTexture,
				this, "parallax_background_layer_mid.png", 0, 669);
		game.mAutoParallaxBackgroundTexture.load();

		Game.engineLock = this.mEngine.getEngineLock();

	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		// Scene scene;
		game.scene = new Scene();

		game.scene.setOnSceneTouchListener(this);

		// Setting up physics
		
		try {
			game.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		} catch (Exception e) {
			Debug.log(DebugLevel.DEBUG, "error in physics world, exeption");
			e.printStackTrace();
			
		}

		// Create a ground
		final Rectangle ground = new Rectangle(0, game.mCameraHeight - 2, game.mCameraWidth, 2, getVertexBufferObjectManager());
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.game.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);

		
		// Create left wall
		final Rectangle left = new Rectangle(0, 0, 2, game.mCameraHeight, getVertexBufferObjectManager());
		PhysicsFactory.createBoxBody(this.game.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);

		game.scene.attachChild(ground);
		game.scene.attachChild(left);

		// ParallaxBackground
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, game.mCameraHeight
				- game.mParallaxLayerBack.getHeight(), game.mParallaxLayerBack, this.getVertexBufferObjectManager())));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-2.0f, new Sprite(0, 80, game.mParallaxLayerMid,
				getVertexBufferObjectManager())));
		game.scene.setBackground(autoParallaxBackground);

		// add turret
		final Sprite turret = new Sprite(0, 417, game.mTurretTextureRegion, this.getVertexBufferObjectManager());
		game.scene.attachChild(turret);

		// Spawn paratroopers after certain time
		game.scene.registerUpdateHandler(new TimerHandler(15f / 20.0f, true, new ITimerCallback() {

			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				// Random Position Generator, done twice...
				final float xPos = MathUtils.random(30.0f, (game.mCameraWidth - 30.0f));
				final float yPos = MathUtils.random(30.0f, (game.mCameraHeight - 30.0f));
				addPara((int) (xPos * yPos));
			}

		}));

		// Check if Bullet hits Para and if bullet/para is out of game level.
		game.scene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() {
			}

			@Override
			public void onUpdate(final float pSecondsElapsed) {

				// Update score in HUD
				updateScore();
				
				// Check life
				checkLife();

				if (!game.ParaVector.isEmpty()) {
					// Sjekk om en kule treffer en paratrooper
					for (int i = 0; i < game.BulletVector.size(); i++) {
						for (int j = 0; j < game.ParaVector.size(); j++) {
							if (game.ParaVector.get(j).collidesWith(game.BulletVector.get(i))) {
								Debug.log(DebugLevel.INFO, "collision");
								Debug.log(DebugLevel.INFO, "Bullet: " + i + " collision with Para: " + j);
								removeBullet(game.BulletVector.get(i));
								Debug.log(DebugLevel.INFO, "Bullet " + i + " was removed, hit para");
								removePara(game.ParaVector.get(j));
								Debug.log(DebugLevel.INFO, "Para was removed");
								game.score++;
								Debug.log(DebugLevel.INFO, "Score: " + game.score);
							}
						}

					}

					for (int i = 0; i < game.BurstVector.size(); i++) {
						for (int j = 0; j < game.ParaVector.size(); j++) {
							if (game.ParaVector.get(j).collidesWith(game.BurstVector.get(i))) {
								Debug.log(DebugLevel.INFO, "burst collision");
								Debug.log(DebugLevel.INFO, "Burst: " + i + " collision with Para: " + j);
								removeBurst(game.BurstVector.get(i));
								Debug.log(DebugLevel.INFO, "Burst " + i + " was removed, hit para");
								removePara(game.ParaVector.get(j));
								Debug.log(DebugLevel.INFO, "Para was removed");
								game.score++;
								Debug.log(DebugLevel.INFO, "Score: " + game.score);
							}
						}

					}

					// sjekk om en kule er nedenfor skjermen
					if (!game.BulletVector.isEmpty()) {
						for (int b = 0; b < game.BulletVector.size(); b++) {
							if (game.BulletVector.get(b).getY() > 410 && (game.BulletVector.get(b).getX() > 800)) {
								removeBullet(game.BulletVector.get(b));
								Debug.log(DebugLevel.INFO, "Bullet " + b + " was removed, Y > 410");
							}
						}
					}

					// sjekk om burst er utafor skjermen
					if (!game.BurstVector.isEmpty()) {
						for (int b = 0; b < game.BurstVector.size(); b++) {
							if (game.BurstVector.get(b).getY() < 0 || (game.BurstVector.get(b).getX() > game.mCameraWidth)) {
								removeBurst(game.BurstVector.get(b));
								Debug.log(DebugLevel.INFO, "Burst " + b + " was removed, X > 900 && Y > 410");
							}
						}
					}

					// sjekk om en paratrooper er nedenfor skjermen
					if (!game.ParaVector.isEmpty()) {
						for (int v = 0; v < game.ParaVector.size(); v++) {
							if (game.ParaVector.get(v).getY() > 420) {
								removePara(game.ParaVector.get(v));
								
								game.life--;
								Debug.log(DebugLevel.INFO, "Para " + v + " was removed, Y > 420");
							}
						}
					}

				}
			}

			
		});

		game.scene.registerUpdateHandler(this.game.mPhysicsWorld);
		game.scene.registerUpdateHandler(game.hud);

		return game.scene;
	}

	private String isLevelUnLocked(int levelNum){
        Db myDB = new Db(this);
        String myReturn = myDB.isLevelUnLocked(levelNum);
        myDB.close();
        return myReturn;
    }
	
	// Check for player touch
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {

		if (pSceneTouchEvent.isActionDown()) {
			game.mDownTimeMilliseconds = pSceneTouchEvent.getMotionEvent().getDownTime();

			Debug.log(DebugLevel.INFO, "TT:X: " + pSceneTouchEvent.getX());
			Debug.log(DebugLevel.INFO, "TT:Y: " + pSceneTouchEvent.getY());

			game.previousDown = (int) pSceneTouchEvent.getY();

			// Stop player from firing if maxburst is reached.
			if (game.BurstVector.size() < game.MAXBURST) {
				createBurst(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				game.soundBurst.play();
				Debug.log(DebugLevel.DEBUG, "test Db1: " + isLevelUnLocked(1));
				Debug.log(DebugLevel.DEBUG, "test Db2: " + isLevelUnLocked(3));
				
				//createBullet(1);
			}
		}

		if (pSceneTouchEvent.isActionUp()) {
			game.upTimeMilliseconds = pSceneTouchEvent.getMotionEvent().getEventTime();
			;
			long diff = game.upTimeMilliseconds - game.mDownTimeMilliseconds;
			// game.mExplosionSound.play();
			// createBullet(diff);

		}		
		return true;
	}

	private void updateScore() {
		game.scoreText = new Text(game.mCameraWidth - 150, 20, game.mFont, "Score: " + game.score, getVertexBufferObjectManager());
		game.lifeText = new Text(game.mCameraWidth -270, 20, game.mFont, "Life: " + game.life, getVertexBufferObjectManager());
		game.hud.detachChildren();
		game.hud.attachChild(game.scoreText);
		game.hud.attachChild(game.lifeText);
	}
	
	private void checkLife(){
		if (game.life < 0) {
			//	Toast.makeText(getApplicationContext(), "GAME OVER", Toast.LENGTH_SHORT);
		}
	}

//	private void createBullet(int power) {
//		Bullet b = new Bullet(150, 150, game.mBulletTextureRegionTiled, getVertexBufferObjectManager(),
//				game.mBulletTextureRegionTiled, game.mPhysicsWorld, game.scene, game.engineLock);
//
//		final Body body;
//		body = PhysicsFactory.createCircleBody(game.mPhysicsWorld, b, BodyType.DynamicBody, game.FIXTURE_DEF);
//
//		b.setbody(body);
//		//b.animate(200);		
//
//		// multiply with power
//		b.shoot = b.shoot.mul(power);
//		
//		body.setLinearVelocity(b.shoot);
//		body.setBullet(true);
//
//		game.scene.attachChild(b);
//		game.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(b, body, true, true));
//
//		game.scene.registerUpdateHandler(b);
//
//		game.BulletVector.add(b);
//		
//	}

	private void removeBullet(final AnimatedSprite bullet) {

		this.runOnUpdateThread(new Runnable() {

			@Override
			public void run() {
				final PhysicsConnector bulletPhysicsConnector = game.mPhysicsWorld.getPhysicsConnectorManager()
						.findPhysicsConnectorByShape(bullet);
				Game.engineLock.lock();
				game.mPhysicsWorld.unregisterPhysicsConnector(bulletPhysicsConnector);
				// game.mPhysicsWorld.destroyBody(bulletPhysicsConnector.getBody());

				game.scene.unregisterTouchArea(bullet);
				game.scene.detachChild(bullet);
				// System.gc();
				game.BulletVector.remove(bullet);
				Game.engineLock.unlock();
			}
		});

	}

	private void removePara(final AnimatedSprite para) {
		this.runOnUpdateThread(new Runnable() {
			// private PhysicsWorld game.mPhysicsWorld;

			@Override
			public void run() {
				Debug.log(DebugLevel.INFO, "removePara");
				final PhysicsConnector paraPhysicsConnector = game.mPhysicsWorld.getPhysicsConnectorManager()
						.findPhysicsConnectorByShape(para);
				game.mPhysicsWorld.unregisterPhysicsConnector(paraPhysicsConnector);
				game.scene.detachChild(para);
				Debug.log(DebugLevel.INFO, "Para Detached");
				// System.gc();
				// Debug.log(DebugLevel.INFO, "gc");
				game.ParaVector.remove(para);
			}
		});

	}

	private void addPara(int randx) {

		Random rand = new Random();
		int min = 250;
		int max = 600;
		rand.setSeed(randx * 1234);

		int randomNum = rand.nextInt(max - min + 1) + min;

		Para para = new Para(randomNum, -3, game.mParaTextureRegionTiled, this.getVertexBufferObjectManager());
		 para.animate(250);
		game.ParaVector.add(para);
		game.scene.attachChild(para);
		game.scene.registerUpdateHandler(para);		
	}

	
	
	private void createBurst(float x, float y) {
		
		// Get vector from touch and shootingposition
		Vector2 shoot = new Vector2((game.FIRE_POS_X - x), (game.FIRE_POS_Y - y));
		
		// Multiply to increase strenght
		shoot = shoot.nor().mul(-game.shotPower);
		
		Burst b = new Burst(game.FIRE_POS_X, game.FIRE_POS_Y, game.mBurstTextureRegionTiled, getVertexBufferObjectManager(),
				game.mBurstTextureRegionTiled, game.mPhysicsWorld, game.scene, game.engineLock);

		final Body body;
		body = PhysicsFactory.createCircleBody(game.mPhysicsWorld, b, BodyType.KinematicBody, game.FIXTURE_DEF);

		b.setbody(body);
		b.animate(200);		

		body.setLinearVelocity(shoot);
		body.setBullet(true);

		game.scene.attachChild(b);
		game.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(b, body, true, true));

		game.scene.registerUpdateHandler(b);

		game.BurstVector.add(b);
	}

	private void removeBurst(final AnimatedSprite burst) {

		this.runOnUpdateThread(new Runnable() {

			@Override
			public void run() {
				final PhysicsConnector burstPhysicsConnector = game.mPhysicsWorld.getPhysicsConnectorManager()
						.findPhysicsConnectorByShape(burst);
				game.engineLock.lock();
				game.mPhysicsWorld.unregisterPhysicsConnector(burstPhysicsConnector);
				// game.mPhysicsWorld.destroyBody(bulletPhysicsConnector.getBody());

				game.scene.unregisterTouchArea(burst);
				game.scene.detachChild(burst);
				// System.gc();
				game.BurstVector.remove(burst);
				game.engineLock.unlock();
			}
		});

	}
}


// Shoot from end of barrel
// private void shootProjectile(float angulo, final float pX, final float pY){
//
//
// int x2 = (int)
// (canon.getSceneCenterCoordinates()[0]+LENGTH_SPRITE*Math.cos(canon.getRotation()));
// int y2 = (int)
// (canon.getSceneCenterCoordinates()[1]+LENGTH_SPRITE*Math.sin(canon.getRotation()));
//
//
// final Sprite projectile;
// projectile = new Sprite( (float) x2, (float) y2,
// mProjectileTextureRegion,this.getVertexBufferObjectManager() );
//
// mMainScene.attachChild(projectile);