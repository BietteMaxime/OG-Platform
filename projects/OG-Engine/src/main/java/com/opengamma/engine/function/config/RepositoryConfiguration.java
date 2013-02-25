// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.engine.function.config;
public class RepositoryConfiguration implements java.io.Serializable {
  private static final long serialVersionUID = -2595680220l;
  private java.util.List<com.opengamma.engine.function.config.FunctionConfiguration> _functions;
  public static final String FUNCTIONS_KEY = "functions";
  public RepositoryConfiguration () {
  }
  protected RepositoryConfiguration (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (FUNCTIONS_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<com.opengamma.engine.function.config.FunctionConfiguration> fudge1;
      fudge1 = new java.util.ArrayList<com.opengamma.engine.function.config.FunctionConfiguration> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          final com.opengamma.engine.function.config.FunctionConfiguration fudge3;
          fudge3 = com.opengamma.engine.function.config.FunctionConfiguration.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge2));
          fudge1.add (fudge3);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a RepositoryConfiguration - field 'functions' is not FunctionConfiguration message", e);
        }
      }
      setFunctions (fudge1);
    }
  }
  public RepositoryConfiguration (java.util.Collection<? extends com.opengamma.engine.function.config.FunctionConfiguration> functions) {
    if (functions == null) _functions = null;
    else {
      final java.util.List<com.opengamma.engine.function.config.FunctionConfiguration> fudge0 = new java.util.ArrayList<com.opengamma.engine.function.config.FunctionConfiguration> (functions);
      for (java.util.ListIterator<com.opengamma.engine.function.config.FunctionConfiguration> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.function.config.FunctionConfiguration fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'functions' cannot be null");
        fudge1.set (fudge2);
      }
      _functions = fudge0;
    }
  }
  protected RepositoryConfiguration (final RepositoryConfiguration source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._functions == null) _functions = null;
    else {
      final java.util.List<com.opengamma.engine.function.config.FunctionConfiguration> fudge0 = new java.util.ArrayList<com.opengamma.engine.function.config.FunctionConfiguration> (source._functions);
      for (java.util.ListIterator<com.opengamma.engine.function.config.FunctionConfiguration> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.function.config.FunctionConfiguration fudge2 = fudge1.next ();
        fudge1.set (fudge2);
      }
      _functions = fudge0;
    }
  }
  public RepositoryConfiguration clone () {
    return new RepositoryConfiguration (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_functions != null)  {
      for (com.opengamma.engine.function.config.FunctionConfiguration fudge1 : _functions) {
        final org.fudgemsg.MutableFudgeMsg fudge2 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), fudge1.getClass (), com.opengamma.engine.function.config.FunctionConfiguration.class);
        fudge1.toFudgeMsg (serializer, fudge2);
        msg.add (FUNCTIONS_KEY, null, fudge2);
      }
    }
  }
  public static RepositoryConfiguration fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.function.config.RepositoryConfiguration".equals (className)) break;
      try {
        return (com.opengamma.engine.function.config.RepositoryConfiguration)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new RepositoryConfiguration (deserializer, fudgeMsg);
  }
  public java.util.List<com.opengamma.engine.function.config.FunctionConfiguration> getFunctions () {
    if (_functions != null) {
      return java.util.Collections.unmodifiableList (_functions);
    }
    else return null;
  }
  public void setFunctions (com.opengamma.engine.function.config.FunctionConfiguration functions) {
    if (functions == null) _functions = null;
    else {
      _functions = new java.util.ArrayList<com.opengamma.engine.function.config.FunctionConfiguration> (1);
      addFunctions (functions);
    }
  }
  public void setFunctions (java.util.Collection<? extends com.opengamma.engine.function.config.FunctionConfiguration> functions) {
    if (functions == null) _functions = null;
    else {
      final java.util.List<com.opengamma.engine.function.config.FunctionConfiguration> fudge0 = new java.util.ArrayList<com.opengamma.engine.function.config.FunctionConfiguration> (functions);
      for (java.util.ListIterator<com.opengamma.engine.function.config.FunctionConfiguration> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.function.config.FunctionConfiguration fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'functions' cannot be null");
        fudge1.set (fudge2);
      }
      _functions = fudge0;
    }
  }
  public void addFunctions (com.opengamma.engine.function.config.FunctionConfiguration functions) {
    if (functions == null) throw new NullPointerException ("'functions' cannot be null");
    if (_functions == null) _functions = new java.util.ArrayList<com.opengamma.engine.function.config.FunctionConfiguration> ();
    _functions.add (functions);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
