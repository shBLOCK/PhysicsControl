package com.shblock.physicscontrol.motionsensor;

import com.shblock.physicscontrol.PhysicsControl;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MotionSensorListeningThread extends Thread {
  private final DatagramSocket socket;

  private boolean closed = false;

  public MotionSensorListeningThread(DatagramSocket socket) {
    super("Motion sensor listening thread");
    this.socket = socket;
  }

  @Override
  public void run() {
    while (!closed) {
      byte[] bytes = new byte[1024];
      DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
      try {
        socket.receive(packet);
      } catch (IOException e) {
        PhysicsControl.log(Level.WARN, "Sensor data receive failed:");
        e.printStackTrace();
        continue;
      }
      String data = new String(bytes, 0, packet.getLength());
      if (closed) break;
      MotionSensorHandler.handleData(data);
    }
  }

  public void close() {
    closed = true;
  }
}
