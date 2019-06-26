package app.di_v.etc.blockchain.model;

import app.di_v.etc.blockchain.StringUtil;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author di-v
 */
public class Block {
    private String mHash;
    private String mPreviousHash;
    private byte[] mData; //our data will be a simple message.
    private long mTimeStamp; //as number of milliseconds since 1/1/1970.
    private int mNonce;

    //Block Constructor.
    public Block(byte[] data, String previousHash ) {
        mData = data;
        mPreviousHash = previousHash;
        mTimeStamp = new Date().getTime();

        mHash = calculateHash(); //Making sure we do this after we set the other values.
    }

    //Calculate new hash based on blocks contents
    public String calculateHash() {
        String str = new String(mData, StandardCharsets.UTF_8);
        //System.out.println("str: " + str);
        String newHash = StringUtil.applySha256(
                mPreviousHash +
                      Long.toString(mTimeStamp) +
                      Integer.toString(mNonce) +
                      str
        );
        System.out.println("Block: mHash: " + newHash);
        return newHash;
    }

    //Increases nonce value until hash target is reached.
    public void mineBlock(int difficulty) {
        String target = StringUtil.getDificultyString(difficulty);

        while(!mHash.substring( 0, difficulty).equals(target)) {
            mNonce++;
            mHash = calculateHash();
        }

        System.out.println("Block Mined!!!: " + mHash);
    }

    public String getHash() {
        return mHash;
    }

    public void setHash(String hash) {
        mHash = hash;
    }

    public String getPreviousHash() {
        return mPreviousHash;
    }

    public void setPreviousHash(String previousHash) {
        mPreviousHash = previousHash;
    }

    public byte[] getData() {
        return mData;
    }

    public void setData(byte[] data) {
        mData = data;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }
}
