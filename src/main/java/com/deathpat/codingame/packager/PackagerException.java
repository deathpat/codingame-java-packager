package com.deathpat.codingame.packager;

/**
 * 
 * @author Patrice Meneguzzi
 *
 */
public class PackagerException extends Exception {
  private static final long serialVersionUID = -6056062704151341385L;

  public PackagerException(String message) {
    super(message);
  }

  public PackagerException(String message, Throwable ex) {
    super(message, ex);
  }
}
