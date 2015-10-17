package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by I062070 on 15/10/2015.
 */
public class Measurements implements IMeasurements {

    static final Logger logger = Logger.getLogger(Measurements.class);

    public static final double VALUE_NOT_EXISTS = -1.0;
    private iMeasurService measurService;
    private Map<Double, Double> rankedValueMap;
    private Map<Double, Double> mrrValueMap;


    public Measurements() {
        measurService = new MeasurService();
        rankedValueMap = new HashMap<>();
        mrrValueMap = new HashMap<>();
    }

    @Override
    public void calculate(List<BlockWithData> blocks, double threshold) {
        if (blocks != null) {
            logger.trace("calculating RankedValue and MRR for threshold " + threshold);
            rankedValueMap.put(threshold, measurService.calcRankedValue(blocks));
            mrrValueMap.put(threshold, measurService.calcMRR(blocks));
        }
    }

    @Override
    public double getRankedValueByThreshold(double threshold) {
        Double rankedValue = rankedValueMap.get(threshold);
        return rankedValue != null ? rankedValue : VALUE_NOT_EXISTS;
    }

    @Override
    public double getMRRByThreshold(double threshold) {
        Double rankedValue = mrrValueMap.get(threshold);
        return rankedValue != null ? rankedValue : VALUE_NOT_EXISTS;
    }

    @Override
    public List<Double> getRankedValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(rankedValueMap);
    }

    @Override
    public List<Double> getMrrValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(mrrValueMap);
    }

    private List<Double> getMeasureSortedByThreshold(Map<Double, Double> measureValue) {
        TreeSet<Double> sortedKeys = new TreeSet<>(measureValue.keySet());
        List<Double> rankedValuesSortedByThreshold = new ArrayList<>();
        for (Double key : sortedKeys) {
            rankedValuesSortedByThreshold.add(measureValue.get(key));
        }
        return rankedValuesSortedByThreshold;
    }

    @Override
    public List<Double> getThresholdSorted() {
        List<Double> list = new ArrayList<>(rankedValueMap.keySet());
        Collections.sort(list);
        return list;
    }
}