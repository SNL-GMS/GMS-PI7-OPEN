package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects;

/**
 * Represents whether objects stored in the OSD should have a publicly accessible visibility (in
 * which case they are in general globally visible to users) or a private visibility (in which case
 * the objects are in general only visible to the user storing the object).
 */
public enum StorageVisibility {
  PUBLIC, PRIVATE
}
