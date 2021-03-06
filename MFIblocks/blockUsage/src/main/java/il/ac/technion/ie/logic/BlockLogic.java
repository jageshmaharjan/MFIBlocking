package il.ac.technion.ie.logic;

import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.probability.ClusterSimilarity;
import il.ac.technion.ie.probability.SimilarityCalculator;
import il.ac.technion.ie.model.*;
import il.ac.technion.ie.search.core.SearchEngine;
import il.ac.technion.ie.search.module.BlockInteraction;
import il.ac.technion.ie.utils.BlockUtils;
import org.apache.log4j.Logger;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by I062070 on 13/03/2015.
 */
public class BlockLogic implements iBlockLogic {

    static final Logger logger = Logger.getLogger(BlockLogic.class);
    private iFindBlockAlgorithm algorithm;


    public BlockLogic() {
        this.algorithm = new FindBlockAlgorithm();
    }

    @Override
    public List<Block> findBlocks(CandidatePairs candidatePairs, int recordsSize) {
        ConcurrentHashMap<Integer, RecordMatches> matches = candidatePairs.getAllMatches();
        logger.debug(String.format("Obtained %d records of matching records", matches.size()));
        Set<Integer> recordsIds = matches.keySet();

        List<NeighborsVector> neighborsVectors = buildNeighborVectors(matches, recordsIds);

        List<Block> blocks = algorithm.findBlocks(neighborsVectors);
        this.addMissingRecords(blocks, recordsSize);
        return blocks;
    }

    @Override
    public void calcProbabilityOnRecords(final List<Block> blocks, MfiContext context) {
        SearchEngine searchEngine = buildSearchEngineForRecords(context);
        calcProbability(blocks, searchEngine);
    }

    @Override
    public final List<Block> findBlocksOfRecord(List<Block> allBlocks, int recordId) {
        List<Block> containedBlocks = new ArrayList<>();
        for (Block block : allBlocks) {
            boolean doesContainMember = block.hasMember(recordId);
            if (doesContainMember) {
                containedBlocks.add(block);
            }
        }
        return containedBlocks;
    }

    @Override
    public void setRecordsInBlocksAsTrueMatch(List<Block> blocks) {
        for (Block block : blocks) {
            for (Integer member : block.getMembers()) {
                block.setMemberProbability(member, 1);
                block.setMemberSimScore(member, (float) (block.getMembers().size() - 1));
            }
        }
    }

    @Override
    public Map<Integer, List<BlockDescriptor>> findAmbiguousRepresentatives(List<Block> blocks, MfiContext context) {
        Map<Integer, List<Block>> map = new HashMap<>();
        for (Block block : blocks) {
            updateBlockRepresentativesMap(map, block);
        }
        logger.info("Finished finding the blocks for all block representatives");
        findRecordsWithSeveralBlocks(map);
        logger.info("Retained only block representative that represent more than one block");
        SearchEngine searchEngine = buildSearchEngineForRecords(context);
        Map<Integer, List<BlockDescriptor>> ambiguousRepresentativesList = retriveTextRecords(map, searchEngine);
        logger.info("Retrieved the actual content of records inside those blocks");
        return ambiguousRepresentativesList;
    }

    private Map<Integer, List<BlockDescriptor>> retriveTextRecords(Map<Integer, List<Block>> map, SearchEngine searchEngine) {
        Map<Integer, List<BlockDescriptor>> ambiguousRepresentativesList = new HashMap<>();
        for (Map.Entry<Integer, List<Block>> entry : map.entrySet()) {
            List<BlockDescriptor> blockDescriptors = BlockUtils.convertBlocksToDescriptors(entry.getValue(), searchEngine);
            ambiguousRepresentativesList.put(entry.getKey(), blockDescriptors);
        }
        return ambiguousRepresentativesList;
    }


