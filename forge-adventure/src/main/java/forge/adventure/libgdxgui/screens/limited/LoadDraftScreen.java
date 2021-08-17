package forge.adventure.libgdxgui.screens.limited;

import com.badlogic.gdx.utils.Align;
import forge.adventure.libgdxgui.Forge;
import forge.adventure.libgdxgui.assets.FSkinFont;
import forge.adventure.libgdxgui.deck.FDeckChooser;
import forge.adventure.libgdxgui.deck.FDeckEditor;
import forge.adventure.libgdxgui.deck.FDeckEditor.EditorType;
import forge.adventure.libgdxgui.itemmanager.DeckManager;
import forge.adventure.libgdxgui.itemmanager.filters.ItemFilter;
import forge.adventure.libgdxgui.screens.LaunchScreen;
import forge.adventure.libgdxgui.screens.LoadingOverlay;
import forge.adventure.libgdxgui.screens.home.LoadGameMenu;
import forge.adventure.libgdxgui.toolbox.FComboBox;
import forge.adventure.libgdxgui.toolbox.FEvent;
import forge.adventure.libgdxgui.toolbox.FEvent.FEventHandler;
import forge.adventure.libgdxgui.toolbox.FLabel;
import forge.adventure.libgdxgui.toolbox.FOptionPane;
import forge.deck.Deck;
import forge.deck.DeckGroup;
import forge.deck.DeckProxy;
import forge.deck.io.DeckPreferences;
import forge.game.GameType;
import forge.game.player.RegisteredPlayer;
import forge.gamemodes.match.HostedMatch;
import forge.gui.FThreads;
import forge.gui.GuiBase;
import forge.gui.util.SGuiChoose;
import forge.itemmanager.ItemManagerConfig;
import forge.localinstance.properties.ForgePreferences.FPref;
import forge.model.FModel;
import forge.player.GamePlayerUtil;
import forge.util.Localizer;

import java.util.ArrayList;
import java.util.List;

public class LoadDraftScreen extends LaunchScreen {
    private final DeckManager lstDecks = add(new DeckManager(GameType.Draft));
    private final FLabel lblTip = add(new FLabel.Builder()
        .text(Localizer.getInstance().getMessage("lblDoubleTapToEditDeck"))
        .textColor(FLabel.INLINE_LABEL_COLOR)
        .align(Align.center).font(FSkinFont.get(12)).build());

    private final FSkinFont GAME_MODE_FONT= FSkinFont.get(12);
    private final FLabel lblMode = add(new FLabel.Builder().text(Localizer.getInstance().getMessage("lblMode")).font(GAME_MODE_FONT).build());
    private final FComboBox<String> cbMode = add(new FComboBox<>());

