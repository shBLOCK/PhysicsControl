package com.shblock.physicscontrol.physics.command;

public interface ICommand {
    void execute();
    void undo();
    void combine();
}
