package graft;

import graft.cpg.CpgBuilder;

/**
 * TODO: javadoc
 */
public class Graft {

    /**
     * TODO: javadoc
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        CpgBuilder cpgBuilder = new CpgBuilder(args[0]);
        cpgBuilder.buildCpg();
    }

}
