import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import algorithms.*;
import domains.MNK;
import match.*;
import transposition.ReplacementScheme;

/**
 * The command line interface for the program.
 */
public class CLI {

    private String[] args;
    private int argsConsumed;

    // ************************************************************************
    // constructors
    // ************************************************************************

    private CLI(String[] args) {
        this.args = args;
        switch (getNextArg()) {
            case "pi":
                digitsOfPiMode();
                System.exit(0);
            case "mnk":
                mnkMode();
                System.exit(0);
            default:
                usage();
        }
    }

    // ************************************************************************
    // main modes
    // ************************************************************************

    private void digitsOfPiMode() {
        System.out.println("DOMAIN: digits of pi");
        int depth = Integer.parseInt(getNextArg());
        String mode = getNextArg();
        assert mode.equals("-a");
        Algorithm algo = getAlgo();
        if (algo instanceof PiAlgorithm) {
            ((PiAlgorithm) algo).evaluate(depth);
            System.out.println("Elapsed time: " + algo.getElapsedTime());
            System.out.println("Nodes explored: " + algo.getNodesExplored());
            System.out.println("Transp. table hits: " + algo.getTranspTableHits());
            System.out.println("Transp. table misses: " + algo.getTranspTableMisses());
            System.out.println("Transp. table size: " + algo.getTranspTableSize());
            algo.resetStats();
        } else {
            exitWithMsg("Algorithm needs to be one of F, F1 or F2");
        }
    }

    private void mnkMode() {
        System.out.println("DOMAIN: MNK");
        String mode = getNextArg();
        switch (mode) {
            case "match":
                mnkMatch();
                break;
            case "eval":
                mnkEval();
                break;
            case "write":
                mnkWrite();
                break;
            default:
                exitWithMsg("Unrecognized mode '" + mode + "'");
        }
    }

    // ************************************************************************
    // MNK modes
    // ************************************************************************

    private void mnkMatch() {
        System.out.println("Setting up two-player MNK match");
        String domainSrc = getNextArg();
        MNK domain = null;
        switch (domainSrc) {
            case "-f":
                try {
                    domain = mnkFromFile();
                } catch (FileNotFoundException e) {
                    exitWithMsg("File not found");
                }
                break;
            case "-b":
                domain = mnkFromDims();
                break;
            default:
                exitWithMsg("Specify -f or -b flag");
                return;
        }

        MNKPlayer player1 = mnkPlayer(MNK.X);
        MNKPlayer player2 = mnkPlayer(MNK.O);

        MNKMatch match = new MNKMatch(player1, player2, domain);
        System.out.println("Match ready, playing...");
        match.play();
    }

    private void mnkEval() {
        System.out.println("Evaluating MNK board");
        String domainSrc = getNextArg();
        MNK domain = null;
        switch (domainSrc) {
            case "-f":
                try {
                    domain = mnkFromFile();
                } catch (FileNotFoundException e) {
                    exitWithMsg("File not found");
                }
                break;
            case "-b":
                domain = mnkFromDims();
                break;
            default:
                exitWithMsg("Specify -f or -b flag");
                return;
        }
        String __ = getNextArg();
        assert __.equals("-a");
        Algorithm algo = getAlgo();
        System.out.println("Best move: " + algo.getBestMove(domain));
        System.out.println("Elapsed time: " + algo.getElapsedTime());
        System.out.println("Nodes explored: " + algo.getNodesExplored());
        System.out.println("Transp. table hits: " + algo.getTranspTableHits());
        System.out.println("Transp. table misses: " + algo.getTranspTableMisses());
        System.out.println("Transp. table size: " + algo.getTranspTableSize());
        algo.resetStats();
    }

    private void mnkWrite() {
        System.out.println("Writing random MNK board to file");
        MNK board = mnkFromDims();
        String filename = getNextArg();
        Random rand = new Random();
        int nrMoves = board.getK() % 2 == 0 ? board.getK() : board.getK() + 1;
        for (int i = 0; i < nrMoves; i++) {
            int move = rand.nextInt(board.getMoves().size());
            board = (MNK) board.makeMove(board.getMoves().get(move));
            board.setMAX(-1 * board.getPlayer());
        }
        try {
            MNK.toFile(board, filename);
        } catch (IOException e) {
            exitWithMsg("Could not write board to file (IOException)");
        }
        System.out.println("Board written to file '" + filename + "'");
    }

