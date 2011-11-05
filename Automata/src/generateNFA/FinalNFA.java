/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package generateNFA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import tools.RegexScanner;
import tools.SpecFileScanner;
import automata.MapBasedNFA;
import automata.NFA;
import automata.State;
import automata.Token;

/**
 *A wrapper class to manage the entire process of generating an NFA for a given lexical specification.
 * @author 
 */
public class FinalNFA {

    private SpecFileScanner scan;
    private Map<String, LinkedList<Token>> identifiers;
    private Map<String, NFA> miniNFAs;
    private RegexScanner scanner;
    private RecursiveDescent parser;

    /**
     * A function to manage the entire process of generating an NFA for a given lexical specification.
     * @param filename File name of a valid lexical specification input file.
     * @return The completed NFA.
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws exceptions.SyntaxErrorException 
     */
    public NFA generate(String filename) throws java.io.FileNotFoundException, java.io.IOException, exceptions.SyntaxErrorException {
        scan = new SpecFileScanner(filename);
        miniNFAs = new HashMap<String, NFA>();
        identifiers = scan.identifierDefs();
        
        State mergeStartState = new State();
        MapBasedNFA mergeStartNFA = new MapBasedNFA(mergeStartState);
        
        
        RecursiveDescentInterState NFABuilder = new RecursiveDescentInterState("", mergeStartNFA);
        for (Map.Entry<String, LinkedList<Token>> a : identifiers.entrySet()) {
            scanner = new RegexScanner(a);
            parser = new RecursiveDescent(scanner, scan.charClasses(), a.getKey());


            RecursiveDescentInterState state = parser.regex();

//            System.out.println("Final regex: " + state.getCurrentRegex());
//            HashMap<State, HashMap<Character, HashSet<State>>> allTransitions = ((MapBasedNFA) (state.getCurrentNFA())).getTransitions();
//            Set<State> allStates = state.getCurrentNFA().allStates();
//            for (State currState : allStates) {
//                HashMap<Character, HashSet<State>> currentTransitions = allTransitions.get(currState);
//                HashSet<Character> charSet = new HashSet<Character>(currentTransitions.keySet());
//                for (Character c : charSet) {
//                    HashSet<State> toStates = currentTransitions.get(c);
//                    for (State toState : toStates) {
//                        String transitionChar;
//                        if (c == null) {
//                            transitionChar = "null";
//                        } else {
//                            transitionChar = Character.toString(c);
//                        }
//                        System.out.println(currState.getName() + "---" + transitionChar + "--->" + toState.getName());
//                    }
//                }
//            }
//            System.out.println("Final States:");
//            Set<State> finalStates = ((MapBasedNFA) (state.getCurrentNFA())).finalStates();
//            for (State s : finalStates) {
//                System.out.println(s.getName());
//            }
            miniNFAs.put(a.getKey(), state.getCurrentNFA());
            NFABuilder = unionStates(NFABuilder, state);
        }

        return NFABuilder.getCurrentNFA();
    }
    
    private RecursiveDescentInterState unionStates(RecursiveDescentInterState state1, RecursiveDescentInterState state2) {
		if(state1==null){
			return state2;
		}
		if(state2 == null){
			return state1;
		}
		MapBasedNFA leftNFA = (MapBasedNFA) state1.getCurrentNFA();
		MapBasedNFA rightNFA = (MapBasedNFA) state2.getCurrentNFA();
		if(leftNFA == null){
			String newRegexString = state1.getCurrentRegex()+ state2.getCurrentRegex();
			RecursiveDescentInterState interState = new RecursiveDescentInterState(newRegexString, state2.getCurrentNFA());
			return interState;
		}
		if(rightNFA == null){
			String newRegexString = state1.getCurrentRegex()+ state2.getCurrentRegex();
			RecursiveDescentInterState interState = new RecursiveDescentInterState(newRegexString, state1.getCurrentNFA());
			return interState;
		}
		
		State leftStartState = leftNFA.startState();
		State rigthStartState = rightNFA.startState();
		leftNFA.addTransition(leftStartState, null, rigthStartState);
		
		HashMap<State, HashMap<Character, HashSet<State>>> allTransitions = rightNFA.getTransitions();
		Set<State> allStates = rightNFA.allStates();
		for(State currState : allStates){
			HashMap<Character, HashSet<State>> currentTransitions = allTransitions.get(currState);
			HashSet<Character> charSet = new HashSet<Character>(currentTransitions.keySet());
			for(Character c : charSet){
				HashSet<State> toStates = currentTransitions.get(c);
				for(State toState : toStates){
					leftNFA.addTransition(currState, c, toState);
				}
			}			
		}		
		String newRegexString = state1.getCurrentRegex()+ state2.getCurrentRegex();
		RecursiveDescentInterState interState = new RecursiveDescentInterState(newRegexString, leftNFA);
		return interState;
	}
}
