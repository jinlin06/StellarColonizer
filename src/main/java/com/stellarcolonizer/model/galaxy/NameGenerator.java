package com.stellarcolonizer.model.galaxy;

import java.util.Random;

// 名称生成器
class NameGenerator {
    private Random random = new Random();

    // 恒星前缀和后缀
    private static final String[] STAR_PREFIXES = {
            "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta",
            "Iota", "Kappa", "Lambda", "Mu", "Nu", "Xi", "Omicron", "Pi",
            "Rho", "Sigma", "Tau", "Upsilon", "Phi", "Chi", "Psi", "Omega"
    };

    private static final String[] STAR_SUFFIXES = {
            "Major", "Minor", "Primus", "Secundus", "Tertius", "Quartus",
            "Quintus", "Sextus", "Septimus", "Octavus", "Nonus", "Decimus"
    };

    // 星座名
    private static final String[] CONSTELLATIONS = {
            "Andromedae", "Antliae", "Apodis", "Aquarii", "Aquilae", "Arae",
            "Arietis", "Aurigae", "Bootis", "Caeli", "Camelopardalis",
            "Cancri", "Canum Venaticorum", "Canis Majoris", "Canis Minoris",
            "Capricorni", "Carinae", "Cassiopeiae", "Centauri", "Cephei",
            "Ceti", "Chamaeleontis", "Circini", "Columbae", "Comae Berenices",
            "Coronae Australis", "Coronae Borealis", "Corvi", "Crateris",
            "Crucis", "Cygni", "Delphini", "Doradus", "Draconis", "Equulei",
            "Eridani", "Fornacis", "Geminorum", "Gruis", "Herculis",
            "Horologii", "Hydrae", "Hydri", "Indi", "Lacertae", "Leonis",
            "Leporis", "Librae", "Lupi", "Lyncis", "Lyrae", "Mensae",
            "Microscopii", "Monocerotis", "Muscae", "Normae", "Octantis",
            "Ophiuchi", "Orionis", "Pavonis", "Pegasi", "Persei", "Phoenicis",
            "Pictoris", "Piscium", "Piscis Austrini", "Puppis", "Pyxidis",
            "Reticuli", "Sagittae", "Sagittarii", "Scorpii", "Sculptoris",
            "Scuti", "Serpentis", "Sextantis", "Tauri", "Telescopii",
            "Trianguli", "Trianguli Australis", "Tucanae", "Ursae Majoris",
            "Ursae Minoris", "Velorum", "Virginis", "Volantis", "Vulpeculae"
    };

    // 行星名（罗马数字）
    private static final String[] ROMAN_NUMERALS = {
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    };

    public String generateStarSystemName() {
        // 随机选择生成方式
        int method = random.nextInt(3);

        switch (method) {
            case 0:
                // 星座 + 希腊字母
                return STAR_PREFIXES[random.nextInt(STAR_PREFIXES.length)] + " " +
                        CONSTELLATIONS[random.nextInt(CONSTELLATIONS.length)];
            case 1:
                // 星座 + 序数
                return CONSTELLATIONS[random.nextInt(CONSTELLATIONS.length)] + " " +
                        STAR_SUFFIXES[random.nextInt(STAR_SUFFIXES.length)];
            case 2:
                // 科学编号
                return "NGC-" + (1000 + random.nextInt(9000));
            default:
                return "Unnamed System";
        }
    }

    public String generatePlanetName(String systemName) {
        // 行星名 = 系统名 + 罗马数字
        return systemName + " " + ROMAN_NUMERALS[random.nextInt(Math.min(ROMAN_NUMERALS.length, 10))];
    }
}
