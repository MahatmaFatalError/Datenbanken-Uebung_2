package de.dhbw.db2.youdb.storage;

import de.dhbw.db2.youdb.storage.types.AbstractSQLValue;
import de.dhbw.db2.youdb.storage.types.SQLInteger;


public class RowPage extends AbstractPage {

	/**
	 * Constructir for a row page with a given (fixed) slot size
	 * @param slotSize
	 */
	public RowPage(int slotSize) {
		super(slotSize);
	}
	
	@Override
	public void insert(int slotNumber, AbstractRecord record, boolean doInsert) {
		if(record.getFixedLength()!=this.slotSize)
			throw new IllegalArgumentException("Fixed length of record length does not fit to page slot size");
		if(slotNumber>this.numRecords)
			throw new IllegalArgumentException("Slot number must be less than or equal the number of recaords in page");
		
		//calculate offset
		int offset = slotNumber * this.slotSize;
		
		//shift data 
		if(doInsert){
			if(!this.recordFitsIntoPage(record))
				throw new IllegalArgumentException("Record data does not fit into page");
			
			if(slotNumber<this.numRecords)
				System.arraycopy(this.data, offset, this.data, offset+this.slotSize, this.offset - offset);
			
			this.offset += this.slotSize;
			this.numRecords++;
		}
		
		//insert at calculated offset 
		this.insertAtOffset(offset, record);
	}
	
	@Override
	public int insert(AbstractRecord record){
		if(record.getFixedLength()!=this.slotSize)
			throw new IllegalArgumentException("Fixed length of record length does not fit to page slot size");
		else if(!this.recordFitsIntoPage(record))
			throw new IllegalArgumentException("Record data does not fit into page");
		
		//insert at calculated offset 
		this.insertAtOffset(this.offset, record);
		this.offset += this.slotSize;
		
		return this.numRecords++;
	}
	
	private void insertAtOffset(int writeOffset, AbstractRecord record) {	
		//copy data
		for(AbstractSQLValue value: record.getValues()){
			byte[] writeData = value.serialize();
			if(value.isFixedLength()){
				//copy fixed-size data
				System.arraycopy(writeData, 0, this.data, writeOffset, writeData.length);
				writeOffset += writeData.length;
			}
			else{
				//copy variable-size data 
				this.offsetEnd -= writeData.length;
				System.arraycopy(writeData, 0, this.data, this.offsetEnd, writeData.length);
				
				//copy length of variable-size data 
				SQLInteger writeLength = new SQLInteger(writeData.length);
				byte[] writeLengthData = writeLength.serialize();
				System.arraycopy(writeLengthData, 0, this.data, writeOffset, writeLengthData.length);
				writeOffset += writeLengthData.length;
								
				//copy offset of variable-size data 
				SQLInteger writeOffsetEnd = new SQLInteger(this.offsetEnd);
				byte[] writeOffsetEndData = writeOffsetEnd.serialize();
				System.arraycopy(writeOffsetEndData, 0, this.data, writeOffset, writeOffsetEndData.length);
				writeOffset += writeOffsetEndData.length;
			}
		}
	}
	
	@Override
	public void read(int slotNumber, AbstractRecord record){
		if(slotNumber>this.numRecords){
			throw new IllegalArgumentException("Slot number not valid");
		}
		
		int readOffset = slotNumber * this.slotSize;
		
		for(AbstractSQLValue value: record.getValues()){
			if(value.isFixedLength()){
				//copy fixed-size data
				byte[] data = new byte[value.getMaxLength()];
				System.arraycopy(this.data, readOffset, data, 0, data.length);
				readOffset += data.length;
				value.deserialize(data);
			}
			else{
				//copy length of variable-size data 
				SQLInteger readLength = new SQLInteger();
				byte[] readLengthData = new byte[readLength.getMaxLength()];
				System.arraycopy(this.data, readOffset, readLengthData, 0, readLengthData.length);
				readOffset += readLengthData.length;
				readLength.deserialize(readLengthData);
				
				//copy offset of variable-size data 
				SQLInteger readOffsetEnd = new SQLInteger();
				byte[] offsetEndData = new byte[readOffsetEnd.getMaxLength()];
				System.arraycopy(this.data, readOffset, offsetEndData, 0, offsetEndData.length);
				readOffset += offsetEndData.length;
				readOffsetEnd.deserialize(offsetEndData);
				
				//copy variable-size data 
				byte[] data = new byte[readLength.getValue()];
				System.arraycopy(this.data, readOffsetEnd.getValue(), data, 0, data.length);
				value.deserialize(data);
			}
		}
	}
}
