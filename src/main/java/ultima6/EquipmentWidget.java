package ultima6;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import ultima6.Objects.Readiable;
import static ultima6.Ultima6.SCREEN_HEIGHT;

public class EquipmentWidget {

    private final SpriteBatch batch;
    private final Stage stage;

    private final List<PlayerIndex> playerSelection;

    private static Texture highlighter;

    static {
        Pixmap pix = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        pix.setColor(Color.YELLOW);
        pix.fillRectangle(0, 0, 50, 3);
        pix.fillRectangle(0, 47, 50, 3);
        pix.fillRectangle(0, 0, 3, 50);
        pix.fillRectangle(47, 0, 3, 50);
        highlighter = new Texture(pix);
        pix.dispose();
    }

    Image focusIndicator;
    Image selectedImage;
    ItemListing selectedItem;
    PlayerIndex selectedPlayer;
    private static final int w = 246;
    private static final int h = 50;
    private static final int dim = 32;

    public EquipmentWidget() {
        this.batch = new SpriteBatch();
        this.stage = new Stage();

        this.focusIndicator = new Image(Ultima6.fillRectangle(246, 50, Color.YELLOW, .35f));
        this.focusIndicator.setWidth(246);
        this.focusIndicator.setHeight(50);

        this.playerSelection = new List<>(Ultima6.skin, "larger");

    }

    private class PlayerIndex {

        final Actor character;
        final Table invTable = new Table(Ultima6.skin);
        final Table spellTable = new Table(Ultima6.skin);

        final Image gump1;
        final Image gump2;
        final Image gump3;
        final Image gump4;

        final Image headIcon;
        final Image neckIcon;
        final Image bodyIcon;
        final Image armIcon;
        final Image arm2Icon;
        final Image handIcon;
        final Image hand2Icon;
        final Image footIcon;
        final Label acLabel;
        final Label damageLabel;
        final Label goldLabel;

        final Label classL;
        final Label expL;
        final Label hpL;
        final Label mxhpL;
        final Label lvlL;
        final Label strL;
        final Label intL;
        final Label dexL;
        final Label magicL;

        final com.badlogic.gdx.scenes.scene2d.Actor[] icons = new com.badlogic.gdx.scenes.scene2d.Actor[22];
        final Image[] slots = new Image[10];
        final Label[] slotTooltips = new Label[10];

        PlayerIndex(Actor sp) {
            this.character = sp;

            invTable.align(Align.top);

            for (Actor.ObjectWrapper it : character.getInventory()) {
                invTable.add(new ItemListing(it, sp));
                invTable.row();
            }

            invTable.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {

                    if (event.toString().equals("touchDown")) {
                        if (focusIndicator.getParent() != null) {
                            focusIndicator.getParent().removeActor(focusIndicator);
                        }
                        if (event.getTarget() instanceof ItemListing) {
                            selectedItem = (ItemListing) event.getTarget();
                            selectedItem.addActor(focusIndicator);
                        } else if (event.getTarget().getParent() instanceof ItemListing) {
                            selectedItem = (ItemListing) event.getTarget().getParent();
                            selectedItem.addActor(focusIndicator);
                        }
                    }

                    return false;
                }
            }
            );

            gump1 = new Image(Constants.TILES[368]);
            gump2 = new Image(Constants.TILES[369]);
            gump3 = new Image(Constants.TILES[370]);
            gump4 = new Image(Constants.TILES[371]);

            gump1.setX(348);
            gump1.setY(SCREEN_HEIGHT - 279);
            gump2.setX(348);
            gump2.setY(SCREEN_HEIGHT - 279);
            gump3.setX(348);
            gump3.setY(SCREEN_HEIGHT - 279);
            gump4.setX(348);
            gump4.setY(SCREEN_HEIGHT - 279);

            headIcon = make(Objects.ACTOR_HEAD, Readiable.BRASS_HELM, 283, SCREEN_HEIGHT - 276);
            neckIcon = make(Objects.ACTOR_NECK, Readiable.ANKH_AMULET, 283, SCREEN_HEIGHT - 220);
            bodyIcon = make(Objects.ACTOR_BODY, Readiable.CHAIN_MAIL, 349, SCREEN_HEIGHT - 220);
            armIcon = make(Objects.ACTOR_ARM, Readiable.CROSSBOW, 415, SCREEN_HEIGHT - 220);
            arm2Icon = make(Objects.ACTOR_ARM_2, Readiable.CROSSBOW, 415, SCREEN_HEIGHT - 276);
            handIcon = make(Objects.ACTOR_HAND, Readiable.RING, 322, SCREEN_HEIGHT - 333);
            hand2Icon = make(Objects.ACTOR_HAND_2, Readiable.RING, 376, SCREEN_HEIGHT - 333);
            footIcon = make(Objects.ACTOR_FOOT, Readiable.DAGGER, 376, SCREEN_HEIGHT - 333);

