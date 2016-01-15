package com.subinkrishna.curah.eunmeration;

/**
 * @author Subinkrishna Gopi
 */
public enum Preferences {

    UnderstoodAboutLatestCurah ("understoodAboutLatestCurah"),
    HasAgreedToTerms("hasAgreedToTerms");

    public String label;

    private Preferences(final String label) {
        this.label = label;
    }

}
