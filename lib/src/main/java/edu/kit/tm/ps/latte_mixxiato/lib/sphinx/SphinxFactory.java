package edu.kit.tm.ps.latte_mixxiato.lib.sphinx;

import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxNode;

import java.math.BigInteger;

public interface SphinxFactory {
    SphinxNode node(BigInteger secret);
    SphinxClient client();
}
