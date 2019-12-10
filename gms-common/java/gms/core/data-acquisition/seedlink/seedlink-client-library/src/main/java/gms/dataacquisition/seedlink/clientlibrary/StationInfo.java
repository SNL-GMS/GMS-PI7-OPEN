package gms.dataacquisition.seedlink.clientlibrary;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class StationInfo {
	private int beginSeq, endSeq;
	private String desc, name, network;
	private List<StreamInfo> streams;
	
	public StationInfo(String name, String net, String desc, int b, int e,
			Iterable<StreamInfo> streams){
		this.beginSeq = b;
		this.endSeq = e;
		this.desc = desc;
		this.network = net;
		this.name = name;
		this.streams = new LinkedList<>();
		streams.iterator().forEachRemaining(this.streams::add);
	}
	
	public StationInfo(){ this(null,null,null,-1,-1,Collections.emptyList()); }
	
	public int getBeginSeq() { return beginSeq; }
	public String getDescription() { return desc; }
	public int getEndSeq() { return endSeq; }
	public String getName() { return name; }
	public String getNetwork() { return network; }
	public List<StreamInfo> getStreams(){ return streams; }
	public void setBeginSeq(int beginSeq) { this.beginSeq = beginSeq; }
	public void setDescription(String desc) { this.desc = desc; }
	public void setEndSeq(int endSeq) { this.endSeq = endSeq; }
	public void setName(String name) { this.name = name; }
	public void setNetwork(String network) { this.network = network; }
	public void setStreams(Iterable<StreamInfo> streams){
		this.streams = new LinkedList<>();
		streams.forEach(this.streams::add);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StationInfo other = (StationInfo) obj;
		if (beginSeq != other.beginSeq)
			return false;
		if (desc == null) {
			if (other.desc != null)
				return false;
		} else if (!desc.equals(other.desc))
			return false;
		if (endSeq != other.endSeq)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (network == null) {
			if (other.network != null)
				return false;
		} else if (!network.equals(other.network))
			return false;
		if (streams == null) {
			if (other.streams != null)
				return false;
		} else if (!streams.equals(other.streams))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + beginSeq;
		result = prime * result + ((desc == null) ? 0 : desc.hashCode());
		result = prime * result + endSeq;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((network == null) ? 0 : network.hashCode());
		result = prime * result + ((streams == null) ? 0 : streams.hashCode());
		return result;
	}
	
	@Override
	public String toString(){
		return new StringBuilder(getClass().getName())
				.append("[name=").append(getName())
				.append(",network=").append(getNetwork())
				.append(",description=").append(getDescription())
				.append(",beginSec=").append(Integer.toHexString(beginSeq))
				.append(",endSec=").append(Integer.toHexString(endSeq))
				.append("]").toString();
	}
	
	public boolean isSeismic(){
		for(StreamInfo s : getStreams()){
			String name = s.getSeedName();
			if(name.length() == 3 && name.charAt(1) == 'H')
				return true;
		}
		return false;
	}
	
	public boolean isInfrasound(){
		for(StreamInfo s : getStreams()){
			String name = s.getSeedName();
			if(name.length() == 3 && name.charAt(1) == 'D' &&
					name.charAt(2) == 'F')
				return true;
		}
		return false;
	}
	
	public boolean isSeismic3C(){
		boolean n = false;
		boolean e = false;
		boolean z = false;
		
		for(StreamInfo s : getStreams()){
			if(s.getSeedName().length() == 3){
				if(s.getSeedName().endsWith("N")) n = true;
				else if(s.getSeedName().endsWith("E")) e = true;
				else if(s.getSeedName().endsWith("Z")) z = true;
			}
		}
		
		return n && e && z;
	}
}
