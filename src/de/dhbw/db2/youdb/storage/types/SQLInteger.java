package de.dhbw.db2.youdb.storage.types;

/**
 * SQL integer value
 * @author cbinnig
 *
 */
public class SQLInteger extends AbstractSQLValue {
	private static int[] SHIFT = {0,8,16,24};
	public static int MASK = 0xff;
	
	public static int LENGTH = 4; //fixed length
	private int value = 0; //integer value
	
	/**
	 * Constructor with default value
	 */
	public SQLInteger(){
		super(SQLType.SqlInteger, LENGTH);
		this.value = 0;
	}
	
	/**
	 * Constructor with value
	 * @param value Integer value
	 */
	public SQLInteger(int value){
		super(SQLType.SqlInteger, LENGTH);
		this.value = value;
	}

	/**
	 * Returns integer value of SQLInteger
	 * @return
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Sets integer value of SQLInteger
	 * @param value
	 */
	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public byte[] serialize() {
		byte[] data = new byte[4];
		for (int i = 0; i < 4; ++i)
		    data[i] = (byte)(this.value >>> SHIFT[i]);

		return data;
	}

	@Override
	public void deserialize(byte[] data) {
		int value = 0;     
		for (int i = 0; i < 4; ++i)
		    value |= (data[i] & MASK) << SHIFT[i];
		
		this.value = value;
	}
	
	@Override
	public String toString(){
		return ""+this.value;
	}
	
	@Override
	public boolean equals(Object o){
		SQLInteger cmp = (SQLInteger)o;
		if(this.value==cmp.value)
			return true;
		
		return false;
	}

	@Override
	public int compareTo(AbstractSQLValue o) {
		SQLInteger cmp = (SQLInteger)o;
		
		if(this.value<cmp.value)
			return 1;
		else if(this.value>cmp.value)
			return -1;
		return 0;
	}
	
	@Override
	public SQLInteger clone(){
		return new SQLInteger(this.value);
	}

	@Override
	public void parseValue(String data) {
		this.value = Integer.parseInt(data);
	}

	@Override
	public int getFixedLength() {
		return LENGTH;
	}

	@Override
	public int getVariableLength() {
		return 0;
	}
}
