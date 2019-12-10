package gms.dataacquisition.seedlink.clientlibrary;

import gms.dataacquisition.seedlink.clientlibrary.blockettes.Beam;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.BeamDelay;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.CalibrationAbort;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.DataExtension;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.DataOnly;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.GenericCalibration;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.GenericEventDetection;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.MurdockEventDetection;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.PseudoRandomCalibration;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.SampleRate;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.SineCalibration;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.StepCalibration;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.Timing;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class Blockette {
	private final Type type;
	
	protected Blockette(Type t){ type = t; }
	
	public Type getType(){ return type; }
	
	public abstract short getNextBlocketteOffset();
	
	public abstract void read(DataInputStream d)
			throws IOException, PacketException;
	
	public void readAscii(String s) throws PacketException{
		try(ByteArrayOutputStream o = new ByteArrayOutputStream()){
			o.write(s.getBytes(StandardCharsets.US_ASCII));
			read(new DataInputStream(new ByteArrayInputStream(
					o.toByteArray())));
		} catch (IOException e){
			//This will never happen because BAOS doesn't throw exceptions:
			throw new PacketException(e);
		}
	}
	
	@Override
	public abstract String toString();
	
	public static enum Type{
		SAMPLE_RATE(SampleRate.TYPE){
			@Override
			public SampleRate create() { return new SampleRate(); }
		},
		
		GENERIC_EVENT_DETECTION(GenericEventDetection.TYPE){
			@Override
			public GenericEventDetection create(){
				return new GenericEventDetection(); }
		},
		
		MURDOCK_EVENT_DETECTION(MurdockEventDetection.TYPE){
			@Override
			public MurdockEventDetection create(){
				return new MurdockEventDetection(); }
		},
		
		LOG_Z_EVENT_DETECTION(202){
			@Override
			public Blockette create() {
				throw new UnsupportedOperationException(
						"Log-Z Event Detection Blockette layout is not "+
						"yet supported");
			}
		},
		
		STEP_CALIBRATION(StepCalibration.TYPE){
			@Override
			public StepCalibration create() { return new StepCalibration(); }
		},
		
		SINE_CALIBRATION(SineCalibration.TYPE){
			@Override
			public SineCalibration create() { return new SineCalibration(); }
		},
		
		PSEUDO_RANDOM_CALIBRATION(PseudoRandomCalibration.TYPE){
			@Override
			public PseudoRandomCalibration create(){
				return new PseudoRandomCalibration();
			}
		},
		
		GENERIC_CALIBRATION(GenericCalibration.TYPE){
			@Override
			public GenericCalibration create(){
				return new GenericCalibration();
			}
		},
		
		CALIBRATION_ABORT(CalibrationAbort.TYPE){
			@Override
			public CalibrationAbort create(){ return new CalibrationAbort(); }
		},
		
		BEAM(Beam.TYPE){
			@Override
			public Beam create(){ return new Beam(); }
		},
		
		BEAM_DELAY(BeamDelay.TYPE){
			@Override
			public BeamDelay create(){ return new BeamDelay(); }
		},
		
		TIMING(Timing.TYPE){
			@Override
			public Timing create(){ return new Timing(); }
		},
		
		DATA_ONLY(DataOnly.TYPE) {
			@Override
			public DataOnly create() { return new DataOnly(); }
		},
		
		DATA_EXTENSION(DataExtension.TYPE) {
			@Override
			public DataExtension create() { return new DataExtension(); }
		},
		;
		
		private final int id;

		Type(int i){ id = i; }
		
		public int getId(){ return id; }
		
		public abstract Blockette create();
		
		public static Type valueOf(int id)
		throws UnknownBlocketteTypeException{
			if(SAMPLE_RATE.getId() == id) return SAMPLE_RATE;
			if(GENERIC_EVENT_DETECTION.getId() == id)
				return GENERIC_EVENT_DETECTION;
			if(MURDOCK_EVENT_DETECTION.getId() == id)
				return MURDOCK_EVENT_DETECTION;
			if(LOG_Z_EVENT_DETECTION.getId() == id)
				return LOG_Z_EVENT_DETECTION;
			if(STEP_CALIBRATION.getId() == id) return STEP_CALIBRATION;
			if(SINE_CALIBRATION.getId() == id) return SINE_CALIBRATION;
			if(PSEUDO_RANDOM_CALIBRATION.getId() == id)
				return PSEUDO_RANDOM_CALIBRATION;
			if(GENERIC_CALIBRATION.getId() == id) return GENERIC_CALIBRATION;
			if(CALIBRATION_ABORT.getId() == id) return CALIBRATION_ABORT;
			if(BEAM.getId() == id) return BEAM;
			if(BEAM_DELAY.getId() == id) return BEAM_DELAY;
			if(TIMING.getId() == id) return TIMING;
			if(DATA_ONLY.getId() == id) return DATA_ONLY;
			if(DATA_EXTENSION.getId() == id) return DATA_EXTENSION;
			
			throw new UnknownBlocketteTypeException(id);
		}
	}
	
	public static class UnknownBlocketteTypeException extends PacketException{
		private static final long serialVersionUID = 1L;
		private int type;
		
		public UnknownBlocketteTypeException(int t){
			super("unknown type id \""+t+"\"");
			this.type = t;
		}
		
		public int getTypeId(){ return type; }
	}
}
