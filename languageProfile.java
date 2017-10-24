package darms_language_detection;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;

class ValueComparatorInteger implements Comparator<String> {

	Map<String, Integer> base;

	public ValueComparatorInteger(Map<String, Integer> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}

class EntryComparator implements Comparator<Entry<String, Integer>> {

	/**
	 * Implements descending order.
	 */
	@Override
	public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
		if (o1.getValue() < o2.getValue()) {
			return 1;
		} else if (o1.getValue() > o2.getValue()) {
			return -1;
		}
		return 0;
	}

}

public class languageProfile {
	private int n_gram;
	private Map<String, Integer> profile_count;
	private Map<String, Integer> n_gram_profile_count;
	// private ValueComparatorInteger bvc;
	// private TreeMap<String,Integer> profile_frequence;
	private String language_file_name;
	private int MAX_DISTANCE = 1000;

	public languageProfile(int n_gram, String language_file_name) {
		this.n_gram = n_gram;
		this.language_file_name = language_file_name;
		this.profile_count = new HashMap<String, Integer>();
		this.n_gram_profile_count = new HashMap<String, Integer>();

		// this.bvc = new ValueComparatorInteger(profile_count);
		// this.profile_frequence = new TreeMap<String,Integer>(bvc);
	}

	public void setGramNum(int n_gram) {
		this.n_gram = n_gram;
	}

	public int getGramNum() {
		return this.n_gram;
	}
	public Map<String, Integer> getN_Gram()
	{
		return this.n_gram_profile_count;
	}
	public List<Entry<String, Integer>> getTopKeysWithOccurences(int top) {
		List<Entry<String, Integer>> results = new ArrayList<>(
				this.profile_count.entrySet());
		Collections.sort(results, new EntryComparator());
		return results.subList(0, top);
	}

	public boolean createProfile() {
		try {
			FileReader fr = new FileReader(this.language_file_name);
			BufferedReader br = new BufferedReader(fr);
			String s;
			String[] return_words;
			while ((s = br.readLine()) != null) {
				s = s.trim();
				return_words = s.split(" ");
				for (String word : return_words) { // here, for each word,
													// calculate the N_gram
													// frequences

					for (int char_no = 0; char_no < word.length(); char_no++) {
						for (int i_gram = 1; i_gram <= this.n_gram; i_gram++) {
							String combineChars = "";

							for (int j = 0; j < i_gram; j++) {
								if (char_no + j < word.length())
									combineChars = combineChars
											+ word.charAt(char_no + j);
								else
									combineChars = combineChars + '#';
							}

							if (profile_count.containsKey(combineChars)) {
								profile_count.put(combineChars,
										profile_count.get(combineChars) + 1);
							} else {
								profile_count.put(combineChars, 1);
							}

						}
					}
				}

				/*
				 * if (profile_count.containsKey(word)) {
				 * profile_count.put(word,profile_count.get(word)+1); } else {
				 * profile_count.put(word,1); }
				 */

			}

			System.out.println("There are " + profile_count.size()
					+ " unique characters");

			// this.profile_frequence.putAll(profile_count);
			// System.out.println("There are "+this.profile_frequence.size()+" unique characters");

			List<Entry<String, Integer>> top_chars;

			top_chars = getTopKeysWithOccurences(500);
			
			int rank_index = 0;
			int last_value = -1;
			for (Entry<String, Integer> a : top_chars) {
				//System.out.println(a.getKey() + " : " + a.getValue());
				if (last_value != a.getValue())
					rank_index ++;
				n_gram_profile_count.put(a.getKey(), rank_index);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public int compute_distance(Map<String, Integer> document_n_gram_profile)
	{
		int distance_sum = 0;
		int single_distance = 0;
		System.out.println("size of document_n_gram = " + document_n_gram_profile.size());
		for( String key : document_n_gram_profile.keySet())
		{
			if (!this.n_gram_profile_count.containsKey(key))
			{
				distance_sum += MAX_DISTANCE;
				continue;
			}
			if (document_n_gram_profile.get(key) != this.n_gram_profile_count.get(key))
			{
				/*System.out.println("key = "+key);
				System.out.println("document_n_gram_profile.get(key) = "+document_n_gram_profile.get(key));
				System.out.println("this.n_gram_profile_count.get(key)"+this.n_gram_profile_count.get(key));*/
				single_distance = document_n_gram_profile.get(key) - this.n_gram_profile_count.get(key);
				if (single_distance < 0)
					single_distance = - single_distance;
				distance_sum += single_distance;
			}
		}
		
		return distance_sum;
	}

	public static void main(String[] argvs) {
		languageProfile german = new languageProfile(5,
				"/work/smt2/dzhu/data/IWSLT/indomain-2013/source_noindex_sen_end");
		german.createProfile();
		languageProfile english = new languageProfile(5,
				"/work/smt2/dzhu/data/IWSLT/indomain-2013/target_noindex_sen_end");
		english.createProfile();
		languageProfile test_language = new languageProfile(5,"/work/smt2/dzhu/data/IWSLT/indomain-2013/source_noindex_sen_end_short");
		test_language.createProfile();
		int english_score = english.compute_distance(test_language.getN_Gram());
		int german_score = german.compute_distance(test_language.getN_Gram());
		System.out.println("german score = "+german_score + "english score = "+english_score );
		System.out.println("Hello World!!");
		// System.out.println((char) 220);
	}

}
