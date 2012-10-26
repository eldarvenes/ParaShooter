package eldar.andengine.test;

import org.andengine.engine.Engine.EngineLock;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;

class Bullet extends AnimatedSprite {

	public Bullet(final float pX, final float pY, final ITiledTextureRegion pTextureRegion,
			final VertexBufferObjectManager pVertexBufferObjectManager, TiledTextureRegion texture, PhysicsWorld physicsW,
			Scene scene, EngineLock engineL) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);

		_texture = texture;
		_physicsW = physicsW;
		_scene = scene;
		_engineL = engineL;
	}

	public void setbody(Body _body) {
		body = _body;

	}

	private TiledTextureRegion _texture;
	private PhysicsWorld _physicsW;
	private Scene _scene;
	private EngineLock _engineL;
	private Body body;
	public Vector2 shoot = new Vector2(15, -2);
	
	@Override
	protected void onManagedUpdate(final float pSecondsElapsed) {
		super.onManagedUpdate(pSecondsElapsed);

	}
}