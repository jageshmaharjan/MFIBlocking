package candidateMatches;

import java.util.Comparator;

public class CandidateMatchComparator implements Comparator<CandidateMatch>{

	private static CandidateMatchComparator self = null;
	private CandidateMatchComparator(){}
	public static CandidateMatchComparator getInstance(){
		if(self == null){
			self = new CandidateMatchComparator();
		}
		return self;
	}
	public int compare(CandidateMatch o1, CandidateMatch o2) {
		return (int)(Math.signum(o1.getScore()-o2.getScore()));
	}

}
