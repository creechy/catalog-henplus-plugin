/*
 * This is free software, licensed under the Gnu Public License (GPL)
 * get a copy from <http://www.gnu.org/licenses/gpl.html>
 */
package org.fakebelieve.henplus.plugins.catalog;

import henplus.AbstractCommand;
import henplus.CommandDispatcher;
import henplus.HenPlus;
import henplus.SQLSession;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CatalogCommand extends AbstractCommand {

    private static final String COMMAND_SETSHOW = "catalog";
    private static final String COMMAND_LIST = "catalogs";

    /**
     *
     */
    public CatalogCommand() {
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#getCommandList()
     */
    @Override
    public String[] getCommandList() {
        return new String[]{COMMAND_SETSHOW, COMMAND_LIST};
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#participateInCommandCompletion()
     */
    @Override
    public boolean participateInCommandCompletion() {
        return true;
    }

    protected List<String> getCatalogs(SQLSession session) throws SQLException {
        List<String> list = new ArrayList<String>();
        ResultSet catalogs = session.getConnection().getMetaData().getCatalogs();
        while (catalogs.next()) {
            list.add(catalogs.getString(1));
        }
        catalogs.close();

        return list;
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#execute(henplus.SQLSession, java.lang.String, java.lang.String)
     */

    @Override
    public int execute(SQLSession session, String command, String parameters) {
        int result = SUCCESS;

        // required: session
        if (session == null) {
            HenPlus.msg().println("You need a valid session for this command.");
            return EXEC_FAILED;
        }

        if (command.equals(COMMAND_LIST)) {
            try {
                for (String catalog : getCatalogs(session)) {
                    HenPlus.msg().println(" " + catalog);
                }
            } catch (SQLException ex) {
                HenPlus.msg().println("Problem - " + ex.getMessage());
            }
        }

        if (command.equals(COMMAND_SETSHOW)) {
            if (parameters == null || parameters.isEmpty()) {
                try {
                    HenPlus.msg().println(" " + session.getConnection().getCatalog());
                } catch (SQLException ex) {
                    HenPlus.msg().println("Problem - " + ex.getMessage());
                }
            } else {
                try {
                    session.getConnection().setCatalog(parameters.trim());
                } catch (SQLException ex) {
                    HenPlus.msg().println("Problem - " + ex.getMessage());
                }
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#isComplete(java.lang.String)
     */
    @Override
    public boolean isComplete(String command) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#requiresValidSession(java.lang.String)
     */
    @Override
    public boolean requiresValidSession(String cmd) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#shutdown()
     */
    @Override
    public void shutdown() {
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#getShortDescription()
     */
    @Override
    public String getShortDescription() {
        return "list/set the database/catalog for a session";
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#getSynopsis(java.lang.String)
     */
    @Override
    public String getSynopsis(String cmd) {
        return "\n" + COMMAND_SETSHOW + ";\n"
                + "or\n"
                + COMMAND_SETSHOW + " " + " <catalog>;\n"
                + "or\n"
                + COMMAND_LIST + " " + ";\n";
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#getLongDescription(java.lang.String)
     */
    @Override
    public String getLongDescription(String cmd) {
        return "\tView databases/catalogs and set default schema for a session\n"
                + "\n"
                + "\tTo view all the databases/catalogs for a session\n"
                + "\t\t" + COMMAND_LIST + ";\n"
                + "\n"
                + "\tTo view current database/catalog for a session\n"
                + "\t\t" + COMMAND_SETSHOW + ";\n"
                + "\tTo set the default database/catalog for a session\n"
                + "\t\t" + COMMAND_SETSHOW + " <catalog>;\n"
                + "\n";
    }

    @Override
    public Iterator complete(CommandDispatcher disp, String partialCommand, String lastWord) {
        HenPlus.getInstance().getCurrentSession();

        try {
            List<String> catalogs = getCatalogs(HenPlus.getInstance().getCurrentSession());
            for (Iterator<String> i = catalogs.listIterator(); i.hasNext();) {
                String catalog = i.next();
                if (!catalog.startsWith(lastWord)) {
                    i.remove();
                }
            }
            return catalogs.iterator();
        } catch (SQLException ex) {
            HenPlus.msg().println("Problem - " + ex.getMessage());
            return super.complete(disp, partialCommand, lastWord);
        }

    }
}
