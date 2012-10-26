package eldar.andengine.test;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

class Para extends AnimatedSprite {
	private final PhysicsHandler mPhysicsHandler;

	public Para(final float pX, final float pY, final ITiledTextureRegion pTextureRegion,
			final VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);

		this.mPhysicsHandler = new PhysicsHandler(this);
		this.registerUpdateHandler(this.mPhysicsHandler);

		this.mPhysicsHandler.setVelocityX(-2);
		this.mPhysicsHandler.setVelocityY(15);
		
	}

	@Override
	protected void onManagedUpdate(final float pSecondsElapsed) {
		super.onManagedUpdate(pSecondsElapsed);

	}
}