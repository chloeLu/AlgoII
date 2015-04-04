public class Outcast {
	private WordNet wordnet;

	public Outcast(WordNet wordnet) { // constructor takes a WordNet object
		this.wordnet = wordnet;
	}

	public String outcast(String[] nouns) { // given an array of WordNet nouns, return an outcast
		long maxDist = 0;
		String outcast = "";
		for (int i = 0; i < nouns.length; i++) {
			String noun = nouns[i];
			long dist = 0;
			for (int j = 0; j < nouns.length; j++) {
				if (j != i) {
					dist += wordnet.distance(noun, nouns[j]);
				}
			}
			if (maxDist < dist) {
				maxDist = dist;
				outcast = noun;
			}
		}
		return outcast;
	}

	public static void main(String[] args) {
		WordNet wordnet = new WordNet(args[0], args[1]);
		Outcast outcast = new Outcast(wordnet);
		for (int t = 2; t < args.length; t++) {
			In in = new In(args[t]);
			String[] nouns = in.readAllStrings();
			StdOut.println(args[t] + ": " + outcast.outcast(nouns));
		}
	}
}