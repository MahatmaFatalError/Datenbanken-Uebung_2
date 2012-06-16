package de.dhbw.db2.youdb.test.access;

import org.junit.Assert;

import de.dhbw.db2.youdb.access.AbstractTable;
import de.dhbw.db2.youdb.access.HeapTable;
import de.dhbw.db2.youdb.access.RowIdentifier;
import de.dhbw.db2.youdb.storage.AbstractRecord;
import de.dhbw.db2.youdb.storage.Record;
import de.dhbw.db2.youdb.storage.types.SQLInteger;
import de.dhbw.db2.youdb.storage.types.SQLVarchar;
import de.dhbw.db2.youdb.test.TestCase;

public class TestHeapTable extends TestCase{
	/**
	 * Insert 2 records into a heap table and read them by their RID (RowIdentifier)
	 */
	public void testTable1(){
		AbstractRecord record1 = new Record(2);
		record1.setValue(0, new SQLInteger(1));
		record1.setValue(1, new SQLVarchar("Hello111", 10));
		
		AbstractRecord record2 = new Record(2);
		record2.setValue(0, new SQLInteger(2));
		record2.setValue(1, new SQLVarchar("Hello112", 10));
		
		AbstractTable table = new HeapTable(record1.clone());
		RowIdentifier rid1 = table.insert(record1);
		RowIdentifier rid2 = table.insert(record2);
		
		AbstractRecord record1Cmp = table.lookup(rid1.getPageNumber(), rid1.getSlotNumber());
		AbstractRecord record2Cmp = table.lookup(rid2.getPageNumber(), rid2.getSlotNumber());
		
		Assert.assertEquals(record1, record1Cmp);
		Assert.assertEquals(record2, record2Cmp);
	}
}
