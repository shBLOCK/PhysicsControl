package com.shblock.physicscontrol.motionsensor;

import codechicken.lib.vec.Vector3;
import com.shblock.physicscontrol.PhysicsControl;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;

public class MotionSensorInstance {
  public final String deviceId;

  public double accX = 0;
  public double accY = 0;
  public double accZ = 0;
  public double angSpdX = 0;
  public double angSpdY = 0;
  public double angSpdZ = 0;
  public double angleX = 0;
  public double angleY = 0;
  public double angleZ = 0;
  public double magX = 0;
  public double magY = 0;
  public double magZ = 0;
  public double temp = Double.NaN;
  public double power = Double.NaN;
  public int signal = -1;
  public String version = null;
  public int error = -1;

  public long lastUpdateTime = -1;
  public double spdX = 0;
  public double spdY = 0;
  public double spdZ = 0;
//  public double angSpdX = 0;
//  public double angSpdY = 0;
//  public double angSpdZ = 0;

  private static int OLD_SPD_LIST_SIZE = 25;
  private final ArrayList<Double> oldSpeeds = new ArrayList<>();

  public MotionSensorInstance(String deviceId) {
    this.deviceId = deviceId;
  }

  /**
   *
   * @param data the string data sent from the sensor but without the sensor id
   * @return if the handle was successful
   */
  public boolean handleData(String data) {
    String[] segments = data.split(",");
    if (segments.length != 17) {
      PhysicsControl.log(Level.WARN, "Sensor data segments length invalid: " + segments.length + ", might be network issues.");
      return false;
    }

    try {
      double lastAccX = accX;
      double lastAccY = accY;
      double lastAccZ = accZ;
//      double lastAngAccX = angAccX;
//      double lastAngAccY = angAccY;
//      double lastAngAccZ = angAccZ;
      accX = Double.parseDouble(segments[0]);
      accY = Double.parseDouble(segments[1]);
      accZ = Double.parseDouble(segments[2]);
      angSpdX = Double.parseDouble(segments[3]);
      angSpdY = Double.parseDouble(segments[4]);
      angSpdZ = Double.parseDouble(segments[5]);
      angleX = Double.parseDouble(segments[6]);
      angleY = Double.parseDouble(segments[7]);
      angleZ = Double.parseDouble(segments[8]);
      magX = Double.parseDouble(segments[9]);
      magY = Double.parseDouble(segments[10]);
      magZ = Double.parseDouble(segments[11]);
      temp = Double.parseDouble(segments[12]);
      power = Double.parseDouble(segments[13]);
      signal = Integer.parseInt(segments[14]);
      version = segments[15];
      error = Integer.parseInt(segments[16]);
      if (lastUpdateTime == -1) {
        lastUpdateTime = System.nanoTime();
        spdX = 0;
        spdY = 0;
        spdZ = 0;
//        angSpdX = 0;
//        angSpdY = 0;
//        angSpdZ = 0;
      } else {
        long currentTime = System.nanoTime();
        long dt = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;
        double time = ((double) dt) / 1E6;

        Vector3 accVec = new Vector3(lastAccX, lastAccY, lastAccZ);
        accVec = accVec.rotate(Math.toRadians(angleX), Vector3.X_POS);
        accVec = accVec.rotate(Math.toRadians(angleY), Vector3.Y_POS);
        accVec = accVec.rotate(Math.toRadians(angleZ), Vector3.Z_POS);
        System.out.println(accVec);
//        accVec = accVec.rotate(Quat.aroundAxis(Math.toRadians(angleX), Math.toRadians(angleY), Math.toRadians(angleZ), -Math.PI * 2));
        accVec = accVec.subtract(0, 0, 1);

        double tempSpdX = spdX + accVec.x * time;
        double tempSpdY = spdY + accVec.y * time;
        double tempSpdZ = spdZ + accVec.z * time;
        oldSpeeds.add(tempSpdX + tempSpdY + tempSpdZ);
        if (oldSpeeds.size() > OLD_SPD_LIST_SIZE)
          oldSpeeds.remove(0);

        if (!isSpdStable()) {
//          if (Math.abs(accVec.x) > .1)
            spdX += accVec.x * time;
//          if (Math.abs(accVec.x) > .1)
            spdY += accVec.y * time;
//          if (Math.abs(accVec.x) > .1)
            spdZ += accVec.z * time;
        } else {
          spdX = 0;
          spdY = 0;
          spdZ = 0;
        }

//        angleX += lastAngAccX * time;
//        angleY += lastAngAccY * time;
//        angleZ += lastAngAccZ * time;
      }
      return true;
    } catch (NullPointerException | NumberFormatException e) {
      PhysicsControl.log(Level.WARN, "Sensor data processing failed, could be a network problem:");
      e.printStackTrace();
    }
    return false;
  }

  private boolean isSpdStable() {
    if (oldSpeeds.size() != OLD_SPD_LIST_SIZE)
      return false;

    double avg = oldSpeeds.stream().mapToDouble(s -> s).average().getAsDouble();
    double squareSum = oldSpeeds.stream().mapToDouble(s -> s - avg).map(s -> s * s).sum();
    System.out.println(squareSum / OLD_SPD_LIST_SIZE);
    return (squareSum / OLD_SPD_LIST_SIZE) < 1;
  }
  
  public void resetSpeed() {
    lastUpdateTime = -1;
  }

  @Override
  public String toString() {
    return "MotionSensorInstance{" +
        "deviceId='" + deviceId + '\'' +
        ", accX=" + accX +
        ", accY=" + accY +
        ", accZ=" + accZ +
        ", angSpdX=" + angSpdX +
        ", angSpdY=" + angSpdY +
        ", angSpdZ=" + angSpdZ +
        ", angleX=" + angleX +
        ", angleY=" + angleY +
        ", angleZ=" + angleZ +
        ", magX=" + magX +
        ", magY=" + magY +
        ", magZ=" + magZ +
        ", temp=" + temp +
        ", power=" + power +
        ", signal=" + signal +
        ", version='" + version + '\'' +
        ", error=" + error +
        ", lastUpdateTime=" + lastUpdateTime +
        ", spdX=" + spdX +
        ", spdY=" + spdY +
        ", spdZ=" + spdZ +
        ", oldSpeeds=" + oldSpeeds +
        '}';
  }
}
