package de.dhbw.db2.youdb.test.access;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteAccess extends TestSuite
{
  public static Test suite()
  {
    TestSuite suite = new TestSuite( "YouDB-Access" );
    suite.addTestSuite( TestHeapTable.class );
    suite.addTestSuite( TestUniqueBPlusTree.class );
    return suite;
  }
}