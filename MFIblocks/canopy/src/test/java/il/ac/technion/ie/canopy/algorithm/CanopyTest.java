package il.ac.technion.ie.canopy.algorithm;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.search.module.SearchResult;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.lucene.index.IndexWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.reflect.Whitebox;

import java.net.URISyntaxException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class CanopyTest {

    public static final int NUMBER_OF_RECORDS_IN_BIG_FILE = 1000;
    @Spy
    private CanopyInteraction canopyInteraction = new CanopyInteraction();

    private Canopy classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = Whitebox.newInstance(Canopy.class);
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = CanopyParametersException.class)
    public void testConstractorT1SmallerThanT2() throws Exception {
        new Canopy(mock(List.class), 0.4, 0.5);
    }

    @Test
    public void testVerifyAllRecordsAreAdded() throws Exception {
        String pathToBigRecordsFile = ExperimentsUtils.getPathToBigRecordsFile();
        List<Record> records = ExperimentsUtils.createRecordsFromTestFile(pathToBigRecordsFile);
        classUnderTest = new Canopy(records, 0.6, 0.3);
        classUnderTest.initSearchEngine(canopyInteraction);
        Mockito.verify(canopyInteraction, Mockito.times(NUMBER_OF_RECORDS_IN_BIG_FILE)).addDoc(Mockito.any(IndexWriter.class), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testSampleRecordRandomly() throws Exception {
        List<Record> objects = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Record record = mock(Record.class);
            when(record.getRecordID()).thenReturn(i);
            objects.add(record);
        }
        classUnderTest = Whitebox.newInstance(Canopy.class);
        Record first = Whitebox.invokeMethod(classUnderTest, "sampleRecordRandomly", objects);
        Record second = Whitebox.invokeMethod(classUnderTest, "sampleRecordRandomly", objects);
        assertThat(first, is(not(second)));
    }

    @Test
    public void testFetchRecordsBasedOnIDs() throws Exception {
        String pathToBigRecordsFile = ExperimentsUtils.getPathToBigRecordsFile();
        List<Record> records = ExperimentsUtils.createRecordsFromTestFile(pathToBigRecordsFile);
        classUnderTest = new Canopy(records, 0.6, 0.3);

        List<Integer> iDsRandomly = createIDsRandomly(records);
        List<SearchResult> searchResults = convertIdsToSearchResults(iDsRandomly);
        List<Record> fetchRecordsBasedOnIDs = Whitebox.invokeMethod(classUnderTest, "fetchRecordsBasedOnIDs", searchResults);
        assertThat(fetchRecordsBasedOnIDs, hasSize(iDsRandomly.size()));
        for (int i = 0; i < fetchRecordsBasedOnIDs.size(); i++) {
            Record record = fetchRecordsBasedOnIDs.get(i);
            assertThat(iDsRandomly, hasItem(record.getRecordID()));
        }
    }

    @Test
    public void testRemoveRecords() throws Exception {
        List<Record> pool = getRecordsFromCsv();
        Record root = pool.get(10);
        List<Record> list = new ArrayList<>(pool.subList(5, 9));
        int expectedSize = pool.size() - list.size() - 1;

        Whitebox.invokeMethod(classUnderTest, "removeRecords", pool, root, list);

        assertThat(pool, hasSize(expectedSize));
        assertThat(pool, not(containsInAnyOrder(list.toArray())));
    }

    private List<SearchResult> convertIdsToSearchResults(List<Integer> iDsRandomly) {
        UniformRealDistribution distribution = new UniformRealDistribution();
        List<SearchResult> list = new ArrayList<>(iDsRandomly.size());

        for (Integer id : iDsRandomly) {
            list.add(new SearchResult(String.valueOf(id), distribution.sample()));
        }
        return list;
    }

    private List<Integer> createIDsRandomly(List<Record> records) {
        Random random = new Random();
        int randomIndex = random.nextInt(records.size());
        int secondRandomIndex = random.nextInt(records.size());
        int lower = Math.min(randomIndex, secondRandomIndex);
        int upper = Math.max(randomIndex, secondRandomIndex);
        int[] rangeNumbersPrimitive = Ints.toArray(ContiguousSet.create(Range.closed(lower, upper), DiscreteDomain.integers()));
        Integer[] rangeNumbers = ArrayUtils.toObject(rangeNumbersPrimitive);

        return new ArrayList<>(Arrays.asList(rangeNumbers));
    }

    private List<Record> getRecordsFromCsv() throws URISyntaxException {
        //read records from CSV file
        String pathToSmallRecordsFile = ExperimentsUtils.getPathToSmallRecordsFile();
        List<Record> records = ExperimentsUtils.createRecordsFromTestFile(pathToSmallRecordsFile);
        assertThat(records, hasSize(20));
        return records;
    }
}