package org.iota;

public class Transaction {
	
	int id; //Unique identifier
	int[] parentTransactions; //List of parent transactions
	Long timestamp; //Time stamp in milliseconds
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int[] getParentTransactions() {
		return parentTransactions;
	}

	public void setParentTransactions(int[] parentTransactions) {
		this.parentTransactions = parentTransactions;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public int getLeftParentTransaction() {
		return getParentTransactions()[0];
	}
	
	public int getRightParentTransaction() {
		return getParentTransactions()[1];
	}
	
	public String toString() {
		return  id + "| \t" + parentTransactions[0] + "\t" + parentTransactions[1] + "\t" + timestamp;  
	}
}
