package de.justsoftware.toolbox.mybatis;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * enum for easy accessing the used jdbc driver
 */
@ParametersAreNonnullByDefault
public enum SupportedJdbcDriver {

        POSTGRES("org.postgresql.Driver"),
        ORACLE("oracle.jdbc.driver.OracleDriver"),

    ;

    private final String _driverClassName;

    SupportedJdbcDriver(final String driverClassName) {
        _driverClassName = driverClassName;
    }

    @Nonnull
    public static SupportedJdbcDriver driverFromUrl(final String url) {
        if (url.startsWith("jdbc:postgresql:")) {
            return POSTGRES;
        } else if (url.startsWith("jdbc:oracle")) {
            return ORACLE;
        } else {
            throw new UnsupportedOperationException("Don't know the driver for the url " + url);
        }
    }

    @Nonnull
    public String getDriverClassName() {
        return _driverClassName;
    }

}
