package be.vdab;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class RekeningRepository extends AbstractRepository {

    //    METHODE OM TE CONTROLEREN OF EEN REKENINGNUMMER AL BESTAAT
    Boolean rekeningBestaat(String rekeningNummer) throws SQLException {
        var sql = """
                SELECT nummer
                FROM rekeningen
                WHERE nummer = ?
                FOR UPDATE
                """;

        try (var connection = super.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, rekeningNummer);
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            var result = statement.executeQuery();
            return result.next();
        }
    }


    void rekeningToevoegen(String rekeningNummer) throws SQLException {
        var sql = """
                INSERT INTO rekeningen
                VALUES(?,?)
                """;

        try (var connection = super.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, rekeningNummer);
            statement.setDouble(2, 0);
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            var result = statement.executeUpdate();
            connection.commit();
        }
    }


    // De gebruiker typt het nummer van de te consulteren rekening. Als dit nummer voorkomt in de table
//rekeningen toon je het saldo van de rekening. Anders toon je een foutboodschap.

    Optional saldoConsulteren(String rekeningNummer) throws SQLException {
        Optional<Integer> saldo;
        var sql = """
                SELECT saldo
                FROM rekeningen
                WHERE nummer = ?
                FOR UPDATE
                """;

        try (var connection = super.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, rekeningNummer);
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            var result = statement.executeQuery();
            connection.commit();
            return result.next() ? Optional.of(result.getDouble("saldo")) : Optional.empty();
        }
    }

    //   BEDRAG OVERSCHRIJVEN
    void bedragOverschrijven(String vanRekeningnummer, String naarRekeningnummer, double bedrag) throws SQLException {
        var sqlSaldoControle = """
                SELECT saldo
                FROM rekeningen
                WHERE nummer = ?
                """;

        var sqlVerminderen = """
                UPDATE rekeningen
                SET saldo = saldo - ?
                WHERE nummer = ?
                """;
        var sqlToevoegen = """
                UPDATE rekeningen
                SET saldo = saldo + ?
                WHERE nummer = ?
                """;

        try (var connection = super.getConnection();
             var statementSaldocontrole = connection.prepareStatement(sqlSaldoControle)) {
            statementSaldocontrole.setString(1, vanRekeningnummer);

//            CONTROLEER OF BEDRAG NIET TE GROOT IS
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            var result = statementSaldocontrole.executeQuery();
            result.next();
            if ( result.getDouble("saldo") < bedrag) {
                throw new IllegalArgumentException("Saldo ontoereikend");
            }
            connection.commit();

        }
//          ALS OK DAN KAN JE UITVOEREN
        try (var connection = super.getConnection();
             var statementVerminderen = connection.prepareStatement(sqlVerminderen);
             var statementToevoegen = connection.prepareStatement(sqlToevoegen)) {

//            PARAMETERS INVULLEN
            statementVerminderen.setDouble(1, bedrag);
            statementVerminderen.setString(2, vanRekeningnummer);
            statementToevoegen.setDouble(1, bedrag);
            statementToevoegen.setString(2, naarRekeningnummer);

//            OVERSCHRIJVING UITVOEREN ALS TRANSACTIE
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            statementVerminderen.executeUpdate();
            statementToevoegen.executeUpdate();

            connection.commit();
        }
    }
}
