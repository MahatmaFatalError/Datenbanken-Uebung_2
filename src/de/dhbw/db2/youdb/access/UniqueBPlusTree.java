package de.dhbw.db2.youdb.access;

import java.util.HashMap;
import java.util.Vector;

import de.dhbw.db2.youdb.storage.AbstractPage;
import de.dhbw.db2.youdb.storage.AbstractRecord;
import de.dhbw.db2.youdb.storage.PageManager;
import de.dhbw.db2.youdb.storage.Record;
import de.dhbw.db2.youdb.storage.types.AbstractSQLValue;
import de.dhbw.db2.youdb.storage.types.SQLInteger;

/**
 * Unique B+-Tree implementation 
 * @author cbinnig
 *
 * @param <T>
 */
public class UniqueBPlusTree<T extends AbstractSQLValue> extends AbstractUniqueIndex<T> {
	/**
	 * Abstract class for an index element (node or leaf)
	 * @author cbinnig
	 */
	protected abstract class AbstractIndexElement{
		//index page to store index entries
		protected AbstractPage indexPage;
		
		/**
		 * Checks if index page exceeds max. fill grade
		 * @return
		 */
		public boolean isFull(){
			return (this.indexPage.getNumRecords()>UniqueBPlusTree.this.maxFillGrade);
		}
		
		/**
		 * creates a leaf or a node depending on type of object
		 * @return
		 */
		public abstract AbstractIndexElement createInstance();
		
		/**
		 * lookup a record for a key in index 
		 * @param key
		 * @return
		 */
		public abstract AbstractRecord lookup(T key);
		
		/**
		 * Inserts a new record into index
		 * @param record
		 * @return True is record could be inserted, false if key is already used
		 */
		public abstract boolean insert( T key, AbstractRecord record);
		
		/**
		 * Get maximum key value of index element
		 * @return
		 */
		public abstract T getMaxKey();
		
		/**
		 * Split index element into two elements
		 * @param element1 Reference to first element 
		 * @param element2 Reference to second element
		 */
		public abstract void split(AbstractIndexElement element1, AbstractIndexElement element2);
		
		/**
		 * Binary search for key
		 * @param key
		 * @return Slot number of node entry
		 */
		public abstract int binarySearch(T key);
		
		/**
		 * Print index elements  
		 * @param level
		 */
		protected abstract void print(int level);
		
		
		/**
		 * Print index elements
		 * @param level
		 */
		public void print() {
			this.print(0);
		}
		
		/**
		 * Returns page number of index element
		 * @return
		 */
		protected int getPageNumber(){
			return this.indexPage.getPageNumber();
		}
		
		/**
		 * Binary search on index element. If element is not found then 
		 * it returns pointer to next bigger element in index element
		 * or points to slot after last element if key is bigger
		 * than all index element entries.
		 * 
		 * @param key lookup key
		 * @param indexRecord record to read from index element (node or leaf)
		 * @return slot number of node entry
		 */
		protected int binarySearch(T key, AbstractRecord indexRecord){
			int end = this.indexPage.getNumRecords() - 1;
			int start = 0;
			int center = -1;
			
			while(start<=end){
				center = (start + end) / 2;
				this.indexPage.read(center, indexRecord);
				AbstractSQLValue keyValue = indexRecord.getValue(KEY_POS);
				
				if(key.compareTo(keyValue)==0){
					return center;
				}
				else if(key.compareTo(keyValue)<0){
					end = center - 1;
				}
				else if(key.compareTo(keyValue)>0){
					start = center + 1;
				}
			}
			return start;
		}
	}
	
	/**
	 * Index node
	 * @author cbinnig
	 *
	 */
	protected class Node extends AbstractIndexElement{
		/**
		 * Node constructor
		 */
		public Node(){
			this.indexPage = PageManager.createDefaultPage(UniqueBPlusTree.this.nodeRecPrototype.getFixedLength());
			UniqueBPlusTree.this.addIndexElement(this);
		}
		
		@Override
		public int binarySearch(T key){
			AbstractRecord nodeRecord = UniqueBPlusTree.this.nodeRecPrototype.clone();
			int index =  this.binarySearch(key, nodeRecord);
			int last = this.indexPage.getNumRecords() - 1;
			return (index<last)?index:last;
		}
		