            String d2 = String.format("%s  LVL %d  %s", "race", 1, "clz");
            classL = new Label(d2, Ultima6.skin, "larger");
            classL.setX(90);
            classL.setY(SCREEN_HEIGHT - 420);

            acLabel = new Label("" + 0, Ultima6.skin, "larger");
            acLabel.setX(360);
            acLabel.setY(SCREEN_HEIGHT - 393);

            damageLabel = new Label("weap", Ultima6.skin, "larger");
            damageLabel.setX(360);
            damageLabel.setY(SCREEN_HEIGHT - 437);

            goldLabel = new Label("" + 0, Ultima6.skin, "larger");
            goldLabel.setX(360);
            goldLabel.setY(SCREEN_HEIGHT - 480);

            expL = new Label("" + character.getExp(), Ultima6.skin, "larger");
            expL.setX(115);
            expL.setY(SCREEN_HEIGHT - 620 - 23);

            hpL = new Label("" + character.getHp(), Ultima6.skin, "larger");
            hpL.setX(115);
            hpL.setY(SCREEN_HEIGHT - 666 - 23);

            mxhpL = new Label("" + character.getHp(), Ultima6.skin, "larger");
            mxhpL.setX(115);
            mxhpL.setY(SCREEN_HEIGHT - 710 - 23);

            strL = new Label("" + character.getStrength(), Ultima6.skin, "larger");
            strL.setX(287);
            strL.setY(SCREEN_HEIGHT - 620 - 23);

            intL = new Label("" + character.getIntelligence(), Ultima6.skin, "larger");
            intL.setX(287);
            intL.setY(SCREEN_HEIGHT - 666 - 23);

            dexL = new Label("" + character.getDex(), Ultima6.skin, "larger");
            dexL.setX(287);
            dexL.setY(SCREEN_HEIGHT - 710 - 23);

            magicL = new Label("" + character.getMagic(), Ultima6.skin, "larger");
            magicL.setX(427);
            magicL.setY(SCREEN_HEIGHT - 620 - 23);

            lvlL = new Label("" + character.getLevel(), Ultima6.skin, "larger");
            lvlL.setX(427);
            lvlL.setY(SCREEN_HEIGHT - 666 - 23);

