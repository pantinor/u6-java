
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static enum Objects {
        NOTHING(0),
        LEATHER_HELM(1),
        CHAIN_COIF(2),
        IRON_HELM(3),
        SPIKED_HELM(4),
        WINGED_HELM(5),
        BRASS_HELM(6),
        GARGOYLE_HELM(7),
        MAGIC_HELM(8),
        WOODEN_SHIELD(9),
        CURVED_HEATER(10),
        WINGED_SHIELD(11),
        KITE_SHIELD(12),
        SPIKED_SHIELD(13),
        BLACK_SHIELD(14),
        DOOR_SHIELD(15),
        MAGIC_SHIELD(16),
        CLOTH_ARMOUR(17),
        LEATHER_ARMOR(18),
        RING_MAIL(19),
        SCALE_MAIL(20),
        CHAIN_MAIL(21),
        PLATE_MAIL(22),
        MAGIC_ARMOUR(23),
        SPIKED_COLLAR(24),
        GUILD_BELT(25),
        GARGOYLE_BELT(26),
        LEATHER_BOOTS(27),
        SWAMP_BOOTS(28),
        EARTH(29),
        FLOOR0(30),
        FLOOR1(31),
        FLOOR2(32),
        SLING(33),
        CLUB(34),
        MAIN_GAUCHE(35),
        SPEAR(36),
        THROWING_AXE(37),
        DAGGER(38),
        MACE(39),
        MORNING_STAR(40),
        BOW(41),
        CROSSBOW(42),
        SWORD(43),
        TWO_HANDED_HAMMER(44),
        TWO_HANDED_AXE(45),
        TWO_HANDED_SWORD(46),
        HALBERD(47),
        GLASS_SWORD(48),
        BOOMERANG(49),
        TRIPLE_CROSSBOW(50),
        FORCE_FIELD(51),
        WIZARD_EYE(52),
        WEB(53),
        MAGIC_BOW(54),
        ARROW(55),
        BOLT(56),
        SPELLBOOK(57),
        SPELL(58),
        CODEX(59),
        BOOK_OF_PROPHECIES(60),
        BOOK_OF_CIRCLES(61),
        VORTEX_CUBE(62),
        LOCK_PICK(63),
        KEY(64),
        BLACK_PEARL(65),
        BIT_OF_BLOOD_MOSS(66),
        BULB_OF_GARLIC(67),
        GINSENG_ROOT(68),
        MANDRAKE_ROOT(69),
        NIGHTSHADE_MUSHROOM(70),
        STRAND_OF_SPIDERSILK(71),
        BIT_OF_SULFUROUS_ASH(72),
        MOONSTONE(73),
        ANKH_AMULET(74),
        SNAKE_AMULET(75),
        AMULET_OF_SUBMISSION(76),
        GEM(77),
        STAFF(78),
        LIGHTNING_WAND(79),
        FIRE_WAND(80),
        STORM_CLOAK(81),
        RING(82),
        FLASK_OF_OIL(83),
        RED_GATE(84),
        MOONGATE(85),
        GAVEL(86),
        ORB_OF_THE_MOONS(87),
        GOLD_COIN(88),
        GOLD_NUGGET(89),
        TORCH(90),
        ZU_YLEM(91),
        SILVER_SNAKE_VENOM(92),
        SEXTANT(93),
        SPINNING_WHEEL(94),
        BUNCH_OF_GRAPES(95),
        BUTTER(96),
        GARGISH_VOCABULARY(97),
        OPEN_CHEST(98),
        BACKPACK(99),
        SCYTHE(100),
        PITCHFORK(101),
        RAKE(102),
        PICK(103),
        SHOVEL(104),
        HOE(105),
        WOODEN_LADDER(106),
        YOKE(107),
        OVEN_SPATULA(108),
        ROLLING_PIN(109),
        SPATULA(110),
        LADLE(111),
        COOKING_SHEET(112),
        CLEAVER(113),
        KNIFE(114),
        WINE(115),
        MEAD(116),
        ALE(117),
        WINE_GLASS(118),
        PLATE(119),
        MUG(120),
        SILVERWARE(121),
        CANDLE(122),
        MIRROR(123),
        TUNIC(124),
        HANGER(125),
        DRESS(126),
        SKILLET(127),
        LOAF_OF_BREAD(128),
        PORTION_OF_MEAT(129),
        ROLLS(130),
        CAKE(131),
        CHEESE(132),
        HAM(133),
        HORSE_CARCASS(134),
        HORSE_CHOPS(135),
        SKEWER(136),
        PANTS(137),
        PLANT(138),
        FLOWERS(139),
        WALL_MOUNT(140),
        DECORATIVE_SWORD(141),
        DECORATIVE_SHIELD(142),
        PICTURE(143),
        TAPESTRY(144),
        CANDELABRA(145),
        PERSON_SLEEPING(146),
        CAULDRON1(147),
        CAULDRON2(148),
        SHIP_DEED(149),
        INKWELL(150),
        BOOK(151),
        SCROLL(152),
        PANPIPES(153),
        TELESCOPE(154),
        CRYSTAL_BALL(155),
        HARPSICHORD(156),
        HARP(157),
        LUTE(158),
        CLOCK(159),
        ENDTABLE(160),
        WATER_VASE(161),
        STOVE(162),
        BED(163),
        FIREPLACE(164),
        STALAGMITE(165),
        SACK_OF_GRAIN(166),
        SACK_OF_FLOUR(167),
        REMAINS(168),
        RUBBER_DUCKY(169),
        URN_OF_ASHES(170),
        FUMAROLE(171),
        SPIKES(172),
        TRAP(173),
        SWITCH(174),
        ELECTRIC_FIELD(175),
        CHEST_OF_DRAWERS(176),
        DESK(177),
        BUCKET(178),
        BUCKET_OF_WATER(179),
        BUCKET_OF_MILK(180),
        CHURN(181),
        BEEHIVE(182),
        HONEY_JAR(183),
        JAR_OF_HONEY(184),
        CLOTH(185),
        OPEN_BARREL(186),
        JUG(187),
        BAG(188),
        CASK(189),
        BALE_OF_WOOL(190),
        BASKET(191),
        OPEN_CRATE(192),
        SMALL_JUG(193),
        MILK_BOTTLE(194),
        WHEAT(195),
        VAT(196),
        WINE_CASK(197),
        CUTTING_TABLE(198),
        LOOM(199),
        HOOD(200),
        FIRE(201),
        HORSESHOES(202),
        PLIERS(203),
        HAMMER(204),
        WATER_TROUGH(205),
        BRAZIER(206),
        ROD(207),
        HOOK(208),
        MEAT(209),
        RIBS(210),
        DEAD_ANIMAL(211),
        FAN(212),
        MOUSE_HOLE(213),
        WINE_PRESS(214),
        STABLE(215),
        BOOKSHELF(216),
        ANVIL(217),
        BELLOWS(218),
        OVEN(219),
        FLAG(220),
        CANNON(221),
        CANNON_BALLS(222),
        POWDER_KEG(223),
        FOOT_RAIL(224),
        SPOOL_OF_THREAD(225),
        SPOOL_OF_SILK(226),
        PENNANT(227),
        TABLE1(228),
        SHADOW1(229),
        TABLE2(230),
        SHADOW2(231),
        SPITTOON(232),
        WELL(233),
        FOUNTAIN(234),
        SUNDIAL(235),
        BELL(236),
        TABLE3(237),
        SHADOW(238),
        TABLE4(239),
        SHADOW3(240),
        SILK_CLOTH(241),
        RUNE_OF_HONESTY(242),
        RUNE_OF_COMPASSION(243),
        RUNE_OF_VALOR(244),
        RUNE_OF_JUSTICE(245),
        RUNE_OF_SACRIFICE(246),
        RUNE_OF_HONOR(247),
        RUNE_OF_SPIRITUALITY(248),
        RUNE_OF_HUMILITY(249),
        TABLE5(250),
        SHADOW5(251),
        CHAIR(252),
        CAMPFIRE(253),
        CROSS(254),
        TOMBSTONE(255),
        PROTECTION_RING(256),
        REGENERATION_RING(257),
        INVISIBILITY_RING(258),
        TABLE_LEG1(259),
        SHADOW6(260),
        TABLE_LEG2(261),
        SHADOW7(262),
        STOCKS(263),
        FISHING_POLE(264),
        FISH(265),
        GRAVE(266),
        GUILLOTINE(267),
        LEVER(268),
        DRAWBRIDGE(269),
        BALLOON_PLANS(270),
        DOORSILL(271),
        STEPS1(272),
        TILE(273),
        YEW_LOG(274),
        POTION(275),
        STEPS2(276),
        YEW_BOARD(277),
        PASSTHROUGH1(278),
        TABLE6(279),
        PASSTHROUGH2(280),
        FENCE(281),
        BARS(282),
        ANCHOR(283),
        ROPE(284),
        POLE(285),
        WALKWAY(286),
        WATER_WHEEL(287),
        CRANK(288),
        LOG_SAW(289),
        MILL_STONE(290),
        SHAFT(291),
        GEARWORK(292),
        CHAIN(293),
        LIGHTSOURCE(294),
        HEATSOURCE(295),
        XYLOPHONE(296),
        OAKEN_DOOR(297),
        WINDOWED_DOOR(298),
        CEDAR_DOOR(299),
        STEEL_DOOR(300),
        DOORWAY(301),
        ARCHWAY(302),
        CARPET(303),
        COOKFIRE(304),
        LADDER(305),
        TRELLIS(306),
        VOLCANO(307),
        HOLE(308),
        BONES(309),
        PORTCULLIS(310),
        STONE_TABLE(311),
        STONE_LION(312),
        SILVER_HORN(313),
        FLOOR(314),
        STONE(315),
        LAMPPOST(316),
        FIRE_FIELD(317),
        POISON_FIELD(318),
        PROTECTION_FIELD(319),
        SLEEP_FIELD(320),
        STATUE(321),
        POOL(322),
        MONOLITH(323),
        PILLAR(324),
        BOOK_STAND(325),
        MINE_SHAFT(326),
        THRONE(327),
        ALTAR(328),
        ALTAR_OF_SINGULARITY(329),
        MAT(330),
        GOVERNMENT_SIGN(331),
        SIGN(332),
        GARGOYLE_SIGN(333),
        SECRET_DOOR(334),
        EGG(335),
        CHARGE(336),
        EFFECT(337),
        BLOOD(338),
        DEAD_BODY(339),
        DEAD_CYCLOPS(340),
        DEAD_GARGOYLE(341),
        GIANT_RAT(342),
        INSECTS(343),
        GIANT_BAT(344),
        GIANT_SQUID(345),
        SEA_SERPENT(346),
        REAPER(347),
        SHEEP(348),
        DOG(349),
        DEER(350),
        WOLF(351),
        GHOST(352),
        GREMLIN(353),
        MOUSE(354),
        GAZER(355),
        BIRD(356),
        CORPSER(357),
        SNAKE(358),
        RABBIT(359),
        ROT_WORMS(360),
        GIANT_SPIDER(361),
        GARGOYLE1(362),
        GARGOYLE2(363),
        ACID_SLUG(364),
        TANGLE_VINE1(365),
        TANGLE_VINE2(366),
        DAEMON(367),
        SKELETON(368),
        DRAKE(369),
        HEADLESS(370),
        TROLL(371),
        MONGBAT(372),
        WISP(373),
        HYDRA(374),
        SLIME(375),
        FIGHTER(376),
        SWASHBUCKLER(377),
        MAGE(378),
        VILLAGER(379),
        MERCHANT(380),
        CHILD(381),
        GUARD(382),
        JESTER(383),
        PEASANT(384),
        FARMER(385),
        MUSICIAN(386),
        WOMAN(387),
        CAT(388),
        SILVER_TABLET(389),
        SILVER_FRAGMENT(390),
        FARMER2(391),
        MUSICIAN2(392),
        SHRINE(393),
        BRITANNIAN_LENS(394),
        BROKEN_LENS(395),
        GARGOYLE_LENS(396),
        STATUE_OF_MONDAIN(397),
        STATUE_OF_MINAX(398),
        STATUE_OF_EXODUS(399),
        PART_OF_A_MAP(400),
        PART_OF_A_MAP1(401),
        PART_OF_A_MAP2(402),
        PART_OF_A_MAP3(403),
        PART_OF_A_MAP4(404),
        PART_OF_A_MAP5(405),
        PART_OF_A_MAP6(406),
        PART_OF_A_MAP7(407),
        PART_OF_A_MAP8(408),
        LORD_BRITISH(409),
        AVATAR(410),
        DRAGON(411),
        SHIP(412),
        SILVER_SERPENT(413),
        SKIFF(414),
        RAFT(415),
        NOTHING2(416),
        DRAGON_EGG(417),
        HATCHED_DRAGON_EGG(418),
        PULL_CHAIN(419),
        BALLOON1(420),
        MAMMOTH_SILK_BAG(421),
        BALLOON_BASKET(422),
        BALLOON2(423),
        CYCLOPS(424),
        HYDRA2(425),
        GIANT_SCORPION(426),
        GIANT_ANT(427),
        COW(428),
        ALLIGATOR(429),
        HORSE(430),
        HORSE_WITH_RIDER1(431);

        private int id;

        private Objects(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static String getName(int id) {
            for (Objects obj : Objects.values()) {
                if (obj.id == id) {
                    return obj.toString();
                }
            }
            return null;
        }
    }

    public static int MOVETYPE_U6_NONE = 0;
    public static int MOVETYPE_U6_LAND = 1;
    public static int MOVETYPE_U6_WATER_LOW = 2;// skiffs, rafts
    public static int MOVETYPE_U6_WATER_HIGH = 3; // ships
    public static int MOVETYPE_U6_AIR_LOW = 4; // balloon, birds... this movetype cannot cross mountain tops.
    public static int MOVETYPE_U6_AIR_HIGH = 5; // dragons
    public static int MOVETYPE_U6_ETHEREAL = 6;

    public static final String[] DUNGEONS = new String[]{
        "Deceit",
        "Despise",
        "Destard",
        "Wrong",
        "Covetous",
        "Shame",
        "Hythloth",
        "GSA",
        "Control",
        "Passion",
        "Diligence",
        "Tomb of Kings",
        "Ant Mound",
        "Swamp Cave",
        "Spider Cave",
        "Cyclops Cave",
        "Heftimus Cave",
        "Heroes' Hole",
        "Pirate Cave",
        "Buccaneer's Cave"
    };

    // Red moongate teleport locations.
    public static int[][] RedMoongate = new int[][]{
        {0x0, 0x0, 0x0},
        {0x383, 0x1f3, 0x0},
        {0x3a7, 0x106, 0x0},
        {0x1b3, 0x18b, 0x0},
        {0x1f7, 0x166, 0x0},
        {0x93, 0x373, 0x0},
        {0x397, 0x3a6, 0x0},
        {0x44, 0x2d, 0x5},
        {0x133, 0x160, 0x0},
        {0xbc, 0x2d, 0x5},
        {0x9f, 0x3ae, 0x0},
        {0x2e3, 0x2bb, 0x0},
        {0x0, 0x0, 0x0},
        {0x0, 0x0, 0x0},
        {0x0, 0x0, 0x0},
        {0xe3, 0x83, 0x0},
        {0x17, 0x16, 0x1},
        {0x80, 0x56, 0x5},
        {0x6c, 0xdd, 0x5},
        {0x39b, 0x36c, 0x0},
        {0x127, 0x26, 0x0},
        {0x4b, 0x1fb, 0x0},
        {0x147, 0x336, 0x0},
        {0x183, 0x313, 0x0},
        {0x33f, 0xa6, 0x0},
        {0x29b, 0x43, 0x0}

    };

    public static enum Direction {
        NORTH, EAST, SOUTH, WEST;
    }

    public static enum ActorAnimation {

        GIANT_RAT(1280, 342, 2, 2, 1, 0),
        INSECTS(1288, 343, 0, 0, 1, 0),
        GIANT_BAT(1292, 344, 0, 0, 1, 0),
        GIANT_SQUID(1296, 345, 0, 0, 1, 0),
        SEA_SERPENT(1300, 346, 2, 2, 1, 0),
        REAPER(1308, 347, 0, 0, 1, 0),
        SHEEP(1312, 348, 2, 2, 1, 0),
        DOG(1320, 349, 2, 2, 1, 0),
        DEER(1328, 350, 2, 2, 1, 0),
        WOLF(1336, 351, 2, 2, 1, 0),
        GHOST(1344, 352, 0, 0, 1, 0),
        GREMLIN(1348, 353, 0, 0, 1, 0),
        MOUSE(1352, 354, 1, 1, 1, 0),
        GAZER(1356, 355, 0, 0, 1, 0),
        BIRD(1360, 356, 0, 0, 1, 0),
        CORPSER(1364, 357, 0, 0, 1, 0),
        SNAKE(1368, 358, 2, 2, 1, 0),
        RABBIT(1376, 359, 0, 0, 1, 0),
        ROT_WORMS(1380, 360, 0, 0, 1, 0),
        GIANT_SPIDER(1384, 361, 2, 2, 1, 0),
        ACID_SLUG(1452, 364, 0, 0, 1, 0),
        TANGLE_VINE(1456, 365, 1, 2, 1, 0),
        DAEMON(1464, 367, 2, 2, 1, 0),
        SKELETON(1472, 368, 2, 2, 1, 0),
        DRAKE(1480, 369, 2, 2, 1, 0),
        HEADLESS(1488, 370, 2, 2, 1, 0),
        TROLL(1496, 371, 2, 2, 1, 0),
        MONGBAT(1504, 372, 2, 2, 1, 0),
        WISP(1512, 373, 0, 0, 1, 0),
        HYDRA(1516, 374, 0, 0, 1, 0),
        SLIME(1520, 375, 0, 0, 0, 0),
        //
        WINGED_GARGOYLE(1392, 362, 3, 12, 4, 0),
        GARGOYLE(1440, 363, 3, 3, 1, 0),
        //
        FIGHTER(1536, 376, 3, 4, 1, 0),
        SWASHBUCKLER(1552, 377, 3, 4, 1, 0),
        MAGE(1568, 378, 3, 4, 1, 0),
        VILLAGER(1584, 379, 3, 4, 1, 0),
        MERCHANT(1600, 380, 3, 4, 1, 0),
        CHILD(1616, 381, 3, 4, 1, 0),
        GUARD(1632, 382, 3, 4, 1, 0),
        JESTER(1648, 383, 3, 4, 1, 0),
        PEASANT(1664, 384, 3, 4, 1, 0),
        FARMER(1680, 385, 3, 4, 1, 0),
        MUSICIAN(1696, 386, 3, 4, 1, 0),
        WOMAN(1712, 387, 3, 4, 1, 0),
        CAT(1728, 388, 1, 1, 1, 0),
        LORD_BRITISH(1760, 409, 3, 4, 1, 0),
        AVATAR(1776, 410, 3, 4, 1, 0),
        //
        DRAGON(1792, 411, 2, 2, 1, 0),
        SILVER_SERPENT(1856, 413, 1, 2, 1, 0),
        CYCLOPS(1888, 424, 2, 8, 4, 0),
        //
        SHIP(1832, 412, 1, 2, 6, 8),
        SKIFF(1872, 414, 1, 1, 1, 0),
        RAFT(1876, 415, 0, 0, 0, 1),
        //
        GIANT_SCORPION(1952, 426, 2, 2, 2, 0),
        GIANT_ANT(1968, 427, 2, 2, 2, 0),
        COW(1984, 428, 2, 2, 2, 0),
        ALLIGATOR(2000, 429, 2, 2, 2, 0),
        HORSE(2016, 430, 2, 2, 2, 0);

        private final int tile;
        private final int object;
        private final int framesPerDirection;
        private final int tilesPerDirection;
        private final int tilesPerFrame;
        private final int tileStartOffset;

        private final Map<Direction, Animation<TextureRegion>> animMap = new HashMap<>();
        private final Map<Direction, TextureRegion> textureMap = new HashMap<>();

        private ActorAnimation(
                int tile,
                int object,
                int framesPerDirection,
                int tilesPerDirection,
                int tilesPerFrame,
                int tileStartOffset) {

            this.tile = tile;
            this.object = object;
            this.framesPerDirection = framesPerDirection;
            this.tilesPerDirection = tilesPerDirection;
            this.tilesPerFrame = tilesPerFrame;
            this.tileStartOffset = tileStartOffset;
        }

        public Animation<TextureRegion> getAnimation(Direction dir) {
            return this.animMap.get(dir);
        }

        public TextureRegion getTexture(Direction dir) {
            return this.textureMap.get(dir);
        }

        public static void init() throws Exception {
            TextureRegion[] tiles = new TextureRegion[32 * 64];
            TextureRegion[][] tmp = TextureRegion.split(new Texture(Gdx.files.classpath("u6tiles+objects.png")), 16, 16);
            for (int y = 0; y < 64; y++) {
                for (int x = 0; x < 32; x++) {
                    tiles[y * 32 + x] = tmp[y][x];
                }
            }

            for (ActorAnimation aa : ActorAnimation.values()) {
                if (aa.framesPerDirection == 0) {
                    Array<TextureRegion> arr = new Array<>();
                    for (int i = 0; i < 4; i++) {
                        arr.add(tiles[aa.tile + i]);
                    }
                    Animation a = new Animation(.3f, arr);
                    for (int i = 0; i < 4; i++) {
                        aa.animMap.put(Direction.values()[i], a);
                        aa.textureMap.put(Direction.values()[i], tiles[aa.tile]);
                    }
                } else {
                    for (int i = 0; i < 4; i++) {

                        if (aa == ActorAnimation.SHIP) {
                        } else if (aa == ActorAnimation.HYDRA) {
                        } else if (aa == ActorAnimation.DRAGON) {
                        } else if (aa == ActorAnimation.SILVER_SERPENT) {

                        } else if (aa.tilesPerFrame == 1) {

                            Array<TextureRegion> arr = new Array<>();
                            for (int j = 0; j < aa.framesPerDirection; j++) {
                                arr.add(tiles[aa.tile + j + i * aa.tilesPerDirection]);
                            }
                            aa.animMap.put(Direction.values()[i], new Animation(.3f, arr));
                            aa.textureMap.put(Direction.values()[i], tiles[aa.tile + i * aa.tilesPerDirection]);

                        } else if (aa.tilesPerFrame == 2 && aa.framesPerDirection == 2) {

                            Array<TextureRegion> arr = new Array<>();
                            arr.add(new TextureRegion(mergeTiles(Direction.values()[i], tiles[aa.tile + 0 + 2 * i], tiles[aa.tile + 8 + 2 * i])));
                            arr.add(new TextureRegion(mergeTiles(Direction.values()[i], tiles[aa.tile + 1 + 2 * i], tiles[aa.tile + 8 + 1 + 2 * i])));
                            aa.animMap.put(Direction.values()[i], new Animation(.3f, arr));

                            aa.textureMap.put(Direction.values()[i], tiles[aa.tile + i * aa.tilesPerDirection]);

                        }
                    }
                }

            }
        }

        private static Texture mergeTiles(Direction dir, TextureRegion... tr) {
            Texture ret = null;
            if (tr.length == 2) {
                Pixmap dest = (dir == Direction.EAST || dir == Direction.WEST ? new Pixmap(32, 16, Format.RGBA8888) : new Pixmap(16, 32, Format.RGBA8888));
                Pixmap p1 = pixmapFromRegion(tr[0]);
                Pixmap p2 = pixmapFromRegion(tr[1]);
                if (dir == Direction.EAST || dir == Direction.WEST) {
                    dest.drawPixmap(p1, (dir == Direction.EAST ? 16 : 0), 0);
                    dest.drawPixmap(p2, (dir == Direction.EAST ? 0 : 16), 0);
                } else {
                    dest.drawPixmap(p1, 0, (dir == Direction.NORTH ? 0 : 16));
                    dest.drawPixmap(p2, 0, (dir == Direction.NORTH ? 16 : 0));
                }
                p1.dispose();
                p2.dispose();
                ret = new Texture(dest);
            } else if (tr.length == 4) {
                Pixmap p1 = pixmapFromRegion(tr[0]);
                Pixmap p2 = pixmapFromRegion(tr[1]);
                Pixmap p3 = pixmapFromRegion(tr[2]);
                Pixmap p4 = pixmapFromRegion(tr[3]);
                Pixmap dest = new Pixmap(32, 32, Format.RGBA8888);
                dest.drawPixmap(p1, 0, 0);
                dest.drawPixmap(p2, 16, 0);
                dest.drawPixmap(p3, 0, 16);
                dest.drawPixmap(p4, 16, 16);
                p1.dispose();
                p2.dispose();
                p3.dispose();
                p4.dispose();
                ret = new Texture(dest);
            }
            return ret;

        }

        private static Pixmap pixmapFromRegion(TextureRegion tr) {
            tr.getTexture().getTextureData().prepare();
            Pixmap source = tr.getTexture().getTextureData().consumePixmap();
            Pixmap dest = new Pixmap(tr.getRegionWidth(), tr.getRegionHeight(), Format.RGBA8888);
            for (int x = 0; x < tr.getRegionWidth(); x++) {
                for (int y = 0; y < tr.getRegionHeight(); y++) {
                    int colorInt = source.getPixel(tr.getRegionX() + x, tr.getRegionY() + y);
                    dest.drawPixel(x, y, colorInt);
                }
            }
            source.dispose();
            return dest;
        }

    }

}
