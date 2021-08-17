package forge.adventure.libgdxgui.screens.planarconquest;

import forge.adventure.libgdxgui.deck.FDeckEditor;
import forge.itemmanager.ColumnDef;
import forge.adventure.libgdxgui.toolbox.FEvent;
import forge.adventure.libgdxgui.toolbox.FEvent.FEventHandler;
import forge.deck.DeckProxy;
import forge.game.GameType;
import forge.gamemodes.planarconquest.ConquestCommander;
import forge.gamemodes.planarconquest.ConquestData;
import forge.itemmanager.ItemColumn;
import forge.itemmanager.ItemManagerConfig;
import forge.model.FModel;
import forge.util.Localizer;

import java.util.Map;

public class ConquestDeckEditor extends FDeckEditor {
    public ConquestDeckEditor(final ConquestCommander commander) {
        super(EditorType.PlanarConquest, new DeckProxy(commander.getDeck(), Localizer.getInstance().getMessage("lblConquestCommander"),
                GameType.PlanarConquest, FModel.getConquest().getDecks()), true);

        setSaveHandler(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                commander.reloadDeck(); //ensure commander receives deck changes
            }
        });
    }

    @Override
    protected boolean allowRename() {
        return false;
    }
    @Override
    protected boolean allowDelete() {
        return false;
    }

    @Override
    protected Map<ColumnDef, ItemColumn> getColOverrides(ItemManagerConfig config) {
        return ConquestData.getColOverrides(config);
    }
}
