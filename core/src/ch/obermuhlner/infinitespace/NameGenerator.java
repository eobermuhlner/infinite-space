package ch.obermuhlner.infinitespace;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.random.Random;
import ch.obermuhlner.infinitespace.model.universe.Planet;
import ch.obermuhlner.infinitespace.model.universe.SpaceStation;
import ch.obermuhlner.infinitespace.model.universe.Star;
import ch.obermuhlner.infinitespace.util.Units;
import ch.obermuhlner.infinitespace.util.WordStatistics.WordGenerator;

import static ch.obermuhlner.infinitespace.model.random.Random.p;

import com.badlogic.gdx.Gdx;

public class NameGenerator {

	private WordGenerator wordGenerator = createWordGenerator(4, "data/texts/words.freq");
	private WordGenerator wordGeneratorFirstNamesMale = createWordGenerator(4, "data/texts/firstnames_male_en.freq");
	private WordGenerator wordGeneratorFirstNamesFemale = createWordGenerator(4, "data/texts/firstnames_female_en.freq");
	private WordGenerator wordGeneratorLastNames = createWordGenerator(4, "data/texts/lastnames_en.freq");

	private static WordGenerator createWordGenerator (int partLength, String filename) {
		WordGenerator wordGenerator = new WordGenerator(partLength);
		wordGenerator.loadString(loadFile(filename));
		return wordGenerator;
	}

	private static String loadFile (String filename) {
		if (Gdx.files == null) {
				try {
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(filename));
				String line = reader.readLine();
				while (line != null) {
					builder.append(line);
					builder.append("\n");
					line = reader.readLine();
				}
				reader.close();
				return builder.toString();
			} catch (IOException e) {
				throw new RuntimeException("File not found: " + filename, e);
			}
		}
		
		return Gdx.files.internal(filename).readString();
		
	}

	public String generateGenericName(Random random) {
		return Units.toFirstUpperRestLowerCaseString(wordGenerator.createWord(random));
	}
	
	public String generateMaleFirstName(Random random) {
		return Units.toFirstUpperRestLowerCaseString(wordGeneratorFirstNamesMale.createWord(random, 3, 20));
	}
	
	public String generateFemaleFirstName(Random random) {
		return Units.toFirstUpperRestLowerCaseString(wordGeneratorFirstNamesFemale.createWord(random, 3, 20));
	}
	
	@SuppressWarnings("unchecked")
	public String generateNodeName(Random random, Node node) {
		String name = generateGenericName(random);
		String prefix = "";
		String suffix = "";
		
		if (node instanceof SpaceStation) {
			SpaceStation spaceStation = (SpaceStation)node;
			prefix = random.nextProbability(
				p(1, "Free "),
				p(2, "Deep "),
				p(spaceStation.type == SpaceStation.Type.CONGLOMERATE ? 10 : 0, "International "),
				p(30, ""));

			suffix = random.nextProbability(
				p(20, " Station"),
				p(10, " Hub"),
				p(10, " Base"),
				p(10, " Orbital"),
				p(10, " Port"),
				p(spaceStation.type == SpaceStation.Type.RING ? 30 : 0, " Ring"),
				p(spaceStation.type == SpaceStation.Type.CUBE ? 30 : 0, " Cube"),
				p(spaceStation.starport ? 30 : 0, " Starport"),
				p(prefix.equals("Free ") ? 0 : 1, " Freehold"));
		} else if (node instanceof Star) {
			prefix = random.nextProbability(
				p(1, "Alpha "),
				p(1, "Beta "),
				p(1, "Gamma "),
				p(1, "Delta "),
				p(1, "Epsilon "),
				p(20, ""));
		} else if (node instanceof Planet) {
			// TODO Use star name + roman number if no population
			prefix = random.nextProbability(
					p(3, "Neu "),
					p(2, "Nieuw "),
					p(2, "New "),
					p(30, ""));
		}
		
		name = prefix + name + suffix;
		
		return name;
	}


	public String generateFirstName(Random random) {
		if (random.nextBoolean()) {
			return generateMaleFirstName(random);
		} else {
			return generateFemaleFirstName(random);
		}
	}

	public String generateLastName(Random random) {
		return Units.toFirstUpperRestLowerCaseString(wordGeneratorLastNames.createWord(random, 3, 20));
	}

}