		@Override
		public boolean insert(T key, AbstractRecord record){
			int slotNumber = this.binarySearch(key);
			AbstractRecord nodeRecord = UniqueBPlusTree.this.nodeRecPrototype.clone();
			this.indexPage.read(slotNumber, nodeRecord);
			
			SQLInteger pageNumber = (SQLInteger) nodeRecord.getValue(PAGE_POS);
			AbstractIndexElement child = UniqueBPlusTree.this.getIndexElement(pageNumber.getValue());
			boolean inserted =  child.insert(key, record);
			
			//split nodes if necessary and insert new nodes
			if(inserted && child.isFull()){
				AbstractIndexElement child1 = child.createInstance();
				AbstractIndexElement child2 = child.createInstance();
				child.split(child1, child2);
				
				nodeRecord.setValue(KEY_POS, child2.getMaxKey() );
				nodeRecord.setValue(PAGE_POS, new SQLInteger(child2.getPageNumber()) );
				this.indexPage.insert(slotNumber, nodeRecord, false);
				
				nodeRecord.setValue(KEY_POS, child1.getMaxKey() );
				nodeRecord.setValue(PAGE_POS, new SQLInteger(child1.getPageNumber()) );
				this.indexPage.insert(slotNumber, nodeRecord, true);
			}
			
			return inserted;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T getMaxKey() {
			AbstractRecord nodeRecord = UniqueBPlusTree.this.nodeRecPrototype.clone();
			this.indexPage.read(this.indexPage.getNumRecords()-1, nodeRecord);
			return (T) nodeRecord.getValue(KEY_POS);
		}

		@Override
		public void split(AbstractIndexElement node1, AbstractIndexElement node2) {
			int cnt = this.indexPage.getNumRecords();
			AbstractRecord nodeRecord = UniqueBPlusTree.this.nodeRecPrototype.clone();
			int slotNumber=0;
			
			for(; slotNumber<cnt/2; ++slotNumber){
				this.indexPage.read(slotNumber, nodeRecord);
				node1.indexPage.insert(nodeRecord);
			}
			
			for(; slotNumber<cnt; ++slotNumber){
				this.indexPage.read(slotNumber, nodeRecord);
				node2.indexPage.insert(nodeRecord);
			}
			UniqueBPlusTree.this.releaseIndexElement(this.indexPage.getPageNumber());
		}

		@Override
		public AbstractRecord lookup(T key) {
			//TODO: implement this method
			
			//Sucht im inneren Knoten den passenden  Verweis (Seitennummer des Kindknotens) und delegiert Suche an Kindknoten
			//Beim lookup prŸfen ob zahl<= wert, dann binarySearch() dann von indexElements zur seite gehn
			
			int slotNumber = this.binarySearch(key);
			AbstractRecord nodeRecord = UniqueBPlusTree.this.nodeRecPrototype.clone();
			this.indexPage.read(slotNumber, nodeRecord);
			
			SQLInteger pageNumber = (SQLInteger) nodeRecord.getValue(PAGE_POS);
			AbstractIndexElement child = UniqueBPlusTree.this.getIndexElement(pageNumber.getValue());
			
			return child.lookup(key);
		}

		@Override
		public AbstractIndexElement createInstance() {
			return new Node();
		}

		@Override
		protected void print(int level) {
			String indent = "";
			for(int i=0; i<level; ++i){
				indent+="\t";
			}
			
			Vector<AbstractIndexElement> children = new Vector<AbstractIndexElement>();
			AbstractRecord nodeRecord = UniqueBPlusTree.this.nodeRecPrototype.clone();
			
			System.out.println(indent+"Node:"+this.indexPage.getPageNumber());
			for(int i=0; i<this.indexPage.getNumRecords();++i){
				this.indexPage.read(i, nodeRecord);
				System.out.println(indent+nodeRecord.toString());
				
				SQLInteger pageNumber = (SQLInteger)nodeRecord.getValue(PAGE_POS);
				children.add(UniqueBPlusTree.this.getIndexElement(pageNumber.getValue()));
			}
			System.out.println("");
			
			for(AbstractIndexElement child: children){
				child.print(level+1);
			}
		}
	}
	
	/**
	 * Index leaf
	 * @author cbinnig
	 */
	protected class Leaf extends AbstractIndexElement{
		/**
		 * Leaf constructor
		 */
		public Leaf(){
			this.indexPage = PageManager.createDefaultPage(UniqueBPlusTree.this.leafRecPrototype.getFixedLength());
			UniqueBPlusTree.this.addIndexElement(this);
		}
		
