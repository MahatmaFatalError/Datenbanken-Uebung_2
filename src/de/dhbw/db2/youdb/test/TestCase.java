package de.dhbw.db2.youdb.test;

import de.dhbw.db2.youdb.catalog.CatalogManager;

public class TestCase extends junit.framework.TestCase {

	public TestCase() {
	}

	public TestCase(String name) {
		super(name);
	}

	public void setUp(){	
		CatalogManager.clear();
	}
}
