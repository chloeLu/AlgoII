public class SAP {

	private final Digraph g;
	private static int INFINITE = Integer.MAX_VALUE;

	// constructor takes a digraph (not necessarily a DAG)
	public SAP(Digraph G) {
		g = new Digraph(G);
	}

	// length of shortest ancestral path between v and w; -1 if no such path
	public int length(int v, int w) {
		BreadthFirstDirectedPaths vBFS = new BreadthFirstDirectedPaths(g, v);
		BreadthFirstDirectedPaths wBFS = new BreadthFirstDirectedPaths(g, w);
		return length(vBFS, wBFS);
	}

	// a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
	public int ancestor(int v, int w) {
		BreadthFirstDirectedPaths vBFS = new BreadthFirstDirectedPaths(g, v);
		BreadthFirstDirectedPaths wBFS = new BreadthFirstDirectedPaths(g, w);
		return ancestor(vBFS, wBFS);
	}

	// length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
	public int length(Iterable<Integer> v, Iterable<Integer> w) {
		BreadthFirstDirectedPaths vBFS = new BreadthFirstDirectedPaths(g, v);
		BreadthFirstDirectedPaths wBFS = new BreadthFirstDirectedPaths(g, w);
		return length(vBFS, wBFS);
	}

	// a common ancestor that participates in shortest ancestral path; -1 if no such path
	public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
		BreadthFirstDirectedPaths vBFS = new BreadthFirstDirectedPaths(g, v);
		BreadthFirstDirectedPaths wBFS = new BreadthFirstDirectedPaths(g, w);
		return ancestor(vBFS, wBFS);
	}

	private int length(BreadthFirstDirectedPaths p1, BreadthFirstDirectedPaths p2) {
		int minDist = INFINITE;
		for (int i = 0; i < g.V(); i++) {
			int distV = p1.distTo(i);
			int distW = p2.distTo(i);
			if (distV != INFINITE && distW != INFINITE && distV + distW < minDist) {
				minDist = distV + distW;
			}
		}
		return minDist == INFINITE ? -1 : minDist;
	}

	private int ancestor(BreadthFirstDirectedPaths p1, BreadthFirstDirectedPaths p2) {
		int minDist = INFINITE;
		int ancestor = -1;
		for (int i = 0; i < g.V(); i++) {
			int distV = p1.distTo(i);
			int distW = p2.distTo(i);
			if (distV != INFINITE && distW != INFINITE && distV + distW < minDist) {
				minDist = distV + distW;
				ancestor = i;
			}
		}
		return ancestor;
	}

	// do unit testing of this class
	public static void main(String[] args) {
		In filePath = new In(args[0]);
		SAP sap = new SAP(new Digraph(filePath));
		while (!StdIn.isEmpty()) {
			int v = StdIn.readInt();
			int w = StdIn.readInt();
			int length = sap.length(v, w);
			int ancestor = sap.ancestor(v, w);
			StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
		}
	}
}