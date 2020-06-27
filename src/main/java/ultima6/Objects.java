package ultima6;

public class Objects {

    public static final int OBJ_STATUS_OK_TO_TAKE = 0x1;
    public static final int OBJ_STATUS_INVISIBLE = 0x2;
    public static final int OBJ_STATUS_CHARMED = 0x4;
    public static final int OBJ_STATUS_ON_MAP = 0x0;
    public static final int OBJ_STATUS_IN_CONTAINER = 0x8;
    public static final int OBJ_STATUS_IN_INVENTORY = 0x10;
    public static final int OBJ_STATUS_READIED = 0x18;
    public static final int OBJ_STATUS_MASK_GET = 0x18;
    public static final int OBJ_STATUS_MASK_SET = 0xE7;
    public static final int OBJ_STATUS_TEMPORARY = 0x20;
    public static final int OBJ_STATUS_EGG_ACTIVE = 0x40;
    public static final int OBJ_STATUS_BROKEN = 0x40;
    public static final int OBJ_STATUS_MUTANT = 0x40;
    public static final int OBJ_STATUS_CURSED = 0x40;
    public static final int OBJ_STATUS_LIT = 0x80;

    public static final int ACTOR_HEAD = 0;
    public static final int ACTOR_NECK = 1;
    public static final int ACTOR_BODY = 2;
    public static final int ACTOR_ARM = 3;
    public static final int ACTOR_ARM_2 = 4;
    public static final int ACTOR_HAND = 5;
    public static final int ACTOR_HAND_2 = 6;
    public static final int ACTOR_FOOT = 7;

    public static enum Object {
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
        BLOOD_MOSS(66),
        GARLIC(67),
        GINSENG(68),
        MANDRAKE(69),
        NIGHTSHADE(70),
        SPIDER_SILK(71),
        SULFUROUS_ASH(72),
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
        BREAD(128),
        MEAT_PORTION(129),
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

        private Object(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static String getName(int id) {
            for (Object obj : Object.values()) {
                if (obj.id == id) {
                    return obj.toString();
                }
            }
            return null;
        }

        public static Object get(int id) {
            for (Object obj : Object.values()) {
                if (obj.id == id) {
                    return obj;
                }
            }
            return null;
        }

    }

