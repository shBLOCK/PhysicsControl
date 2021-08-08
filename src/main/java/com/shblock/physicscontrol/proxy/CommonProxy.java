package com.shblock.physicscontrol.proxy;

import com.shblock.physicscontrol.command.CommandSerializer;
import com.shblock.physicscontrol.command.EditOperations2D;
import com.shblock.physicscontrol.physics.BulletNativeHandler;

public class CommonProxy {
    public static void setup() {
        BulletNativeHandler.load();
        CommandSerializer.init();
        EditOperations2D.init();
    }
}
