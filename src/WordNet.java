import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WordNet {
	private static String DELIM_COMMA = ",";
	private static String DELIM_SPACE = " ";
	private Map<String, Set<Integer>> synsetDict = new HashMap<String, Set<Integer>>();
	private List<String> synsetCol = new ArrayList<String>();
	private SAP sap;

	// constructor takes the name of the two input files
	public WordNet(String synsets, String hypernyms) {
		In synsetIn = new In(synsets);
		int V = 0;
		while (synsetIn.hasNextLine()) {
			V++;
			String line = synsetIn.readLine();
			String[] splitted = line.split(DELIM_COMMA);
			int id = Integer.parseInt(splitted[0]);
			synsetCol.add(splitted[1]);
			String[] nouns = splitted[1].split(DELIM_SPACE);
			for (String noun : nouns) {
				if (!synsetDict.containsKey(noun)) {
					synsetDict.put(noun, new HashSet<Integer>());
				}
				synsetDict.get(noun).add(id);
			}
		}

		Digraph g = new Digraph(V);
		In hypernymsIn = new In(hypernyms);
		while (hypernymsIn.hasNextLine()) {
			String[] splitted = hypernymsIn.readLine().split(DELIM_COMMA);
			int focalSID = Integer.parseInt(splitted[0]);
			for (int i = 1; i < splitted.length; i++) {
				int hypernymId = Integer.parseInt(splitted[i]);
				g.addEdge(focalSID, hypernymId);
			}
		}
		sap = new SAP(g);
	}

	// returns all WordNet nouns
	public Iterable<String> nouns() {
		return synsetDict.keySet();
	}

	// is the word a WordNet noun?
	public boolean isNoun(String word) {
		return synsetDict.keySet().contains(word);
	}

	// distance between nounA and nounB (defined below)
	public int distance(String nounA, String nounB) throws IllegalArgumentException {
		if (!isNoun(nounA) || !isNoun(nounB)) {
			throw new IllegalArgumentException("At least one of the words is not a WordNet noun");
		}
		Set<Integer> aSynsets = synsetDict.get(nounA);
		Set<Integer> bSynsets = synsetDict.get(nounB);
		return sap.length(aSynsets, bSynsets);
	}

	// a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
	// in a shortest ancestral path (defined below)
	public String sap(String nounA, String nounB) throws IllegalArgumentException {
		if (!isNoun(nounA) || !isNoun(nounB)) {
			throw new IllegalArgumentException("At least one of the words is not a WordNet noun");
		}
		Set<Integer> aSynsets = synsetDict.get(nounA);
		Set<Integer> bSynsets = synsetDict.get(nounB);
		int ancestorId = sap.ancestor(aSynsets, bSynsets);
		return synsetCol.get(ancestorId);
	}

	// do unit testing of this class
	public static void main(String[] args) {

	}
}