package de.dhbw.db2.youdb.test.storage.types;

import junit.framework.Assert;
import de.dhbw.db2.youdb.storage.types.SQLVarchar;
import de.dhbw.db2.youdb.test.TestCase;


public class TestSQLVarchar  extends TestCase {
	public void testSerializeDeserialize1(){
		String value = "123456789";
		
		SQLVarchar sqlVarchar = new SQLVarchar(value, 255);
		byte[] data = sqlVarchar.serialize();
		
		SQLVarchar sqlVarchar2 = new SQLVarchar(255);
		sqlVarchar2.deserialize(data);
		
		Assert.assertEquals(sqlVarchar.getValue(), sqlVarchar2.getValue());
	}
	
	public void testSerializeDeserialize2(){
		for(int i = 0; i<=10000; ++i){
			int length = (int) (Math.random() * 255);
			StringBuffer valueBuffer = new StringBuffer();
			for(int j=0; j<length; ++j){
				char c =  (char) (Math.random() * 127);
				valueBuffer.append(c);
			}
			String value = valueBuffer.toString();
			
			SQLVarchar sqlVarchar = new SQLVarchar(value, 255);
			byte[] data = sqlVarchar.serialize();
			
			SQLVarchar sqlVarchar2 = new SQLVarchar(255);
			sqlVarchar2.deserialize(data);
			
			Assert.assertEquals(sqlVarchar.getValue(), sqlVarchar2.getValue());
		}
	}
}
