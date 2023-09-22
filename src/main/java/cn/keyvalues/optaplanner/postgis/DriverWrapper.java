package cn.keyvalues.optaplanner.postgis;

import org.postgresql.Driver;
import org.postgresql.PGConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("all")
public class DriverWrapper extends Driver {
    protected static final Logger logger = Logger.getLogger("cn.keyvalues.optaplanner.postgis.DriverWrapper");

    public static final String REVISION = "$Revision: 2570 $";
    protected static TypesAdder ta72 = null;
    protected static TypesAdder ta74 = null;
    protected static TypesAdder ta80 = null;

    protected TypesAdder typesAdder;

    /**
     * Default constructor.
     *
     * This also loads the appropriate TypesAdder for our SQL Driver instance.
     *
     * @throws SQLException
     */
    public DriverWrapper() throws SQLException {
        super();
        typesAdder = getTypesAdder(this);
        // The debug method is @since 7.2
        if (super.getMajorVersion() > 8 || super.getMinorVersion() > 1) {
            logger.fine(this.getClass().getName() + " loaded TypesAdder: " + typesAdder.getClass().getName());
        }
    }

    protected static TypesAdder getTypesAdder(Driver d) throws SQLException {
        if (d.getMajorVersion() == 7) {
            if (d.getMinorVersion() >= 3) {
                if (ta74 == null) {
                    ta74 = loadTypesAdder("74");
                }
                return ta74;
            } else {
                if (ta72 == null) {
                    ta72 = loadTypesAdder("72");
                }
                return ta72;
            }
        } else {
            if (ta80 == null) {
                ta80 = loadTypesAdder("80");
            }
            return ta80;
        }
    }

    private static TypesAdder loadTypesAdder(String version) throws SQLException {
        try {
            Class klass = Class.forName("cn.keyvalues.optaplanner.postgis.DriverWrapper$TypesAdder" + version);
            return (TypesAdder) klass.newInstance();
        } catch (Exception e) {
            throw new SQLException("Cannot create TypesAdder instance! " + e.getMessage());
        }
    }

    static {
        try {
            // Try to register ourself to the DriverManager
            java.sql.DriverManager.registerDriver(new DriverWrapper());
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error registering PostGIS Wrapper Driver", e);
        }
    }

    /**
     * Creates a postgresql connection, and then adds the PostGIS data types to
     * it calling addpgtypes()
     *
     * @param url the URL of the database to connect to
     * @param info a list of arbitrary tag/value pairs as connection arguments
     * @return a connection to the URL or null if it isnt us
     * @exception SQLException if a database access error occurs
     *
     * @see java.sql.Driver#connect
     * @see org.postgresql.Driver
     */
    public java.sql.Connection connect(String url, Properties info) throws SQLException {
        Connection result = super.connect(url, info);
        typesAdder.addGT(result, useLW(result));
        return result;
    }

    /**
     * Do we have HexWKB as well known text representation - to be overridden by
     * subclasses.
     */
    protected boolean useLW(Connection result) {
        if (result == null) {
            throw new IllegalArgumentException("null is no valid parameter");
        }
        return false;
    }

    /**
     * Returns our own CVS version plus postgres Version
     */
    public static String getVersion() {
        return "PostGisWrapper " + REVISION + ", wrapping " + Driver.getVersion();
    }

    /*
     * Here follows the addGISTypes() stuff. This is a little tricky because the
     * pgjdbc people had several, partially incompatible API changes during 7.2
     * and 8.0. We still want to support all those releases, however.
     *
     */
    /**
     * adds the JTS/PostGIS Data types to a PG 7.3+ Connection. If you use
     * PostgreSQL jdbc drivers V8.0 or newer, those methods are deprecated due
     * to some class loader problems (but still work for now), and you may want
     * to use the method below instead.
     *
     * @throws SQLException
     *
     */
    public static void addGISTypes(PGConnection pgconn) throws SQLException {
        loadTypesAdder("74").addGT((Connection) pgconn, false);
    }

