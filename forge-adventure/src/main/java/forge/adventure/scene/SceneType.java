package forge.adventure.scene;


public enum SceneType {
    StartScene(new forge.adventure.scene.StartScene()),
    NewGameScene(new forge.adventure.scene.NewGameScene()),
    SettingsScene(new forge.adventure.scene.SettingsScene()),
    GameScene(new forge.adventure.scene.GameScene()),
    DuelScene(new forge.adventure.scene.DuelScene()),
    SaveLoadScene(new forge.adventure.scene.SaveLoadScene()),
    DeckEditScene(new forge.adventure.scene.DeckEditScene()),
    TileMapScene(new forge.adventure.scene.TileMapScene());
    public final forge.adventure.scene.Scene instance;

    SceneType(forge.adventure.scene.Scene scene) {
        this.instance = scene;
    }
}
