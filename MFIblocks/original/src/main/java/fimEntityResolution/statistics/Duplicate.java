package fimEntityResolution.statistics;

import il.ac.technion.ie.model.RecordMatches;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Duplicate {

	private Set<Integer> duplicatedEntityRecordIDs;
	private boolean wasDuplicateDetected;
	private boolean isFinishedTesting;
	private int comparisonsMade;
	
	public Duplicate(Set<Integer> ids) {
		this.duplicatedEntityRecordIDs = ids;
		this.wasDuplicateDetected = false;
		this.isFinishedTesting = false;
		this.comparisonsMade = 0;
	}
	
	//call contractor that obtain Set
	public Duplicate(List<Integer> ids) {
		this( new HashSet<Integer>(ids) );
	}

	
	/**
	 * This method determine if the duplicate was found by the MFIBlock output<br>
	 * <p> We consider a duplicate to be detected if it was found in any block.
	 * <pre>True Matching: {1,2};<br>Blocks Found:{1,3},{2,1}<br></pre>I this case we consider the duplicate to be detected,
	 * even that is appease in just one block and not in two.
	 *
	 * </p>
	 * 
	 * @param recordMatches - possible duplicates
	 * @param recordId - recordID whose possible duplicate records were found
	 */
	public void decideIfDetected(RecordMatches recordMatches, Integer recordId ) {
		if (!isFinishedTesting) {
			Set<Integer> matchedIds = new HashSet<Integer>(recordMatches.getMatchedIds());
			comparisonsMade += matchedIds.size();
			matchedIds.add(recordId);
			wasDuplicateDetected = matchedIds.containsAll(duplicatedEntityRecordIDs);
			//Once we find that a duplicate was detected in any block we consider the duplicate to be found
			if (wasDuplicateDetected) {
				isFinishedTesting = true;
			}
		}
	}

	/**
	 * Returns The Duplicate decision, whether or not it was detected
	 * @return
	 */
	public boolean wasDuplicateDetected() {
		return wasDuplicateDetected;
	}

	/**
	 * Return number of comparisons made till the duplicate entity was detected.<br>
	 * If the duplicate was not detected, then return total number of comparisons 
	 * 
	 * @return
	 */
	public int getComparisonsMade() {
		return comparisonsMade;
	}
	
}
