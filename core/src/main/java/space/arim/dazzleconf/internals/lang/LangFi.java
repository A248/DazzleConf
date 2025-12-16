/*
 * DazzleConf
 * Copyright © 2025 Anand Beh
 *
 * DazzleConf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DazzleConf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */

package space.arim.dazzleconf.internals.lang;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf.backend.Printable;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

import static space.arim.dazzleconf.backend.Printable.preBuilt;

public final class LangFi implements LanguageCandidate {

    @Override
    public Locale getActualLocale() {
        return Locale.forLanguageTag("fi");
    }

    @Override
    public @NonNull String location() {
        return "sijainti";
    }

    @Override
    public @NonNull String line() {
        return "rivi";
    }

    @Override
    public @NonNull String backendMessage() {
        return "backend viesti";
    }

    @Override
    public @NonNull String syntaxLinter() {
        return "syntaksin tarkistin";
    }

    @Override
    public @NonNull String failed() {
        return "Epäonnistui";
    }

    @Override
    public @NonNull String missingValue() {
        return "Tässä ei ole määritetty arvoa, mutta se on pakollinen.";
    }

    @Override
    public @NonNull Printable wrongTypeForValue(Object value, String expectedType, String actualType) {
        return preBuilt("Arvo < " + value + " > on tyypiltään " + actualType + ", mutta sen pitäisi olla " + expectedType + '.');
    }

    @Override
    public @NonNull Printable mustBeBetween(String value, Number min, Number max) {
        return preBuilt("Arvon on oltava välillä " + min + " ja " + max + ", mutta se on " + value + '.');
    }

    @Override
    public @NonNull Printable notAccepted(@NonNull String value, @NonNull String[] permitted) {
        return preBuilt("Valittu arvo " + value + " ei ole sallittu. Sen pitäisi olla jokin seuraavista: " + Arrays.toString(permitted) + '.');
    }

    @Override
    public @NonNull String forExample() {
        return "esimerkiksi";
    }

    @Override
    public @NonNull String badValue() {
        return "Tämä arvo ei ole oikein.";
    }

    @Override
    public @NonNull String errorIntro() {
        return "Havaitsimme ongelmia konfiguraatiotiedoston lataamisessa.";
    }

    @Override
    public @NonNull String errorContext() {
        return "Missä tai miten virhe tapahtui:";
    }

    @Override
    public @NonNull String more(String what) {
        return what + " lisää...";
    }

    @Override
    public @NonNull String trueFalse() {
        return "true/false";
    }

    @Override
    public @NonNull String text() {
        return "teksti/merkkijono";
    }

    @Override
    public @NonNull String smallInteger() {
        return "pieni kokonaisluku";
    }

    @Override
    public @NonNull String integer() {
        return "kokonaisluku";
    }

    @Override
    public @NonNull String character() {
        return "merkki";
    }

    @Override
    public @NonNull String decimal() {
        return "desimaali";
    }

    @Override
    public @NonNull String list() {
        return "lista";
    }

    @Override
    public @NonNull String configurationSection() {
        return "konfiguraatio-osio";
    }

    @Override
    public @NonNull String syntaxInvalidPleaseTryAt(@NonNull URL url) {
        return "Konfiguraatiotiedostosi syntaksi on virheellinen. Voit käyttää apuna validaattorityökalua, kuten " + url + ". " +
                "Voit liittää konfiguraatiotiedostosi sinne ja käyttää sitä virheiden korjaamiseen.";
    }

    @Override
    public @NonNull String syntax() {
        return "Muotoilu";
    }

    @Override
    public @NonNull String otherReason() {
        return "Muu syy";
    }

    @Override
    public @NonNull String yamlNotAMap() {
        return "YAML-tiedoston on oltava assosiaatiotaulu, ei toinen YAML-tyyppi.";
    }

    @Override
    public @NonNull String tomlDateType() {
        return "TOML-päivämäärätyyppejä ei voi käyttää. Käytä lainausmerkkejä.";
    }

}
