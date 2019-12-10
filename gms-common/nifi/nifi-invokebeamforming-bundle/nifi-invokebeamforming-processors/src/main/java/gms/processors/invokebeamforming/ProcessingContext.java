package gms.processors.invokebeamforming;

public class ProcessingContext {

  private final ProcessingStepReference processingStepReference;
  private final String storageVisibility;

  public ProcessingContext(ProcessingStepReference processingStepReference) {
    this.processingStepReference = processingStepReference;
    this.storageVisibility = "PUBLIC";
  }

  public ProcessingStepReference getProcessingStepReference() {
    return processingStepReference;
  }

  public String getStorageVisibility() {
    return storageVisibility;
  }

  @Override
  public String toString() {
    return "ProcessingContext{" +
        "processingStepReference=" + processingStepReference +
        ", storageVisibility='" + storageVisibility + '\'' +
        '}';
  }
}

