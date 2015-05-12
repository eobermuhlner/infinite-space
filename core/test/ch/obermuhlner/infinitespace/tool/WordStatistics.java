package ch.obermuhlner.infinitespace.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.obermuhlner.infinitespace.model.random.Random;
import ch.obermuhlner.infinitespace.model.random.Random.Probability;
import ch.obermuhlner.infinitespace.model.random.Seed;

/**
 * Funny/interesting things that had to be handled.
 * 
 * - end of word is as important as start of word to give the feeling for the language
 * - important to handle special characters (e.g. with diacrits)
 * - beware files in strange encodings 
 * - German source (a random book from Gutenberg project) had a character that spoke french. Only noticed when some generated words had 'á' in it. 
 * - French has special case of the word "à" which is the only word starting with 'à'. Unhandled this could lead to generated words starting mith multiple "àà". 
 * - Italian source (the italian constitution) had lots of roman numbers. Produced entries for "i", "ii", "iii", which then generated words like "iiiiiiiiiiiiiiii".
 * - Conversion rules of diacrit characters depend on language. e.g. 'ä' in german is "ae" but in french/english it is "a" - not yet solved
 * - Will need a black list or maybe even heuristic to remove things that are not really words (e.g. roman numbers, "5th", abbreviations, ...)
 * 
 * 
 * Sources:
 *  English
 *   - Project Gutenberg's Frankenstein, by Mary Wollstonecraft (Godwin) Shelley
 *     http://www.gutenberg.org/cache/epub/84/pg84.txt
 *   - Project Gutenberg's The Yellow Wallpaper, by Charlotte Perkins Gilman
 *     http://www.gutenberg.org/cache/epub/1952/pg1952.txt
 *   - The Project Gutenberg EBook of Emma, by Jane Austen
 *     http://www.gutenberg.org/cache/epub/158/pg158.txt
 *     Removed VOLUME and CHAPTER lines (contained roman numbers)
 *     Removed "Emma"
 *  German:
 *    - https://www.gutenberg.org/cache/epub/2407/pg2407.txt
 *      The Project Gutenberg EBook of Die Leiden des jungen Werther--Buch 1, by Johann Wolfgang von Goethe   
 *  French:
 *    - Project Gutenberg's 20000 Lieues sous les mers (complète), by Jules Verne
 *      http://www.gutenberg.org/cache/epub/5097/pg5097.txt
 *      Removed entire TABLE DES MATIÈRES (contained roman numbers)
 *      Removed roman numbers of chapters
 *    - The Project Gutenberg EBook of Les misérables Tome I, by Victor Hugo
 *      http://www.gutenberg.org/cache/epub/17489/pg17489.txt
 *      Removed Chapitre lines (contained roman numbers)
 *  Italian:
 *    - Project Gutenberg's Le rive della Bormida nel 1794, by Giuseppe Cesare Abba
 *      http://www.gutenberg.org/cache/epub/21425/pg21425.txt
 *    - The Project Gutenberg EBook of Una Donna, by Sibilla Aleramo
 *      http://www.gutenberg.org/cache/epub/47786/pg47786.txt
 *      Removed [Illustration: LOGO]
 *      Removed table of contents and chapter roman numbers
 *  Spanish:
 *    - The Project Gutenberg EBook of Mala Hierba, by Pío Baroja
 *      http://www.gutenberg.org/cache/epub/43017/pg43017.txt 
 *  Portuguese:
 *    - Project Gutenberg's Historias Sem Data, by Joaquim Maria Machado de Assis
 *      http://www.gutenberg.org/cache/epub/33056/pg33056.txt
 *     
 *   In all cases removed Gutenberg prefix and postfix
 * 
 * Special sources:
 * - http://deron.meranda.us/data/
 */
public class WordStatistics {

	private static final int COUNT_GENERATED_WORD_PRINT = 20;
	private static final int COUNT_GENERATED_WORD_COLLISION_TEST = 1000;

	private static final boolean CONVERT_TO_ASCII = true;
	
	private static final String COMMON_CHARACTERS = "a-z0-9\\.\\-:<>#\"',?!&*/;\\[\\]\\(\\) ";
	
	private static final String WORD_CHARACTERS = "a-zßäöüéèêëáàâíìîïæœóòôúùûçñ";

	/**
	 * This that look like word but are not really words (e.g. roman numbers) 
	 */
	private static final Set<String> WORD_BLACKLIST = new HashSet<String>();
	static {
		// TODO generate more roman numbers
		WORD_BLACKLIST.addAll(Arrays.asList("i", "ii", "iii", "iiii", "iiv", "iv", "v", "vi", "vii", "viii", "viiii", "iix", "ix", "x", "xi", "xii", "xiii", "xiiv", "xiv"));
	}
	