    // ************************************************************************
    // MNK helpers
    // ************************************************************************

    private MNK mnkFromFile() throws FileNotFoundException {
        String filename = getNextArg();
        System.out.println("Reading MNK board from file '" + filename + "'");
        return MNK.fromFile(filename);
    }

    private MNKPlayer mnkPlayer(int side) {
        String playerMode = getNextArg();
        switch (playerMode) {
            case "--user":
                return new IOPlayer(side);
            case "--random":
                return new RandomPlayer(side);
            case "-a":
                return mnkAlgoPlayer(side);
            default:
                exitWithMsg("Unrecognized player mode '" + playerMode + "'");
        }
        return null;
    }

    private AlgoPlayer mnkAlgoPlayer(int side) {
        Algorithm algo = getAlgo();
        return new AlgoPlayer(side, algo);
    }

    private MNK mnkFromDims() {
        System.out.println("Generating new MNK domain");
        int m = Integer.parseInt(getNextArg());
        int n = Integer.parseInt(getNextArg());
        int k = Integer.parseInt(getNextArg());
        return new MNK(m, n, k, MNK.X);
    }

    // ************************************************************************
    // other instance helpers
    // ************************************************************************

    private String getNextArg() {
        try {
            return args[argsConsumed++];
        } catch (ArrayIndexOutOfBoundsException e) {
            exitWithMsg("Invalid command line args");
        }
        return "";
    }

    private Algorithm getAlgo() {
        String algoName = getNextArg();
        int depth;
        int timeCutoff;
        ReplacementScheme repScheme;

        switch (algoName) {
            case "f":
                return new F();
            case "f1":
                return new F1();
            case "f2":
                return new F2();
            case "scout":
                depth = Integer.parseInt(getNextArg());
                repScheme = getRepScheme(getNextArg());
                if (repScheme.equals(ReplacementScheme.NONE)) {
                    return new NegaScout(depth);
                } else {
                    return new NegaScout(depth, repScheme);
                }
            case "iter-ab":
                depth = Integer.parseInt(getNextArg());
                repScheme = getRepScheme(getNextArg());
                timeCutoff = Integer.parseInt(getNextArg());
                return new IterativeDeepeningAlphaBetaMNK(depth, repScheme, timeCutoff);
            case "nega":
                depth = Integer.parseInt(getNextArg());
                return new Negamax(depth);
            case "nega-ab":
                depth = Integer.parseInt(getNextArg());
                repScheme = getRepScheme(getNextArg());
                if (repScheme.equals(ReplacementScheme.NONE)) {
                    return new NegaAlphaBeta(depth);
                } else {
                    return new NegaAlphaBeta(depth, repScheme);
                }
            case "mtdf":
                depth = Integer.parseInt(getNextArg());
                repScheme = getRepScheme(getNextArg());
                timeCutoff = Integer.parseInt(getNextArg());
                return new MTDf(depth, repScheme, timeCutoff);
            default:
                exitWithMsg("Unrecognized algorithm '" + algoName + "'");
        }
        return null;
    }

    // ************************************************************************
    // static methods
    // ************************************************************************

    private static ReplacementScheme getRepScheme(String repScheme) {
        switch (repScheme) {
            case "--new":
                return ReplacementScheme.NEW;
            case "--deep":
                return ReplacementScheme.DEEP;
            case "--2deep":
                return ReplacementScheme.TWO_DEEP;
            case "--none":
                return ReplacementScheme.NONE;
            default:
                exitWithMsg("Unrecognized replacement scheme '" + repScheme + "'");
        }
        return ReplacementScheme.NONE;
    }

    private static void usage() {
        exitWithMsg("Usage: gradle run --args='<domain> <player1> <player2>'\nSee README for more details");
    }

    private static void exitWithMsg(String msg) {
        System.out.println(msg);
        System.exit(0);
    }

    // ************************************************************************
    // driver
    // ************************************************************************

    /**
     * Command line interface (see README).
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
        }
        new CLI(args);
    }

}
