package com.shblock.physicscontrol.client.gui.PhysicsSimulator;

import com.shblock.physicscontrol.physics.user_obj.BodyUserObj;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.contacts.Contact;

public class GuiSimulatorContactListener implements ContactListener {
    private boolean shouldPlaySound = false;

    private static final GuiSimulatorContactListener INSTANCE = new GuiSimulatorContactListener();

    private GuiSimulatorContactListener() {}

    public static GuiSimulatorContactListener getInstance() {
        return INSTANCE;
    }

    @Override
    public void beginContact(Contact contact) {
        this.shouldPlaySound = true;
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    private float getScreenScale() {
        GuiPhysicsSimulator gui = GuiPhysicsSimulator.tryGetInstance();
        if (gui == null) {
            return 1F;
        }
        return gui.getGlobalScale();
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        if (this.shouldPlaySound) {
            this.shouldPlaySound = false;

            float volume = 0F;
            for (float force : impulse.normalImpulses) {
                volume += force;
            }
            volume *= getScreenScale();
            volume /= 2000F;

            Body bodyA = contact.getFixtureA().getBody();
            Body bodyB = contact.getFixtureB().getBody();
            if (bodyA.getUserData() instanceof BodyUserObj && bodyB.getUserData() instanceof BodyUserObj) {
                ((BodyUserObj) bodyA.getUserData()).playCollideSoundUI(volume);
                ((BodyUserObj) bodyB.getUserData()).playCollideSoundUI(volume);
            }
        }
    }
}