		@Override
		public int binarySearch(T key){
			AbstractRecord leafRecord = UniqueBPlusTree.this.leafRecPrototype.clone();
			return this.binarySearch(key, leafRecord);
		}
		
		@Override
		public boolean insert(T key, AbstractRecord record){
			//search for key and return false if existing
			int slotNumber = 0;
			AbstractRecord leafRecord = UniqueBPlusTree.this.leafRecPrototype.clone();
			if(this.indexPage.getNumRecords()>0){
				slotNumber = this.binarySearch(key);
				
				if(slotNumber<this.indexPage.getNumRecords()){
					this.indexPage.read(slotNumber, leafRecord);
					
					if(leafRecord.getValue(UniqueBPlusTree.this.keyColumnNumber).compareTo(key)==0)
						return false;	
				}
			}
			
			//insert new record into table and leaf
			RowIdentifier rid = UniqueBPlusTree.this.table.insert(record);
			
			leafRecord.setValue(UniqueBPlusTree.KEY_POS, key );
			leafRecord.setValue(UniqueBPlusTree.PAGE_POS, new SQLInteger(rid.getPageNumber()) );
			leafRecord.setValue(UniqueBPlusTree.SLOT_POS, new SQLInteger(rid.getSlotNumber()) );
			this.indexPage.insert(slotNumber, leafRecord, true);
			
			return true;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public T getMaxKey() {
			AbstractRecord leafRecord = UniqueBPlusTree.this.leafRecPrototype.clone();
			this.indexPage.read(this.indexPage.getNumRecords()-1, leafRecord);
			return (T) leafRecord.getValue(KEY_POS);
		}

		@Override
		public void split(AbstractIndexElement leaf1, AbstractIndexElement leaf2) {
			int cnt = this.indexPage.getNumRecords();
			AbstractRecord leafRecord = UniqueBPlusTree.this.leafRecPrototype.clone();
			int slotNumber=0;
			
			for(; slotNumber<cnt/2; ++slotNumber){
				this.indexPage.read(slotNumber, leafRecord);
				leaf1.indexPage.insert(leafRecord);
			}
			
			for(; slotNumber<cnt; ++slotNumber){
				this.indexPage.read(slotNumber, leafRecord);
				leaf2.indexPage.insert(leafRecord);
			}
			UniqueBPlusTree.this.releaseIndexElement(this.indexPage.getPageNumber());
		}

		@Override
		public AbstractRecord lookup(T key) {
			//TODO: implement this method
			
			int slotNumber = this.binarySearch(key);
			AbstractRecord leafRecord = UniqueBPlusTree.this.leafRecPrototype.clone();
			
			this.indexPage.read(slotNumber, leafRecord);
			
			
			SQLInteger slot = (SQLInteger) leafRecord.getValue(SLOT_POS);
			SQLInteger page = (SQLInteger) leafRecord.getValue(PAGE_POS);
			
			
			return table.lookup(page.getValue(), slot.getValue());
		}
		
		@Override
		public AbstractIndexElement createInstance() {
			return new Leaf();
		}

		@Override
		protected void print(int level) {
			String indent = "";
			for(int i=0; i<level; ++i){
				indent+="\t";
			}
			
			AbstractRecord leafRecord = UniqueBPlusTree.this.leafRecPrototype.clone();
			
			System.out.println(indent+"Leaf:"+this.indexPage.getPageNumber());
			for(int i=0; i<this.indexPage.getNumRecords();++i){
				this.indexPage.read(i, leafRecord);
				System.out.println(indent + leafRecord.toString());
				
				SQLInteger pageNumber = (SQLInteger)leafRecord.getValue(PAGE_POS);
				SQLInteger slotNumber = (SQLInteger)leafRecord.getValue(SLOT_POS);
				System.out.println(indent + UniqueBPlusTree.this.table.lookup(pageNumber.getValue(), slotNumber.getValue()));
			}
			System.out.println("");
		}
	}
	
	//constants
	public static final int KEY_POS = 0;
	public static final int PAGE_POS = 1;
	public static final int SLOT_POS = 2;
	public static final int DEFAULT_FILL_GRADE = 100;
	