    private void findRecordsWithSeveralBlocks(Map<Integer, List<Block>> map) {
        Iterator<Map.Entry<Integer, List<Block>>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, List<Block>> entry = iter.next();
            if (entry.getValue().size() < 2) {
                logger.debug("Removing '" + entry.getKey() + "', it represents just one block");
                iter.remove();
            }
        }
    }

    /**
     * The method update @map and for each blockRepresentative it keep track of the blocks he represents.
     *
     * @param map
     * @param block
     */
    private void updateBlockRepresentativesMap(Map<Integer, List<Block>> map, Block block) {
        Map<Integer, Float> blockRepresentatives = block.findBlockRepresentatives();

        for (Map.Entry<Integer, Float> blockRepresentative : blockRepresentatives.entrySet()) {
            if (map.containsKey(blockRepresentative.getKey())) {
                List<Block> list = map.get(blockRepresentative.getKey());
                list.add(block);
            } else {
                ArrayList<Block> list = new ArrayList<>();
                list.add(block);
                map.put(blockRepresentative.getKey(), list);
            }
        }
    }

    private void addMissingRecords(List<Block> blocks, int recordsSize) {
        List<Integer> itemsNotDiscovered = findMissingRecordsFromBlocks(blocks, recordsSize);
        List<Block> missingItemsBlocks = createBlocksForMissingRecords(itemsNotDiscovered);
        blocks.addAll(missingItemsBlocks);
    }

    private List<Block> createBlocksForMissingRecords(List<Integer> itemsNotDiscovered) {
        List<Block> singletontesBlocks = new ArrayList<>();
        for (Integer recordId : itemsNotDiscovered) {
            singletontesBlocks.add(new Block(new ArrayList<>(Arrays.asList(recordId)),Block.RANDOM_ID));
        }
        return singletontesBlocks;
    }

    private List<Integer> findMissingRecordsFromBlocks(List<Block> blocks, int recordsSize) {
        BitSet bitSet = new BitSet(recordsSize);
        for (Block block : blocks) {
            List<Integer> members = block.getMembers();
            for (Integer member : members) {
                bitSet.set(member - 1);
            }
        }
        List<Integer> itemsNotDiscovered = new ArrayList<>();

        bitSet.flip(0, recordsSize);
        for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
            itemsNotDiscovered.add(i + 1);
        }
        if (itemsNotDiscovered.size() < recordsSize) {
            logger.info("Following records are missing from blocks: " + itemsNotDiscovered.toString() + ". They will be added as single blocks each");
        }
        return itemsNotDiscovered;
    }

    private void calcProbability(List<Block> blocks, SearchEngine searchEngine) {
        SimilarityCalculator similarityCalculator = new SimilarityCalculator(new JaroWinkler());
        //iterate on each block
        for (Block block : blocks) {
            calcBlockSimilarity(similarityCalculator, block, searchEngine);
            calcRecordsProbabilityInBlock(block);
            block.findBlockRepresentatives();
        }
    }

    private void calcBlockSimilarity(SimilarityCalculator similarityCalculator, Block block, SearchEngine searchEngine) {
        List<Integer> blockMembers = block.getMembers();
        //retrieve block Text attributes
        Map<Integer, List<String>> blockAttributes = getMembersAtributes(blockMembers, searchEngine);

        for (Integer currentRecordId : blockAttributes.keySet()) {
            float currentRecordSimilarity = ClusterSimilarity.calcRecordSimilarityInCluster(currentRecordId, blockAttributes, similarityCalculator);
            block.setMemberSimScore(currentRecordId, currentRecordSimilarity);
        }
    }

    private void calcRecordsProbabilityInBlock(Block block) {
        float allScores = 0;
        for (Integer member : block.getMembers()) {
            allScores += block.getMemberScore(member);
        }

        for (Integer member : block.getMembers()) {
            block.setMemberProbability(member, block.getMemberScore(member) / allScores);
        }

    }

    /**
     * The method retrieves list if IDs and an instance of @SearchEngine and returns for each ID a list
     * with fields that record has.
     *
     * @param recordID
     * @param searchEngine
     * @return <Integer, List<String>>
     */
    private Map<Integer, List<String>> getMembersAtributes(List<Integer> recordID, SearchEngine searchEngine) {
        Map<Integer, List<String>> blockFields = new HashMap<>();
        for (Integer blockMemberID : recordID) {
            List<String> recordAttributes = searchEngine.getRecordAttributes(String.valueOf(blockMemberID));
            blockFields.put(blockMemberID, recordAttributes);
        }
        return blockFields;
    }

    /**
     *
     * @param context
     * @return
     */

    private SearchEngine buildSearchEngineForRecords(MfiContext context) {
        String recordsPath = context.getOriginalRecordsPath();
        logger.debug("about to index: " + recordsPath);
        SearchEngine searchEngine = new SearchEngine(new BlockInteraction(context.getDatasetName()));
        searchEngine.addRecords(recordsPath);
        return searchEngine;
    }

    private List<NeighborsVector> buildNeighborVectors(ConcurrentHashMap<Integer, RecordMatches> matches, Set<Integer> recordsIds) {
        int numberOfRecords = recordsIds.size();
        List<NeighborsVector> neighborsVectors = new ArrayList<>(numberOfRecords);
        for (Integer recordsId : recordsIds) {
            NeighborsVector vector = new NeighborsVector(recordsId, numberOfRecords);
            Set<Integer> matchedIds = matches.get(recordsId).getMatchedIds();
            for (Integer matchedId : matchedIds) {
                vector.exitsNeighbor(matchedId);
            }
            neighborsVectors.add(vector);
        }
        logger.debug("Finished building 'NeighborVectors' for matched input");
        return neighborsVectors;
    }

}
