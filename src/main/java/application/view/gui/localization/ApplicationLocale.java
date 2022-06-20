package application.view.gui.localization;

import java.util.Locale;

public enum ApplicationLocale {
    RUSSIAN("ru", "RU"),
    ENGLISH("en", "US"),
    FINNISH("fi", "FI"),
    CATALAN("ca", "CN");

    private final Locale locale;

    ApplicationLocale(String language, String country) {
        locale = new Locale(language, country);
    }

    public Locale getLocale() {
        return locale;
    }

    public static ApplicationLocale getMyLocale(Locale locale) {
        for (ApplicationLocale ml : ApplicationLocale.values()) {
            if (ml.getLocale().getLanguage().equals(locale.getLanguage()) &&
                    ml.getLocale().getCountry().equals(locale.getCountry())) return ml;
        }
        return ApplicationLocale.ENGLISH;
    }
}
