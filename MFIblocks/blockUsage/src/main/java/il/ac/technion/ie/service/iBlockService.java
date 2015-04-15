package il.ac.technion.ie.service;


import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.model.CandidatePairs;

import java.util.List;

/**
 * Created by I062070 on 13/03/2015.
 */
public interface iBlockService {
    public List<Block> getBlocks(CandidatePairs candidatePairs, MfiContext context);

    public List<Integer> getBlocksOfRecord(CandidatePairs candidatePairs, int record);
}
