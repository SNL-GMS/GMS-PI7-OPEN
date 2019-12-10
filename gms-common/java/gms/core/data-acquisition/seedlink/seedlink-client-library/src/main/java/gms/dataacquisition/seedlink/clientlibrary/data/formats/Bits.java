package gms.dataacquisition.seedlink.clientlibrary.data.formats;

import java.util.Arrays;

/**
 * Useful for decompressing byte[]'s
 * @author bjlawry
 */
public class Bits {
	private byte[] bits;
	private int off, len;
	
	private Bits(byte[] b, int offset, int length){
		bits = b; 
		off = offset;
		len = length;
	}
	
	public byte[] toByteArray(){return Arrays.copyOfRange(bits, off, off+len);}
	
	public int count(){ return len*8; }
	
	public void set(int i, boolean v){
		int idx = off+(i/8);
		int pos = 7-i%8;
		if(v) bits[idx] = (byte)(bits[idx] | (1 << pos));
		else bits[idx] = (byte)(bits[idx] & ~(1 << pos));
	}
	
	public int one(int i){
		return (bits[off+(i/8)] >> (7-i%8)) & 1;
	}
	
	//TODO these could probably be made faster:
	
	public int two(int i){ return (one(i) << 1) | one(i+1); }
	
	public int four(int i){ return (two(i) << 2) | two(i+2); }
	
	public int eight(int i){ return (four(i) << 4) | four(i+4); }
	
	public int sixteen(int i, boolean swapBytes){
		return swapBytes ? (eight(i+8) << 8) | eight(i) :
			(eight(i) << 8) | eight(i+8);
	}
	
	public int thirtyTwo(int i, boolean swapBytes){
		return swapBytes ? (eight(i+24) << 24) | eight(i+16) << 16 |
				(eight(i+8) << 8) | eight(i) :
			(eight(i) << 24) | eight(i+8) << 16 |
			(eight(i+16) << 8) | eight(i+24);
	}
	
	public long thirtyTwoL(int i, boolean swapBytes){
		return Integer.toUnsignedLong(thirtyTwo(i,swapBytes));
	}
	
	public int asInt(int index, int numBits){
		if(numBits < 1 || numBits > 32) throw new IllegalArgumentException(
				"numBits must be between 1 and 32 but was "+numBits);
		
		int bits = 0;
		for(int i = 0; i < numBits; i++)
			bits |= (one(index+i) << ((numBits-1)-i));
		return bits;
	}
	
	//TODO end slow methods.
	
	public String toBitString(int fromIndexInclusive, int toIndexExclusive){
		StringBuilder sb = new StringBuilder();
		for(int i = fromIndexInclusive; i < toIndexExclusive; i++)
			sb.append(one(i));
		return sb.toString();
	}
	
	public String toBitString(){ return toBitString(0,count()); }
	
	@Override
	public String toString(){
		return new StringBuilder(getClass().getName())
				.append('[')
				.append(toBitString())
				.append(']').toString();
	}
	
	public static Bits of(int b){
		return of(new byte[]{
				(byte)((b & 0xFF000000) >> 24),
				(byte)((b & 0xFF0000) >> 16),
				(byte)((b & 0xFF00) >> 8),
				(byte)(b & 0xFF)});
	}
	
	public static Bits of(byte[] b){ return of(b,0,b.length); }
	
	public static Bits of(byte[] b, int off, int len){
		return new Bits(b,off,len);
	}
	
	public static void main(String[] args){
		Bits b = Bits.of(Integer.MAX_VALUE);
		System.out.println(b);
		System.out.println(b.thirtyTwo(0,true));
	}
}