	private static final Map<String, String> MAP_DIACRITS_TO_ASCII = new HashMap<String, String>();
	static {
		// TODO conversion rules depend on language. e.g. ä in german is "ae" but in french/english it is "a"
		MAP_DIACRITS_TO_ASCII.put("áàâ", "a");
		MAP_DIACRITS_TO_ASCII.put("éèêë", "e");
		MAP_DIACRITS_TO_ASCII.put("íìîï", "i");
		MAP_DIACRITS_TO_ASCII.put("óòô", "o");
		MAP_DIACRITS_TO_ASCII.put("úùû", "u");
		MAP_DIACRITS_TO_ASCII.put("æä", "ae");
		MAP_DIACRITS_TO_ASCII.put("œö", "oe");
		MAP_DIACRITS_TO_ASCII.put("ü", "ue");
		MAP_DIACRITS_TO_ASCII.put("ß", "ss");
		MAP_DIACRITS_TO_ASCII.put("ç", "c");
		MAP_DIACRITS_TO_ASCII.put("ñ", "n");
	}
	
	private final int partLength;
	private int minWordLength;

	private final Counter<String> counter = new Counter<String>();

	
	public WordStatistics (int partLength, int minWordLength) {
		this.partLength = partLength;
		this.minWordLength = minWordLength;
	}
	
	public boolean add (String word) {
		if (word.length() >= minWordLength) {
			addSubs(word, partLength);
			return true;
		}
		return false;
	}
	
	private void addSubs(String word, int length) {
		if (length > 1) {
			word = word + "$";
			if (word.length() == 2) {
				// special case: words of only 1 char must not enter the single char without end-marker
				// otherwise this fouls up in french where the word "à" is the only word starting with 'à'
				counter.add(word);
				return;
			}
		}
		
		// add all start-parts smaller than length
		for (int i = 1; i < Math.min(length, word.length()); i++) {
			String sub = word.substring(0, i);
			counter.add(sub);
		}
		
		// add all parts of exactly length
		for (int i = 0; i < word.length()-length+1; i++) {
			String sub = word.substring(i, i+length);
			counter.add(sub);
		}
	}
	
	public void print() {
		List<String> subs = new ArrayList<String>(counter.getElements());
		Collections.sort(subs);
		for (String sub : subs) {
			System.out.println(sub + " : " + counter.getCount(sub));
		}
	}

	private void writeToFile (String filename) {
		try {
			PrintWriter writer = new PrintWriter(filename);
			
			List<String> subs = new ArrayList<String>(counter.getElements());
			Collections.sort(subs);
			for (String sub : subs) {
				writer.println(sub + " " + counter.getCount(sub));
			}
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public Set<Entry<String, Integer>> getEntries() {
		return counter.getEntries();
	}
	
	public static void main (String[] args) {
		final int partLength = 4;
		final int analyzeMinWordLength = 5;
		final int generateMinWordLength = -1;
		
		//runGenerator("firstnames_male_en", partLength, 0, generateMinWordLength);
		//runGenerator("firstnames_female_en", partLength, 0, generateMinWordLength);
		//runGenerator("lastnames_en", partLength, 0, generateMinWordLength);
		//runGenerator("towns_uk", partLength, analyzeMinWordLength, generateMinWordLength);
		//runGenerator("nouns_en", partLength, analyzeMinWordLength, generateMinWordLength);
		runGenerator("words_en", partLength, analyzeMinWordLength, generateMinWordLength);
		runGenerator("words_de", partLength, analyzeMinWordLength, generateMinWordLength);
		runGenerator("words_fr", partLength, analyzeMinWordLength, generateMinWordLength);
		runGenerator("words_it", partLength, analyzeMinWordLength, generateMinWordLength);
		runGenerator("words_es", partLength, analyzeMinWordLength, generateMinWordLength);

//		runGenerator("test_words", partLength);
	}
	
	private static void runGenerator(String filename, int partLength, int analyzeMinWordLength, int generateMinWordLength) {
		WordStatistics statistics = createStatistics(filename, partLength, analyzeMinWordLength);
		statistics.writeToFile(filename + ".freq");
		
		WordGenerator generator = new WordGenerator(partLength);
		generator.loadFile(filename + ".freq");
		
		Random random = new Seed(0).getRandom();
		Set<String> generatedWords = new HashSet<String>();
		for (int i = 0; i < COUNT_GENERATED_WORD_COLLISION_TEST; i++) {
			String word;
			if (generateMinWordLength < 0) {
				word = generator.createWord(random);
			} else {
				word = generator.createWord(random, generateMinWordLength, 18);
			}
			if (i < COUNT_GENERATED_WORD_PRINT) {
				System.out.println("WORD " + word);
			}
			if (generatedWords.contains(word)) {
				System.out.println("COLLSION at [" + i + "] " + word);
			} else {
				generatedWords.add(word);
			}
		}
		
		System.out.println();
	}

	private static WordStatistics createStatistics (String filename, int partLength, int minWordLength) {
		System.out.println("### Analyzing corpus '" + filename + "' with part length " + partLength + " ignoring words under " + minWordLength);
		WordStatistics statistics = new WordStatistics(partLength, minWordLength);
		
		loadFileIfExists(filename + ".txt", statistics);
		boolean exists = true;
		for (int index = 1; exists==true; index++) {
			exists = loadFileIfExists(filename + index + ".txt", statistics);
		}
		
		return statistics;
	}
	
	private static boolean loadFileIfExists(String filename, WordStatistics statistics) {
		File file = new File(filename);
		if (!file.exists()) {
			return false;
		}
		
		loadFile(filename, statistics);
		return true;
	}

	private static void loadFile (String filename, WordStatistics statistics) {
		try {
			System.out.println("## Loading " + filename);
			int wordCount = 0;
			String longestWord = "";
			Set<Character> uncommonChars = new HashSet<Character>();
			Counter<String> wordCounter = new Counter<String>();
			
			int entriesCountBefore = statistics.getEntries().size();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "Cp1252"));
			String line = reader.readLine();
			while (line != null) {
				line = line.toLowerCase();
				String invalid = line.replaceAll("[" + COMMON_CHARACTERS + "]", "");
				addCharacters(uncommonChars, invalid);
				line = line.replaceAll("[^" + WORD_CHARACTERS + "]", " ");
				if (CONVERT_TO_ASCII) {
					line = toSimpleCharacterString(line);
				}
				String[] words = line.split(" ");
				for (String word : words) {
					if (word.length() > 0) {
						boolean added = statistics.add(word);
						if (added) {
							wordCounter.add(word);
							wordCount++;
							if (word.length() > longestWord.length()) {
								longestWord = word;
							}
						}
					}
				}

				line = reader.readLine();
			}
			reader.close();
			System.out.println("# word count: " + wordCount);
			System.out.println("# longest word: " + longestWord);
			System.out.println("# uncommon characters: " + uncommonChars);
			System.out.println("# number of frequency entries added: " + (statistics.getEntries().size() - entriesCountBefore));
			System.out.println("# word statistics: ");
			wordCounter.printTop(20);
		} catch (IOException e) {
			throw new RuntimeException("Loading file failed: " + filename, e);
		}
	}
	