    public LoadDraftScreen() {
        super(null, LoadGameMenu.getMenu());

        cbMode.setFont(GAME_MODE_FONT);
        cbMode.addItem(Localizer.getInstance().getMessage("lblGauntlet"));
        cbMode.addItem(Localizer.getInstance().getMessage("lblSingleMatch"));

        lstDecks.setup(ItemManagerConfig.DRAFT_DECKS);
        lstDecks.setItemActivateHandler(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                editSelectedDeck();
            }
        });
    }

    @Override
    public void onActivate() {
        lstDecks.setPool(DeckProxy.getAllDraftDecks());
        lstDecks.setSelectedString(DeckPreferences.getDraftDeck());
    }

    private void editSelectedDeck() {
        final DeckProxy deck = lstDecks.getSelectedItem();
        if (deck == null) { return; }

        DeckPreferences.setDraftDeck(deck.getName());
        Forge.openScreen(new FDeckEditor(EditorType.Draft, deck, true));
    }

    @Override
    protected void doLayoutAboveBtnStart(float startY, float width, float height) {
        float x = ItemFilter.PADDING;
        float y = startY;
        float w = width - 2 * x;
        float labelHeight = lblTip.getAutoSizeBounds().height;
        float listHeight = height - labelHeight - y - FDeckChooser.PADDING;
        float comboBoxHeight = cbMode.getHeight();

        lblMode.setBounds(x, y, lblMode.getAutoSizeBounds().width + FDeckChooser.PADDING / 2, comboBoxHeight);
        cbMode.setBounds(x + lblMode.getWidth(), y, w - lblMode.getWidth(), comboBoxHeight);
        y += comboBoxHeight + FDeckChooser.PADDING;
        lstDecks.setBounds(x, y, w, listHeight);
        y += listHeight + FDeckChooser.PADDING;
        lblTip.setBounds(x, y, w, labelHeight);
        y += labelHeight + FDeckChooser.PADDING;
    }

    @Override
    protected void startMatch() {
        FThreads.invokeInBackgroundThread(new Runnable() {
            @Override
            public void run() {
                Localizer localizer = Localizer.getInstance();
                final DeckProxy humanDeck = lstDecks.getSelectedItem();
                if (humanDeck == null) {
                    FOptionPane.showErrorDialog(localizer.getMessage("lblYouMustSelectExistingDeck"), localizer.getMessage("lblNoDeck"));
                    return;
                }

                // TODO: if booster draft tournaments are supported in the future, add the possibility to choose them here
                final boolean gauntlet = cbMode.getSelectedItem().equals(localizer.getMessage("lblGauntlet"));

                if (gauntlet) {
                    final Integer rounds = SGuiChoose.getInteger(localizer.getMessage("lblHowManyOpponents"),
                            1, FModel.getDecks().getDraft().get(humanDeck.getName()).getAiDecks().size());
                    if (rounds == null) {
                        return;
                    }

                    FThreads.invokeInEdtLater(new Runnable() {
                        @Override
                        public void run() {
                            if (!checkDeckLegality(humanDeck)) {
                                return;
                            }

                            LoadingOverlay.show(localizer.getMessage("lblLoadingNewGame"), new Runnable() {
                                @Override
                                public void run() {
                                    FModel.getGauntletMini().resetGauntletDraft();
                                    FModel.getGauntletMini().launch(rounds, humanDeck.getDeck(), GameType.Draft);
                                }
                            });
                        }
                    });
                } else {
                    final Integer aiIndex = SGuiChoose.getInteger(localizer.getMessage("lblWhichOpponentWouldYouLikeToFace"),
                            1, FModel.getDecks().getDraft().get(humanDeck.getName()).getAiDecks().size());
                    if (aiIndex == null) {
                        return; // Cancel was pressed
                    }

                    final DeckGroup opponentDecks = FModel.getDecks().getDraft().get(humanDeck.getName());
                    final Deck aiDeck = opponentDecks.getAiDecks().get(aiIndex - 1);
                    if (aiDeck == null) {
                        throw new IllegalStateException("Draft: Computer deck is null!");
                    }

                    FThreads.invokeInEdtLater(new Runnable() {
                        @Override
                        public void run() {
                            LoadingOverlay.show(localizer.getMessage("lblLoadingNewGame"), new Runnable() {
                                @Override
                                public void run() {
                                    if (!checkDeckLegality(humanDeck)) {
                                        return;
                                    }

                                    final List<RegisteredPlayer> starter = new ArrayList<>();
                                    final RegisteredPlayer human = new RegisteredPlayer(humanDeck.getDeck()).setPlayer(GamePlayerUtil.getGuiPlayer());
                                    starter.add(human);
                                    starter.add(new RegisteredPlayer(aiDeck).setPlayer(GamePlayerUtil.createAiPlayer()));
                                    for (final RegisteredPlayer pl : starter) {
                                        pl.assignConspiracies();
                                    }

                                    FModel.getGauntletMini().resetGauntletDraft();
                                    final HostedMatch hostedMatch = GuiBase.getInterface().hostMatch();
                                    hostedMatch.startMatch(GameType.Draft, null, starter, human, GuiBase.getInterface().getNewGuiGame());
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private boolean checkDeckLegality(DeckProxy humanDeck) {
        if (FModel.getPreferences().getPrefBoolean(FPref.ENFORCE_DECK_LEGALITY)) {
            String errorMessage = GameType.Draft.getDeckFormat().getDeckConformanceProblem(humanDeck.getDeck());
            if (errorMessage != null) {
                FOptionPane.showErrorDialog(Localizer.getInstance().getMessage("lblInvalidDeckDesc").replace("%n", errorMessage), Localizer.getInstance().getMessage("lblInvalidDeck"));
                return false;
            }
        }
        return true;
    }
}
