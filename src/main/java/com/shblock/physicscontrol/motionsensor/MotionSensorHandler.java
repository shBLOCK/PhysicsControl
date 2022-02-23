package com.shblock.physicscontrol.motionsensor;

import com.shblock.physicscontrol.PhysicsControl;
import org.apache.logging.log4j.Level;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

public class MotionSensorHandler {
  private static final HashMap<String, MotionSensorInstance> sensors = new HashMap<>();

  private static MotionSensorListeningThread listener;

  public static void init() throws SocketException {
    DatagramSocket socket = new DatagramSocket(8560);

    if (listener != null && listener.isAlive()) {
      listener.close();
    }

    listener = new MotionSensorListeningThread(socket);
    listener.start();
  }

  public static MotionSensorInstance addSensorInstance(String deviceId) {
    MotionSensorInstance sensor = new MotionSensorInstance(deviceId);
    sensors.put(deviceId, sensor);
    return sensor;
  }

  public static void removeSensorInstance(String deviceId) {
    sensors.remove(deviceId);
  }

  public static void handleData(String data) {
    String deviceId;
    try {
      deviceId = data.substring(0, 12);
    } catch (StringIndexOutOfBoundsException e) {
      PhysicsControl.log(Level.WARN, "Sensor device id decoding failed, could be a network issue:");
      e.printStackTrace();
      return;
    }
    MotionSensorInstance sensor = sensors.get(deviceId);
    if (sensor == null) {
//      PhysicsControl.log(Level.WARN, "Received sensor data from unknown device: " + deviceId);
      return;
    }
//    System.out.println(data.substring(14, data.length() - 2));
    sensor.handleData(data.substring(14, data.length() - 2));
  }
}
