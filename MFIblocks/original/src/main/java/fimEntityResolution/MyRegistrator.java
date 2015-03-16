package fimEntityResolution;

import com.esotericsoftware.kryo.Kryo;
import fimEntityResolution.bitsets.EWAH_BitSet_Factory;
import fimEntityResolution.interfaces.ClearerFactory;
import il.ac.technion.ie.data.structure.Clearer;
import il.ac.technion.ie.model.IFRecord;
import il.ac.technion.ie.data.structure.SetPairIF;
import org.apache.spark.serializer.KryoRegistrator;
/***
 * In order to use Kryo serialization we have to register our classes. (Spark tuning)
 * @author Jonathan Svirsky
 */
public class MyRegistrator implements KryoRegistrator {

	@Override
	public void registerClasses(Kryo kryo) {
		kryo.register(EWAH_BitSet_Factory.class);
		kryo.register(Clearer.class);
		kryo.register(ClearerFactory.class);
		kryo.register(IFRecord.class);
		kryo.register(SetPairIF.class);
		kryo.register(FrequentItem.class);
	}

}