package gms.dataacquisition.seedlink.clientlibrary.data;

import gms.dataacquisition.seedlink.clientlibrary.data.formats.Ascii;
import gms.dataacquisition.seedlink.clientlibrary.data.formats.IEEEDouble;
import gms.dataacquisition.seedlink.clientlibrary.data.formats.IEEEFloat;
import gms.dataacquisition.seedlink.clientlibrary.data.formats.Int16;
import gms.dataacquisition.seedlink.clientlibrary.data.formats.Int24;
import gms.dataacquisition.seedlink.clientlibrary.data.formats.Int32;
import gms.dataacquisition.seedlink.clientlibrary.data.formats.Steim1;
import gms.dataacquisition.seedlink.clientlibrary.data.formats.Steim2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Formats {
	private static Map<Integer,Format> map = new TreeMap<>();
	static{
		Arrays.asList(new Format[]{ new Ascii(), new Int16(), new Int24(),
				new Int32(), new IEEEFloat(), new IEEEDouble(),
				new Steim1(), new Steim2()})
		.stream().forEach(fmt -> map.put(fmt.code(), fmt));
	}
	
	public static Format getFormat(int code){
		Format f = map.get(code);
		if(f == null) throw new UnsupportedOperationException(
				"unknown format: "+code);
		return f;
	}
	
	public static String name(int code){
		Format f = map.get(code);
		if(f != null) return f.name();
		return ""+code;
	}
	
	public static List<Format> getSupportedFormats(){
		return new ArrayList<>(map.values());
	}
}
