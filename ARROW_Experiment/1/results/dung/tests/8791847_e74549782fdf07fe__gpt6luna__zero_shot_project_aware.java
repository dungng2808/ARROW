package net.automatalib.util.automata;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.Word;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Automata_ArrowTest {

    @Test
    public void testStateCover() {
        Alphabet<Character> alphabet = Alphabets.characters('a', 'a');
        CompactDFA<Character> automaton = new CompactDFA<>(alphabet);
        int initial = automaton.addInitialState(false);
        int accepting = automaton.addState(true);
        automaton.addTransition(initial, 'a', accepting);

        List<Word<Character>> cover = Automata.stateCover(automaton, alphabet);

        assertThat(cover).containsExactly(Word.epsilon(), Word.fromLetter('a'));
    }

    @Test
    public void testTransitionCover() {
        Alphabet<Character> alphabet = Alphabets.characters('a', 'a');
        CompactDFA<Character> automaton = new CompactDFA<>(alphabet);
        int initial = automaton.addInitialState(false);
        int accepting = automaton.addState(true);
        automaton.addTransition(initial, 'a', accepting);

        List<Word<Character>> cover = Automata.transitionCover(automaton, alphabet);

        assertThat(cover).containsExactly(Word.epsilon(), Word.fromLetter('a'), Word.fromLetter('a'));
    }

    @Test
    public void testStructuralCover() {
        Alphabet<Character> alphabet = Alphabets.characters('a', 'a');
        CompactDFA<Character> automaton = new CompactDFA<>(alphabet);
        int initial = automaton.addInitialState(false);
        int accepting = automaton.addState(true);
        automaton.addTransition(initial, 'a', accepting);

        List<Word<Character>> cover = Automata.structuralCover(automaton, alphabet);

        assertThat(cover).containsExactly(Word.epsilon(), Word.fromLetter('a'), Word.fromLetter('a'));
    }

    @Test
    public void testHasUndefinedInputWhenTransitionIsMissing() {
        Alphabet<Character> alphabet = Alphabets.characters('a', 'b');
        CompactDFA<Character> automaton = new CompactDFA<>(alphabet);
        automaton.addInitialState(false);

        assertThat(Automata.hasUndefinedInput(automaton, alphabet)).isTrue();
    }

    @Test
    public void testHasUndefinedInputWhenAllTransitionsAreDefined() {
        Alphabet<Character> alphabet = Alphabets.characters('a', 'a');
        CompactDFA<Character> automaton = new CompactDFA<>(alphabet);
        int state = automaton.addInitialState(false);
        automaton.addTransition(state, 'a', state);

        assertThat(Automata.hasUndefinedInput(automaton, alphabet)).isFalse();
    }

    @Test
    public void testFindUndefinedInputReturnsNullWhenAllTransitionsAreDefined() {
        Alphabet<Character> alphabet = Alphabets.characters('a', 'a');
        CompactDFA<Character> automaton = new CompactDFA<>(alphabet);
        int state = automaton.addInitialState(false);
        automaton.addTransition(state, 'a', state);

        assertThat(Automata.findUndefinedInput(automaton, alphabet)).isNull();
    }

    @Test
    public void testAllDefinedInputsIterator() {
        Alphabet<Character> alphabet = Alphabets.characters('a', 'b');
        CompactDFA<Character> automaton = new CompactDFA<>(alphabet);
        int state = automaton.addInitialState(false);
        automaton.addTransition(state, 'a', state);

        Iterator<TransRef<Integer, Character, ?>> iterator =
                Automata.allDefinedInputsIterator(automaton, alphabet);

        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next().getInput()).isEqualTo('a');
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void testAllUndefinedInputsIterator() {
        Alphabet<Character> alphabet = Alphabets.characters('a', 'b');
        CompactDFA<Character> automaton = new CompactDFA<>(alphabet);
        int state = automaton.addInitialState(false);
        automaton.addTransition(state, 'a', state);

        Iterator<TransRef<Integer, Character, ?>> iterator =
                Automata.allUndefinedInputsIterator(automaton, alphabet);

        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next().getInput()).isEqualTo('b');
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void testAllDefinedInputsIterable() {
        Alphabet<Character> alphabet = Alphabets.characters('a', 'b');
        CompactDFA<Character> automaton = new CompactDFA<>(alphabet);
        int state = automaton.addInitialState(false);
        automaton.addTransition(state, 'a', state);

        Iterable<TransRef<Integer, Character, ?>> refs = Automata.allDefinedInputs(automaton, alphabet);

        assertThat(refs).hasSize(1);
    }

    @Test
    public void testAllUndefinedInputsIterable() {
        Alphabet<Character> alphabet = Alphabets.characters('a', 'b');
        CompactDFA<Character> automaton = new CompactDFA<>(alphabet);
        int state = automaton.addInitialState(false);
        automaton.addTransition(state, 'a', state);

        Iterable<TransRef<Integer, Character, ?>> refs = Automata.allUndefinedInputs(automaton, alphabet);

        assertThat(refs).hasSize(1);
    }
}
