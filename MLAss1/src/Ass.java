import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class Ass {
	protected int countForSupport = 1;
	protected double support = 0.01;
	protected double confidence = 0;
	
	protected Map<Long, Set<String>> basketToCat;
	protected Map<String, Set<Long>> catToBasket;
	protected int nTransactions;

	public Ass() throws FileNotFoundException {
		read();
		countForSupport = (int) (this.support * nTransactions);
		System.out.println("SuppCnt=" + countForSupport);

		work();
	}

	public static void main (String [] args) throws FileNotFoundException {
		new Ass();	
	}
	
	protected void read() throws FileNotFoundException {
		Scanner in = new Scanner(new FileInputStream("supermarket.arff"));
		try {
			basketToCat = new HashMap<Long, Set<String>>();
			catToBasket = new HashMap<String, Set<Long>>();
			nTransactions = 0;
			while (in.hasNext()) {
				String[] purchase = in.nextLine().split(",");
				if (purchase.length != 4)
					continue;
				
				String cat = purchase[1];
				Long basketId = Long.parseLong(purchase[3]);
				if (!basketToCat.containsKey(basketId)) {
					basketToCat.put(basketId, new HashSet<String>());
				}
				basketToCat.get(basketId).add(cat);

				if (!catToBasket.containsKey(cat)) {
					catToBasket.put(cat, new HashSet<Long>());
				}
				catToBasket.get(cat).add(basketId);		
				nTransactions++;
			}
		} finally {
			in.close();
		}	
	}
	
	protected void work() {		
		List<List<String>> freqSets = new ArrayList<List<String>>();
		List<List<String>> oneSets = new ArrayList<List<String>>();
		for (String cat : catToBasket.keySet()) {
			if (catToBasket.get(cat).size() >= countForSupport) {
				List<String> newSet = new ArrayList<String>();
				newSet.add(cat);
				freqSets.add(newSet);
				oneSets.add(newSet);
			}
		}
		
		for (int i = 2; i <= catToBasket.keySet().size(); i++){
			List<List<String>> newSets = genSets(oneSets, i);
			if (newSets.isEmpty())
				break;
			for (List<String> newSet : newSets){
				freqSets.add(newSet);
			}
		}
				
		for (List<String> freqSet : freqSets) {
			Set<String> productCatSet = new HashSet<>();
			productCatSet.addAll(freqSet);
			
			int sup = calculateSupport(freqSet);
			for (Set<String> from : getSubsets(productCatSet)) {
				if (from.size() > 0 && from.size() < productCatSet.size()) {
					Set<String> to = new HashSet<>();
					to.addAll(productCatSet);
					to.removeAll(from);
					List<String> fromToList = new ArrayList<String>();
					fromToList.addAll(from);
					double conf = 1. * sup / calculateSupport(fromToList);
					if (conf >= confidence) {
						List<String> toToList = new ArrayList<String>();
						toToList.addAll(to);
						printRule(fromToList, toToList, conf);
					}
				}
			}
		}
	}
	
	public void printRule(List<String> fromToList, List<String> toToList, double conf) {
		for (String from : fromToList) {
			System.out.print(from + ",");
		}
		System.out.print(" ==> ");
		for (String to : toToList) {
			System.out.print(to + ",");
		}
		System.out.println("   conf = " + conf);
	}
	
	protected int calculateSupport(List<String> prSet) {
		int minSup = Integer.MAX_VALUE;
		String minSupCat = "";
		for (String cat : prSet) {
			if (catToBasket.get(cat).size() < minSup){
				minSup = catToBasket.get(cat).size();
				minSupCat = cat;
			}
		}
		int sup = 0;
		for (Long basket : catToBasket.get(minSupCat)) {
			if (basketToCat.get(basket).containsAll(prSet))
				sup++;
		}
		return sup;
	}

	public Set<Set<String>> getSubsets(Set<String> original) {
		Set<Set<String>> subsets = new HashSet<Set<String>>();
	    subsets.add(new HashSet<String>()); //empty set
	    for (String element : original) {
	        Set<Set<String>> tempClone = new HashSet<Set<String>>(subsets);
	        for (Set<String> subset : tempClone) {
	            Set<String> extended = new HashSet<String>(subset);
	            extended.add(element);
	            subsets.add(extended);
	        }
	    }
	    return subsets;
	}
	
	protected List<List<String>> genSets(List<List<String>> oneSets, int newDim){
		List<List<String>> res = new ArrayList<List<String>>();
		for (List<String> possible : getCombinations(oneSets, newDim)) {
			if (calculateSupport(possible) >= countForSupport)
				res.add(possible);
		}
		return res;
	}
	
	public List<List<String>> getCombinations(List<List<String>> oneSets, int k){
		List<String> ones = new ArrayList<String>();
		for (List<String> el : oneSets){
			ones.addAll(el);
		}
		List<List<String>> combinations = new ArrayList<List<String>>();
		buildComb(combinations, new ArrayList<String>(), 0, ones, k);
		return combinations;

	}

	protected void buildComb(List<List<String>> combinations, List<String> newComb, int minInd, List<String> ones, int k) {
		if (k == 0) {
			combinations.add(newComb);
			return;
		}
		for (int i = minInd; i < ones.size(); ++i) {
			List<String> newerComb = new ArrayList<String>();
			newerComb.addAll(newComb);
			newerComb.add(ones.get(i));
			buildComb(combinations, newerComb, i + 1, ones, k - 1);
		}
	}

}