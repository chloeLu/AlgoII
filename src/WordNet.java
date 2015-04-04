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
	private Digraph g;
	private SAP sap;

	// constructor takes the name of the two input files
	public WordNet(String synsets, String hypernyms) {
		validateArgsNPE(synsets, hypernyms);
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

		g = new Digraph(V);
		In hypernymsIn = new In(hypernyms);
		while (hypernymsIn.hasNextLine()) {
			String[] splitted = hypernymsIn.readLine().split(DELIM_COMMA);
			int focalSID = Integer.parseInt(splitted[0]);
			for (int i = 1; i < splitted.length; i++) {
				int hypernymId = Integer.parseInt(splitted[i]);
				g.addEdge(focalSID, hypernymId);
			}
		}
		validateDAG(g);
		sap = new SAP(g);
	}

	private void validateDAG(Digraph g2) throws IllegalArgumentException {

		int root = -1;
		for (int v = 0; v < g2.V(); v++) {
			if (((Bag<Integer>) g2.adj(v)).size() == 0) {
				if (root != -1)
					throw new IllegalArgumentException("Not DAG- more than 1 root!");
				else
					root = v;
			}
		}

		DirectedCycle cycle = new DirectedCycle(g2);
		if (cycle.hasCycle()) {
			throw new IllegalArgumentException("Not DAG- graph has cycle!");
		}

	}

	// returns all WordNet nouns
	public Iterable<String> nouns() {
		return synsetDict.keySet();
	}

	// is the word a WordNet noun?
	public boolean isNoun(String word) {
		validateArgsNPE(word);
		return synsetDict.keySet().contains(word);
	}

	// distance between nounA and nounB (defined below)
	public int distance(String nounA, String nounB) {
		validateArgs(nounA, nounB);
		Set<Integer> aSynsets = synsetDict.get(nounA);
		Set<Integer> bSynsets = synsetDict.get(nounB);
		return sap.length(aSynsets, bSynsets);
	}

	// a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
	// in a shortest ancestral path (defined below)
	public String sap(String nounA, String nounB) {
		validateArgs(nounA, nounB);
		Set<Integer> aSynsets = synsetDict.get(nounA);
		Set<Integer> bSynsets = synsetDict.get(nounB);
		int ancestorId = sap.ancestor(aSynsets, bSynsets);
		return synsetCol.get(ancestorId);
	}

	private void validateArgsNPE(String... strings) throws NullPointerException {
		for (String s : strings) {
			if (s == null) {
				throw new NullPointerException("Arg cannot be null");
			}
		}
	}

	private void validateArgs(String s1, String s2) throws NullPointerException, IllegalArgumentException {
		validateArgsNPE(s1, s2);
		if (!isNoun(s1) || !isNoun(s2)) {
			throw new IllegalArgumentException("At least one of the words is not a WordNet noun");
		}
	}

	// do unit testing of this class
	public static void main(String[] args) {

	}
}