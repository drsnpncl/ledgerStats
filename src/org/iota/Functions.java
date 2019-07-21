package org.iota;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*	Common basic functions to be used	*/
public class Functions {
	
	/*	Reads the ledger(database file) and returns a list of all transactions	*/
	public List<Transaction> readLedger(String database){
		List<Transaction> transactions = new ArrayList<>();
		int nodes;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(database));
			nodes = Integer.parseInt(br.readLine());
			for(int id = 1; id <= nodes; id++) {
				transactions.add(convertToObject(br.readLine(), id));
			}
			br.close();
		} catch (IOException e) {
			System.out.println("Error reading database file. Please ensure it is properly structured.");
		}
		return transactions;
	}

	/*	Returns a map of (transaction, depth)	*/
	/*	The algorithm used here is inspired from Dijkstra's shortest path algorithm, but not entirely.
	 *	Here is how it goes,
	 *	First, the map of (node, list of it's in-references) is generated using getInReferences() function.
	 *	An N x N matrix of distances is initiated with all the values initialized at some MAX number(N+1), where N is number of transactions in the ledger. 
	 *	The depth can not be more than number of transactions, that's why I'm using N+1.
	 * 	The distance of origin to itself is set to 0
	 * 	
	 * 	For all the nodes, we check their in-references only. If in-references have distance to origin more than the node's distance to origin + 1, we change it to node's distance to origin + 1.
	 * 	
	 * 	if dist(in-ref, origin) < dist(transaction, origin) + 1
	 * 		depth =  dist(in-ref, origin)
	 * 	else depth =  dist(transaction, origin) + 1
	 * 
	 * 	It return N x N matrix, then find minimum distance from each column, ie depth of that node.
	 * 
	 * */
	public Map<Integer, Integer> generateMinDepth(List<Transaction> ledger) {
		Map<Integer, List<Integer>> referenceMap = getInReferences(ledger);
		int nodes = ledger.size() + 1;
		int[][]  distances = new int[nodes][nodes];
		Map<Integer, Integer> minDepth = new HashMap<>();
		
		for( int row = 0; row < nodes; row++) {
			for( int column = 0; column < nodes; column++) {
				if(row == 0 && column == 0) distances[row][column] = 0;
				else distances[row][column] = nodes;
			}
		}
		
		for(int id = 1; id < nodes; id++) {
			List<Integer> values = referenceMap.get(id);
			for(Integer value : values) {
				if(id == 1) {
					distances[id - 1][value - 1] =  1;
				} else {
					distances[id - 1][value - 1] = distances[id - 2][value - 1] > distances[id - 2][id - 1] + 1 ? distances[id - 2][id - 1] + 1 : distances[id - 2][value - 1];
				}
			}
		}
		
		for(int column = 0; column < nodes; column++) {
			int[] depths = new int[nodes];
			for(int row = 0; row < nodes; row++) {
				depths[row] = distances[row][column];
			}
			minDepth.put(column + 1, getMin(depths));
		}
		
		/*for( int row = 0; row < nodes; row++) {
			for( int column = 0; column < nodes; column++) {
				System.out.print(distances[row][column]);
				System.out.print("\t");
			}
			System.out.println();
		}*/
		
		return minDepth;
	}
	
	/*	It goes through the whole ledger and makes a map of (transaction, list of its in-references)	*/
	public Map<Integer, List<Integer>> getInReferences(List<Transaction> ledger) {
		int nodes = ledger.size() + 1; 
		Map<Integer, List<Integer>> referenceMap = new HashMap<>();
		for(int id = 1; id <= nodes; id++) {
			List<Integer> references = new ArrayList<>();
			for(Transaction node : ledger) {
				if(node.getLeftParentTransaction() == id) references.add(node.getId());
				if(node.getRightParentTransaction() == id) references.add(node.getId());
			}
			referenceMap.put(id, references);
		}
		return referenceMap;
	}

	/*	Returns a map of (transaction, list of approved transactions)*/
	/*	It is helpful to calculate how many previous transactions a new transaction approves. 
	 *	We can calculate the percentage of total transactions being approved by a single transaction.
	 * */
	public Map<Integer, List<Integer>> getApprovedTransactions(List<Transaction> ledger){
		Map<Integer, List<Integer>> inReferences = getInReferences(ledger);
		Map<Integer, List<Integer>> approvedTransactions = new HashMap<>();
		
		for(Integer key : inReferences.keySet()) {
			for(Integer transaction : inReferences.get(key)) {
				if(!approvedTransactions.containsKey(transaction)) {
					approvedTransactions.put(transaction, new ArrayList<>(Arrays.asList(key)));
				} else {
					List<Integer> approved = new ArrayList<>();
					approved = approvedTransactions.get(transaction);
					if(!approved.contains(key)) {
						approved.add(key);
						approvedTransactions.put(transaction, approved);
					}
				}
				
				List<Integer> previouslyApprovedList = new ArrayList<>();
				List<Integer> approved = new ArrayList<>();
				approved = approvedTransactions.get(transaction);
				for(Integer previouslyApproved : approved) {
					if(previouslyApproved != 1) {
						previouslyApprovedList.addAll(approvedTransactions.get(previouslyApproved));
					}
				}
				/*	To remove duplicates	*/
				Set<Integer> set = new LinkedHashSet<>(approved);
				set.addAll(previouslyApprovedList);
				/*	Ignoring origin	*/
				set.remove(1);
				List<Integer> finalList = new ArrayList<>(set);
				Collections.sort(finalList);
				approvedTransactions.put(transaction, finalList);
			}
		}
		return approvedTransactions;
	}
	
	/*	It takes string input and converts it to Transaction object	*/
	public Transaction convertToObject(String rawData, int id) {
		Transaction transaction = new Transaction();
		String[] array = rawData.split(" ");
		
		int[] parentTransactions = {1,1};
		parentTransactions[0] = Integer.parseInt(array[0]);
		parentTransactions[1] = Integer.parseInt(array[1]);
		
		transaction.setId(id+1);
		transaction.setParentTransactions(parentTransactions);
		transaction.setTimestamp(Long.parseLong(array[2]));
		
		return transaction;
	}

	/*	Print the whole ledger	*/
	public void printLedger(List<Transaction> transactions) {
		for(Transaction transaction : transactions) {
			System.out.println(transaction.toString());
		}
	}

	/*Find minimum from the input int array	*/
	public int getMin(int[] array) {
		int min = array[0];
		for(int i = 1; i < array.length; i++) min = array[i] < min ? array[i] : min;
		return min;
	}

	public float getMaxStrength(int ledgerSize) {
		int maxApprovals = 0;
		float avgMaxApprovals = 0f;
		for(int i = 1; i < ledgerSize; i++) {
			maxApprovals += i;
		}
		avgMaxApprovals = (float) maxApprovals/ledgerSize;
		return avgMaxApprovals;				
	}
}
