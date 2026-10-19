package com.ready2ride.desktop;

/**
 * Standard launcher workaround for launching JavaFX applications
 * from classpaths without requiring explicit module-path VM flags.
 */
public class AppLauncher {

    public static void main(String[] args) {
        Ready2RideApplication.main(args);
    }
}
