package il.ac.technion.ie.experiments.service;

import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.FebrlMeasuresContext;
import il.ac.technion.ie.model.Record;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by I062070 on 17/10/2015.
 */
public interface IMeasurements {
    void calculate(List<BlockWithData> blocks, double threshold);

    double getRankedValueByThreshold(double threshold);

    double getMRRByThreshold(double threshold);

    List<Double> getRankedValuesSortedByThreshold();

    List<Double> getMrrValuesSortedByThreshold();

    List<Double> getThresholdSorted();

    List<Double> getNormalizedMRRValuesSortedByThreshold();

    List<Double> getNormalizedRankedValuesSortedByThreshold();

    FebrlMeasuresContext getFebrlMeasuresContext(Double threshold);

    /**
     * The method calculates the number of records that used to represent more than one block,
     * but now represent only a single block.
     * <p/>
     * <br><br>
     * Also know as measurement #4
     *
     * @param duplicates
     * @param cleaned
     * @return
     */
    DuplicateReductionContext representativesDuplicateElimination(Multimap<Record, BlockWithData> duplicates, Multimap<Record, BlockWithData> cleaned);

    /**
     * This method finds the number of records in @param source that were not present in @param other.
     * It then calculate the percentage of those records from the total number of records in @param source.
     * The result is stored in @param reductionContext.
     * <br><br>
     * Also know as measurement #1
     *
     * @param source           source records, preferably the True representatives.
     * @param other            records from other source, preferably the representatives that were found in blocks
     * @param reductionContext context to store the result
     */
    void representationDiff(final Set<Record> source, final Set<Record> other, DuplicateReductionContext reductionContext);

    /**
     * This method calculates for each block representative that is also true representative of some block the number
     * of records from the clean block that it managed to pull into the canopy block. <br><br>a
     * Also know as measurement #2
     *
     * @param trueRepsMap             mapping of true block representatives and their blocks.
     * @param convexBPRepresentatives mapping of blocks that were generated by Canopy and there representatives.
     * @param reductionContext        context to store the result
     * @return the calculated value
     */
    double calcPowerOfRep(final Map<Record, BlockWithData> trueRepsMap, final Multimap<Record, BlockWithData> convexBPRepresentatives, DuplicateReductionContext reductionContext);

    /**
     * There is one 'dirty' block that is most similar to some clean block.
     * This "similarity" is measured by the number of records they share.<br>
     * <p/>
     * For any clean block, this method found the closest match dirty block and check if they have the same representative.
     * It calculate the percentage of cases that both blocks shared the same representative.
     * <p/>
     * of records from the clean block that it managed to pull into the canopy block. <br><br>
     * Also know as measurement #3
     *
     * @param cleanBlocks      a set of the real blocks
     * @param dirtyBlocks      a set of overlapping blocks
     * @param reductionContext context to store the result
     * @return the calculated value
     */
    double calcWisdomCrowds(Set<BlockWithData> cleanBlocks, Set<BlockWithData> dirtyBlocks, DuplicateReductionContext reductionContext);
}
