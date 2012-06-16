package de.dhbw.db2.youdb.access;

import java.util.HashMap;
import java.util.Vector;

import de.dhbw.db2.youdb.catalog.objects.Attribute;
import de.dhbw.db2.youdb.catalog.objects.Index;
import de.dhbw.db2.youdb.storage.AbstractRecord;
import de.dhbw.db2.youdb.storage.types.SQLType;

/**
 * Abstract class for a table
 * @author cbinnig
 *
 */
@SuppressWarnings("rawtypes") 
public abstract class AbstractTable implements Iterable{
	//data
	protected AbstractRecord prototype; //prototype for all records in table
	
	//metadata
	protected int recordCount=0;
	protected Vector<Attribute> attributes = new Vector<Attribute>(); 
	protected Vector<Attribute> primaryKeys = new Vector<Attribute>();
	protected HashMap<String, Index> indexes = new HashMap<String, Index>();
	
	
	/**
	 * Creates a table for a given record prototype which defines 
	 * @param prototypeRecord
	 */
	public AbstractTable(AbstractRecord prototype) {
		this.prototype = prototype;
	}
	
	/**
	 * Returns record prototype
	 * @return Prototype record of table
	 */
	public AbstractRecord getPrototype() {
		return prototype;
	}

	/**
	 * Sets record prototype
	 * @param prototype
	 */
	public void setPrototype(AbstractRecord prototype) {
		this.prototype = prototype;
	}
	
	/**
	 * Sets attribute names for table
	 * @param attributes
	 */
	public void setAttributes(Vector<Attribute> attributes){
		this.attributes = attributes;
	}
	
	
	/**
	 * Get attributes of table
	 * @return Attributes of table
	 */
	public Vector<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * Returns the column number for a given attribute name (if existing).
	 * Otherwise, null is returned.
	 * @param name Column number
	 * @return
	 */
	public int getColumnNumber(String name){
		int nr = -1;
		int i=0;
		for(Attribute attribute: this.attributes){
			if(attribute.getName().equals(name)){
				nr=i;
				break;
			}
			i++;
		}
		return nr;
	}
	
	/**
	 * Returns EnumSQLType for given column number
	 * @param colNumber
	 * @return Type of column
	 */
	public SQLType getType(int colNumber){
		return this.prototype.getValue(colNumber).getType();
	}
	
	/**
	 * Get primary key attributes
	 * @return Primary key attributes
	 */
	public Vector<Attribute> getPrimaryKeys() {
		return primaryKeys;
	}

	/**
	 * Set primary key attributes
	 * @param primaryKeys
	 */
	public void setPrimaryKeys(Vector<Attribute> primaryKeys) {
		this.primaryKeys = primaryKeys;
	}
	
	
	/**
	 * Adds metadata of index 
	 * @param attribute
	 * @param index
	 */
	public void addIndex(Attribute attribute, Index index){
		this.indexes.put(attribute.getName(), index);
	}
	
	/**
	 * Get index metadata
	 * @param attribute
	 * @return
	 */
	public Index getIndex(Attribute attribute){
		return this.indexes.get(attribute.getName());
	}
	
	/**
	 * Returns metadata for primary key (if existing)
	 * @return
	 */
	public Index getPrimaryIndex(){
		if(this.primaryKeys.size()==0)
			return null;
		
		Attribute attribute = this.primaryKeys.get(0);
		if(attribute==null)
			return null;
		
		return this.indexes.get(attribute.getName());
	}
	
	/**
	 * Returns primary key attribute (if existing). 
	 * Otherwise, null is returned.
	 * @return Primary key attribute
	 */
	public Attribute getPrimaryKey(){
		if(this.primaryKeys.size()==0)
			return null;
		
		return this.primaryKeys.get(0);
	}

	/**
	 * Returns number of records in table
	 * @return Number of records in table
	 */
	public int getRecordCount() {
		return recordCount;
	}

	/**
	 * Insert a new record into the table
	 * @param record
	 * @return RowIdentifier (pagenumber, slotNumber) of record in table
	 */
	public abstract RowIdentifier insert(AbstractRecord record);

	/**
	 * Returns a record for a given page and slot number
	 * @param pageNumber
	 * @param slotNumber
	 * @return Record which was found for page and slot
	 */
	public abstract AbstractRecord lookup(int pageNumber, int slotNumber);
	
	/**
	 * Returns a record for a given RID
	 * @param rid
	 * @return
	 */
	public abstract AbstractRecord lookup(RowIdentifier rid);
	
	/**
	 * Creates an iterator over the table
	 */
	public abstract java.util.Iterator<AbstractRecord> iterator();
}
