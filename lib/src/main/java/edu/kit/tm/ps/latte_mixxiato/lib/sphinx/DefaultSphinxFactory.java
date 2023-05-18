package edu.kit.tm.ps.latte_mixxiato.lib.sphinx;

import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.SphinxParams;
import com.robertsoultanaev.javasphinx.pki.PkiGenerator;

import java.math.BigInteger;

public class DefaultSphinxFactory implements SphinxFactory {

    private final SphinxParams params;

    public DefaultSphinxFactory() {
        this.params = new SphinxParams();
    }

    @Override
    public SphinxNode node(BigInteger secret) {
        return new SphinxNode(params, secret);
    }

    @Override
    public SphinxClient client() {
        return new SphinxClient(params);
    }

    @Override
    public PkiGenerator pkiGenerator() {
        return new PkiGenerator(params);
    }
}
