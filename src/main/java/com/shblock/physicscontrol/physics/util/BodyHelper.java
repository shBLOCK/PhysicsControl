package com.shblock.physicscontrol.physics.util;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;

import java.util.function.Consumer;

public class BodyHelper {
    public static void forEachFixture(Body body, Consumer<Fixture> consumer) {
        Fixture fixture = body.getFixtureList();
        while (fixture != null) {
            consumer.accept(fixture);
            fixture = fixture.m_next;
        }
    }
}
