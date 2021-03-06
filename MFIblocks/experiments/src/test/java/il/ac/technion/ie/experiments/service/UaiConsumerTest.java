package il.ac.technion.ie.experiments.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.UaiVariableContext;
import il.ac.technion.ie.experiments.parsers.ReadBinaryFile;
import il.ac.technion.ie.model.Record;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.support.membermodification.MemberMatcher.constructor;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UaiConsumer.class})
public class UaiConsumerTest {

    @InjectMocks
    private UaiConsumer classUnderTest;

    @Mock
    private ReadBinaryFile readBinaryFile;

    @Mock
    UaiVariableContext variableContext;

    @Before
    public void setUp() throws Exception {
        suppress(constructor(UaiConsumer.class));
        classUnderTest = spy(Whitebox.newInstance(UaiConsumer.class));

        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = FileNotFoundException.class)
    public void testConsumePotentials_exception() throws Exception {
        classUnderTest.consumePotentials();
    }

    @Test
    public void testConsumePotentials() throws Exception {
        //mocking
        Map<Integer, Double> lineToProbabilityMap = Maps.newHashMap(new ImmutableMap.Builder<Integer, Double>()
                .put(1, 0.0).put(2, 0.0).put(3, 1.0)
                .put(4, 1.0).put(5, 0.0)
                .put(6, 0.0).put(7, 0.0).put(8, 0.1).put(9, 0.2).put(10, 0.3).put(11, 0.4)
                .build());
        when(readBinaryFile.readFile(Mockito.any(File.class))).thenReturn(lineToProbabilityMap);
        when(variableContext.getSizeOfVariables()).thenReturn(Lists.newArrayList(3, 2, 6));
        when(variableContext.getBlockIdByVariableId(0)).thenReturn(13);
        when(variableContext.getBlockIdByVariableId(1)).thenReturn(14);
        when(variableContext.getBlockIdByVariableId(2)).thenReturn(22);
        Whitebox.setInternalState(classUnderTest, "binaryFile", mock(File.class));

        //execution
        classUnderTest.consumePotentials();

        //assertion
        ListMultimap<Integer, Double> variableIdToProbabilities = Whitebox.getInternalState(classUnderTest, "variableIdToProbabilities");
        assertThat(variableIdToProbabilities.get(0), contains(0.0, 0.0, 1.0));
        assertThat(variableIdToProbabilities.get(1), contains(1.0, 0.0));
        assertThat(variableIdToProbabilities.get(2), contains(0.0, 0.0, 0.1, 0.2, 0.3, 0.4));
        assertThat(classUnderTest.isPotentialConsumed(), is(true));

        //verify
        verifyPrivate(classUnderTest, times(3)).invoke("fillProbsForVariable", Mockito.any(Iterator.class), Mockito.anyInt());
        verifyPrivate(classUnderTest, times(11)).invoke("assertIterator", Mockito.any(Iterator.class));
    }

    @Test
    public void testApplyNewProbabilities_potentialNotConsumed() throws Exception {
        //mocking
        List blocks = mock(List.class);
        when(blocks.iterator()).thenReturn(mock(Iterator.class)); //mocks the iterator for the foreach loop
        doNothing().when(classUnderTest).consumePotentials();

        //execute
        classUnderTest.applyNewProbabilities(blocks);

        //verify
        Mockito.verify(classUnderTest, times(1));
    }

    @Test(expected = SizeNotEqualException.class)
    public void testAssertBlockSize() throws Exception {
        //mocking
        List<Double> newProbabilities = mock(List.class);
        when(newProbabilities.size()).thenReturn(7);
        List<Record> sortedMembers = mock(List.class);
        when(sortedMembers.size()).thenReturn(5);

        //execute
        Whitebox.invokeMethod(classUnderTest, "assertBlockSize", newProbabilities, sortedMembers);
    }
}