            icons[0] = gump1;
            icons[1] = gump2;
            icons[2] = gump3;
            icons[3] = gump4;
            icons[4] = headIcon;
            icons[5] = neckIcon;
            icons[6] = bodyIcon;
            icons[7] = armIcon;
            icons[8] = arm2Icon;
            icons[9] = handIcon;
            icons[10] = hand2Icon;
            icons[11] = footIcon;
            icons[12] = acLabel;
            icons[13] = damageLabel;
            icons[14] = goldLabel;
            icons[15] = expL;
            icons[16] = hpL;
            icons[17] = mxhpL;
            icons[18] = strL;
            icons[19] = intL;
            icons[20] = dexL;
            icons[21] = lvlL;
            icons[22] = magicL;
            icons[23] = classL;

        }

        private Image make(int type, Readiable it, int x, int y) {
            TextureRegion tr = icon(it);
            Image im = new Image(tr);
            im.setX(x);
            im.setY(y);
            im.setUserObject(it);
            im.addListener(new InvItemChangeListener(type));
            return im;
        }

        private void save() {

//            character.weapon = (Item) weaponIcon.getUserObject();
//            character.armor = (Item) armorIcon.getUserObject();
//            character.helm = (Item) helmIcon.getUserObject();
//            character.shield = (Item) shieldIcon.getUserObject();
//            character.glove = (Item) glovesIcon.getUserObject();
//            character.item1 = (Item) item1Icon.getUserObject();
//            character.item2 = (Item) item2Icon.getUserObject();
//
//            character.inventory.clear();
//            for (com.badlogic.gdx.scenes.scene2d.Actor a : invTable.getChildren()) {
//                if (a instanceof ItemListing) {
//                    ItemListing il = (ItemListing) a;
//                    character.inventory.add(il.item);
//                }
//            }
        }

        @Override
        public String toString() {
            return character.getName().toUpperCase();
        }

        private class InvItemChangeListener implements EventListener {

            final int type;

            InvItemChangeListener(int type) {
                this.type = type;
            }

            @Override
            public boolean handle(Event event) {
//                if (selectedItem != null && event.toString().equals("touchDown")) {
//                    if (this.type == selectedItem.getReadiableType() && selectedItem.item.canUse(PlayerIndex.this.character.classType)) {
//                        //Sounds.play(Sound.TRIGGER);
//                        Item old = (Item) event.getTarget().getUserObject();
//                        event.getTarget().setUserObject(selectedItem.item);
//                        ((Image) event.getTarget()).setDrawable(new TextureRegionDrawable(icon(selectedItem.item)));
//
//                        if (old != null) {
//                            PlayerIndex.this.invTable.add(new ItemListing(old, selectedItem.rec));
//                            PlayerIndex.this.invTable.row();
//                        }
//
//                        selectedItem.removeActor(focusIndicator);
//                        PlayerIndex.this.invTable.removeActor(selectedItem);
//                        selectedItem = null;
//
//                        acLabel.setText("" + calculateAC());
//
//                        if (PlayerIndex.this.weaponIcon.getUserObject() != null) {
//                            Item weap = (Item) PlayerIndex.this.weaponIcon.getUserObject();
//                            selectedPlayer.damageLabel.setText(weap.damage.toString());
//                        } else {
//                            selectedPlayer.damageLabel.setText("1d2");
//                        }
//
//                    } else {
//                        //Sounds.play(Sound.NEGATIVE_EFFECT);
//                    }
//                } else if (event.toString().equals("enter")) {
//                    Item i = (Item) event.getTarget().getUserObject();
//                    invDesc.setText(i != null ? i.name + " " + (i.numberUses > 0 ? "(" + i.numberUses + ")" : "") : "");
//                    selectedImage = (Image) event.getTarget();
//                } else if (event.toString().equals("exit")) {
//                    invDesc.setText("");
//                }

                return false;
            }

        }

        private int calculateAC() {
            int ac = 10;

            Readiable head = (Readiable) headIcon.getUserObject();
            Readiable neck = (Readiable) neckIcon.getUserObject();
            Readiable body = (Readiable) bodyIcon.getUserObject();
            Readiable arm = (Readiable) armIcon.getUserObject();
            Readiable arm2 = (Readiable) arm2Icon.getUserObject();
            Readiable hand = (Readiable) handIcon.getUserObject();
            Readiable hand2 = (Readiable) hand2Icon.getUserObject();
            Readiable foot = (Readiable) footIcon.getUserObject();

            if (head != null) {
                ac -= head.getDefense();
            }
            if (neck != null) {
                ac -= neck.getDefense();
            }
            if (body != null) {
                ac -= body.getDefense();
            }
            if (arm != null) {
                ac -= arm.getDefense();
            }
            if (arm2 != null) {
                ac -= arm2.getDefense();
            }
            if (hand != null) {
                ac -= hand.getDefense();
            }
            if (hand2 != null) {
                ac -= hand2.getDefense();
            }
            if (foot != null) {
                ac -= foot.getDefense();
            }
            return ac;
        }
    }

    private TextureRegion icon(Readiable it) {
        return Constants.TILES[it.getTile()];
    }

    private class ItemListing extends Group {

        Actor.ObjectWrapper obj;
        final Image icon;
        final Label label;
        final Image canusebkgnd;
        final Actor rec;

        ItemListing(Actor.ObjectWrapper obj, Actor rec) {
            this.rec = rec;
            this.obj = obj;

            this.icon = new Image(icon(Readiable.DAGGER));
            this.label = new Label(Readiable.DAGGER.name(), Ultima6.skin, "larger");
            this.canusebkgnd = new Image();

//            boolean canUse = item.canUse(rec.classType);
//            if (!canUse) {
//                canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(redBackgrnd)));
//            } else {
//                canusebkgnd.setDrawable(new TextureRegionDrawable(new TextureRegion(clearBackgrnd)));
//            }
            addActor(this.icon);
            addActor(this.label);
            //addActor(this.canusebkgnd);

            this.icon.setBounds(getX() + 3, getY() + 3, dim, dim);
            this.label.setPosition(getX() + dim + 10, getY() + 10);
            //this.canusebkgnd.setBounds(getX(), getY(), w, h);
            this.setBounds(getX(), getY(), w, h);

        }

        public int getReadiableType() {
            Readiable r = Readiable.get(obj.getObject());
            if (r != null) {
                return r.getLocation();
            }
            return -1;
        }

    }

}
