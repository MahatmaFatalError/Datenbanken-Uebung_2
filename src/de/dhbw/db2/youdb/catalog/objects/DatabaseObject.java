package de.dhbw.db2.youdb.catalog.objects;

import de.dhbw.db2.youdb.catalog.CatalogManager;

public class DatabaseObject {
	protected int oid;
	protected String name;
	
	public DatabaseObject(String name){
		this.name = name.toUpperCase();
		this.oid = CatalogManager.getOid(this.name);	
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.toUpperCase();
	}

	public int getOid() {
		return oid;
	}

	public void setOid(int oid) {
		this.oid = oid;
	}
	
	public int hashCode(){
		return this.oid;
	}
}
