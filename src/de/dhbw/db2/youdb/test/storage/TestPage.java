package de.dhbw.db2.youdb.test.storage;

import org.junit.Assert;

import de.dhbw.db2.youdb.storage.AbstractPage;
import de.dhbw.db2.youdb.storage.AbstractRecord;
import de.dhbw.db2.youdb.storage.Record;
import de.dhbw.db2.youdb.storage.RowPage;
import de.dhbw.db2.youdb.storage.types.SQLInteger;
import de.dhbw.db2.youdb.storage.types.SQLVarchar;
import de.dhbw.db2.youdb.test.TestCase;


public class TestPage extends TestCase{
	public void testInsertWrongRecord(){
		try{
			AbstractPage p = new RowPage(4);
			
			//insert record
			AbstractRecord r1 = new Record(2);
			r1.setValue(0, new SQLInteger(123456789));
			r1.setValue(1, new SQLVarchar("Test", 10));
			p.insert(r1);
		}
		catch(RuntimeException e){
			Assert.assertTrue(true);
			return;
		}
		
		Assert.assertTrue(false);
	}
	
	public void testInsert1Record(){
		//insert record
		AbstractRecord r1 = new Record(2);
		r1.setValue(0, new SQLInteger(123456789));
		r1.setValue(1, new SQLVarchar("Test", 10));
		AbstractPage p = new RowPage(r1.getFixedLength());
		p.insert(r1);
		
		//read record
		AbstractRecord r1cmp = new Record(2);
		r1cmp.setValue(0, new SQLInteger());
		r1cmp.setValue(1, new SQLVarchar(10));
		p.read(0, r1cmp);
		
		//compare
		Assert.assertEquals(r1, r1cmp);
	}
	
	public void testInsert2Records(){
		//insert 1st record
		AbstractRecord r1 = new Record(2);
		r1.setValue(0, new SQLInteger(123456789));
		r1.setValue(1, new SQLVarchar("Test1", 10));
		AbstractPage p = new RowPage(r1.getFixedLength());
		int slot = p.insert(r1);
		
		//insert 2nd record
		AbstractRecord r2 = new Record(2);
		r2.setValue(0, new SQLInteger(123456788));
		r2.setValue(1, new SQLVarchar("Test2", 10));
		p.insert(slot, r2, true);
		Assert.assertEquals(p.getNumRecords(), 2);
		
		//read 2nd record and cmp
		AbstractRecord rcmp = new Record(2);
		rcmp.setValue(0, new SQLInteger());
		rcmp.setValue(1, new SQLVarchar(10));
		p.read(0, rcmp);
		Assert.assertEquals(r2, rcmp);
		
		//read 1st record and cmp
		p.read(1, rcmp);
		Assert.assertEquals(r1, rcmp);
	}
	
	public void testUpdate1Record(){
		//insert record
		AbstractRecord r1 = new Record(2);
		r1.setValue(0, new SQLInteger(123456789));
		r1.setValue(1, new SQLVarchar("Test", 10));
		AbstractPage p = new RowPage(r1.getFixedLength());
		int slot = p.insert(r1);
		
		//update record
		r1.setValue(0, new SQLInteger(123456788));
		p.insert(slot, r1, false);
		
		//read record and cmp
		AbstractRecord r1cmp = new Record(2);
		r1cmp.setValue(0, new SQLInteger());
		r1cmp.setValue(1, new SQLVarchar(10));
		p.read(0, r1cmp);
		
		Assert.assertEquals(p.getNumRecords(), 1);
		Assert.assertEquals(r1, r1cmp);
	}
	
	public void testCreateNRecords(){
		//insert  n records 
		int number = 100;
		AbstractRecord r1 = new Record(2);
		r1.setValue(0, new SQLInteger());
		r1.setValue(1, new SQLVarchar(10));
		AbstractPage p = new RowPage(r1.getFixedLength());;
		
		for(int i=0; i<number; i++){
			r1.setValue(0, new SQLInteger(i));
			r1.setValue(1, new SQLVarchar("Test"+i, 255));
			p.insert(r1);
		}
		
		//read n records and cmp
		AbstractRecord r1cmp = new Record(2);
		r1cmp.setValue(0, new SQLInteger());
		r1cmp.setValue(1, new SQLVarchar(255));
		
		for(int i=0; i<number; i++){
			r1.setValue(0, new SQLInteger(i));
			r1.setValue(1, new SQLVarchar("Test"+i, 255));
			p.read(i, r1cmp);
			Assert.assertEquals(r1, r1cmp);
		}
		
	}
}
