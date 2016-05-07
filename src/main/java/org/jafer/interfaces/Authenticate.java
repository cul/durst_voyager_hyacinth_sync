package org.jafer.interfaces;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
import org.jafer.conf.CompressedXMLDecoder;

public abstract class Authenticate implements Serializable {
  abstract public boolean authenticate(String username, String groupname, String password, String clientIp);

  public static Authenticate load(URL config) {
  try {
    CompressedXMLDecoder decoder = new CompressedXMLDecoder(config.openStream());
    return (Authenticate)decoder.readObject();
  } catch (Exception ex) {
    ex.printStackTrace();
    return null;
  }
  }
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
  }
  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
  }
}