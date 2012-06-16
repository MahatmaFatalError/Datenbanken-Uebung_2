package de.dhbw.db2.youdb.test.storage.types;

import junit.framework.Assert;
import de.dhbw.db2.youdb.storage.types.SQLInteger;
import de.dhbw.db2.youdb.test.TestCase;


public class TestSQLInteger extends TestCase {
	public void testSerializeDeserialize1(){
		int value = 123456789;
		
		SQLInteger sqlInt = new SQLInteger(value);
		byte[] content = sqlInt.serialize();
		
		SQLInteger sqlInt2 = new SQLInteger();
		sqlInt2.deserialize(content);
		
		Assert.assertEquals(sqlInt.getValue(), sqlInt2.getValue());
	}
	
	public void testSerializeDeserialize2(){
		for(int i = 0; i<=10000; ++i){
			int value = (int) (Math.random() * Integer.MAX_VALUE);
			
			SQLInteger sqlInt = new SQLInteger(value);
			byte[] content = sqlInt.serialize();
			
			SQLInteger sqlInt2 = new SQLInteger();
			sqlInt2.deserialize(content);
			
			Assert.assertEquals(sqlInt.getValue(), sqlInt2.getValue());
		}
		
		for(int i = 0; i<=10000; ++i){
			int value = (int) (Math.random() * Integer.MIN_VALUE);
			
			SQLInteger sqlInt = new SQLInteger(value);
			byte[] content = sqlInt.serialize();
			
			SQLInteger sqlInt2 = new SQLInteger();
			sqlInt2.deserialize(content);
			
			Assert.assertEquals(sqlInt.getValue(), sqlInt2.getValue());
		}
	}
}
