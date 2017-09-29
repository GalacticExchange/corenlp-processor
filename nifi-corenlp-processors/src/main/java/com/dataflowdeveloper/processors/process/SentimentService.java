package com.dataflowdeveloper.processors.process;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.ejml.simple.SimpleMatrix;

import java.util.Properties;


/**
 * @author tspann
 *
 */
public class SentimentService {
	
	private StanfordCoreNLP pipeline;

	public SentimentService(){
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, lemma, ner, mention, relation, coref, sentiment");
		props.setProperty("threads","8");
		pipeline = new StanfordCoreNLP(props);
	}


	/**
	 * get stanford coreNLP sentiment analysis of sentence sent
	 * @param sentence
	 * @return String of JSON sentiment
	 */
	public Object getSentiment(String sentence) {
		if ( sentence == null ) {
			return "";
		}
		String outputJSON = "";
        ResultOutput resultOutput = new ResultOutput();
		if ( sentence != null) {
			try {
		        if (sentence != null && sentence.length() > 0) {
		            Annotation annotation = pipeline.process(sentence);
                    for (CorefChain cc : annotation.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
                        resultOutput.addCorefChain(cc.toString());
                    }
		            for (CoreMap sentenceStructure : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                        Sentence resultSentence = new Sentence();
                        for (Mention m : sentenceStructure.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
                            resultSentence.addMention(m.toString());
                        }
                        for (CoreLabel token: sentenceStructure.get(TokensAnnotation.class)) {
                            // this is the text of the token
                            String word = token.get(TextAnnotation.class);
                            // this is the POS tag of the token
                            String pos = token.get(PartOfSpeechAnnotation.class);
                            // this is the NER label of the token
                            String ne = token.get(NamedEntityTagAnnotation.class);
                            // this is the Role label of the token
                            String role = token.get(CoreAnnotations.RoleAnnotation.class);
                            Word resultWord = new Word(word,pos,ne,role);
                            resultSentence.addWord(resultWord);
                        }

                        Tree tree = sentenceStructure.get(SentimentAnnotatedTree.class);
                        SimpleMatrix matrix = RNNCoreAnnotations.getPredictions(tree);

                        for (int i = 0; i < matrix.numRows(); i++) {
                            resultSentence.addSentimentProbs(matrix.get(i));
                        }
                        resultOutput.addSentence(resultSentence);
		            }

		        }
                Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
				outputJSON = gson.toJson(resultOutput);
			} catch (Exception e) {
				return e;
			}
		}
		
		return "{\"output\":"+outputJSON+"}";
	}
	
	/**
	 * tester
	 * @param args
	 */
	public static void main(String[] args)  {

		long start_time = System.currentTimeMillis();
		SentimentService service = new SentimentService();

        System.out.println(service.getSentiment("This is some text! I hate this planet. This flower is so beautiful." +
                " Barack Obama was born in Hawaii. He is the president. Obama was elected in 2008."));
	}

}