	public static String toSimpleCharacterString(String string) {
		String result = string;
		
		for (Entry<String, String> entry : MAP_DIACRITS_TO_ASCII.entrySet()) {
			result = result.replaceAll("[" + entry.getKey() + "]", entry.getValue());
		}
		
		return result;
	}
	
	private static void addCharacters (Collection<Character> result, String string) {
		for(char c : string.toCharArray()) {
			result.add(c);
		}
	}

	public static class WordGenerator {
		private final int partLength;

		private Map<String, List<Probability<String>>> map = new HashMap<String, List<Probability<String>>>();

		public WordGenerator (int partLength) {
			this.partLength = partLength;
		}
		
		public void loadFile(String filename) {
			try {
				loadReader(new BufferedReader(new FileReader(filename)));
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Frequency data file not found: " + filename, e);
			}
		}
		
		public void loadString(String string) {
			loadReader(new BufferedReader(new StringReader(string)));
		}
		
		private void loadReader(BufferedReader reader) {
			try {
				String line = reader.readLine();
				while (line != null) {
					String[] splitLines = line.split(",");
					for (int i = 0; i < splitLines.length; i++) {
						String[] splitWords = splitLines[i].split(" ");
						if (splitWords.length >= 2) {
							add(splitWords[0], Integer.parseInt(splitWords[1]));
						}
					}

					line = reader.readLine();
				}
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException("Loading frequency data failed");
			}
		}

		
		public void add(String string, int count) {
			String in = string.substring(0, string.length() - 1);
			String out = string.substring(string.length() - 1, string.length());
			
			List<Probability<String>> list = map.get(in);
			if (list == null) {
				list = new ArrayList<Probability<String>>();
				map.put(in, list);
			}
			list.add(new Probability<String>(count, out));
		}
		
		public String createWord(Random random) {
			int minLength = random.nextInt(6, 9);
			int maxLength = random.nextInt(16, 20);
			return createWord(random, minLength, maxLength);
		}	

		public String createWord(Random random, int minLength, int maxLength) {
			StringBuilder word = new StringBuilder();
			int length = 0;
			
			String last = "";
			for (;;) {
				List<Probability<String>> nextParts = map.get(last);
				if (nextParts != null && length < minLength) {
					// remove end-markers from nextParts since it is not allowed anyway
					nextParts = removeEndMarker(nextParts);
				}
				while (nextParts == null || nextParts.size() == 0) {
					last = last.substring(1);
					nextParts = map.get(last);
				}
				String str = random.nextProbability(nextParts);
				if ("$".equals(str)) {
					break;
				}
				if (length + str.length() > maxLength) {
					break;
				}
				length += str.length();
				word.append(str);
				last = last + str;
				if (last.length() >= partLength) {
					last = last.substring(1, last.length());
				}
			}
			return word.toString();
		}

		private List<Probability<String>> removeEndMarker (List<Probability<String>> nextParts) {
			List<Probability<String>> nextPartsNoEnd = new ArrayList<Random.Probability<String>>();
			for (Probability<String> probability : nextParts) {
				if (!"$".equals(probability.value)) {
					nextPartsNoEnd.add(probability);
				}
			}
			return nextPartsNoEnd;
		}
	}
}
