package gms.core.signaldetection.qccontrol;

import gms.shared.frameworks.common.annotations.Control;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Control("waveform-qc-control")
@Path("/waveform-qc/waveform-qc-control")
public interface QcControlInterface {

  /**
   * Invocation request, primary means of invoking processing.
   * Accepts a body representing the expected input to the invocation.
   *
   * @return {@link QcMaskVersionDescriptor} objects created from execution.
   */
  @Path("/invoke")
  @POST
  @Operation(description = "Performs QC masking on the segments requested")
  List<QcMaskVersionDescriptor> executeAutomatic(@RequestBody(
      description = "The channel segment descriptor", required = true)
      ChannelSegmentDescriptor segmentDescriptor);
}
