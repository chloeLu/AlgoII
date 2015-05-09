import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class BaseballElimination {
	private int numTeams = 0;
	private List<String> teams;
	private int[] w;
	private int[] l;
	private int[] r;
	private Map<Match, Integer> remainingMatches = new HashMap<Match, Integer>();

	// create a baseball division from given filename in format specified below
	public BaseballElimination(String filename) {
		In inFile = new In(new File(filename));
		numTeams = inFile.readInt();
		teams = new ArrayList<String>(numTeams);
		w = new int[numTeams];
		l = new int[numTeams];
		r = new int[numTeams];

		int countLn = 0;
		while (true) {
			try {
				teams.add(inFile.readString());
			} catch (NoSuchElementException e) {
				break;
			}
			w[countLn] = inFile.readInt();
			l[countLn] = inFile.readInt();
			r[countLn] = inFile.readInt();

			for (int i = 0; i < numTeams; i++) {
				// ignore these because no need to store
				if (i <= countLn) {
					inFile.readInt();
					continue;
				}
				// only store non-zero matches
				int num = inFile.readInt();
				if (num == 0) {
					continue;
				} else {
					remainingMatches.put(new Match(countLn, i), num);
				}
			}
			countLn++;
		}

	}

	// number of teams
	public int numberOfTeams() {
		return teams.size();
	}

	// all teams
	public Iterable<String> teams() {
		return teams;
	}

	// number of wins for given team
	public int wins(String team) throws IllegalArgumentException {
		int idx = teams.indexOf(team);
		validateArgs(idx);
		return w[idx];
	}

	// number of losses for given team
	public int losses(String team) {
		int idx = teams.indexOf(team);
		validateArgs(idx);
		return l[idx];
	}

	// number of remaining games for given team
	public int remaining(String team) {
		int idx = teams.indexOf(team);
		validateArgs(idx);
		return r[idx];
	}

	// number of remaining games between team1 and team2
	public int against(String team1, String team2) {
		int idx1 = teams.indexOf(team1);
		int idx2 = teams.indexOf(team2);
		validateArgs(idx1, idx2);

		// swap and put smaller idx in idx1
		if (idx2 < idx1) {
			int temp = idx1;
			idx1 = idx2;
			idx2 = temp;
		}

		Match match = new Match(idx1, idx2);
		return remainingMatches.get(match);
	}

	// is given team eliminated?
	public boolean isEliminated(String team) {
		int teamIdx = teams.indexOf(team);
		validateArgs(teamIdx);

		// trivial reasons
		int maxWins = w[teamIdx] + r[teamIdx];
		for (int win : w) {
			if (win >= maxWins) {
				return true;
			}
		}

		// flow diagram
		FlowNetwork fn = constructFlowNetwork(teamIdx, maxWins);
		FordFulkerson ff = new FordFulkerson(fn, 0, fn.V() - 1);
		int numTeams = this.numTeams - 1;
		int nGameVertices = numTeams * (numTeams - 1) / 2;
		for (int i = 1; i <= nGameVertices; i++) {
			if (ff.inCut(i)) {
				return false;
			}
		}
		return true;
	}

	private FlowNetwork constructFlowNetwork(int teamIdx, int maxWins) {
		int numTeams = this.numTeams - 1;
		// construct flow network
		int nGameVertices = numTeams * (numTeams - 1) / 2;
		int v = 1 + nGameVertices + numTeams + 1; // s + 1stLayer + 2ndLayer + t
		FlowNetwork fn = new FlowNetwork(v);

		int t1 = 0;
		int t2 = 1;
		for (int i = 1; i <= nGameVertices; i++) {
			// get capacity
			int t11 = t1 >= teamIdx ? t1+1 : t1;
			int t12 = t2 >= teamIdx ? t2+1 : t2;
			Integer num = remainingMatches.get(new Match(t11, t12));

			// s -> gameVertices: how many remaining matches
			if (num != null) {
				fn.addEdge(new FlowEdge(0, i, num));
			}

			// gameVertices -> teamVertices: infinite capacity
			fn.addEdge(new FlowEdge(i, nGameVertices + t1, Double.POSITIVE_INFINITY));
			fn.addEdge(new FlowEdge(i, nGameVertices + t2, Double.POSITIVE_INFINITY));

			// update team 1 and team 2
			if (t2 == numTeams - 1) {
				t1++;
				t2 = t1 + 1;
			} else {
				t2++;
			}
		}

		t1 = 0;
		for (int i = nGameVertices + 1; i < v - 1; i++) {
			t1 = (t1 == teamIdx) ? t1++ : t1;
			int maxWinsRemainingT1 = maxWins - w[t1];
			fn.addEdge(new FlowEdge(i, v - 1, (double) maxWinsRemainingT1));
		}

		return fn;
	}

	// subset R of teams that eliminates given team; null if not eliminated
	public Iterable<String> certificateOfElimination(String team) {
		int teamIdx = teams.indexOf(team);
		validateArgs(teamIdx);

		Set<String> eTeams = new HashSet<String>();
		// trivial elimination
		int maxWins = w[teamIdx] + r[teamIdx];
		for (int i = 0; i < teamIdx; i++) {
			if (w[i] > maxWins) {
				eTeams.add(teams.get(i));
			}
		}
		if (eTeams.size() > 0) {
			return eTeams;
		}

		FlowNetwork fn = constructFlowNetwork(teamIdx, maxWins);
		FordFulkerson ff = new FordFulkerson(fn, 0, fn.V() - 1);
		int numTeams = this.numTeams - 1;
		int nGameVertices = numTeams * (numTeams - 1) / 2;

		for (int i = 1; i <= nGameVertices; i++) {
			if (ff.inCut(i)) {
				int[] eliminatingTeamIdx = computeTeams(i, teamIdx, numTeams, nGameVertices);
				eTeams.add(teams.get(eliminatingTeamIdx[0]));
				eTeams.add(teams.get(eliminatingTeamIdx[1]));
			}
		}
		return eTeams;
	}

	public int[] computeTeams(int nGameV, int teamIdx, int numTeams, int nGameVertices) {
		int t1 = 0;
		int t2 = 1;
		for (int i = 1; i <= nGameVertices; i++) {
			// get capacity
			int t11 = t1 >= teamIdx ? t1+1 : t1;
			int t12 = t2 >= teamIdx ? t2+1 : t2;
			if (i == nGameV) {
				return new int[] { t11, t12 };
			}
			// update team 1 and team 2
			if (t2 == numTeams - 1) {
				t1++;
				t2 = t1 + 1;
			} else {
				t2++;
			}
		}
		return null;
	}

	private void validateArgs(int... args) {
		for (int i : args) {
			if (i == -1) {
				throw new IllegalArgumentException();
			}
		}
	}

	public static void main(String[] args) {
		BaseballElimination division = new BaseballElimination(args[0]);
		for (String team : division.teams()) {
			if (division.isEliminated(team)) {
				StdOut.print(team + " is eliminated by the subset R = { ");
				for (String t : division.certificateOfElimination(team)) {
					StdOut.print(t + " ");
				}
				StdOut.println("}");
			} else {
				StdOut.println(team + " is not eliminated");
			}
		}
	}
}

class Match {
	private Integer t1;
	private Integer t2;

	public Match(int t1, int t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Match) {
			Match anotherMatch = (Match) obj;
			return (t1 == anotherMatch.t1 && t2 == anotherMatch.t2);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return t1.hashCode() + t2.hashCode();
	}
	
	@Override
	public String toString() {
		return "Match [t1="+t1 + "; t2=" +t2 +"]";
	}
}
