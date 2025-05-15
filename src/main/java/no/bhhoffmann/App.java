package no.bhhoffmann;

import java.sql.Connection;
import java.sql.DriverManager;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class App {

  public static void main(String[] args) {
    migrate(
        "some-gcp-project-id",
        "some-spanner-instance-id",
        "some-spanner-database-id",
        "db/changelog.yaml"
    );
  }

  private static void migrate(String gcpProjectId, String spannerInstanceId, String spannerDatabaseId, String masterChangelogFilePath) {
    String spannerJdbcUrl = String.format(
        "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
        gcpProjectId,
        spannerInstanceId,
        spannerDatabaseId
    );

    Database lqDb = null;
    try {
      Connection conn = DriverManager.getConnection(spannerJdbcUrl);
      System.out.println("Successfully connected to Spanner.");

      lqDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
      System.out.println("Liquibase detected database product: " + lqDb.getDatabaseProductName() + ", version: " + lqDb.getDatabaseProductVersion());

      Liquibase liquibase = new Liquibase(masterChangelogFilePath, new ClassLoaderResourceAccessor(), lqDb);
      System.out.println("Liquibase instance created.");

      System.out.println("Running Liquibase update...");
      liquibase.update();
      System.out.println("Liquibase migrations completed successfully.");

    } catch (Exception e) {
      System.out.println("Something went horribly wrong: " + e.getMessage());
    } finally {
      System.out.println("Cleaning up Liquibase resources...");
      if (lqDb != null) {
        try {
          lqDb.close();
          System.out.println("Liquibase database object closed.");
        } catch (DatabaseException e) {
          System.out.println("Error closing Liquibase database object: " + e.getMessage());
        }
      }
    }

  }
}
