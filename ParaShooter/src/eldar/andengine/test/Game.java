package eldar.andengine.test;

import java.io.IOException;
import java.util.Vector;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.Engine;
import org.andengine.engine.Engine.EngineLock;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.debug.Debug;

import android.content.Context;

import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Game {

	public Game() {

	}
    
	// Physics world
	public final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	public PhysicsWorld mPhysicsWorld;

	public int mCameraWidth = 720;
	public int mCameraHeight = 480;
	public Scene scene;
	Sound mExplosionSound;
	Sound soundBurst;

	// Bullet
	public ITexture mTexture;
	public ITextureRegion mBulletTextureRegion;

	// Bullet Tiled
	public BitmapTextureAtlas mBitmapTextureAtlasBullet;
	public TiledTextureRegion mBulletTextureRegionTiled;
	public TiledTextureRegion mParaTextureRegionTiled;

	// Turret
	public BitmapTextureAtlas mBitmapTextureAtlas;
	public TextureRegion mTurretTextureRegion;

	// ParallaxBackground
	public BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	public ITextureRegion mParallaxLayerBack;
	public TextureRegion mParallaxLayerMid;

	// Para
	public TextureRegion mParaTextureRegion;

	// Burst
	public TiledTextureRegion mBurstTextureRegionTiled;

	// Bullet Array
	public Vector<Bullet> BulletVector = new Vector<Bullet>();
	public Vector<Para> ParaVector = new Vector<Para>();
	public Vector<Burst> BurstVector = new Vector<Burst>();

	public final int MAXBURST = 3;

	
	
	final int FIRE_POS_X = 37;
	final int FIRE_POS_Y = 460;
	int shotPower = 17;
	
	// The para
	Para para = null;

	// engine
	static EngineLock engineLock;

	// HUD
	HUD hud;

	// Score text
	Text scoreText;
	Text lifeText;
	
	public int score = 0;
	public int life = 5;
		
	// Camera
	Camera mCamera;

	// Font
	Font mFont;

	// Touch time in ms
	long mDownTimeMilliseconds;
	long upTimeMilliseconds;

	// Used to check how much to move angle of cannon
	int previousDown = 0;

	public void setupSoundResources(Engine mEngine, Context context) {
		SoundFactory.setAssetBasePath("mfx/");
		try {
			mExplosionSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), context, "explosion.ogg");
			soundBurst = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), context, "burst.ogg");
		} catch (final IOException e) {
			Debug.e(e);
		}
	}

}
