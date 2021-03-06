package fimEntityResolution.bitsets;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.NotSupportedException;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIterator;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
//import javaewah.EWAHCompressedBitmap;
//import javaewah.IntIterator;
import fimEntityResolution.Utilities;
import fimEntityResolution.interfaces.BitSetIF;
import fimEntityResolution.interfaces.IFRecord;
import fimEntityResolution.interfaces.SetPairIF;

public class EWAH_BitSet implements BitSetIF{

	EWAHCompressedBitmap comBS = null;

	public EWAH_BitSet(){
		comBS = new EWAHCompressedBitmap();
	}
	
	//this method creates a new object!!!
	public BitSetIF and(final BitSetIF other) {
		EWAH_BitSet otherEWAH = (EWAH_BitSet)other;
		comBS = comBS.and(otherEWAH.comBS);
		return this;
		
	}

	
	public boolean get(int recordId) {
		throw new NotImplementedException();
	}

	
	//has linear running time!!!
	public int getCardinality() {
		return comBS.cardinality();
	}

	
	public String getSupportString() {
		StringBuilder sb = new StringBuilder("{");
		IntIterator it = comBS.intIterator();
		boolean first = true;
		while(it.hasNext()){
			long next = it.next();
			if(first){
				sb.append(next);
				first = false;
			}
			else{
				sb.append(", ").append(next);
			}
		}
		sb.append("}");
		return sb.toString();
	}

	
	public void set(int recordId) {
		comBS.set(recordId);
	}
	
	//created a new object
	public void clearAll() {
		comBS = null;
		comBS = new EWAHCompressedBitmap();
	}
	
	//this method creates a new object!!!
	public BitSetIF or(final BitSetIF other) throws NotSupportedException {
		EWAH_BitSet otherEWAH = (EWAH_BitSet)other;
		comBS = comBS.or(otherEWAH.comBS);
		return this;
	}
	
	public List<IFRecord> getRecords() {
		List<IFRecord> retVal = new ArrayList<IFRecord>(comBS.cardinality());
		IntIterator iterator = comBS.intIterator();
		while(iterator.hasNext()){
			int index = iterator.next();
			retVal.add(Utilities.globalRecords.get(index));
		}		
		return retVal;
	}
	
	public int markPairs(SetPairIF spf, double score) {		
		int cnt =0;
		List<Integer> positions = comBS.getPositions();		
		for(int i=0 ; i < positions.size() ; i++){
			for(int j=i+1 ; j < positions.size() ; j++){
				spf.setPair(positions.get(i), positions.get(j),score);	
				cnt++;
			}
		}
		return cnt;
	}
	
	public void orInto(BitSetIF other) {
		List<Integer> positions = comBS.getPositions();	
		for(int i=0 ; i < positions.size() ; i++){
			other.set(positions.get(i));
		}
		
	}

}
