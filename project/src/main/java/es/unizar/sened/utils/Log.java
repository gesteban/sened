package es.unizar.sened.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Simple static logging class.
 * 
 * @author gesteban@unizar.es
 */
public final class Log {

  public static final String TAG = Log.class.getSimpleName();

  public enum LogBehaviour {
    DEBUG, INFO, WARNINGS, FILE;
  }

  private static LogBehaviour behaviour;
  private static FileWriter fstream;
  private static BufferedWriter out;

  static {
    setLogBehaviour(LogBehaviour.DEBUG);
  }

  public static void setLogBehaviour(LogBehaviour thisBehaviour) {
    behaviour = thisBehaviour;
    if (behaviour == LogBehaviour.FILE && out == null) {
      try {
        fstream = new FileWriter("log.txt");
        out = new BufferedWriter(fstream);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public static void closeStream() {
    try {
      fstream.close();
    } catch (IOException ex) {
      Log.e(TAG, "IOException, no se pudo cerrar el fichero");
      ex.printStackTrace();
    }
  }

  public static void w(String tag, String message) {
    try {
      switch (behaviour) {
      case DEBUG:
      case INFO:
      case WARNINGS:
        System.out.println("WARN  [" + tag + "] " + message);
        break;
      case FILE:
        out.write("WARN  [" + tag + "] " + message + "\n");
      }
    } catch (IOException ex) {
      Log.e(TAG, "IOException, no se pudo escribir en fichero");
      behaviour = LogBehaviour.DEBUG;
      ex.printStackTrace();
    }
  }

  public static void i(String tag, String message) {
    try {
      switch (behaviour) {
      case DEBUG:
      case INFO:
        System.out.println("INFO  [" + tag + "] " + message);
        break;
      case FILE:
        out.write("INFO [" + tag + "] " + message + "\n");
      default:
      }
    } catch (IOException ex) {
      Log.e(TAG, "IOException, no se pudo escribir en fichero");
      behaviour = LogBehaviour.DEBUG;
      ex.printStackTrace();
    }
  }

  public static void d(String tag, String message) {
    try {
      switch (behaviour) {
      case DEBUG:
        System.out.println("DEBUG [" + tag + "] " + message);
        break;
      case FILE:
        out.write("DEBUG [" + tag + "] " + message + "\n");
      default:
      }
    } catch (IOException ex) {
      Log.e(TAG, "IOException, no se pudo escribir en fichero");
      behaviour = LogBehaviour.DEBUG;
      ex.printStackTrace();
    }
  }

  public static void e(String tag, String message) {
    try {
      switch (behaviour) {
      case FILE:
        out.write("ERROR [" + tag + "] " + message + "\n");
      default:
        System.err.println("ERROR [" + tag + "] " + message);
      }
    } catch (IOException ex) {
      Log.e(TAG, "IOException, no se pudo escribir en fichero");
      behaviour = LogBehaviour.DEBUG;
      ex.printStackTrace();
    }
  }

}
