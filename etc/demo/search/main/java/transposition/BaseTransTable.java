package transposition;

public abstract class BaseTransTable implements TranspositionTable {

    public static int HASH_SIZE = 64;

    // ************************************************************************
    // instance attributes
    // ************************************************************************

    /**
     * The hash key is the first HASHKEY_SIZE bits of the hash, used to index into the array.
     * This value cannot exceed 32, as we need the array index (ie. hash key) to be an integer.
     */
    protected int HASHKEY_SIZE;

    /**
     * The hash value is the remaining HASHVAL_SIZE bits, which we store as an attribute to the
     * table entry. Note that HASHKEY_SIZE + HASHVAL_SIZE = HASH_SIZE. This means that this value
     * can exceed 32, and therefore we store the hash value as a long.
     */
    protected int HASHVAL_SIZE;

    /**
     * The size of the underlying array, namely 2^HASHKEY_SIZE.
     */
    protected int TABLE_SIZE;

    /**
     * The number of entries currently stored in the table.
     */
    protected int nrEntries;

    public BaseTransTable(int hashkeySize) {
        if (hashkeySize < 1 || hashkeySize > 32) {
            throw new RuntimeException("Hash key size must be between 1 and 32");
        }

        HASHKEY_SIZE = hashkeySize;
        HASHVAL_SIZE = HASH_SIZE - HASHKEY_SIZE;
        TABLE_SIZE = (int) Math.pow(2, HASHKEY_SIZE);
    }

    @Override
    public int nrEntries() {
        return nrEntries;
    }

    @Override
    public boolean isEmpty() {
        return nrEntries == 0;
    }

}
