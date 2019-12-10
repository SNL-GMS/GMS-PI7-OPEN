package gms.dataacquisition.seedlink.clientlibrary;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StreamInfo {
	public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private long beginTime, endTime;
	private String location, seedName, type;
	
	public StreamInfo(String name, String loc, String type, long b, long e){
		this.seedName = name;
		this.location = loc;
		this.type = type;
		this.beginTime = b;
		this.endTime = e;
	}
	
	public StreamInfo(){ this(null,null,null,-1,-1); }
	
	public long getBeginTime() { return beginTime; }
	public long getEndTime() { return endTime; }
	public String getLocation() { return location; }
	public String getSeedName() { return seedName; }
	public String getType() { return type; }
	public void setBeginTime(long beginTime) { this.beginTime = beginTime; }
	public void setEndTime(long endTime) { this.endTime = endTime; }
	public void setLocation(String location) { this.location = location; }
	public void setSeedName(String seedName) { this.seedName = seedName; }
	public void setType(String type) { this.type = type; }
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StreamInfo other = (StreamInfo) obj;
		if (beginTime != other.beginTime)
			return false;
		if (endTime != other.endTime)
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (seedName == null) {
			if (other.seedName != null)
				return false;
		} else if (!seedName.equals(other.seedName))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (beginTime ^ (beginTime >>> 32));
		result = prime * result + (int) (endTime ^ (endTime >>> 32));
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result
				+ ((seedName == null) ? 0 : seedName.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	
	@Override
	public String toString(){
		DateFormat df = new SimpleDateFormat(TIME_FORMAT);
		return new StringBuilder(getClass().getName())
				.append("[seedName=").append(seedName)
				.append(",location=").append(location)
				.append(",type=").append(type)
				.append(",beginTime=").append(df.format(new Date(beginTime)))
				.append(",endTime=").append(df.format(new Date(endTime)))
				.append("]").toString();
	}
}