    public static enum Readiable {
        LEATHER_HELM(ACTOR_HEAD, 1, 0, 512),
        CHAIN_COIF(ACTOR_HEAD, 2, 0, 513),
        IRON_HELM(ACTOR_HEAD, 3, 0, 514),
        SPIKED_HELM(ACTOR_HEAD, 3, 4, 515),
        WINGED_HELM(ACTOR_HEAD, 2, 0, 516),
        BRASS_HELM(ACTOR_HEAD, 2, 0, 517),
        GARGOYLE_HELM(ACTOR_HEAD, 3, 0, 518),
        MAGIC_HELM(ACTOR_HEAD, 5, 0, 519),
        WOODEN_SHIELD(ACTOR_ARM, 2, 0, 520),
        CURVED_HEATER(ACTOR_ARM, 3, 0, 521),
        WINGED_SHIELD(ACTOR_ARM, 3, 0, 522),
        KITE_SHIELD(ACTOR_ARM, 3, 0, 523),
        SPIKED_SHIELD(ACTOR_ARM, 2, 4, 524),
        BLACK_SHIELD(ACTOR_ARM, 2, 0, 525),
        DOOR_SHIELD(ACTOR_ARM, 4, 0, 526),
        MAGIC_SHIELD(ACTOR_ARM, 5, 0, 527),
        CLOTH_ARMOUR(ACTOR_BODY, 1, 0, 528),
        LEATHER_ARMOR(ACTOR_BODY, 2, 0, 529),
        RING_MAIL(ACTOR_BODY, 3, 0, 530),
        SCALE_MAIL(ACTOR_BODY, 4, 0, 531),
        CHAIN_MAIL(ACTOR_BODY, 5, 0, 532),
        PLATE_MAIL(ACTOR_BODY, 7, 0, 533),
        MAGIC_ARMOUR(ACTOR_BODY, 10, 0, 534),
        SPIKED_COLLAR(ACTOR_NECK, 2, 0, 535),
        GUILD_BELT(ACTOR_BODY, 0, 0, 536),
        GARGOYLE_BELT(ACTOR_BODY, 0, 0, 537),
        LEATHER_BOOTS(ACTOR_FOOT, 0, 0, 538),
        SWAMP_BOOTS(ACTOR_FOOT, 0, 0, 539),
        SLING(ACTOR_ARM, 0, 6, 544),
        CLUB(ACTOR_ARM, 0, 8, 545),
        MAIN_GAUCHE(ACTOR_ARM, 1, 8, 546),
        SPEAR(ACTOR_ARM, 0, 10, 547),
        THROWING_AXE(ACTOR_ARM, 0, 10, 548),
        DAGGER(ACTOR_ARM, 0, 6, 549),
        MACE(ACTOR_ARM, 0, 15, 550),
        MORNING_STAR(ACTOR_ARM, 0, 15, 551),
        BOW(ACTOR_ARM_2, 0, 10, 552),
        CROSSBOW(ACTOR_ARM_2, 0, 12, 553),
        SWORD(ACTOR_ARM, 0, 15, 554),
        TWO_HANDED_HAMMER(ACTOR_ARM_2, 0, 20, 555),
        TWO_HANDED_AXE(ACTOR_ARM_2, 0, 20, 556),
        TWO_HANDED_SWORD(ACTOR_ARM_2, 0, 20, 557),
        HALBERD(ACTOR_ARM_2, 0, 30, 558),
        GLASS_SWORD(ACTOR_ARM, 0, 255, 559),
        BOOMERANG(ACTOR_ARM, 0, 8, 560),
        TRIPLE_CROSSBOW(ACTOR_ARM_2, 0, 12 * 3, 561),
        MAGIC_BOW(ACTOR_ARM_2, 0, 20, 565),
        SPELLBOOK(ACTOR_ARM, 0, 0, 568),
        ANKH_AMULET(ACTOR_NECK, 0, 0, 592),
        SNAKE_AMULET(ACTOR_NECK, 0, 0, 593),
        AMULET_OF_SUBMISSION(ACTOR_NECK, 0, 0, 594),
        STAFF(ACTOR_ARM, 0, 4, 596),
        LIGHTNING_WAND(ACTOR_ARM, 0, 30, 597),
        FIRE_WAND(ACTOR_ARM, 0, 20, 598),
        STORM_CLOAK(ACTOR_BODY, 0, 0, 599),
        RING(ACTOR_HAND, 0, 0, 600),
        FLASK_OF_OIL(ACTOR_ARM, 0, 4, 601),
        TORCH(ACTOR_ARM, 0, 0, 610),
        SCYTHE(ACTOR_ARM, 0, 0, 624),
        PITCHFORK(ACTOR_ARM, 0, 0, 625),
        RAKE(ACTOR_ARM, 0, 0, 626),
        PICK(ACTOR_ARM, 0, 0, 627),
        SHOVEL(ACTOR_ARM, 0, 0, 628),
        HOE(ACTOR_ARM, 0, 0, 629),
        ROLLING_PIN(ACTOR_ARM, 0, 2, 633),
        CLEAVER(ACTOR_ARM, 0, 4, 637),
        KNIFE(ACTOR_ARM, 0, 4, 638),
        TUNIC(ACTOR_BODY, 0, 0, 652),
        DRESS(ACTOR_BODY, 0, 0, 654),
        PANTS(ACTOR_BODY, 0, 0, 669),
        LUTE(ACTOR_ARM, 0, 0, 697),
        PLIERS(ACTOR_ARM, 0, 0, 793),
        HAMMER(ACTOR_ARM, 0, 0, 794),
        PROTECTION_RING(ACTOR_HAND, 0, 0, 893),
        REGENERATION_RING(ACTOR_HAND, 0, 0, 894),
        INVISIBILITY_RING(ACTOR_HAND, 0, 0, 895),
        ZU_YLEM(ACTOR_ARM, 0, 0, 612);

        private int location;
        private int defense;
        private int attack;
        private int tile;

        private Readiable(int location, int defense, int attack, int tile) {
            this.location = location;
            this.defense = defense;
            this.attack = attack;
            this.tile = tile;
        }

        public int getLocation() {
            return location;
        }

        public int getDefense() {
            return defense;
        }

        public int getAttack() {
            return attack;
        }

        public int getTile() {
            return tile;
        }

        public Object getObject() {
            return Object.valueOf(this.toString());
        }

        public static Readiable get(Object obj) {
            if (obj == null) {
                return null;
            }
            Readiable rd = Readiable.valueOf(obj.toString());
            return rd;
        }

    }

}
