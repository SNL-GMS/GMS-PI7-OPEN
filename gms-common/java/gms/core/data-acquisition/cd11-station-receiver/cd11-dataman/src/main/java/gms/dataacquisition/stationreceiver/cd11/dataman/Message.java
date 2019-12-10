package gms.dataacquisition.stationreceiver.cd11.dataman;


import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;

class Message {

  final MessageType messageType;
  final Cd11Frame cd11Frame;

  Message(MessageType messageType) {
    this(messageType, null);
  }

  Message(MessageType messageType, Cd11Frame cd11Frame) {
    this.messageType = messageType;
    this.cd11Frame = cd11Frame;
  }

  MessageType getMessageType() {
    return this.messageType;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Message) {
      Message mt = (Message) obj;
      return (this.messageType == mt.getMessageType());
    } else {
      return false;
    }
  }
}
