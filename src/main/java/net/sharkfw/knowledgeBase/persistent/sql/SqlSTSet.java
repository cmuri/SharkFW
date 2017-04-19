package net.sharkfw.knowledgeBase.persistent.sql;

import net.sharkfw.knowledgeBase.*;

import java.sql.*;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Created by Dustin Feurich
 */
public class SqlSTSet implements STSet {

    private Connection connection;
    private int stSetID;
    private SqlSharkKB sqlSharkKB;

    /**
     * Write StSet to database
     */
    public SqlSTSet(SqlSharkKB sharkKB) throws SQLException {
        try {
            Class.forName(sharkKB.getDialect());
            connection = DriverManager.getConnection(sharkKB.getDbAddress());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.sqlSharkKB = sharkKB;
        StringBuilder sql = new StringBuilder();
        sql.append("PRAGMA foreign_keys = ON; ");
        sql.append("INSERT INTO tag_set VALUES (NULL);");
        SqlHelper.executeSQLCommand(connection, sql.toString());
        stSetID = SqlHelper.getLastCreatedEntry(connection, "tag_set");
    }

    public int getStSetID()
    {
        return stSetID;
    }

    @Override
    public SemanticTag getSemanticTag(String[] si) throws SharkKBException {
        return new SqlSemanticTag(si[0], stSetID, sqlSharkKB);
    }

    @Override
    public SemanticTag merge(SemanticTag tag) throws SharkKBException {
        return null;
    }

    @Override
    public SemanticTag createSemanticTag(String name, String[] sis) throws SharkKBException {
        return null;
    }

    @Override
    public SemanticTag createSemanticTag(String name, String si) throws SharkKBException {
        return null;
    }

    @Override
    public void removeSemanticTag(SemanticTag tag) throws SharkKBException {

    }

    @Override
    public void removeSemanticTag(String si) throws SharkKBException {

    }

    @Override
    public void removeSemanticTag(String[] sis) throws SharkKBException {

    }

    @Override
    public void setEnumerateHiddenTags(boolean hide) {

    }

    @Override
    public Enumeration<SemanticTag> tags() throws SharkKBException {
        return null;
    }

    @Override
    public Iterator<SemanticTag> stTags() throws SharkKBException {
        return null;
    }

    @Override
    public SemanticTag getSemanticTag(String si) throws SharkKBException {
        return null;
    }

    @Override
    public Iterator<SemanticTag> getSemanticTagByName(String pattern) throws SharkKBException {
        return null;
    }

    @Override
    public STSet fragment(SemanticTag anchor) throws SharkKBException {
        return null;
    }

    @Override
    public FragmentationParameter getDefaultFP() {
        return null;
    }

    @Override
    public void setDefaultFP(FragmentationParameter fp) {

    }

    @Override
    public STSet fragment(SemanticTag anchor, FragmentationParameter fp) throws SharkKBException {
        return null;
    }

    @Override
    public STSet contextualize(Enumeration<SemanticTag> anchorSet, FragmentationParameter fp) throws SharkKBException {
        return null;
    }

    @Override
    public STSet contextualize(Enumeration<SemanticTag> anchorSet) throws SharkKBException {
        return null;
    }

    @Override
    public STSet contextualize(STSet context, FragmentationParameter fp) throws SharkKBException {
        return null;
    }

    @Override
    public STSet contextualize(STSet context) throws SharkKBException {
        return null;
    }

    @Override
    public void merge(STSet stSet) throws SharkKBException {

    }

    @Override
    public void addListener(STSetListener listen) {

    }

    @Override
    public void removeListener(STSetListener listener) throws SharkKBException {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }
}