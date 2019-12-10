package gms.dataacquisition.stationreceiver.cd11.dataman;


enum MessageType {
  NewFrameReceived,
  PersistGapState,
  RemoveExpiredGaps,
  SendAcknack,
  Shutdown
}