	protected int keyColumnNumber; //column number of key attribute in table
	protected int maxFillGrade; //max. fill grade
	
	protected AbstractRecord nodeRecPrototype; //node record: [keyValue: T, pageNumber: SQLInteger]
	protected AbstractRecord leafRecPrototype; //leaf record: [keyValue: T, pageNumber: SQLInteger, slot number: SQLInteger]
	
	protected AbstractIndexElement root; //root index element
	
	private HashMap<Integer, AbstractIndexElement> indexElements = new HashMap<Integer, AbstractIndexElement>(); //map (pageNumber, indexElement)
	
	/**
	 * Constructor of B+-Tree with user-defined fil-grade
	 * @param table Table to be indexed
	 * @param keyColumnNumber Number of unique column which should be indexed
	 * @param fillGrade fill grade of index
	 */
	public UniqueBPlusTree(AbstractTable table, int keyColumnNumber, int fillGrade) {
		super(table);
		
		//set index properties
		this.keyColumnNumber = keyColumnNumber;
		this.maxFillGrade = fillGrade*2;
	
		//attributes of index records
		AbstractSQLValue keyValue = this.table.getPrototype().getValue(keyColumnNumber);
		SQLInteger pageNumber = new SQLInteger();
		SQLInteger slotNumber = new SQLInteger();
		
		//prototype for node record
		this.nodeRecPrototype = new Record(2);
		this.nodeRecPrototype.setValue(KEY_POS, keyValue);
		this.nodeRecPrototype.setValue(PAGE_POS, pageNumber);
		
		//prototype for leaf record
		this.leafRecPrototype = new Record(3);
		this.leafRecPrototype.setValue(KEY_POS, keyValue);
		this.leafRecPrototype.setValue(PAGE_POS, pageNumber);
		this.leafRecPrototype.setValue(SLOT_POS, slotNumber);
		
		//create root node
		this.root = new Leaf();
		
		//check fill grade
		if((this.maxFillGrade+1) > AbstractPage.estimateRecords(this.leafRecPrototype) ){
			throw new IllegalArgumentException("Fill grade exceeds number of records that can be stored in a index node!");
		}
	} 
	
	/**
	 * Constructor for B+-tree with default fill grade
	 * @param table table to be indexed 
	 * @param keyNumber Number of unique column which should be indexed
	 */
	public UniqueBPlusTree(AbstractTable table, int keyColumnNumber) {
		this(table, keyColumnNumber, DEFAULT_FILL_GRADE);
	}

	/**
	 * Add index element to list of index elements in index
	 * @param node
	 */
	private void addIndexElement(AbstractIndexElement indexElement){;
		this.indexElements.put(indexElement.getPageNumber(), indexElement);
	}
	
	/**
	 * Get index element by page number
	 * @param pageNumber
	 * @return
	 */
	private AbstractIndexElement getIndexElement(int pageNumber){
		return this.indexElements.get(pageNumber);
	}
	
	/**
	 * Remove index element by page number
	 * @param pageNumber
	 */
	private void releaseIndexElement(int pageNumber){
		this.indexElements.remove(pageNumber);
	}
	
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public boolean insert(AbstractRecord record) {
		//insert record
		T key = (T) record.getValue(this.keyColumnNumber);
		boolean inserted =  this.root.insert(key, record);
		
		//split root if necessary
		if(inserted && root.isFull()){
			//split current root
			AbstractIndexElement indexElement1 = this.root.createInstance();
			AbstractIndexElement indexElement2 = this.root.createInstance();
			root.split(indexElement1, indexElement2);
			
			//create new root node
			this.root = new Node();
			AbstractRecord nodeRecord = UniqueBPlusTree.this.nodeRecPrototype.clone();
			nodeRecord.setValue(KEY_POS, indexElement1.getMaxKey() );
			nodeRecord.setValue(PAGE_POS, new SQLInteger(indexElement1.getPageNumber()) );
			root.indexPage.insert(nodeRecord);
			nodeRecord.setValue(KEY_POS, indexElement2.getMaxKey() );
			nodeRecord.setValue(PAGE_POS, new SQLInteger(indexElement2.getPageNumber()) );
			root.indexPage.insert(nodeRecord);
		}
		
		return inserted;
	}
	
	@Override
	public AbstractRecord lookup(T key) {
		return this.root.lookup(key);
	}

	@Override
	public void print() {
		this.root.print();
	}
}