    /**
     * adds the JTS/PostGIS Data types to a PG 8.0+ Connection.
     */
    public static void addGISTypes80(PGConnection pgconn) throws SQLException {
        loadTypesAdder("80").addGT((Connection) pgconn, false);
    }

    /**
     * adds the JTS/PostGIS Data types to a PG 7.2 Connection.
     *
     * @throws SQLException
     */
    public static void addGISTypes72(org.postgresql.Connection pgconn) throws SQLException {
        loadTypesAdder("72").addGT((Connection) pgconn, false);
    }

    /** Base class for the three typewrapper implementations */
    protected abstract static class TypesAdder {
        public final void addGT(java.sql.Connection conn, boolean lw) throws SQLException {
            if (lw) {
                addBinaryGeometries(conn);
            } else {
                addGeometries(conn);
            }
            addBoxen(conn);
        }

        public abstract void addGeometries(Connection conn) throws SQLException;

        public abstract void addBoxen(Connection conn) throws SQLException;

        public abstract void addBinaryGeometries(Connection conn) throws SQLException;
    }

    /** addGISTypes for V7.3 and V7.4 pgjdbc */
    protected static final class TypesAdder74 extends TypesAdder {
        public void addGeometries(Connection conn) {
            PGConnection pgconn = (PGConnection) conn;
            pgconn.addDataType("geometry", "org.postgis.PGgeometry");
            pgconn.addDataType("geography", "org.postgis.PGgeometry");
        }

        public void addBoxen(Connection conn) {
            PGConnection pgconn = (PGConnection) conn;
            pgconn.addDataType("box3d", "org.postgis.PGbox3d");
            pgconn.addDataType("box2d", "org.postgis.PGbox2d");
        }

        public void addBinaryGeometries(Connection conn) {
            PGConnection pgconn = (PGConnection) conn;
            pgconn.addDataType("geometry", "org.postgis.PGgeometryLW");
            pgconn.addDataType("geography", "org.postgis.PGgeometryLW");
        }
    }

    /** addGISTypes for V7.2 pgjdbc */
    protected static class TypesAdder72 extends TypesAdder {
        public void addGeometries(Connection conn) {
            org.postgresql.Connection pgconn = (org.postgresql.Connection) conn;
            pgconn.addDataType("geometry", "org.postgis.PGgeometry");
            pgconn.addDataType("geography", "org.postgis.PGgeometry");
        }

        public void addBoxen(Connection conn) {
            org.postgresql.Connection pgconn = (org.postgresql.Connection) conn;
            pgconn.addDataType("box3d", "org.postgis.PGbox3d");
            pgconn.addDataType("box2d", "org.postgis.PGbox2d");
        }

        public void addBinaryGeometries(Connection conn) {
            org.postgresql.Connection pgconn = (org.postgresql.Connection) conn;
            pgconn.addDataType("geometry", "org.postgis.PGgeometryLW");
            pgconn.addDataType("geography", "org.postgis.PGgeometryLW");
        }
    }

    /** addGISTypes for V8.0 (and hopefully newer) pgjdbc */
    protected static class TypesAdder80 extends TypesAdder {
        public void addGeometries(Connection conn) throws SQLException {
            PGConnection pgconn = (PGConnection) conn;
            pgconn.addDataType("geometry", org.postgis.PGgeometry.class);
            pgconn.addDataType("geography", org.postgis.PGgeometry.class);
        }

        public void addBoxen(Connection conn) throws SQLException {
            PGConnection pgconn = (PGConnection) conn;
            pgconn.addDataType("box3d", org.postgis.PGbox3d.class);
            pgconn.addDataType("box2d", org.postgis.PGbox2d.class);
        }

        public void addBinaryGeometries(Connection conn) throws SQLException {
            PGConnection pgconn = (PGConnection) conn;
            pgconn.addDataType("geometry", org.postgis.PGgeometryLW.class);
            pgconn.addDataType("geography", org.postgis.PGgeometryLW.class);
        }
    }
}
