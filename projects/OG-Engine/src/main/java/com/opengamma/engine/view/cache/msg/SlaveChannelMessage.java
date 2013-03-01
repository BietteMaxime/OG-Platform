// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.engine.view.cache.msg;
public class SlaveChannelMessage extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitSlaveChannelMessage (this); }
  private static final long serialVersionUID = 1l;
  public SlaveChannelMessage () {
  }
  protected SlaveChannelMessage (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
  }
  public SlaveChannelMessage (Long correlationId) {
    super (correlationId);
  }
  protected SlaveChannelMessage (final SlaveChannelMessage source) {
    super (source);
  }
  public SlaveChannelMessage clone () {
    return new SlaveChannelMessage (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
  }
  public static SlaveChannelMessage fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.SlaveChannelMessage".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.SlaveChannelMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new SlaveChannelMessage (deserializer, fudgeMsg);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
