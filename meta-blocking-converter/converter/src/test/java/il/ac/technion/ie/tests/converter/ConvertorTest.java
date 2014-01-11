package il.ac.technion.ie.tests.converter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import il.ac.technion.ie.converter.Convertor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import DataStructures.Attribute;
import DataStructures.EntityProfile;

@RunWith(MockitoJUnitRunner.class)
public class ConvertorTest {
	private Convertor classUnderTest;
	private Convertor classUnderTestSpied;

	@Mock
	private EntityProfile entityProfile;
	
	@Mock 
	private Attribute attributeOne;
	@Mock 
	private Attribute attributeTwo;
	@Mock 
	private Attribute attributeThree;
	
	@Before
	public void setUp() throws Exception {
		classUnderTest = new Convertor();
		classUnderTestSpied = spy(classUnderTest);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExtractFieldsNames() {
		HashSet<Attribute> attributes = new HashSet<Attribute>();
		attributes.add(attributeOne);
		attributes.add(attributeTwo);
		attributes.add(attributeThree);
		when(attributeOne.getName()).thenReturn("jhon");
		when(attributeTwo.getName()).thenReturn("jhon");
		when(attributeThree.getName()).thenReturn("smith");
		when(entityProfile.getAttributes()).thenReturn(attributes);
		SortedSet<String> extractFieldsNames = classUnderTest.extractFieldsNames(Arrays.asList(entityProfile));
		Assert.assertEquals("Result size it to big", 2, extractFieldsNames.size());
		assertThat(extractFieldsNames, hasItems("jhon","smith"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExtractValues() {
		when(classUnderTestSpied.extractFieldsNames(Mockito.anyList())).thenReturn(new TreeSet<String>( Arrays.asList("staring", "writer") ));
		HashSet<Attribute> hashSet = new HashSet<Attribute>();
		hashSet.add(new Attribute("staring", "A"));
		hashSet.add(new Attribute("writer", "B"));
		hashSet.add(new Attribute("staring", "C"));
		hashSet.add(new Attribute("imdbId", "stringValue"));
		when(entityProfile.getAttributes()).thenReturn(hashSet);
		
		List<Map<String,String>> values = classUnderTestSpied.extractValues(Arrays.asList(entityProfile));
		Assert.assertEquals("Created bigger result", 1, values.size());
		Map<String, String> map = values.get(0);
		assertThat(map.values(), hasItems("B", "A C"));
		assertThat(map.get("title"), is(equalTo("stringValue")));
	}

	@Test
	public void testLoadEntityProfile() {
		URL resource = ConvertorTest.class.getClassLoader().getResource("serlized/dbpediaMovies");
		String filePath = resource.getFile();
		ArrayList<EntityProfile> entityProfiles = classUnderTest.loadEntityProfile(filePath);
		Assert.assertNotNull(entityProfiles);
	}

	@Test
	@Ignore
	public void testDoAlgorithm() {
		fail("Not yet implemented");
	}

}